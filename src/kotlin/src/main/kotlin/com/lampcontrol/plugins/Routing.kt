package com.lampcontrol.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.lampcontrol.api.apis.DefaultApi
import com.lampcontrol.service.InMemoryLampRepository

fun Application.configureRouting() {
    val lampService = InMemoryLampRepository()
    
    routing {
        get("/") {
            call.respondText("Lamp Control API - Kotlin Implementation")
        }
        
        // Health check endpoint
        get("/health") {
            call.respondText("OK")
        }
        
        // API v1 routes
        route("/v1") {
            DefaultApi(lampService)
        }
    }
}
