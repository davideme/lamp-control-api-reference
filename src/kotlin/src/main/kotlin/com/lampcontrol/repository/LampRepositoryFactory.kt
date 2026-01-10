package com.lampcontrol.repository

import com.lampcontrol.database.DatabaseFactory
import com.lampcontrol.service.InMemoryLampRepository
import org.slf4j.LoggerFactory

/**
 * Factory for creating the appropriate LampRepository implementation based on configuration.
 * Uses PostgreSQL if DATABASE_URL or DB_NAME is configured, otherwise uses in-memory storage.
 */
object LampRepositoryFactory {
    private val logger = LoggerFactory.getLogger(LampRepositoryFactory::class.java)

    /**
     * Create and initialize the appropriate repository implementation.
     * Returns PostgresLampRepository if database is configured, otherwise InMemoryLampRepository.
     */
    fun create(): LampRepository {
        val database = DatabaseFactory.init()

        return if (database != null) {
            logger.info("Using PostgreSQL storage for lamp repository")
            PostgresLampRepository()
        } else {
            logger.info("Using in-memory storage for lamp repository (no DATABASE_URL or DB_NAME configured)")
            InMemoryLampRepository()
        }
    }
}
