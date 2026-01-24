package com.lampcontrol.plugins

import com.lampcontrol.api.apis.DefaultApi
import com.lampcontrol.di.ServiceContainer
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

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
