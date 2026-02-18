package com.lampcontrol.plugins

import io.ktor.server.application.*
import io.ktor.server.resources.Resources

fun Application.configureResources() {
    install(Resources)
}
