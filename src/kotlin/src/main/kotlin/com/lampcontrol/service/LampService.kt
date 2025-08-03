package com.lampcontrol.service

import com.lampcontrol.api.models.Lamp
import com.lampcontrol.api.models.LampCreate
import com.lampcontrol.api.models.LampUpdate
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Service class for managing lamp operations
 */
class LampService {
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
        val id = UUID.randomUUID()
        val lamp = Lamp(
            id = id,
            status = lampCreate.status
        )
        lamps[id.toString()] = lamp
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
     * Delete a lamp
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
