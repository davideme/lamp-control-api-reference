package com.lampcontrol.di

import com.lampcontrol.service.LampService

/**
 * Simple dependency injection container
 */
object ServiceContainer {
    val lampService: LampService by lazy { LampService() }
}
