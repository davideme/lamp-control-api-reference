package com.lampcontrol.di

import com.lampcontrol.mapper.LampMapper
import com.lampcontrol.repository.LampRepository
import com.lampcontrol.service.InMemoryLampRepository
import com.lampcontrol.service.LampService

/**
 * Simple dependency injection container
 */
object ServiceContainer {
    private val lampRepository: LampRepository by lazy { InMemoryLampRepository() }
    private val lampMapper: LampMapper by lazy { LampMapper() }
    val lampService: LampService by lazy { LampService(lampRepository, lampMapper) }
}
