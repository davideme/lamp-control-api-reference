package com.lampcontrol.di

import com.lampcontrol.repository.LampRepository
import com.lampcontrol.service.InMemoryLampRepository

/**
 * Simple dependency injection container
 */
object ServiceContainer {
    val lampService: LampRepository by lazy { InMemoryLampRepository() }
}
