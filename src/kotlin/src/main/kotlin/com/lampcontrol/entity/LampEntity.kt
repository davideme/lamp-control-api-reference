package com.lampcontrol.entity

import java.time.Instant
import java.util.*

/**
 * Domain entity representing a Lamp in the system.
 * This entity is independent of the API model and represents the core business object.
 */
data class LampEntity(
    val id: UUID,
    val status: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        /**
         * Create a new LampEntity with a generated ID and current timestamps
         */
        fun create(status: Boolean): LampEntity {
            val now = Instant.now()
            return LampEntity(
                id = UUID.randomUUID(),
                status = status,
                createdAt = now,
                updatedAt = now
            )
        }
    }
    
    /**
     * Create an updated copy of this entity with a new status and updated timestamp
     */
    fun withUpdatedStatus(newStatus: Boolean): LampEntity {
        return copy(
            status = newStatus,
            updatedAt = Instant.now()
        )
    }
}