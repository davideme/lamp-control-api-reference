package com.lampcontrol.service

import com.lampcontrol.api.models.Lamp
import com.lampcontrol.api.models.LampCreate
import com.lampcontrol.api.models.LampUpdate
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Service for managing lamp operations
 */
class LampService {
    // In-memory storage for lamps - using string keys for simplicity
    private val lamps = ConcurrentHashMap<String, Lamp>()
    
    /**
     * Get all lamps
     */
    fun getAllLamps(): List<Lamp> {
        return lamps.values.toList()
    }
    
    /**
     * Get a lamp by ID
     */
    fun getLampById(id: String): Lamp? {
        return lamps[id]
    }
    
    /**
     * Create a new lamp
     */
    fun createLamp(lampCreate: LampCreate): Lamp {
        val uuid = UUID.randomUUID()
        val lamp = Lamp(
            id = uuid,
            status = lampCreate.status
        )
        // Store using string representation of UUID for consistent lookup
        lamps[uuid.toString()] = lamp
        return lamp
    }
    
    /**
     * Update an existing lamp
     */
    fun updateLamp(id: String, lampUpdate: LampUpdate): Lamp? {
        val existingLamp = lamps[id] ?: return null
        val updatedLamp = existingLamp.copy(status = lampUpdate.status)
        lamps[id] = updatedLamp
        return updatedLamp
    }
    
    /**
     * Delete a lamp by ID
     */
    fun deleteLamp(id: String): Boolean {
        return lamps.remove(id) != null
    }
    
    /**
     * Check if a lamp exists
     */
    fun lampExists(id: String): Boolean {
        return lamps.containsKey(id)
    }
}
