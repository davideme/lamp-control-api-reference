package com.lampcontrol.service

import com.lampcontrol.api.models.Lamp
import com.lampcontrol.api.models.LampCreate
import com.lampcontrol.api.models.LampUpdate
import com.lampcontrol.entity.LampEntity
import com.lampcontrol.mapper.LampMapper
import com.lampcontrol.repository.LampRepository
import java.util.*

/**
 * Service layer that handles business logic and coordinates between API and domain layers.
 * Uses mappers to maintain separation between API models and domain entities.
 */
class LampService(
    private val lampRepository: LampRepository,
    private val lampMapper: LampMapper
) {
    
    /**
     * Get all lamps as API models
     */
    fun getAllLamps(): List<Lamp> {
        return lampRepository.getAllLamps()
            .map { lampMapper.toApiModel(it) }
    }
    
    /**
     * Get a lamp by string ID and return as API model
     */
    fun getLampById(lampId: String): Lamp? {
        val uuid = try {
            UUID.fromString(lampId)
        } catch (e: IllegalArgumentException) {
            return null
        }
        
        return lampRepository.getLampById(uuid)
            ?.let { lampMapper.toApiModel(it) }
    }
    
    /**
     * Create a new lamp from API model
     */
    fun createLamp(lampCreate: LampCreate): Lamp {
        val domainEntity = lampMapper.toDomainEntityCreate(lampCreate)
        val savedEntity = lampRepository.createLamp(domainEntity)
        return lampMapper.toApiModel(savedEntity)
    }
    
    /**
     * Update a lamp by string ID with API update model
     */
    fun updateLamp(lampId: String, lampUpdate: LampUpdate): Lamp? {
        val uuid = try {
            UUID.fromString(lampId)
        } catch (e: IllegalArgumentException) {
            return null
        }
        
        val existingEntity = lampRepository.getLampById(uuid) ?: return null
        val updatedEntity = lampMapper.updateDomainEntity(existingEntity, lampUpdate)
        val savedEntity = lampRepository.updateLamp(updatedEntity) ?: return null
        return lampMapper.toApiModel(savedEntity)
    }
    
    /**
     * Delete a lamp by string ID
     */
    fun deleteLamp(lampId: String): Boolean {
        val uuid = try {
            UUID.fromString(lampId)
        } catch (e: IllegalArgumentException) {
            return false
        }
        
        return lampRepository.deleteLamp(uuid)
    }
    
    /**
     * Check if a lamp exists by string ID
     */
    fun lampExists(lampId: String): Boolean {
        val uuid = try {
            UUID.fromString(lampId)
        } catch (e: IllegalArgumentException) {
            return false
        }
        
        return lampRepository.lampExists(uuid)
    }
}