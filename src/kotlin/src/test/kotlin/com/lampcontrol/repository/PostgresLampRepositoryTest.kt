package com.lampcontrol.repository

import com.lampcontrol.database.LampsTable
import com.lampcontrol.entity.LampEntity
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.*

/**
 * Integration tests for PostgresLampRepository using Testcontainers.
 * Runs tests against a real PostgreSQL database in a Docker container.
 */
@Testcontainers
class PostgresLampRepositoryTest {

    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:13").apply {
            withDatabaseName("lamp_control_test")
            withUsername("test_user")
            withPassword("test_password")
        }
    }

    private lateinit var repository: PostgresLampRepository
    private lateinit var database: Database

    @BeforeEach
    fun setup() {
        // Connect to the Testcontainers PostgreSQL instance
        database = Database.connect(
            url = postgres.jdbcUrl,
            driver = "org.postgresql.Driver",
            user = postgres.username,
            password = postgres.password
        )

        // Create the lamps table
        transaction(database) {
            SchemaUtils.create(LampsTable)
        }

        repository = PostgresLampRepository()
    }

    @AfterEach
    fun tearDown() {
        // Drop the table after each test
        transaction(database) {
            SchemaUtils.drop(LampsTable)
        }
    }

    @Test
    fun `createLamp should add a new lamp to the database`() = runTest {
        // Given
        val entity = LampEntity.create(true)

        // When
        val created = repository.createLamp(entity)

        // Then
        assertEquals(entity.id, created.id)
        assertEquals(entity.status, created.status)
        assertTrue(repository.lampExists(entity.id))
    }

    @Test
    fun `getLampById should return lamp when it exists`() = runTest {
        // Given
        val entity = LampEntity.create(false)
        repository.createLamp(entity)

        // When
        val retrieved = repository.getLampById(entity.id)

        // Then
        assertNotNull(retrieved)
        assertEquals(entity.id, retrieved.id)
        assertEquals(entity.status, retrieved.status)
    }

    @Test
    fun `getLampById should return null when lamp does not exist`() = runTest {
        // Given
        val nonExistentId = java.util.UUID.randomUUID()

        // When
        val retrieved = repository.getLampById(nonExistentId)

        // Then
        assertNull(retrieved)
    }

    @Test
    fun `getAllLamps should return all non-deleted lamps`() = runTest {
        // Given
        val lamp1 = LampEntity.create(true)
        val lamp2 = LampEntity.create(false)
        repository.createLamp(lamp1)
        repository.createLamp(lamp2)

        // When
        val lamps = repository.getAllLamps()

        // Then
        assertEquals(2, lamps.size)
        assertTrue(lamps.any { it.id == lamp1.id })
        assertTrue(lamps.any { it.id == lamp2.id })
    }

    @Test
    fun `getAllLamps should not return deleted lamps`() = runTest {
        // Given
        val lamp1 = LampEntity.create(true)
        val lamp2 = LampEntity.create(false)
        repository.createLamp(lamp1)
        repository.createLamp(lamp2)
        repository.deleteLamp(lamp1.id)

        // When
        val lamps = repository.getAllLamps()

        // Then
        assertEquals(1, lamps.size)
        assertEquals(lamp2.id, lamps[0].id)
    }

    @Test
    fun `updateLamp should modify existing lamp`() = runTest {
        // Given
        val entity = LampEntity.create(false)
        repository.createLamp(entity)

        // When
        val updated = entity.copy(status = true)
        val result = repository.updateLamp(updated)

        // Then
        assertNotNull(result)
        assertEquals(entity.id, result.id)
        assertEquals(true, result.status)

        // Verify in database
        val retrieved = repository.getLampById(entity.id)
        assertNotNull(retrieved)
        assertEquals(true, retrieved.status)
    }

    @Test
    fun `updateLamp should return null when lamp does not exist`() = runTest {
        // Given
        val entity = LampEntity.create(true)

        // When
        val result = repository.updateLamp(entity)

        // Then
        assertNull(result)
    }

    @Test
    fun `updateLamp should return null when lamp is deleted`() = runTest {
        // Given
        val entity = LampEntity.create(true)
        repository.createLamp(entity)
        repository.deleteLamp(entity.id)

        // When
        val updated = entity.copy(status = false)
        val result = repository.updateLamp(updated)

        // Then
        assertNull(result)
    }

    @Test
    fun `deleteLamp should soft delete the lamp`() = runTest {
        // Given
        val entity = LampEntity.create(true)
        repository.createLamp(entity)

        // When
        val deleted = repository.deleteLamp(entity.id)

        // Then
        assertTrue(deleted)
        assertNull(repository.getLampById(entity.id))
        assertFalse(repository.lampExists(entity.id))
    }

    @Test
    fun `deleteLamp should return false when lamp does not exist`() = runTest {
        // Given
        val nonExistentId = java.util.UUID.randomUUID()

        // When
        val deleted = repository.deleteLamp(nonExistentId)

        // Then
        assertFalse(deleted)
    }

    @Test
    fun `deleteLamp should return false when lamp is already deleted`() = runTest {
        // Given
        val entity = LampEntity.create(true)
        repository.createLamp(entity)
        repository.deleteLamp(entity.id)

        // When
        val deleted = repository.deleteLamp(entity.id)

        // Then
        assertFalse(deleted)
    }

    @Test
    fun `lampExists should return true when lamp exists`() = runTest {
        // Given
        val entity = LampEntity.create(true)
        repository.createLamp(entity)

        // When
        val exists = repository.lampExists(entity.id)

        // Then
        assertTrue(exists)
    }

    @Test
    fun `lampExists should return false when lamp does not exist`() = runTest {
        // Given
        val nonExistentId = java.util.UUID.randomUUID()

        // When
        val exists = repository.lampExists(nonExistentId)

        // Then
        assertFalse(exists)
    }

    @Test
    fun `lampExists should return false when lamp is deleted`() = runTest {
        // Given
        val entity = LampEntity.create(true)
        repository.createLamp(entity)
        repository.deleteLamp(entity.id)

        // When
        val exists = repository.lampExists(entity.id)

        // Then
        assertFalse(exists)
    }

    @Test
    fun `repository should handle multiple lamps correctly`() = runTest {
        // Given
        val lamps = (1..5).map { LampEntity.create(it % 2 == 0) }
        lamps.forEach { repository.createLamp(it) }

        // When
        val retrieved = repository.getAllLamps()

        // Then
        assertEquals(5, retrieved.size)
    }

    @Test
    fun `create, retrieve, update and delete lamp lifecycle`() = runTest {
        // Create
        val entity = LampEntity.create(true)
        val created = repository.createLamp(entity)
        assertEquals(entity.id, created.id)

        // Retrieve
        val retrieved = repository.getLampById(entity.id)
        assertNotNull(retrieved)
        assertEquals(true, retrieved.status)

        // Update
        val updated = retrieved.copy(status = false)
        val updatedResult = repository.updateLamp(updated)
        assertNotNull(updatedResult)
        assertEquals(false, updatedResult.status)

        // Delete
        val deleted = repository.deleteLamp(entity.id)
        assertTrue(deleted)
        assertNull(repository.getLampById(entity.id))
    }
}
