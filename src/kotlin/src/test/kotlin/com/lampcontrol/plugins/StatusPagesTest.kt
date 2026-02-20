@file:Suppress("TooGenericExceptionThrown")

package com.lampcontrol.plugins

import com.lampcontrol.domain.DomainException
import com.lampcontrol.module
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import io.ktor.server.testing.testApplication
import kotlinx.serialization.SerializationException
import org.junit.jupiter.api.Test
import kotlin.test.*

class StatusPagesTest {
    @Test
    fun `unhandled exception returns 500`() =
        testApplication {
            application {
                module()
                routing {
                    get("/boom") { throw RuntimeException("boom") }
                }
            }
            val res = client.get("/boom")
            assertEquals(HttpStatusCode.InternalServerError, res.status)
            assertTrue(res.bodyAsText().contains("Internal server error"))
        }

    @Test
    fun `serialization exception returns 400`() =
        testApplication {
            application {
                module()
                routing {
                    get("/serr") { throw SerializationException("bad json") }
                }
            }
            val res = client.get("/serr")
            assertEquals(HttpStatusCode.BadRequest, res.status)
        }

    @Test
    fun `illegal argument returns 400`() =
        testApplication {
            application {
                module()
                routing {
                    get("/ierr") { throw IllegalArgumentException("nope") }
                }
            }
            val res = client.get("/ierr")
            assertEquals(HttpStatusCode.BadRequest, res.status)
        }

    @Test
    fun `number format exception returns 400`() =
        testApplication {
            application {
                module()
                routing {
                    get("/nfe") {
                        // simulate parsing a malformed numeric query param
                        val v = call.request.queryParameters["pageSize"] ?: "null"
                        val parsed = v.toInt() // will throw NumberFormatException for non-numeric
                        call.respondText("ok $parsed")
                    }
                }
            }

            val res = client.get("/nfe?pageSize=null")
            assertEquals(HttpStatusCode.BadRequest, res.status)
        }

    @Test
    fun `bad request wrapping number format returns 400`() =
        testApplication {
            application {
                module()
                routing {
                    get("/breq") {
                        // Simulate Ktor wrapping a NumberFormatException inside BadRequestException
                        throw io.ktor.server.plugins.BadRequestException(
                            "Can't transform call to resource",
                            NumberFormatException("For input string: \"null\""),
                        )
                    }
                }
            }

            val res = client.get("/breq")
            assertEquals(HttpStatusCode.BadRequest, res.status)
        }

    @Test
    fun `domain NotFound exception returns 404`() =
        testApplication {
            application {
                module()
                routing {
                    get("/not-found") { throw DomainException.NotFound("test-id") }
                }
            }

            val res = client.get("/not-found")
            assertEquals(HttpStatusCode.NotFound, res.status)
            assertTrue(res.bodyAsText().contains("Lamp not found"))
        }

    @Test
    fun `domain InvalidId exception returns 400`() =
        testApplication {
            application {
                module()
                routing {
                    get("/invalid-id") { throw DomainException.InvalidId("bad-id") }
                }
            }

            val res = client.get("/invalid-id")
            assertEquals(HttpStatusCode.BadRequest, res.status)
            assertTrue(res.bodyAsText().contains("Invalid lampId format"))
        }
}
