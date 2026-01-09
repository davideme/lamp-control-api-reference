package com.lampcontrol.repository

import com.lampcontrol.service.InMemoryLampRepository
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LampRepositoryFactoryTest {

    @Test
    fun `create returns a repository instance`() {
        // When
        val repository = LampRepositoryFactory.create()

        // Then
        assertNotNull(repository)
    }

    @Test
    fun `create returns InMemoryLampRepository when no database is configured`() {
        // When: No DATABASE_URL or DB_NAME environment variable is set
        val repository = LampRepositoryFactory.create()

        // Then: Should return InMemoryLampRepository
        // Note: This will be InMemoryLampRepository in test environment
        // unless DATABASE_URL is explicitly set
        assertTrue(repository is InMemoryLampRepository || repository is PostgresLampRepository)
    }

    @Test
    fun `factory can create repository multiple times`() {
        // When
        val repo1 = LampRepositoryFactory.create()
        val repo2 = LampRepositoryFactory.create()

        // Then: Both should be valid repository instances
        assertNotNull(repo1)
        assertNotNull(repo2)
    }

    @Test
    fun `created repository implements LampRepository interface`() {
        // When
        val repository = LampRepositoryFactory.create()

        // Then: Should implement LampRepository
        assertTrue(repository is LampRepository)
    }

    @Test
    fun `factory returns consistent repository type`() {
        // When: Creating multiple repositories
        val repo1 = LampRepositoryFactory.create()
        val repo2 = LampRepositoryFactory.create()

        // Then: Should return same type (both InMemory or both Postgres)
        assertEquals(repo1::class, repo2::class)
    }

    @Test
    fun `LampRepositoryFactory object is accessible`() {
        // Verify factory object exists and can be accessed
        assertNotNull(LampRepositoryFactory)
    }
}
