package com.lampcontrol.repository

import com.lampcontrol.api.models.Lamp
import com.lampcontrol.api.models.LampCreate
import com.lampcontrol.api.models.LampUpdate

/**
 * Repository contract for managing Lamps.
 * This abstracts the storage so implementations can be in-memory or backed by a database.
 */
interface LampRepository {
    fun getAllLamps(): List<Lamp>
    fun getLampById(id: String): Lamp?
    fun createLamp(lampCreate: LampCreate): Lamp
    fun updateLamp(id: String, lampUpdate: LampUpdate): Lamp?
    fun deleteLamp(id: String): Boolean
    fun lampExists(id: String): Boolean
}
