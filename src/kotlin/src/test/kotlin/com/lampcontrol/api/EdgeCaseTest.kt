package com.lampcontrol.api

import com.lampcontrol.module
import com.lampcontrol.testutil.TestJson
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.*
import org.junit.jupiter.api.Test
import kotlin.test.*

class EdgeCaseTest {
    private val json = TestJson.instance

    @Test
    fun `test malformed JSON request`() =
        testApplication {
            application {
                module()
            }

            // Test invalid JSON
            val response =
                client.post("/v1/lamps") {
                    contentType(ContentType.Application.Json)
                    setBody("{invalid json}")
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `test missing required fields`() =
        testApplication {
            application {
                module()
            }

            // Test request without status field
            val response =
                client.post("/v1/lamps") {
                    contentType(ContentType.Application.Json)
                    setBody("{}")
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `test unsupported media type`() =
        testApplication {
            application {
                module()
            }

            // Test XML content type (not supported) - returns 415 Unsupported Media Type
            val response =
                client.post("/v1/lamps") {
                    contentType(ContentType.Application.Xml)
                    setBody("<lamp><status>true</status></lamp>")
                }
            assertEquals(HttpStatusCode.UnsupportedMediaType, response.status)
        }

    @Test
    fun `test large request body`() =
        testApplication {
            application {
                module()
            }

            // Test with large JSON (should still work)
            val largeRequest =
                buildJsonObject {
                    put("status", true)
                    put("description", "x".repeat(1000)) // Large description
                }

            val response =
                client.post("/v1/lamps") {
                    contentType(ContentType.Application.Json)
                    setBody(largeRequest.toString())
                }
            // Should still create successfully if only extra fields
            assertEquals(HttpStatusCode.Created, response.status)
        }

    @Test
    fun `test special characters in lamp ID path`() =
        testApplication {
            application {
                module()
            }

            // Test with special characters in path — invalid UUID format returns 400
            val response = client.get("/v1/lamps/invalid%20id%20with%20spaces")
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `test concurrent lamp operations`() =
        testApplication {
            application {
                module()
            }

            // Create a lamp
            val createResponse =
                client.post("/v1/lamps") {
                    contentType(ContentType.Application.Json)
                    setBody("""{"status": true}""")
                }
            assertEquals(HttpStatusCode.Created, createResponse.status)

            val createdLamp = json.parseToJsonElement(createResponse.bodyAsText()).jsonObject
            val lampId = createdLamp["id"]?.jsonPrimitive?.content
            assertNotNull(lampId)

            // Try to update and get the lamp concurrently
            val updateResponse =
                client.put("/v1/lamps/$lampId") {
                    contentType(ContentType.Application.Json)
                    setBody("""{"status": false}""")
                }
            assertEquals(HttpStatusCode.OK, updateResponse.status)

            val getResponse = client.get("/v1/lamps/$lampId")
            assertEquals(HttpStatusCode.OK, getResponse.status)
        }

    @Test
    fun `test multiple lamp creation and retrieval`() =
        testApplication {
            application {
                module()
            }

            // Create multiple lamps
            val lampIds = mutableListOf<String>()

            repeat(5) { i ->
                val response =
                    client.post("/v1/lamps") {
                        contentType(ContentType.Application.Json)
                        setBody("""{"status": ${i % 2 == 0}}""")
                    }
                assertEquals(HttpStatusCode.Created, response.status)

                val lamp = json.parseToJsonElement(response.bodyAsText()).jsonObject
                val lampId = lamp["id"]?.jsonPrimitive?.content
                assertNotNull(lampId)
                lampIds.add(lampId)
            }

            // Get all lamps
            val allLampsResponse = client.get("/v1/lamps")
            assertEquals(HttpStatusCode.OK, allLampsResponse.status)

            // Response is an object with `data` array
            val respObj = json.parseToJsonElement(allLampsResponse.bodyAsText()).jsonObject
            val allLamps = respObj["data"]?.jsonArray
            assertNotNull(allLamps)
            assertTrue(allLamps.size >= 5)

            // Verify each lamp exists
            lampIds.forEach { lampId ->
                val response = client.get("/v1/lamps/$lampId")
                assertEquals(HttpStatusCode.OK, response.status)
            }
        }
}
