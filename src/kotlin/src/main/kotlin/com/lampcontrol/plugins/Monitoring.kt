package com.lampcontrol.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.request.path
import org.slf4j.event.Level

fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }
    install(CallId) {
        header(HttpHeaders.X_REQUEST_ID)
        verify { callId: String ->
            callId.isNotEmpty()
        }
    }
}

private object HttpHeaders {
    const val X_REQUEST_ID = "X-Request-ID"
}
