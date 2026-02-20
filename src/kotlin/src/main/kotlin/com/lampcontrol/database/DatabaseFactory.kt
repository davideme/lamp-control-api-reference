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
                validate()
            }

        val dataSource = HikariDataSource(hikariConfig)
        val database = Database.connect(dataSource)

        // Set default transaction isolation level
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_REPEATABLE_READ

        return database
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
                port = System.getenv("DB_PORT")?.toIntOrNull() ?: 5432,
                database = database ?: "lamp_control",
                user = user ?: "lamp_user",
                password = System.getenv("DB_PASSWORD") ?: "",
                poolMin = System.getenv("DB_POOL_MIN_SIZE")?.toIntOrNull() ?: 0,
                poolMax = System.getenv("DB_POOL_MAX_SIZE")?.toIntOrNull() ?: 4,
                maxLifetimeMs = System.getenv("DB_MAX_LIFETIME_MS")?.toLongOrNull() ?: 3600000, // 1 hour
                idleTimeoutMs = System.getenv("DB_IDLE_TIMEOUT_MS")?.toLongOrNull() ?: 1800000, // 30 minutes
                connectionTimeoutMs = System.getenv("DB_CONNECTION_TIMEOUT_MS")?.toLongOrNull() ?: 30000, // 30 seconds
            )
        }

        /**
         * Parse DATABASE_URL format: postgresql://user:password@host:port/database
         */
        private fun parseDatabaseUrl(url: String): DatabaseConfig {
            val schemeMatch = Regex("""^(postgres(?:ql)?)://""", RegexOption.IGNORE_CASE).find(url)
            val scheme = schemeMatch?.groupValues?.get(1)?.lowercase()
            val schemePrefixLength = schemeMatch?.value?.length ?: 0
            if (scheme != "postgresql" && scheme != "postgres") {
                throw IllegalArgumentException(
                    "Invalid DATABASE_URL value: '$url'. Expected format like " +
                        "'postgresql://user:password@host:5432/database' or " +
                        "'postgres://user:password@host:5432/database'.",
                )
            }

            val rawWithoutScheme = url.substring(schemePrefixLength)
            val atIndex = rawWithoutScheme.indexOf('@')
            if (atIndex <= 0) {
                throw IllegalArgumentException(
                    "Invalid DATABASE_URL value: '$url'. Expected format like " +
                        "'postgresql://user:password@host:5432/database' or " +
                        "'postgres://user:password@host:5432/database'.",
                )
            }

            val rawUserInfo = rawWithoutScheme.substring(0, atIndex)
            val userInfoParts = rawUserInfo.split(":", limit = 2)
            if (userInfoParts.size != 2 || userInfoParts[0].isBlank()) {
                throw IllegalArgumentException(
                    "Invalid DATABASE_URL value: '$url'. Expected format like " +
                        "'postgresql://user:password@host:5432/database' or " +
                        "'postgres://user:password@host:5432/database'.",
                )
            }
            val user = URLDecoder.decode(userInfoParts[0], StandardCharsets.UTF_8)
            val password = URLDecoder.decode(userInfoParts[1], StandardCharsets.UTF_8)

            val authorityAndPath = rawWithoutScheme.substring(atIndex + 1)
            val slashIndex = authorityAndPath.indexOf('/')
            if (slashIndex < 0) {
                throw IllegalArgumentException(
                    "Invalid DATABASE_URL value: '$url'. Expected format like " +
                        "'postgresql://user:password@host:5432/database' or " +
                        "'postgres://user:password@host:5432/database'.",
                )
            }

            val authority = authorityAndPath.substring(0, slashIndex)
            val pathAndQuery = authorityAndPath.substring(slashIndex)
            val uri =
                try {
                    URI("$scheme://$authority$pathAndQuery")
                } catch (_: Exception) {
                    throw IllegalArgumentException(
                        "Invalid DATABASE_URL value: '$url'. Expected format like " +
                            "'postgresql://user:password@host:5432/database' or " +
                            "'postgres://user:password@host:5432/database'.",
                    )
                }

            val rawPath = uri.rawPath.orEmpty().removePrefix("/")
            if (rawPath.isBlank()) {
                throw IllegalArgumentException(
                    "Invalid DATABASE_URL value: '$url'. Expected format like " +
                        "'postgresql://user:password@host:5432/database' or " +
                        "'postgres://user:password@host:5432/database'.",
                )
            }
            val database = URLDecoder.decode(rawPath, StandardCharsets.UTF_8)

            val queryParams = parseQueryParams(uri.rawQuery)
            val socketHost = queryParams["host"]
            val authorityHost =
                uri.host ?: authority.takeIf { it.isNotBlank() }?.substringBefore(":")?.removePrefix("[")?.removeSuffix("]")
            val host = authorityHost ?: socketHost
            if (host.isNullOrBlank()) {
                throw IllegalArgumentException(
                    "Invalid DATABASE_URL value: '$url'. Expected a host in authority " +
                        "or a 'host=' query parameter for Unix socket connections.",
                )
            }
            val port = if (uri.port > 0) uri.port else 5432
            val jdbcUrl =
                if (uri.host != null) {
                    "jdbc:postgresql://${uri.host}:$port/$database${formatRawQuery(uri.rawQuery)}"
                } else {
                    "jdbc:postgresql:///$database${formatRawQuery(uri.rawQuery)}"
                }

            return DatabaseConfig(
                host = host,
                port = port,
                database = database,
                user = user,
                password = password,
                poolMin = System.getenv("DB_POOL_MIN_SIZE")?.toIntOrNull() ?: 0,
                poolMax = System.getenv("DB_POOL_MAX_SIZE")?.toIntOrNull() ?: 4,
                maxLifetimeMs = System.getenv("DB_MAX_LIFETIME_MS")?.toLongOrNull() ?: 3600000, // 1 hour
                idleTimeoutMs = System.getenv("DB_IDLE_TIMEOUT_MS")?.toLongOrNull() ?: 1800000, // 30 minutes
                connectionTimeoutMs = System.getenv("DB_CONNECTION_TIMEOUT_MS")?.toLongOrNull() ?: 30000, // 30 seconds
                jdbcUrlOverride = jdbcUrl,
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

        private fun formatRawQuery(rawQuery: String?): String {
            return if (rawQuery.isNullOrBlank()) "" else "?$rawQuery"
        }
    }

    /**
     * Build JDBC connection string from configuration
     */
    fun connectionString(): String {
        return jdbcUrlOverride ?: "jdbc:postgresql://$host:$port/$database"
    }
}
