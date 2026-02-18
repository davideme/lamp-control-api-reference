package com.lampcontrol.api

import com.lampcontrol.api.infrastructure.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.install
import io.ktor.server.auth.*
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import io.ktor.server.testing.testApplication
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

class ApiKeyAuthTest {
    @Test
    fun `header-based API key auth works`() =
        testApplication {
            application {
                install(Authentication) {
                    apiKeyAuth("apiKeyHeader") {
                        apiKeyName = "X-API-KEY"
                        apiKeyLocation = ApiKeyLocation.HEADER
                        validate { cred: ApiKeyCredential ->
                            if (cred.value == "topsecret") ApiPrincipal(cred) else null
                        }
                    }
                }
                routing {
                    authenticate("apiKeyHeader") {
                        get("/secure") { call.respondText("secure") }
                    }
                }
            }

            // No credentials -> 401
            val noCreds = client.get("/secure")
            assertEquals(HttpStatusCode.Unauthorized, noCreds.status)

            // Invalid credentials -> 401
            val badCreds =
                client.get("/secure") {
                    header("X-API-KEY", "wrong")
                }
            assertEquals(HttpStatusCode.Unauthorized, badCreds.status)

            // Valid credentials -> 200
            val ok =
                client.get("/secure") {
                    header("X-API-KEY", "topsecret")
                }
            assertEquals(HttpStatusCode.OK, ok.status)
            assertEquals("secure", ok.bodyAsText())
        }

    @Test
    fun `query-based API key auth works`() =
        testApplication {
            application {
                install(Authentication) {
                    apiKeyAuth("apiKeyQuery") {
                        apiKeyName = "api_key"
                        apiKeyLocation = ApiKeyLocation.QUERY
                        validate { cred: ApiKeyCredential ->
                            if (cred.value == "letmein") ApiPrincipal(cred) else null
                        }
                    }
                }
                routing {
                    authenticate("apiKeyQuery") {
                        get("/secure-q") { call.respondText("secure-q") }
                    }
                }
            }

            // No api_key -> 401
            val noKey = client.get("/secure-q")
            assertEquals(HttpStatusCode.Unauthorized, noKey.status)

            // Wrong api_key -> 401
            val wrong = client.get("/secure-q?api_key=bad")
            assertEquals(HttpStatusCode.Unauthorized, wrong.status)

            // Correct api_key -> 200
            val ok = client.get("/secure-q?api_key=letmein")
            assertEquals(HttpStatusCode.OK, ok.status)
            assertEquals("secure-q", ok.bodyAsText())
        }
}
