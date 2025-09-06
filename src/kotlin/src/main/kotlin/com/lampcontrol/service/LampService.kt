package com.lampcontrol.service

import com.lampcontrol.api.models.Lamp
import com.lampcontrol.api.models.LampCreate
import com.lampcontrol.api.models.LampUpdate
import com.lampcontrol.repository.LampRepository
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.time.Instant

/**
 * In-memory repository implementation for managing lamp operations.
 */
class InMemoryLampRepository : LampRepository {
    // In-memory storage for lamps - using string keys for simplicity
    private val lamps = ConcurrentHashMap<String, Lamp>()
    
    /**
     * Get all lamps
     */
    override fun getAllLamps(): List<Lamp> {
        return lamps.values.toList()
    }
    
    /**
     * Get a lamp by ID
     */
    override fun getLampById(id: String): Lamp? {
        return lamps[id]
    }
    
    /**
     * Create a new lamp
     */
    override fun createLamp(lampCreate: LampCreate): Lamp {
        val uuid = UUID.randomUUID()
        val now = Instant.now().toString()
        val lamp = Lamp(
            id = uuid,
            status = lampCreate.status,
            createdAt = now,
            updatedAt = now
        )
        // Store using string representation of UUID for consistent lookup
        lamps[uuid.toString()] = lamp
        return lamp
    }
    
    /**
     * Update an existing lamp
     */
    override fun updateLamp(id: String, lampUpdate: LampUpdate): Lamp? {
        val existingLamp = lamps[id] ?: return null
    val now = Instant.now().toString()
    val updatedLamp = existingLamp.copy(status = lampUpdate.status, updatedAt = now)
        lamps[id] = updatedLamp
        return updatedLamp
    }
    
    /**
     * Delete a lamp by ID
     */
    override fun deleteLamp(id: String): Boolean {
        return lamps.remove(id) != null
    }
    
    /**
     * Check if a lamp exists
     */
    override fun lampExists(id: String): Boolean {
        return lamps.containsKey(id)
    }
}
