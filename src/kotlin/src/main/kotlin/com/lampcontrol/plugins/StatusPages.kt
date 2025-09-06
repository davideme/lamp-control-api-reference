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
        // Use respondText with explicit JSON and Content-Type so a body is always sent even
        // when serialization/content-negotiation isn't available at the time the handler runs.
        fun jsonError(error: String, message: String?): String {
            val safeMessage = message?.replace("\"", "\\\"") ?: ""
            return "{\"error\":\"$error\",\"message\":\"$safeMessage\"}"
        }

        exception<NumberFormatException> { call, cause ->
            call.respondText(
                jsonError("Invalid numeric parameter", cause.message),
                ContentType.Application.Json,
                HttpStatusCode.BadRequest
            )
        }
        exception<SerializationException> { call, cause ->
            call.respondText(
                jsonError("Invalid JSON format", cause.message),
                ContentType.Application.Json,
                HttpStatusCode.BadRequest
            )
        }

        // Ktor may wrap parameter binding failures in BadRequestException (e.g. when
        // converting a query parameter to an Int). If the cause is a NumberFormatException
        // surface it as a 400 Bad Request with a helpful message.
        exception<BadRequestException> { call, cause ->
            val nf = cause.cause
            if (nf is NumberFormatException) {
                call.respondText(
                    jsonError("Invalid numeric parameter", nf.message),
                    ContentType.Application.Json,
                    HttpStatusCode.BadRequest
                )
            } else {
                // Fallback to the generic handler below by rethrowing so it is caught by the
                // broader Exception mapping, which logs and returns a 500.
                throw cause
            }
        }
        
        exception<IllegalArgumentException> { call, cause ->
            call.respondText(
                jsonError("Invalid argument", cause.message),
                ContentType.Application.Json,
                HttpStatusCode.BadRequest
            )
        }

        exception<Exception> { call, cause ->
            call.respondText(
                jsonError("Internal server error", "An unexpected error occurred"),
                ContentType.Application.Json,
                HttpStatusCode.InternalServerError
            )
        }
    }
}
