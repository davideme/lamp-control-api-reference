package com.lampcontrol.database

import com.zaxxer.hikari.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.sql.Connection

/**
 * Factory for database connection management using HikariCP and Exposed
 */
object DatabaseFactory {
    private val logger = LoggerFactory.getLogger(DatabaseFactory::class.java)
    private const val CLOUD_SQL_PATH_PREFIX = "/cloudsql/"
    private const val CLOUD_SQL_SOCKET_FACTORY = "com.google.cloud.sql.postgres.SocketFactory"

    /**
     * Initialize database connection from environment variables.
     * Returns null if no PostgreSQL configuration is found.
     */
    fun init(): Database? {
        val config = DatabaseConfig.fromEnv()
        if (config == null) {
            logger.warn("Database initialization skipped: PostgreSQL configuration not found in environment variables")
            return null
        }

        // Run database migrations before initializing connection pool (unless skipped)
        val skipMigrations = System.getProperty("skip.migrations") == "true"
        if (!skipMigrations) {
            val migrationSuccess = FlywayConfig.runMigrations(config)
            if (!migrationSuccess) {
                logger.error("Database migrations failed. Database connection will not be initialized.")
                return null
            }
        } else {
            logger.info("Skipping database migrations (serve-only mode)")
        }

        val hikariConfig =
            HikariConfig().apply {
                jdbcUrl = config.connectionString()
                driverClassName = "org.postgresql.Driver"
                username = config.user
                password = config.password
                maximumPoolSize = config.poolMax
                minimumIdle = config.poolMin
                maxLifetime = config.maxLifetimeMs
                idleTimeout = config.idleTimeoutMs
                connectionTimeout = config.connectionTimeoutMs
                isAutoCommit = false
                transactionIsolation = "TRANSACTION_REPEATABLE_READ"
                configureCloudSqlProperties(this, config)
                validate()
            }

        val dataSource = HikariDataSource(hikariConfig)
        val database = Database.connect(dataSource)

        // Set default transaction isolation level
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_REPEATABLE_READ

        return database
    }

    private fun configureCloudSqlProperties(
        hikariConfig: HikariConfig,
        config: DatabaseConfig,
    ) {
        if (!config.host.startsWith(CLOUD_SQL_PATH_PREFIX)) {
            return
        }

        hikariConfig.addDataSourceProperty("socketFactory", CLOUD_SQL_SOCKET_FACTORY)
        hikariConfig.addDataSourceProperty("unixSocketPath", config.host)
        val cloudSqlInstance = extractCloudSqlInstance(config.host)
        if (cloudSqlInstance != null) {
            hikariConfig.addDataSourceProperty("cloudSqlInstance", cloudSqlInstance)
        }

        val cloudRunService = System.getenv("K_SERVICE")
        val cloudRunRevision = System.getenv("K_REVISION")
        if (!cloudRunService.isNullOrBlank() || !cloudRunRevision.isNullOrBlank()) {
            hikariConfig.addDataSourceProperty("cloudSqlRefreshStrategy", "lazy")
        }
    }

    private fun extractCloudSqlInstance(unixSocketPath: String): String? {
        val instance = unixSocketPath.removePrefix(CLOUD_SQL_PATH_PREFIX)
        return instance.takeIf { it.isNotBlank() }
    }
}

/**
 * Database configuration from environment variables
 */
data class DatabaseConfig(
    val host: String,
    val port: Int,
    val database: String,
    val user: String,
    val password: String,
    val poolMin: Int,
    val poolMax: Int,
    val maxLifetimeMs: Long,
    val idleTimeoutMs: Long,
    val connectionTimeoutMs: Long,
    val jdbcUrlOverride: String? = null,
) {
    companion object {
        private const val DEFAULT_POSTGRES_PORT = 5432
        private const val DEFAULT_POOL_MIN = 0
        private const val DEFAULT_POOL_MAX = 4
        private const val DEFAULT_MAX_LIFETIME_MS = 3600000L
        private const val DEFAULT_IDLE_TIMEOUT_MS = 1800000L
        private const val DEFAULT_CONNECTION_TIMEOUT_MS = 30000L

        /**
         * Create DatabaseConfig from environment variables.
         * Returns null if PostgreSQL is not configured.
         *
         * Consider PostgreSQL configured if:
         * - DATABASE_URL is set, or
         * - DB_NAME is explicitly provided, or
         * - Both DB_HOST and DB_USER are explicitly provided
         */
        fun fromEnv(): DatabaseConfig? {
            val databaseUrl = System.getenv("DATABASE_URL")
            val host = System.getenv("DB_HOST")
            val database = System.getenv("DB_NAME")
            val user = System.getenv("DB_USER")

            // Check if PostgreSQL is configured
            val postgresConfigured =
                !databaseUrl.isNullOrEmpty() ||
                    !database.isNullOrEmpty() ||
                    (!host.isNullOrEmpty() && !user.isNullOrEmpty())

            if (!postgresConfigured) {
                return null
            }

            // Use DATABASE_URL if provided (for services like Heroku, Render, etc.)
            if (!databaseUrl.isNullOrEmpty()) {
                return parseDatabaseUrl(databaseUrl)
            }

            // Otherwise, build from individual environment variables with defaults
            return DatabaseConfig(
                host = host ?: "localhost",
                port = System.getenv("DB_PORT")?.toIntOrNull() ?: DEFAULT_POSTGRES_PORT,
                database = database ?: "lamp_control",
                user = user ?: "lamp_user",
                password = System.getenv("DB_PASSWORD") ?: "",
                poolMin = System.getenv("DB_POOL_MIN_SIZE")?.toIntOrNull() ?: DEFAULT_POOL_MIN,
                poolMax = System.getenv("DB_POOL_MAX_SIZE")?.toIntOrNull() ?: DEFAULT_POOL_MAX,
                maxLifetimeMs = System.getenv("DB_MAX_LIFETIME_MS")?.toLongOrNull() ?: DEFAULT_MAX_LIFETIME_MS,
                idleTimeoutMs = System.getenv("DB_IDLE_TIMEOUT_MS")?.toLongOrNull() ?: DEFAULT_IDLE_TIMEOUT_MS,
                connectionTimeoutMs =
                    System.getenv("DB_CONNECTION_TIMEOUT_MS")?.toLongOrNull() ?: DEFAULT_CONNECTION_TIMEOUT_MS,
            )
        }

        /**
         * Parse DATABASE_URL format: postgresql://user:password@host:port/database
         */
        private fun parseDatabaseUrl(url: String): DatabaseConfig {
            val components = parseDatabaseUrlComponents(url)
            val credentials = parseCredentials(components.rawUserInfo, url)
            val uri = parseUri(components.scheme, components.authority, components.pathAndQuery, url)
            val database = parseDatabaseName(uri, url)
            val host = resolveHost(uri, components.authority, url)
            val port = if (uri.port > 0) uri.port else DEFAULT_POSTGRES_PORT
            val jdbcUrl = buildJdbcUrl(uri, port, database)

            return DatabaseConfig(
                host = host,
                port = port,
                database = database,
                user = credentials.first,
                password = credentials.second,
                poolMin = System.getenv("DB_POOL_MIN_SIZE")?.toIntOrNull() ?: DEFAULT_POOL_MIN,
                poolMax = System.getenv("DB_POOL_MAX_SIZE")?.toIntOrNull() ?: DEFAULT_POOL_MAX,
                maxLifetimeMs = System.getenv("DB_MAX_LIFETIME_MS")?.toLongOrNull() ?: DEFAULT_MAX_LIFETIME_MS,
                idleTimeoutMs = System.getenv("DB_IDLE_TIMEOUT_MS")?.toLongOrNull() ?: DEFAULT_IDLE_TIMEOUT_MS,
                connectionTimeoutMs =
                    System.getenv("DB_CONNECTION_TIMEOUT_MS")?.toLongOrNull() ?: DEFAULT_CONNECTION_TIMEOUT_MS,
                jdbcUrlOverride = jdbcUrl,
            )
        }

        private data class DatabaseUrlComponents(
            val scheme: String,
            val rawUserInfo: String,
            val authority: String,
            val pathAndQuery: String,
        )

        private fun parseDatabaseUrlComponents(url: String): DatabaseUrlComponents {
            val schemeMatch = Regex("""^(postgres(?:ql)?)://""", RegexOption.IGNORE_CASE).find(url)
            val scheme = schemeMatch?.groupValues?.get(1)?.lowercase() ?: invalidDatabaseUrl(url)
            if (scheme != "postgresql" && scheme != "postgres") {
                invalidDatabaseUrl(url)
            }

            val rawWithoutScheme = url.substring(schemeMatch.value.length)
            val atIndex = rawWithoutScheme.indexOf('@')
            if (atIndex <= 0) {
                invalidDatabaseUrl(url)
            }

            val rawUserInfo = rawWithoutScheme.substring(0, atIndex)
            val authorityAndPath = rawWithoutScheme.substring(atIndex + 1)
            val slashIndex = authorityAndPath.indexOf('/')
            if (slashIndex < 0) {
                invalidDatabaseUrl(url)
            }

            return DatabaseUrlComponents(
                scheme = scheme,
                rawUserInfo = rawUserInfo,
                authority = authorityAndPath.substring(0, slashIndex),
                pathAndQuery = authorityAndPath.substring(slashIndex),
            )
        }

        private fun parseCredentials(
            rawUserInfo: String,
            url: String,
        ): Pair<String, String> {
            val userInfoParts = rawUserInfo.split(":", limit = 2)
            if (userInfoParts.size != 2 || userInfoParts[0].isBlank()) {
                invalidDatabaseUrl(url)
            }

            return URLDecoder.decode(userInfoParts[0], StandardCharsets.UTF_8) to
                URLDecoder.decode(userInfoParts[1], StandardCharsets.UTF_8)
        }

        private fun parseUri(
            scheme: String,
            authority: String,
            pathAndQuery: String,
            url: String,
        ): URI {
            return try {
                URI("$scheme://$authority$pathAndQuery")
            } catch (_: Exception) {
                invalidDatabaseUrl(url)
            }
        }

        private fun parseDatabaseName(
            uri: URI,
            url: String,
        ): String {
            val rawPath = uri.rawPath.orEmpty().removePrefix("/")
            if (rawPath.isBlank()) {
                invalidDatabaseUrl(url)
            }
            return URLDecoder.decode(rawPath, StandardCharsets.UTF_8)
        }

        private fun resolveHost(
            uri: URI,
            authority: String,
            url: String,
        ): String {
            val queryParams = parseQueryParams(uri.rawQuery)
            val socketHost = queryParams["host"] ?: queryParams["unixSocketPath"]
            val authorityHost =
                uri.host ?: authority.takeIf { it.isNotBlank() }?.substringBefore(":")?.removePrefix("[")?.removeSuffix("]")
            return authorityHost
                ?: socketHost
                ?: throw IllegalArgumentException(
                    "Invalid DATABASE_URL value: '$url'. Expected a host in authority " +
                        "or a 'host='/'unixSocketPath=' query parameter for Unix socket connections.",
                )
        }

        private fun buildJdbcUrl(
            uri: URI,
            port: Int,
            database: String,
        ): String {
            val querySuffix = uri.rawQuery?.takeIf { it.isNotBlank() }?.let { "?$it" }.orEmpty()
            return if (uri.host != null) {
                "jdbc:postgresql://${uri.host}:$port/$database$querySuffix"
            } else {
                "jdbc:postgresql://localhost/$database$querySuffix"
            }
        }

        private fun invalidDatabaseUrl(url: String): Nothing {
            throw IllegalArgumentException(
                "Invalid DATABASE_URL value: '$url'. Expected format like " +
                    "'postgresql://user:password@host:5432/database' or " +
                    "'postgres://user:password@host:5432/database'.",
            )
        }

        private fun parseQueryParams(rawQuery: String?): Map<String, String> {
            if (rawQuery.isNullOrBlank()) {
                return emptyMap()
            }

            return rawQuery
                .split("&")
                .mapNotNull { token ->
                    if (token.isBlank()) {
                        null
                    } else {
                        val parts = token.split("=", limit = 2)
                        val key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8)
                        val value = URLDecoder.decode(parts.getOrElse(1) { "" }, StandardCharsets.UTF_8)
                        key to value
                    }
                }.toMap()
        }
    }

    /**
     * Build JDBC connection string from configuration
     */
    fun connectionString(): String {
        return jdbcUrlOverride ?: "jdbc:postgresql://$host:$port/$database"
    }
}
