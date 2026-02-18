package com.lampcontrol.api.models

import java.util.UUID
import kotlin.test.*
import org.junit.jupiter.api.Test

class ApiModelsTest {
    @Test
    fun `ListLamps200Response can be created with data`() {
        val lamp1 = Lamp(id = UUID.randomUUID(), status = true, createdAt = "2024-01-01T00:00:00Z", updatedAt = "2024-01-01T00:00:00Z")
        val lamp2 = Lamp(id = UUID.randomUUID(), status = false, createdAt = "2024-01-01T00:00:00Z", updatedAt = "2024-01-01T00:00:00Z")

        val response = ListLamps200Response(data = listOf(lamp1, lamp2), hasMore = false)

        assertNotNull(response)
        assertEquals(2, response.data.size)
        assertEquals(lamp1, response.data[0])
        assertEquals(lamp2, response.data[1])
    }

    @Test
    fun `ListLamps200Response with pagination`() {
        val response = ListLamps200Response(data = emptyList(), hasMore = true, nextCursor = "cursor123")

        assertEquals(true, response.hasMore)
        assertEquals("cursor123", response.nextCursor)
    }

    @Test
    fun `Lamp can be created with all properties`() {
        val id = UUID.randomUUID()
        val lamp =
            Lamp(
                id = id,
                status = true,
                createdAt = "2024-01-01T00:00:00Z",
                updatedAt = "2024-01-01T00:00:00Z",
            )

        assertEquals(id, lamp.id)
        assertEquals(true, lamp.status)
        assertEquals("2024-01-01T00:00:00Z", lamp.createdAt)
        assertEquals("2024-01-01T00:00:00Z", lamp.updatedAt)
    }

    @Test
    fun `Error model can be created`() {
        val error = Error(error = "Test error message")

        assertNotNull(error)
        assertEquals("Test error message", error.error)
    }

    @Test
    fun `LampCreate can be created with status`() {
        val create = LampCreate(status = true)

        assertNotNull(create)
        assertEquals(true, create.status)
    }

    @Test
    fun `LampUpdate can be created with status`() {
        val update = LampUpdate(status = false)

        assertNotNull(update)
        assertEquals(false, update.status)
    }
}
