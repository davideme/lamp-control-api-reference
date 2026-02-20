package com.lampcontrol.service

import com.lampcontrol.entity.LampEntity
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID
import kotlin.test.*

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

    @Test
    fun `getLampsPage should return bounded deterministic slices`() =
        runTest {
            val t1 = Instant.parse("2026-01-01T00:00:00Z")
            val t2 = Instant.parse("2026-01-01T00:00:01Z")
            val idA = UUID.fromString("00000000-0000-0000-0000-000000000001")
            val idB = UUID.fromString("00000000-0000-0000-0000-000000000002")
            val idC = UUID.fromString("00000000-0000-0000-0000-000000000003")

            repo.createLamp(LampEntity(id = idB, status = true, createdAt = t1, updatedAt = t1))
            repo.createLamp(LampEntity(id = idA, status = false, createdAt = t1, updatedAt = t1))
            repo.createLamp(LampEntity(id = idC, status = true, createdAt = t2, updatedAt = t2))

            val page = repo.getLampsPage(offset = 0, limit = 2)
            assertEquals(listOf(idA, idB), page.map { it.id })

            val next = repo.getLampsPage(offset = 2, limit = 2)
            assertEquals(listOf(idC), next.map { it.id })
        }
}
