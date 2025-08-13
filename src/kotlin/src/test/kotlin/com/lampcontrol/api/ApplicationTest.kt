package com.lampcontrol.api

import com.lampcontrol.api.models.LampCreate
import com.lampcontrol.api.models.LampUpdate
import com.lampcontrol.serialization.UUIDSerializer
import com.lampcontrol.module
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.modules.SerializersModule
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ApplicationTest {
    
    // Configure JSON with the same settings as the application
    private val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
        serializersModule = SerializersModule {
            contextual(UUID::class, UUIDSerializer)
        }
    }
    
    @Test
    fun testHealthEndpoint() = testApplication {
        application {
            module()
        }
        
        client.get("/health").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("OK", bodyAsText())
        }
    }

    @Test
    fun testRootEndpoint() = testApplication {
        application {
            module()
        }
        
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Lamp Control API - Kotlin Implementation", bodyAsText())
        }
    }

    @Test
    fun testListLampsEmpty() = testApplication {
        application {
            module()
        }
        
        client.get("/v1/lamps").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("[]", bodyAsText())
        }
    }

    @Test
    fun testCreateAndGetLamp() = testApplication {
        application {
            module()
        }
        
        // Create a lamp
        val lampCreate = LampCreate(status = true)
        val createResponse = client.post("/v1/lamps") {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(lampCreate))
        }
        
        assertEquals(HttpStatusCode.Created, createResponse.status)
        val createdLampJson = createResponse.bodyAsText()
        assertNotNull(createdLampJson)
        println("DEBUG: Created lamp response: $createdLampJson")
        
        // Use proper JSON parsing instead of string manipulation
        val createdLamp = json.decodeFromString<com.lampcontrol.api.models.Lamp>(createdLampJson)
        assertTrue(createdLamp.status)
        
        // Get the created lamp
        val getResponse = client.get("/v1/lamps/${createdLamp.id}")
        assertEquals(HttpStatusCode.OK, getResponse.status)
        
        val retrievedLampJson = getResponse.bodyAsText()
        println("DEBUG: Retrieved lamp response: $retrievedLampJson")
        val retrievedLamp = json.decodeFromString<com.lampcontrol.api.models.Lamp>(retrievedLampJson)
        assertTrue(retrievedLamp.status)
        assertEquals(createdLamp.id, retrievedLamp.id)
    }

    @Test
    fun testGetNonExistentLamp() = testApplication {
        application {
            module()
        }
        
        client.get("/v1/lamps/non-existent-id").apply {
            assertEquals(HttpStatusCode.NotFound, status)
            assertTrue(bodyAsText().contains("Lamp not found"))
        }
    }

    @Test
    fun testUpdateLamp() = testApplication {
        application {
            module()
        }
        
        // Create a lamp
        val lampCreate = LampCreate(status = true)
        val createResponse = client.post("/v1/lamps") {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(lampCreate))
        }
        
        assertEquals(HttpStatusCode.Created, createResponse.status)
        val createdLampJson = createResponse.bodyAsText()
        
        // Use proper JSON parsing instead of string manipulation
        val createdLamp = json.decodeFromString<com.lampcontrol.api.models.Lamp>(createdLampJson)
        
        // Update the lamp
        val lampUpdate = LampUpdate(status = false)
        val updateResponse = client.put("/v1/lamps/${createdLamp.id}") {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(lampUpdate))
        }
        
        assertEquals(HttpStatusCode.OK, updateResponse.status)
        val updatedLampJson = updateResponse.bodyAsText()
        val updatedLamp = json.decodeFromString<com.lampcontrol.api.models.Lamp>(updatedLampJson)
        assertEquals(false, updatedLamp.status)
        assertEquals(createdLamp.id, updatedLamp.id)
    }

    @Test
    fun testUpdateNonExistentLamp() = testApplication {
        application {
            module()
        }
        
        val lampUpdate = LampUpdate(status = false)
        client.put("/v1/lamps/non-existent-id") {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(lampUpdate))
        }.apply {
            assertEquals(HttpStatusCode.NotFound, status)
            assertTrue(bodyAsText().contains("Lamp not found"))
        }
    }

    @Test
    fun testDeleteLamp() = testApplication {
        application {
            module()
        }
        
        // Create a lamp
        val lampCreate = LampCreate(status = true)
        val createResponse = client.post("/v1/lamps") {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(lampCreate))
        }
        
        assertEquals(HttpStatusCode.Created, createResponse.status)
        val createdLampJson = createResponse.bodyAsText()
        
        // Use proper JSON parsing instead of string manipulation
        val createdLamp = json.decodeFromString<com.lampcontrol.api.models.Lamp>(createdLampJson)
        
        // Delete the lamp
        val deleteResponse = client.delete("/v1/lamps/${createdLamp.id}")
        assertEquals(HttpStatusCode.NoContent, deleteResponse.status)
        
        // Verify lamp is deleted
        val getResponse = client.get("/v1/lamps/${createdLamp.id}")
        assertEquals(HttpStatusCode.NotFound, getResponse.status)
    }

    @Test
    fun testDeleteNonExistentLamp() = testApplication {
        application {
            module()
        }
        
        client.delete("/v1/lamps/non-existent-id").apply {
            assertEquals(HttpStatusCode.NotFound, status)
            assertTrue(bodyAsText().contains("Lamp not found"))
        }
    }
}
