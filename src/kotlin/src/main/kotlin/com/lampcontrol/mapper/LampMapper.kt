package com.lampcontrol.mapper

import com.lampcontrol.api.models.Lamp
import com.lampcontrol.api.models.LampCreate
import com.lampcontrol.api.models.LampUpdate
import com.lampcontrol.entity.LampEntity
import java.time.Instant

/**
 * Mapper to convert between domain entities and API models.
 * This separation allows the internal domain model to evolve independently
 * from the external API contract.
 */
class LampMapper {
    /**
     * Convert from domain entity to API model
     */
    fun toApiModel(entity: LampEntity): Lamp {
        return Lamp(
            id = entity.id,
            status = entity.status,
            createdAt = entity.createdAt.toString(),
            updatedAt = entity.updatedAt.toString(),
        )
    }

    /**
     * Convert from API model to domain entity
     */
    fun toDomainEntity(apiModel: Lamp): LampEntity {
        return LampEntity(
            id = apiModel.id,
            status = apiModel.status,
            createdAt = parseInstant(apiModel.createdAt),
            updatedAt = parseInstant(apiModel.updatedAt),
        )
    }

    /**
     * Convert from API create model to domain entity
     */
    fun toDomainEntityCreate(apiModel: LampCreate): LampEntity {
        return LampEntity.create(apiModel.status)
    }

    /**
     * Update domain entity from API update model
     */
    fun updateDomainEntity(
        entity: LampEntity,
        updateModel: LampUpdate,
    ): LampEntity {
        return entity.withUpdatedStatus(updateModel.status)
    }

    /**
     * Parse ISO 8601 timestamp string to Instant
     */
    private fun parseInstant(timestampStr: String): Instant {
        return Instant.parse(timestampStr)
    }
}
