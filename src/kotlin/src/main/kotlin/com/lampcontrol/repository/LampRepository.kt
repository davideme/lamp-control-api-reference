package com.lampcontrol.repository

import com.lampcontrol.entity.LampEntity
import java.util.*

/**
 * Repository contract for managing Lamps.
 * This abstracts the storage so implementations can be in-memory or backed by a database.
 * Works with domain entities to maintain separation from API models.
 */
interface LampRepository {
    fun getAllLamps(): List<LampEntity>
    fun getLampById(id: UUID): LampEntity?
    fun createLamp(entity: LampEntity): LampEntity
    fun updateLamp(entity: LampEntity): LampEntity?
    fun deleteLamp(id: UUID): Boolean
    fun lampExists(id: UUID): Boolean
}
