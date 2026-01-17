package com.lampcontrol.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.slf4j.LoggerFactory
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

        // Run database migrations before initializing connection pool
        val migrationSuccess = FlywayConfig.runMigrations(config)
        if (!migrationSuccess) {
            logger.error("Database migrations failed. Database connection will not be initialized.")
            return null
        }

        val hikariConfig = HikariConfig().apply {
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
    val connectionTimeoutMs: Long
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
            val postgresConfigured = !databaseUrl.isNullOrEmpty() ||
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
                connectionTimeoutMs = System.getenv("DB_CONNECTION_TIMEOUT_MS")?.toLongOrNull() ?: 30000 // 30 seconds
            )
        }

        /**
         * Parse DATABASE_URL format: postgresql://user:password@host:port/database
         */
        private fun parseDatabaseUrl(url: String): DatabaseConfig {
            val regex = Regex("""postgres(?:ql)?://([^:]+):([^@]+)@([^:]+):(\d+)/(.+)""")
            val match = regex.matchEntire(url)
                ?: throw IllegalArgumentException(
                    "Invalid DATABASE_URL value: '$url'. Expected format like " +
                        "'postgresql://user:password@host:5432/database' or " +
                        "'postgres://user:password@host:5432/database'."
                )

            val (user, password, host, port, database) = match.destructured

            return DatabaseConfig(
                host = host,
                port = port.toInt(),
                database = database,
                user = user,
                password = password,
                poolMin = System.getenv("DB_POOL_MIN_SIZE")?.toIntOrNull() ?: 0,
                poolMax = System.getenv("DB_POOL_MAX_SIZE")?.toIntOrNull() ?: 4,
                maxLifetimeMs = System.getenv("DB_MAX_LIFETIME_MS")?.toLongOrNull() ?: 3600000, // 1 hour
                idleTimeoutMs = System.getenv("DB_IDLE_TIMEOUT_MS")?.toLongOrNull() ?: 1800000, // 30 minutes
                connectionTimeoutMs = System.getenv("DB_CONNECTION_TIMEOUT_MS")?.toLongOrNull() ?: 30000 // 30 seconds
            )
        }
    }

    /**
     * Build JDBC connection string from configuration
     */
    fun connectionString(): String {
        return "jdbc:postgresql://$host:$port/$database"
    }
}
