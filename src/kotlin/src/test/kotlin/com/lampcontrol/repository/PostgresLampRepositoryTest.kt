package com.lampcontrol.repository

import com.lampcontrol.database.LampsTable
import com.lampcontrol.entity.LampEntity
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Instant
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Integration tests for PostgresLampRepository using Testcontainers.
 * Tests against a real PostgreSQL database running in a Docker container.
 */
@Testcontainers
class PostgresLampRepositoryTest {

    companion object {
        @Container
        private val postgres = PostgreSQLContainer("postgres:16").apply {
            withDatabaseName("lamp_control_test")
            withUsername("test")
            withPassword("test")
        }

        private lateinit var database: Database

        @JvmStatic
        @BeforeAll
        fun setupDatabase() {
            val hikariConfig = HikariConfig().apply {
                jdbcUrl = postgres.jdbcUrl
                driverClassName = "org.postgresql.Driver"
                username = postgres.username
                password = postgres.password
                maximumPoolSize = 5
                minimumIdle = 1
                isAutoCommit = false
            }

            val dataSource = HikariDataSource(hikariConfig)
            database = Database.connect(dataSource)

            // Create schema
            transaction(database) {
                SchemaUtils.create(LampsTable)
            }
        }

        @JvmStatic
        @AfterAll
        fun teardownDatabase() {
            transaction(database) {
                SchemaUtils.drop(LampsTable)
            }
        }
    }

    private lateinit var repository: PostgresLampRepository

    @BeforeEach
    fun setup() {
        repository = PostgresLampRepository()

        // Clean up all lamps before each test
        runBlocking {
            repository.getAllLamps().forEach { lamp ->
                repository.deleteLamp(lamp.id)
            }
        }
    }

    @Test
    fun `getAllLamps returns empty list when no lamps exist`() = runBlocking {
        val lamps = repository.getAllLamps()

        assertTrue(lamps.isEmpty())
    }

    @Test
    fun `createLamp persists lamp to database`() = runBlocking {
        val entity = LampEntity.create(status = true)

        val created = repository.createLamp(entity)

        assertNotNull(created)
        assertEquals(entity.id, created.id)
        assertEquals(entity.status, created.status)
        assertEquals(entity.createdAt, created.createdAt)
        assertEquals(entity.updatedAt, created.updatedAt)
    }

    @Test
    fun `getLampById returns lamp when it exists`() = runBlocking {
        val entity = LampEntity.create(status = false)
        repository.createLamp(entity)

        val retrieved = repository.getLampById(entity.id)

        assertNotNull(retrieved)
        assertEquals(entity.id, retrieved.id)
        assertEquals(entity.status, retrieved.status)
    }

    @Test
    fun `getLampById returns null when lamp does not exist`() = runBlocking {
        val nonExistentId = UUID.randomUUID()

        val retrieved = repository.getLampById(nonExistentId)

        assertNull(retrieved)
    }

    @Test
    fun `getAllLamps returns all non-deleted lamps`() = runBlocking {
        val lamp1 = LampEntity.create(status = true)
        val lamp2 = LampEntity.create(status = false)
        val lamp3 = LampEntity.create(status = true)

        repository.createLamp(lamp1)
        repository.createLamp(lamp2)
        repository.createLamp(lamp3)

        val lamps = repository.getAllLamps()

        assertEquals(3, lamps.size)
        assertTrue(lamps.any { it.id == lamp1.id })
        assertTrue(lamps.any { it.id == lamp2.id })
        assertTrue(lamps.any { it.id == lamp3.id })
    }

    @Test
    fun `updateLamp updates status and timestamp`() = runBlocking {
        val entity = LampEntity.create(status = true)
        repository.createLamp(entity)

        // Wait a bit to ensure timestamp changes
        Thread.sleep(10)

        val updated = entity.withUpdatedStatus(newStatus = false)
        val result = repository.updateLamp(updated)

        assertNotNull(result)
        assertEquals(updated.id, result.id)
        assertEquals(false, result.status)
        assertTrue(result.updatedAt > entity.updatedAt)
    }

    @Test
    fun `updateLamp returns null when lamp does not exist`() = runBlocking {
        val nonExistentEntity = LampEntity(
            id = UUID.randomUUID(),
            status = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        val result = repository.updateLamp(nonExistentEntity)

        assertNull(result)
    }

    @Test
    fun `deleteLamp soft deletes lamp`() = runBlocking {
        val entity = LampEntity.create(status = true)
        repository.createLamp(entity)

        val deleted = repository.deleteLamp(entity.id)

        assertTrue(deleted)

        // Verify lamp is not returned by queries
        val retrieved = repository.getLampById(entity.id)
        assertNull(retrieved)

        val allLamps = repository.getAllLamps()
        assertTrue(allLamps.none { it.id == entity.id })
    }

    @Test
    fun `deleteLamp returns false when lamp does not exist`() = runBlocking {
        val nonExistentId = UUID.randomUUID()

        val deleted = repository.deleteLamp(nonExistentId)

        assertTrue(!deleted)
    }

    @Test
    fun `deleteLamp returns false when lamp already deleted`() = runBlocking {
        val entity = LampEntity.create(status = true)
        repository.createLamp(entity)
        repository.deleteLamp(entity.id)

        // Try to delete again
        val deleted = repository.deleteLamp(entity.id)

        assertTrue(!deleted)
    }

    @Test
    fun `lampExists returns true when lamp exists`() = runBlocking {
        val entity = LampEntity.create(status = true)
        repository.createLamp(entity)

        val exists = repository.lampExists(entity.id)

        assertTrue(exists)
    }

    @Test
    fun `lampExists returns false when lamp does not exist`() = runBlocking {
        val nonExistentId = UUID.randomUUID()

        val exists = repository.lampExists(nonExistentId)

        assertTrue(!exists)
    }

    @Test
    fun `lampExists returns false when lamp is deleted`() = runBlocking {
        val entity = LampEntity.create(status = true)
        repository.createLamp(entity)
        repository.deleteLamp(entity.id)

        val exists = repository.lampExists(entity.id)

        assertTrue(!exists)
    }

    @Test
    fun `updateLamp does not affect deleted lamps`() = runBlocking {
        val entity = LampEntity.create(status = true)
        repository.createLamp(entity)
        repository.deleteLamp(entity.id)

        val updated = entity.withUpdatedStatus(newStatus = false)
        val result = repository.updateLamp(updated)

        assertNull(result)
    }

    @Test
    fun `multiple lamps can be managed independently`() = runBlocking {
        val lamp1 = LampEntity.create(status = true)
        val lamp2 = LampEntity.create(status = false)

        repository.createLamp(lamp1)
        repository.createLamp(lamp2)

        // Update lamp1
        val updated1 = lamp1.withUpdatedStatus(newStatus = false)
        repository.updateLamp(updated1)

        // Delete lamp2
        repository.deleteLamp(lamp2.id)

        // Verify final state
        val allLamps = repository.getAllLamps()
        assertEquals(1, allLamps.size)
        assertEquals(lamp1.id, allLamps[0].id)
        assertEquals(false, allLamps[0].status)
    }

    @Test
    fun `created and updated timestamps are preserved correctly`() = runBlocking {
        val entity = LampEntity.create(status = true)
        val createdTime = entity.createdAt

        repository.createLamp(entity)

        Thread.sleep(10)

        val updated = entity.withUpdatedStatus(newStatus = false)
        val result = repository.updateLamp(updated)

        assertNotNull(result)
        assertEquals(createdTime, result.createdAt)
        assertTrue(result.updatedAt > createdTime)
    }
}
