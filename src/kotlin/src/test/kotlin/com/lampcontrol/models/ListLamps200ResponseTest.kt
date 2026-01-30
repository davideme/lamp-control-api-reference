package com.lampcontrol.models

import com.lampcontrol.api.models.*
import java.util.UUID
import kotlin.test.*
import org.junit.jupiter.api.Test

class ListLamps200ResponseTest {
    @Test
    fun `list response holds data and pagination`() {
        val now = java.time.Instant.now().toString()
        val lamp = Lamp(id = UUID.randomUUID(), status = true, createdAt = now, updatedAt = now)
        val resp = ListLamps200Response(data = listOf(lamp), hasMore = false, nextCursor = null)

        assertEquals(1, resp.data.size)
        assertFalse(resp.hasMore)
        assertNull(resp.nextCursor)
    }
}
