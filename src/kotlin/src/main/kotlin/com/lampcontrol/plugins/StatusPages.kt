package com.lampcontrol.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import kotlinx.serialization.SerializationException

fun Application.configureStatusPages() {
    install(StatusPages) {
        // Map numeric parse errors to 400 so malformed numeric query params (e.g. pageSize=null)
        // don't surface as 500 Internal Server Error. This lets the client know the input
        // was invalid while preserving `pageSize: kotlin.Int?` in generated `Paths`.
        exception<NumberFormatException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Invalid numeric parameter", "message" to cause.message)
            )
        }
        exception<SerializationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Invalid JSON format", "message" to cause.message)
            )
        }

        // Ktor may wrap parameter binding failures in BadRequestException (e.g. when
        // converting a query parameter to an Int). If the cause is a NumberFormatException
        // surface it as a 400 Bad Request with a helpful message.
        exception<BadRequestException> { call, cause ->
            val nf = cause.cause
            if (nf is NumberFormatException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Invalid numeric parameter", "message" to nf.message)
                )
            } else {
                // Fallback to the generic handler below by rethrowing so it is caught by the
                // broader Exception mapping, which logs and returns a 500.
                throw cause
            }
        }
        
        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Invalid argument", "message" to cause.message)
            )
        }
        
        exception<Exception> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to "Internal server error", "message" to "An unexpected error occurred")
            )
        }
    }
}
