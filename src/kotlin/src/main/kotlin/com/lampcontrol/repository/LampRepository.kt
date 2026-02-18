package com.lampcontrol.repository

import com.lampcontrol.entity.LampEntity
import java.util.UUID

/**
 * Repository contract for managing Lamps.
 * This abstracts the storage so implementations can be in-memory or backed by a database.
 * Works with domain entities to maintain separation from API models.
 */
interface LampRepository {
    suspend fun getAllLamps(): List<LampEntity>

    suspend fun getLampsPage(
        offset: Int,
        limit: Int,
    ): List<LampEntity>

    suspend fun getLampById(id: UUID): LampEntity?

    suspend fun createLamp(entity: LampEntity): LampEntity

    suspend fun updateLamp(entity: LampEntity): LampEntity?

    suspend fun deleteLamp(id: UUID): Boolean

    suspend fun lampExists(id: UUID): Boolean
}
