package com.lampcontrol.service

import com.lampcontrol.api.models.*
import com.lampcontrol.domain.DomainException
import com.lampcontrol.mapper.LampMapper
import com.lampcontrol.repository.LampRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

class LampServiceTest {
    private lateinit var lampService: LampService

    @BeforeEach
    fun setUp() {
        val repository: LampRepository = InMemoryLampRepository()
        val mapper = LampMapper()
        lampService = LampService(repository, mapper)
    }

    @Test
    fun `createLamp should create a new lamp with generated UUID`() =
        runTest {
            val lampCreate = LampCreate(status = true)
            val createdLamp = lampService.createLamp(lampCreate)

            assertNotNull(createdLamp.id)
            assertEquals(true, createdLamp.status)
            assertTrue(lampService.lampExists(createdLamp.id.toString()))
        }

    @Test
    fun `getAllLamps should return empty list initially`() =
        runTest {
            val lamps = lampService.getAllLamps()
            assertTrue(lamps.isEmpty())
        }

    @Test
    fun `getAllLamps should return all created lamps`() =
        runTest {
            val lamp1 = lampService.createLamp(LampCreate(status = true))
            val lamp2 = lampService.createLamp(LampCreate(status = false))

            val lamps = lampService.getAllLamps()
            assertEquals(2, lamps.size)
            assertTrue(lamps.contains(lamp1))
            assertTrue(lamps.contains(lamp2))
        }

    @Test
    fun `getLampById should throw InvalidId for invalid UUID`() =
        runTest {
            assertThrows<DomainException.InvalidId> {
                lampService.getLampById("non-existent-id")
            }
        }

    @Test
    fun `getLampById should throw NotFound for non-existent lamp`() =
        runTest {
            assertThrows<DomainException.NotFound> {
                lampService.getLampById("01ad9dac-6699-436d-9516-d473a6e62447")
            }
        }

    @Test
    fun `getLampById should throw IllegalArgumentException for blank ID`() =
        runTest {
            assertThrows<IllegalArgumentException> {
                lampService.getLampById("")
            }
        }

    @Test
    fun `getLampById should return existing lamp`() =
        runTest {
            val createdLamp = lampService.createLamp(LampCreate(status = true))
            val retrievedLamp = lampService.getLampById(createdLamp.id.toString())

            assertEquals(createdLamp, retrievedLamp)
        }

    @Test
    fun `updateLamp should throw InvalidId for invalid UUID`() =
        runTest {
            val lampUpdate = LampUpdate(status = false)
            assertThrows<DomainException.InvalidId> {
                lampService.updateLamp("non-existent-id", lampUpdate)
            }
        }

    @Test
    fun `updateLamp should throw NotFound for non-existent lamp`() =
        runTest {
            val lampUpdate = LampUpdate(status = false)
            assertThrows<DomainException.NotFound> {
                lampService.updateLamp("01ad9dac-6699-436d-9516-d473a6e62447", lampUpdate)
            }
        }

    @Test
    fun `updateLamp should update existing lamp status`() =
        runTest {
            val createdLamp = lampService.createLamp(LampCreate(status = true))
            val lampUpdate = LampUpdate(status = false)
            val updatedLamp = lampService.updateLamp(createdLamp.id.toString(), lampUpdate)

            assertEquals(createdLamp.id, updatedLamp.id)
            assertEquals(false, updatedLamp.status)
        }

    @Test
    fun `deleteLamp should throw InvalidId for invalid UUID`() =
        runTest {
            assertThrows<DomainException.InvalidId> {
                lampService.deleteLamp("non-existent-id")
            }
        }

    @Test
    fun `deleteLamp should throw NotFound for non-existent lamp`() =
        runTest {
            assertThrows<DomainException.NotFound> {
                lampService.deleteLamp("01ad9dac-6699-436d-9516-d473a6e62447")
            }
        }

    @Test
    fun `deleteLamp should delete existing lamp`() =
        runTest {
            val createdLamp = lampService.createLamp(LampCreate(status = true))
            val lampId = createdLamp.id.toString()

            assertTrue(lampService.lampExists(lampId))
            lampService.deleteLamp(lampId)
            assertFalse(lampService.lampExists(lampId))
            assertThrows<DomainException.NotFound> {
                lampService.getLampById(lampId)
            }
        }

    @Test
    fun `lampExists should throw InvalidId for invalid UUID`() =
        runTest {
            assertThrows<DomainException.InvalidId> {
                lampService.lampExists("non-existent-id")
            }
        }

    @Test
    fun `lampExists should return false for non-existent valid UUID`() =
        runTest {
            assertFalse(lampService.lampExists("01ad9dac-6699-436d-9516-d473a6e62447"))
        }

    @Test
    fun `lampExists should return true for existing lamp`() =
        runTest {
            val createdLamp = lampService.createLamp(LampCreate(status = true))
            assertTrue(lampService.lampExists(createdLamp.id.toString()))
        }
}
