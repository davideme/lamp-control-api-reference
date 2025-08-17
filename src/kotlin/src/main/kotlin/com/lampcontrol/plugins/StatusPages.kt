package com.lampcontrol.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
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
