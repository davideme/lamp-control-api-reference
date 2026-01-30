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
    fun `ListLamps200Response with empty list`() {
        val response = ListLamps200Response(data = emptyList(), hasMore = false)

        assertNotNull(response)
        assertEquals(0, response.data.size)
        assert(response.data.isEmpty())
    }

    @Test
    fun `ListLamps200Response data property is accessible`() {
        val lamp = Lamp(id = UUID.randomUUID(), status = true, createdAt = "2024-01-01T00:00:00Z", updatedAt = "2024-01-01T00:00:00Z")
        val response = ListLamps200Response(data = listOf(lamp), hasMore = false)

        val extractedData = response.data
        assertNotNull(extractedData)
        assertEquals(1, extractedData.size)
    }

    @Test
    fun `ListLamps200Response copy function works`() {
        val lamp = Lamp(id = UUID.randomUUID(), status = true, createdAt = "2024-01-01T00:00:00Z", updatedAt = "2024-01-01T00:00:00Z")
        val response = ListLamps200Response(data = listOf(lamp), hasMore = false)

        val copied = response.copy(data = emptyList(), hasMore = true)

        assertEquals(0, copied.data.size)
        assertEquals(1, response.data.size) // Original unchanged
    }

    @Test
    fun `ListLamps200Response toString contains data`() {
        val lamp = Lamp(id = UUID.randomUUID(), status = true, createdAt = "2024-01-01T00:00:00Z", updatedAt = "2024-01-01T00:00:00Z")
        val response = ListLamps200Response(data = listOf(lamp), hasMore = false)

        val string = response.toString()
        assert(string.contains("ListLamps200Response"))
        assert(string.contains("data"))
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
    fun `Error model toString contains error message`() {
        val error = Error(error = "Test error")

        val string = error.toString()
        assert(string.contains("Test error"))
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

    @Test
    fun `ListLamps200Response with hasMore true`() {
        val response = ListLamps200Response(data = emptyList(), hasMore = true, nextCursor = "cursor123")

        assertEquals(true, response.hasMore)
        assertEquals("cursor123", response.nextCursor)
    }

    @Test
    fun `ListLamps200Response with null nextCursor`() {
        val response = ListLamps200Response(data = emptyList(), hasMore = false, nextCursor = null)

        assertEquals(false, response.hasMore)
        assertEquals(null, response.nextCursor)
    }

    @Test
    fun `ListLamps200Response equals works correctly`() {
        val lamp = Lamp(id = UUID.randomUUID(), status = true, createdAt = "2024-01-01T00:00:00Z", updatedAt = "2024-01-01T00:00:00Z")
        val response1 = ListLamps200Response(data = listOf(lamp), hasMore = false)
        val response2 = ListLamps200Response(data = listOf(lamp), hasMore = false)

        assertEquals(response1, response2)
    }

    @Test
    fun `Lamp with different statuses`() {
        val lamp1 = Lamp(id = UUID.randomUUID(), status = true, createdAt = "2024-01-01T00:00:00Z", updatedAt = "2024-01-01T00:00:00Z")
        val lamp2 = Lamp(id = UUID.randomUUID(), status = false, createdAt = "2024-01-01T00:00:00Z", updatedAt = "2024-01-01T00:00:00Z")

        assertEquals(true, lamp1.status)
        assertEquals(false, lamp2.status)
        assert(lamp1 != lamp2)
    }

    @Test
    fun `Lamp copy function works`() {
        val id = UUID.randomUUID()
        val lamp = Lamp(id = id, status = true, createdAt = "2024-01-01T00:00:00Z", updatedAt = "2024-01-01T00:00:00Z")
        val copied = lamp.copy(status = false)

        assertEquals(id, copied.id)
        assertEquals(false, copied.status)
        assertEquals(true, lamp.status) // Original unchanged
    }

    @Test
    fun `Error copy function works`() {
        val error = Error(error = "Original error")
        val copied = error.copy(error = "New error")

        assertEquals("New error", copied.error)
        assertEquals("Original error", error.error)
    }

    @Test
    fun `LampCreate copy function works`() {
        val create = LampCreate(status = true)
        val copied = create.copy(status = false)

        assertEquals(false, copied.status)
        assertEquals(true, create.status)
    }

    @Test
    fun `LampUpdate copy function works`() {
        val update = LampUpdate(status = true)
        val copied = update.copy(status = false)

        assertEquals(false, copied.status)
        assertEquals(true, update.status)
    }
}
