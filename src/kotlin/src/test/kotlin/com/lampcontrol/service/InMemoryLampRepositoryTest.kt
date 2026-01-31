package com.lampcontrol.service

import com.lampcontrol.entity.LampEntity
import kotlin.test.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class InMemoryLampRepositoryTest {
    private val repo = InMemoryLampRepository()

    @Test
    fun `create, retrieve, update and delete lamp lifecycle`() =
        runTest {
            val entity = LampEntity.create(true)
            val created = repo.createLamp(entity)

            assertNotNull(created.id)
            assertTrue(created.status)
            assertNotNull(created.createdAt)
            assertNotNull(created.updatedAt)

            // retrieval
            val byId = repo.getLampById(created.id)
            assertEquals(created, byId)

            // update
            val beforeUpdatedAt = created.updatedAt
            val updatedEntity = created.withUpdatedStatus(false)
            val updated = repo.updateLamp(updatedEntity)
            assertNotNull(updated)
            assertFalse(updated!!.status)
            assertNotEquals(beforeUpdatedAt, updated.updatedAt)

            // existence and delete
            assertTrue(repo.lampExists(created.id))
            assertTrue(repo.deleteLamp(created.id))
            assertFalse(repo.lampExists(created.id))
            assertNull(repo.getLampById(created.id))
        }
}
