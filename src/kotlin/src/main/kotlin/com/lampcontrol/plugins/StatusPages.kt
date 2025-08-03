package com.lampcontrol.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.SerializationException

fun Application.configureStatusPages() {
    install(StatusPages) {
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
