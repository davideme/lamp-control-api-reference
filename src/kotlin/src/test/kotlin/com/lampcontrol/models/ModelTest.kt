package com.lampcontrol.models

import com.lampcontrol.api.models.*
import com.lampcontrol.testutil.TestJson
import java.util.UUID
import kotlin.test.*
import kotlinx.serialization.*
import org.junit.jupiter.api.Test

class ModelTest {
    private val json = TestJson.instance

    @Test
    fun `test Lamp model serialization`() {
        val uuid = UUID.randomUUID()
        val now = java.time.Instant.now().toString()
        val lamp = Lamp(id = uuid, status = true, createdAt = now, updatedAt = now)

        val serialized = json.encodeToString(lamp)
        val deserialized = json.decodeFromString<Lamp>(serialized)

        assertEquals(lamp.id, deserialized.id)
        assertEquals(lamp.status, deserialized.status)
    }

    @Test
    fun `test LampCreate model serialization`() {
        val lampCreate = LampCreate(status = false)

        val serialized = json.encodeToString(lampCreate)
        val deserialized = json.decodeFromString<LampCreate>(serialized)

        assertEquals(lampCreate.status, deserialized.status)
    }

    @Test
    fun `test LampUpdate model serialization`() {
        val lampUpdate = LampUpdate(status = true)

        val serialized = json.encodeToString(lampUpdate)
        val deserialized = json.decodeFromString<LampUpdate>(serialized)

        assertEquals(lampUpdate.status, deserialized.status)
    }

    @Test
    fun `test Lamp model toString`() {
        val uuid = UUID.randomUUID()
        val now = java.time.Instant.now().toString()
        val lamp = Lamp(id = uuid, status = true, createdAt = now, updatedAt = now)

        val string = lamp.toString()
        assertNotNull(string)
    }

    @Test
    fun `test Lamp model equals and hashCode`() {
        val uuid = UUID.randomUUID()
        val now = java.time.Instant.now().toString()
        val lamp1 = Lamp(id = uuid, status = true, createdAt = now, updatedAt = now)
        val lamp2 = Lamp(id = uuid, status = true, createdAt = now, updatedAt = now)
        val lamp3 = Lamp(id = UUID.randomUUID(), status = false, createdAt = now, updatedAt = now)

        assertEquals(lamp1, lamp2)
        assertEquals(lamp1.hashCode(), lamp2.hashCode())

        // Different lamps should not be equal
        assertEquals(false, lamp1 == lamp3)
    }
}
