package com.lampcontrol.database

import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.slf4j.LoggerFactory

/**
 * Flyway configuration for database migrations
 */
object FlywayConfig {
    private val logger = LoggerFactory.getLogger(FlywayConfig::class.java)

    /**
     * Run database migrations using Flyway.
     * Should be called before initializing the database connection.
     *
     * @param config Database configuration
     * @return true if migrations were successful or skipped, false if migrations failed
     */
    fun runMigrations(config: DatabaseConfig): Boolean {
        return try {
            logger.info("Starting database migrations with Flyway")

            val flyway = Flyway.configure()
                .dataSource(
                    config.connectionString(),
                    config.user,
                    config.password
                )
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .validateOnMigrate(true)
                .load()

            val migrationsExecuted = flyway.migrate()

            if (migrationsExecuted.migrationsExecuted > 0) {
                logger.info(
                    "Successfully executed {} migration(s). Current schema version: {}",
                    migrationsExecuted.migrationsExecuted,
                    migrationsExecuted.targetSchemaVersion
                )
            } else {
                logger.info(
                    "Database schema is up to date at version: {}",
                    flyway.info().current()?.version ?: "baseline"
                )
            }

            true
        } catch (e: Exception) {
            logger.error("Failed to run database migrations", e)
            false
        }
    }
}
