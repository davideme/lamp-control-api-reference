package com.lampcontrol.plugins

import com.lampcontrol.api.apis.DefaultApi
import com.lampcontrol.di.ServiceContainer
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val lampService = ServiceContainer.lampService

    routing {
        get("/") {
            call.respondText("Lamp Control API - Kotlin Implementation")
        }

        // Health check endpoint
        get("/health") {
            call.respond(mapOf("status" to "ok"))
        }

        // API v1 routes
        route("/v1") {
            DefaultApi(lampService)
        }
    }
}
