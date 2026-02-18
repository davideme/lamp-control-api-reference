package com.lampcontrol.di

import com.lampcontrol.mapper.LampMapper
import com.lampcontrol.repository.*
import com.lampcontrol.service.LampService

/**
 * Simple dependency injection container.
 * Automatically selects PostgreSQL or in-memory storage based on environment configuration.
 */
object ServiceContainer {
    private val lampRepository: LampRepository by lazy { LampRepositoryFactory.create() }
    private val lampMapper: LampMapper by lazy { LampMapper() }
    val lampService: LampService by lazy { LampService(lampRepository, lampMapper) }
}
