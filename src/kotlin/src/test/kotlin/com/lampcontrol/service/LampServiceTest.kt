package com.lampcontrol.service

import com.lampcontrol.api.models.LampCreate
import com.lampcontrol.api.models.LampUpdate
import com.lampcontrol.mapper.LampMapper
import com.lampcontrol.repository.LampRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class LampServiceTest {
    private lateinit var lampService: LampService

    @BeforeEach
    fun setUp() {
        val repository: LampRepository = InMemoryLampRepository()
        val mapper = LampMapper()
        lampService = LampService(repository, mapper)
    }

    @Test
    fun `createLamp should create a new lamp with generated UUID`() = runTest {
        val lampCreate = LampCreate(status = true)
        val createdLamp = lampService.createLamp(lampCreate)

        assertNotNull(createdLamp.id)
        assertEquals(true, createdLamp.status)
        assertTrue(lampService.lampExists(createdLamp.id.toString()))
    }

    @Test
    fun `getAllLamps should return empty list initially`() = runTest {
        val lamps = lampService.getAllLamps()
        assertTrue(lamps.isEmpty())
    }

    @Test
    fun `getAllLamps should return all created lamps`() = runTest {
        val lamp1 = lampService.createLamp(LampCreate(status = true))
        val lamp2 = lampService.createLamp(LampCreate(status = false))

        val lamps = lampService.getAllLamps()
        assertEquals(2, lamps.size)
        assertTrue(lamps.contains(lamp1))
        assertTrue(lamps.contains(lamp2))
    }

    @Test
    fun `getLampById should return null for non-existent lamp`() = runTest {
        val lamp = lampService.getLampById("non-existent-id")
        assertNull(lamp)
    }

    @Test
    fun `getLampById should return existing lamp`() = runTest {
        val createdLamp = lampService.createLamp(LampCreate(status = true))
        val retrievedLamp = lampService.getLampById(createdLamp.id.toString())

        assertNotNull(retrievedLamp)
        assertEquals(createdLamp, retrievedLamp)
    }

    @Test
    fun `updateLamp should return null for non-existent lamp`() = runTest {
        val lampUpdate = LampUpdate(status = false)
        val updatedLamp = lampService.updateLamp("non-existent-id", lampUpdate)

        assertNull(updatedLamp)
    }

    @Test
    fun `updateLamp should update existing lamp status`() = runTest {
        val createdLamp = lampService.createLamp(LampCreate(status = true))
        val lampUpdate = LampUpdate(status = false)
        val updatedLamp = lampService.updateLamp(createdLamp.id.toString(), lampUpdate)

        assertNotNull(updatedLamp)
        assertEquals(createdLamp.id, updatedLamp!!.id)
        assertEquals(false, updatedLamp.status)
    }

    @Test
    fun `deleteLamp should return false for non-existent lamp`() = runTest {
        val deleted = lampService.deleteLamp("non-existent-id")
        assertFalse(deleted)
    }

    @Test
    fun `deleteLamp should delete existing lamp`() = runTest {
        val createdLamp = lampService.createLamp(LampCreate(status = true))
        val lampId = createdLamp.id.toString()

        assertTrue(lampService.lampExists(lampId))
        assertTrue(lampService.deleteLamp(lampId))
        assertFalse(lampService.lampExists(lampId))
        assertNull(lampService.getLampById(lampId))
    }

    @Test
    fun `lampExists should return false for non-existent lamp`() = runTest {
        assertFalse(lampService.lampExists("non-existent-id"))
    }

    @Test
    fun `lampExists should return true for existing lamp`() = runTest {
        val createdLamp = lampService.createLamp(LampCreate(status = true))
        assertTrue(lampService.lampExists(createdLamp.id.toString()))
    }
}
