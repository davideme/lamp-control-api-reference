package com.lampcontrol.api

import com.lampcontrol.api.models.LampCreate
import com.lampcontrol.api.models.LampUpdate
import com.lampcontrol.module
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ApplicationTest {
    
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
            setBody(Json.encodeToString(lampCreate))
        }
        
        assertEquals(HttpStatusCode.Created, createResponse.status)
        val createdLampJson = createResponse.bodyAsText()
        assertNotNull(createdLampJson)
        assertTrue(createdLampJson.contains("\"status\":true"))
        
        // Extract lamp ID from response (simplified parsing)
        val lampIdStart = createdLampJson.indexOf("\"id\":\"") + 6
        val lampIdEnd = createdLampJson.indexOf("\"", lampIdStart)
        val lampId = createdLampJson.substring(lampIdStart, lampIdEnd)
        
        // Get the created lamp
        val getResponse = client.get("/v1/lamps/$lampId")
        assertEquals(HttpStatusCode.OK, getResponse.status)
        
        val retrievedLampJson = getResponse.bodyAsText()
        assertTrue(retrievedLampJson.contains("\"status\":true"))
        assertTrue(retrievedLampJson.contains(lampId))
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
            setBody(Json.encodeToString(lampCreate))
        }
        
        assertEquals(HttpStatusCode.Created, createResponse.status)
        val createdLampJson = createResponse.bodyAsText()
        
        // Extract lamp ID from response
        val lampIdStart = createdLampJson.indexOf("\"id\":\"") + 6
        val lampIdEnd = createdLampJson.indexOf("\"", lampIdStart)
        val lampId = createdLampJson.substring(lampIdStart, lampIdEnd)
        
        // Update the lamp
        val lampUpdate = LampUpdate(status = false)
        val updateResponse = client.put("/v1/lamps/$lampId") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(lampUpdate))
        }
        
        assertEquals(HttpStatusCode.OK, updateResponse.status)
        val updatedLampJson = updateResponse.bodyAsText()
        assertTrue(updatedLampJson.contains("\"status\":false"))
        assertTrue(updatedLampJson.contains(lampId))
    }

    @Test
    fun testUpdateNonExistentLamp() = testApplication {
        application {
            module()
        }
        
        val lampUpdate = LampUpdate(status = false)
        client.put("/v1/lamps/non-existent-id") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(lampUpdate))
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
            setBody(Json.encodeToString(lampCreate))
        }
        
        assertEquals(HttpStatusCode.Created, createResponse.status)
        val createdLampJson = createResponse.bodyAsText()
        
        // Extract lamp ID from response
        val lampIdStart = createdLampJson.indexOf("\"id\":\"") + 6
        val lampIdEnd = createdLampJson.indexOf("\"", lampIdStart)
        val lampId = createdLampJson.substring(lampIdStart, lampIdEnd)
        
        // Delete the lamp
        val deleteResponse = client.delete("/v1/lamps/$lampId")
        assertEquals(HttpStatusCode.NoContent, deleteResponse.status)
        
        // Verify lamp is deleted
        val getResponse = client.get("/v1/lamps/$lampId")
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
