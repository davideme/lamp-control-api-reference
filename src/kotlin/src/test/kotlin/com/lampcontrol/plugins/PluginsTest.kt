package com.lampcontrol.plugins

import com.lampcontrol.module
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PluginsTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

    @Test
    fun `test application plugins configuration`() =
        testApplication {
            application {
                module()
            }

            // Test call logging and monitoring by making requests
            val response1 = client.get("/health")
            assertEquals(HttpStatusCode.OK, response1.status)
            val healthResponse = json.decodeFromString<Map<String, String>>(response1.bodyAsText())
            assertEquals("ok", healthResponse["status"])

            // Test CORS headers
            val response2 =
                client.get("/") {
                    header("Origin", "http://localhost:3000")
                }
            assertEquals(HttpStatusCode.OK, response2.status)

            // Test call ID handling
            val response3 =
                client.get("/health") {
                    header("X-Request-Id", "test-123")
                }
            assertEquals(HttpStatusCode.OK, response3.status)
            val healthResponse3 = json.decodeFromString<Map<String, String>>(response3.bodyAsText())
            assertEquals("ok", healthResponse3["status"])
        }

    @Test
    fun `test error handling with status pages`() =
        testApplication {
            application {
                module()
            }

            // Test 404 for non-existent endpoint
            val response = client.get("/non-existent-endpoint")
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `test content negotiation`() =
        testApplication {
            application {
                module()
            }

            // Test JSON content type handling
            val response =
                client.post("/v1/lamps") {
                    contentType(ContentType.Application.Json)
                    setBody("""{"status": true}""")
                }
            assertEquals(HttpStatusCode.Created, response.status)
        }
}
