package com.lampcontrol.api

import com.lampcontrol.module
import com.lampcontrol.api.models.*
import com.lampcontrol.testutil.TestJson
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.server.testing.testApplication
import kotlin.test.*
import kotlinx.serialization.*
import org.junit.jupiter.api.Test

class ApplicationTest {
    private val json = TestJson.instance

    @Test
    fun testHealthEndpoint() =
        testApplication {
            application {
                module()
            }

            client.get("/health").apply {
                assertEquals(HttpStatusCode.OK, status)
                val responseBody = json.decodeFromString<Map<String, String>>(bodyAsText())
                assertEquals("ok", responseBody["status"])
            }
        }

    @Test
    fun testRootEndpoint() =
        testApplication {
            application {
                module()
            }

            client.get("/").apply {
                assertEquals(HttpStatusCode.OK, status)
                assertEquals("Lamp Control API - Kotlin Implementation", bodyAsText())
            }
        }

    @Test
    fun testListLampsEmpty() =
        testApplication {
            application {
                module()
            }

            client.get("/v1/lamps").apply {
                assertEquals(HttpStatusCode.OK, status)
                // Response should be an object with `data` array per OpenAPI
                val respJson = bodyAsText()
                val parsed = json.decodeFromString<ListLamps200Response>(respJson)
                assertTrue(parsed.data.isEmpty())
            }
        }

    @Test
    fun testCreateAndGetLamp() =
        testApplication {
            application {
                module()
            }

            // Create a lamp
            val lampCreate = LampCreate(status = true)
            val createResponse =
                client.post("/v1/lamps") {
                    contentType(ContentType.Application.Json)
                    setBody(json.encodeToString(lampCreate))
                }

            assertEquals(HttpStatusCode.Created, createResponse.status)
            val createdLampJson = createResponse.bodyAsText()
            assertNotNull(createdLampJson)

            // Use proper JSON parsing instead of string manipulation
            val createdLamp = json.decodeFromString<Lamp>(createdLampJson)
            assertTrue(createdLamp.status)

            // Get the created lamp
            val getResponse = client.get("/v1/lamps/${createdLamp.id}")
            assertEquals(HttpStatusCode.OK, getResponse.status)

            val retrievedLampJson = getResponse.bodyAsText()
            val retrievedLamp = json.decodeFromString<Lamp>(retrievedLampJson)
            assertTrue(retrievedLamp.status)
            assertEquals(createdLamp.id, retrievedLamp.id)
        }

    @Test
    fun testGetLampWithInvalidId() =
        testApplication {
            application {
                module()
            }

            client.get("/v1/lamps/non-existent-id").apply {
                assertEquals(HttpStatusCode.BadRequest, status)
                assertTrue(bodyAsText().contains("Invalid lampId format"))
            }
        }

    @Test
    fun testGetNonExistentLamp() =
        testApplication {
            application {
                module()
            }

            client.get("/v1/lamps/01ad9dac-6699-436d-9516-d473a6e62447").apply {
                assertEquals(HttpStatusCode.NotFound, status)
                assertTrue(bodyAsText().contains("Lamp not found"))
            }
        }

    @Test
    fun testUpdateLamp() =
        testApplication {
            application {
                module()
            }

            // Create a lamp
            val lampCreate = LampCreate(status = true)
            val createResponse =
                client.post("/v1/lamps") {
                    contentType(ContentType.Application.Json)
                    setBody(json.encodeToString(lampCreate))
                }

            assertEquals(HttpStatusCode.Created, createResponse.status)
            val createdLampJson = createResponse.bodyAsText()

            // Use proper JSON parsing instead of string manipulation
            val createdLamp = json.decodeFromString<Lamp>(createdLampJson)

            // Update the lamp
            val lampUpdate = LampUpdate(status = false)
            val updateResponse =
                client.put("/v1/lamps/${createdLamp.id}") {
                    contentType(ContentType.Application.Json)
                    setBody(json.encodeToString(lampUpdate))
                }

            assertEquals(HttpStatusCode.OK, updateResponse.status)
            val updatedLampJson = updateResponse.bodyAsText()
            val updatedLamp = json.decodeFromString<Lamp>(updatedLampJson)
            assertEquals(false, updatedLamp.status)
            assertEquals(createdLamp.id, updatedLamp.id)
        }

    @Test
    fun testUpdateLampWithInvalidId() =
        testApplication {
            application {
                module()
            }

            val lampUpdate = LampUpdate(status = false)
            client.put("/v1/lamps/non-existent-id") {
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(lampUpdate))
            }.apply {
                assertEquals(HttpStatusCode.BadRequest, status)
                assertTrue(bodyAsText().contains("Invalid lampId format"))
            }
        }

    @Test
    fun testUpdateNonExistentLamp() =
        testApplication {
            application {
                module()
            }

            val lampUpdate = LampUpdate(status = false)
            client.put("/v1/lamps/01ad9dac-6699-436d-9516-d473a6e62447") {
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(lampUpdate))
            }.apply {
                assertEquals(HttpStatusCode.NotFound, status)
                assertTrue(bodyAsText().contains("Lamp not found"))
            }
        }

    @Test
    fun testDeleteLamp() =
        testApplication {
            application {
                module()
            }

            // Create a lamp
            val lampCreate = LampCreate(status = true)
            val createResponse =
                client.post("/v1/lamps") {
                    contentType(ContentType.Application.Json)
                    setBody(json.encodeToString(lampCreate))
                }

            assertEquals(HttpStatusCode.Created, createResponse.status)
            val createdLampJson = createResponse.bodyAsText()

            // Use proper JSON parsing instead of string manipulation
            val createdLamp = json.decodeFromString<Lamp>(createdLampJson)

            // Delete the lamp
            val deleteResponse = client.delete("/v1/lamps/${createdLamp.id}")
            assertEquals(HttpStatusCode.NoContent, deleteResponse.status)

            // Verify lamp is deleted
            val getResponse = client.get("/v1/lamps/${createdLamp.id}")
            assertEquals(HttpStatusCode.NotFound, getResponse.status)
        }

    @Test
    fun testDeleteLampWithInvalidId() =
        testApplication {
            application {
                module()
            }

            client.delete("/v1/lamps/non-existent-id").apply {
                assertEquals(HttpStatusCode.BadRequest, status)
                assertTrue(bodyAsText().contains("Invalid lampId format"))
            }
        }

    @Test
    fun testDeleteNonExistentLamp() =
        testApplication {
            application {
                module()
            }

            client.delete("/v1/lamps/01ad9dac-6699-436d-9516-d473a6e62447").apply {
                assertEquals(HttpStatusCode.NotFound, status)
                assertTrue(bodyAsText().contains("Lamp not found"))
            }
        }
}
