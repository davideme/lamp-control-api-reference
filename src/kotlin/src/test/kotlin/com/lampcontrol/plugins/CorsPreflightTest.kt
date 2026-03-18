package com.lampcontrol.plugins

import com.lampcontrol.module
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Test
import kotlin.test.*

class CorsPreflightTest {
    @Test
    fun `CORS preflight responds with 200 and headers`() =
        testApplication {
            application { module() }
            val res =
                client.options("/v1/lamps") {
                    header(HttpHeaders.Origin, "http://example.com")
                    header(HttpHeaders.AccessControlRequestMethod, "GET")
                }
            assertEquals(HttpStatusCode.OK, res.status)
            // At least verify some CORS headers are present
            assertNotNull(res.headers["Access-Control-Allow-Origin"])
        }
}
