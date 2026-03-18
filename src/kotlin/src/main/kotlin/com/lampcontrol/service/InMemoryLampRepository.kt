package com.lampcontrol.service

import com.lampcontrol.entity.LampEntity
import com.lampcontrol.repository.LampRepository
import java.time.Instant
import java.util.UUID
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
    override suspend fun getAllLamps(): List<LampEntity> {
        return lamps.values.toList()
    }

    override suspend fun getLampsPage(
        offset: Int,
        limit: Int,
    ): List<LampEntity> {
        val ordered =
            lamps.values.sortedWith(
                compareBy<LampEntity>({ it.createdAt }, { it.id }),
            )
        val start = offset.coerceAtLeast(0)
        if (start >= ordered.size) return emptyList()
        val end = (start + limit).coerceAtMost(ordered.size)
        return ordered.subList(start, end)
    }

    /**
     * Get a lamp by ID
     */
    override suspend fun getLampById(id: UUID): LampEntity? {
        return lamps[id]
    }

    /**
     * Create a new lamp
     */
    override suspend fun createLamp(entity: LampEntity): LampEntity {
        lamps[entity.id] = entity
        return entity
    }

    /**
     * Update an existing lamp, preserving createdAt from the stored entity
     */
    override suspend fun updateLamp(
        id: UUID,
        status: Boolean,
    ): LampEntity? {
        val existing = lamps[id] ?: return null
        val updated = existing.copy(status = status, updatedAt = Instant.now())
        lamps[id] = updated
        return updated
    }

    /**
     * Delete a lamp by ID
     */
    override suspend fun deleteLamp(id: UUID): Boolean {
        return lamps.remove(id) != null
    }

    /**
     * Check if a lamp exists
     */
    override suspend fun lampExists(id: UUID): Boolean {
        return lamps.containsKey(id)
    }
}
