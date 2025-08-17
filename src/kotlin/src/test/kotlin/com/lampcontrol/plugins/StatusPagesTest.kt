package com.lampcontrol.plugins

import com.lampcontrol.module
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.SerializationException
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StatusPagesTest {

    @Test
    fun `unhandled exception returns 500`() = testApplication {
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
    fun `serialization exception returns 400`() = testApplication {
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
    fun `illegal argument returns 400`() = testApplication {
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
    fun `number format exception returns 400`() = testApplication {
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
    fun `bad request wrapping number format returns 400`() = testApplication {
        application {
            module()
            routing {
                get("/breq") {
                    // Simulate Ktor wrapping a NumberFormatException inside BadRequestException
                    throw io.ktor.server.plugins.BadRequestException("Can't transform call to resource", NumberFormatException("For input string: \"null\""))
                }
            }
        }

        val res = client.get("/breq")
        assertEquals(HttpStatusCode.BadRequest, res.status)
    }
}
