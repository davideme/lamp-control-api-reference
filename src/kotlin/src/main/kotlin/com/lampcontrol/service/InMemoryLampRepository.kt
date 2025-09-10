package com.lampcontrol.service

import com.lampcontrol.entity.LampEntity
import com.lampcontrol.repository.LampRepository
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory repository implementation for managing lamp operations.
 * Uses domain entities to maintain separation from API models.
 */
class InMemoryLampRepository : LampRepository {
    // In-memory storage for lamps - using UUID keys
    private val lamps = ConcurrentHashMap<UUID, LampEntity>()
    
    /**
     * Get all lamps
     */
    override fun getAllLamps(): List<LampEntity> {
        return lamps.values.toList()
    }
    
    /**
     * Get a lamp by ID
     */
    override fun getLampById(id: UUID): LampEntity? {
        return lamps[id]
    }
    
    /**
     * Create a new lamp
     */
    override fun createLamp(entity: LampEntity): LampEntity {
        lamps[entity.id] = entity
        return entity
    }
    
    /**
     * Update an existing lamp
     */
    override fun updateLamp(entity: LampEntity): LampEntity? {
        val existingLamp = lamps[entity.id] ?: return null
        lamps[entity.id] = entity
        return entity
    }
    
    /**
     * Delete a lamp by ID
     */
    override fun deleteLamp(id: UUID): Boolean {
        return lamps.remove(id) != null
    }
    
    /**
     * Check if a lamp exists
     */
    override fun lampExists(id: UUID): Boolean {
        return lamps.containsKey(id)
    }
}
