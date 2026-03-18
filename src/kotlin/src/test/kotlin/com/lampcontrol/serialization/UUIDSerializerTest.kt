@file:Suppress("SwallowedException")

package com.lampcontrol.serialization

import com.lampcontrol.testutil.TestJson
import kotlinx.serialization.*
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals

class UUIDSerializerTest {
    private val json = TestJson.instance

    @Test
    fun `test UUID serialization`() {
        val uuid = UUID.randomUUID()
        val serialized = json.encodeToString(UUIDSerializer, uuid)
        val deserialized = json.decodeFromString(UUIDSerializer, serialized)

        assertEquals(uuid, deserialized)
    }

    @Test
    fun `test UUID serialization with known value`() {
        val uuid = UUID.fromString("01ad9dac-6699-436d-9516-d473a6e62447")
        val serialized = json.encodeToString(UUIDSerializer, uuid)
        val expected = "\"01ad9dac-6699-436d-9516-d473a6e62447\""

        assertEquals(expected, serialized)

        val deserialized = json.decodeFromString(UUIDSerializer, serialized)
        assertEquals(uuid, deserialized)
    }

    @Test
    fun `test invalid UUID string throws exception`() {
        val invalidUuidString = "\"invalid-uuid\""

        try {
            json.decodeFromString(UUIDSerializer, invalidUuidString)
            throw AssertionError("Expected exception was not thrown")
        } catch (e: Exception) {
            // Expected behavior
        }
    }
}
