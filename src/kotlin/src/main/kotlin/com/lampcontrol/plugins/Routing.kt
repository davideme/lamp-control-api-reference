package com.lampcontrol.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        
        // Health check endpoint
        get("/health") {
            call.respondText("OK")
        }
        
        // OpenAPI documentation will be available at /swagger-ui
        // Generated routes will be added here after code generation
    }
}
