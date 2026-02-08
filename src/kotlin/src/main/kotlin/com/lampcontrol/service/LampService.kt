package com.lampcontrol.service

import com.lampcontrol.api.models.*
import com.lampcontrol.domain.DomainException
import com.lampcontrol.entity.LampEntity
import com.lampcontrol.extensions.toUuidOrNull
import com.lampcontrol.mapper.LampMapper
import com.lampcontrol.repository.LampRepository
import java.util.UUID

/**
 * Service layer that handles business logic and coordinates between API and domain layers.
 * Uses mappers to maintain separation between API models and domain entities.
 */
class LampService(
    private val lampRepository: LampRepository,
    private val lampMapper: LampMapper,
) {
    /**
     * Get all lamps as API models
     */
    suspend fun getAllLamps(): List<Lamp> {
        return lampRepository.getAllLamps()
            .map { lampMapper.toApiModel(it) }
    }

    /**
     * Get a lamp by string ID and return as API model
     * @throws DomainException.InvalidId if the ID is not a valid UUID
     * @throws DomainException.NotFound if no lamp exists with the given ID
     */
    suspend fun getLampById(lampId: String): Lamp {
        val entity = findLampEntity(lampId)
        return lampMapper.toApiModel(entity)
    }

    /**
     * Create a new lamp from API model
     */
    suspend fun createLamp(lampCreate: LampCreate): Lamp {
        val domainEntity = lampMapper.toDomainEntityCreate(lampCreate)
        val savedEntity = lampRepository.createLamp(domainEntity)
        return lampMapper.toApiModel(savedEntity)
    }

    /**
     * Update a lamp by string ID with API update model
     * @throws DomainException.InvalidId if the ID is not a valid UUID
     * @throws DomainException.NotFound if no lamp exists with the given ID
     */
    suspend fun updateLamp(
        lampId: String,
        lampUpdate: LampUpdate,
    ): Lamp {
        val existingEntity = findLampEntity(lampId)
        val updatedEntity = lampMapper.updateDomainEntity(existingEntity, lampUpdate)
        val savedEntity = lampRepository.updateLamp(updatedEntity) ?: throw DomainException.NotFound(lampId)
        return lampMapper.toApiModel(savedEntity)
    }

    /**
     * Delete a lamp by string ID
     * @throws DomainException.InvalidId if the ID is not a valid UUID
     * @throws DomainException.NotFound if no lamp exists with the given ID
     */
    suspend fun deleteLamp(lampId: String) {
        val uuid = parseUuid(lampId)
        val deleted = lampRepository.deleteLamp(uuid)
        if (!deleted) throw DomainException.NotFound(lampId)
    }

    /**
     * Check if a lamp exists by string ID
     * @throws DomainException.InvalidId if the ID is not a valid UUID
     */
    suspend fun lampExists(lampId: String): Boolean {
        val uuid = parseUuid(lampId)
        return lampRepository.lampExists(uuid)
    }

    /**
     * Parses and validates a lamp ID string, then retrieves the corresponding entity.
     * @throws DomainException.InvalidId if the ID is not a valid UUID
     * @throws DomainException.NotFound if no lamp exists with the given ID
     */
    private suspend fun findLampEntity(lampId: String): LampEntity {
        val uuid = parseUuid(lampId)
        return lampRepository.getLampById(uuid) ?: throw DomainException.NotFound(lampId)
    }

    /**
     * Parses a string lamp ID into a UUID.
     * @throws IllegalArgumentException if the ID is blank
     * @throws DomainException.InvalidId if the ID is not a valid UUID format
     */
    private fun parseUuid(lampId: String): UUID {
        require(lampId.isNotBlank()) { "Lamp ID must not be blank" }
        return lampId.toUuidOrNull() ?: throw DomainException.InvalidId(lampId)
    }
}
