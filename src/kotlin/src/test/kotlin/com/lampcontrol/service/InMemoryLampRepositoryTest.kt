package com.lampcontrol.service

import com.lampcontrol.api.models.LampCreate
import com.lampcontrol.api.models.LampUpdate
import org.junit.jupiter.api.Test
import kotlin.test.*

class InMemoryLampRepositoryTest {
    private val repo = InMemoryLampRepository()

    @Test
    fun `create, retrieve, update and delete lamp lifecycle`() {
        val created = repo.createLamp(LampCreate(status = true))

        assertNotNull(created.id)
        assertTrue(created.status)
        assertNotNull(created.createdAt)
        assertNotNull(created.updatedAt)

        // retrieval
        val byId = repo.getLampById(created.id.toString())
        assertEquals(created, byId)

        // update
        val beforeUpdatedAt = created.updatedAt
        val updated = repo.updateLamp(created.id.toString(), LampUpdate(status = false))
        assertNotNull(updated)
        assertFalse(updated!!.status)
        assertNotEquals(beforeUpdatedAt, updated.updatedAt)

        // existence and delete
        assertTrue(repo.lampExists(created.id.toString()))
        assertTrue(repo.deleteLamp(created.id.toString()))
        assertFalse(repo.lampExists(created.id.toString()))
        assertNull(repo.getLampById(created.id.toString()))
    }
}
