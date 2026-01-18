package com.lampcontrol.mapper

import com.lampcontrol.api.models.Lamp
import com.lampcontrol.api.models.LampCreate
import com.lampcontrol.api.models.LampUpdate
import com.lampcontrol.entity.LampEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class LampMapperTest {
    private val mapper = LampMapper()

    @Test
    fun `should convert domain entity to API model`() {
        // Given
        val uuid = UUID.randomUUID()
        val now = Instant.now()
        val entity =
            LampEntity(
                id = uuid,
                status = true,
                createdAt = now,
                updatedAt = now,
            )

        // When
        val apiModel = mapper.toApiModel(entity)

        // Then
        assertEquals(uuid, apiModel.id)
        assertEquals(true, apiModel.status)
        assertEquals(now.toString(), apiModel.createdAt)
        assertEquals(now.toString(), apiModel.updatedAt)
    }

    @Test
    fun `should convert API model to domain entity`() {
        // Given
        val uuid = UUID.randomUUID()
        val timestamp = "2023-01-01T00:00:00Z"
        val apiModel =
            Lamp(
                id = uuid,
                status = false,
                createdAt = timestamp,
                updatedAt = timestamp,
            )

        // When
        val entity = mapper.toDomainEntity(apiModel)

        // Then
        assertEquals(uuid, entity.id)
        assertEquals(false, entity.status)
        assertEquals(Instant.parse(timestamp), entity.createdAt)
        assertEquals(Instant.parse(timestamp), entity.updatedAt)
    }

    @Test
    fun `should create domain entity from API create model`() {
        // Given
        val lampCreate = LampCreate(status = true)

        // When
        val entity = mapper.toDomainEntityCreate(lampCreate)

        // Then
        assertNotNull(entity.id)
        assertEquals(true, entity.status)
        assertNotNull(entity.createdAt)
        assertNotNull(entity.updatedAt)
    }

    @Test
    fun `should update domain entity from API update model`() {
        // Given
        val originalEntity = LampEntity.create(false)
        val lampUpdate = LampUpdate(status = true)

        // When
        val updatedEntity = mapper.updateDomainEntity(originalEntity, lampUpdate)

        // Then
        assertEquals(originalEntity.id, updatedEntity.id)
        assertEquals(true, updatedEntity.status)
        assertEquals(originalEntity.createdAt, updatedEntity.createdAt)
        assertNotEquals(originalEntity.updatedAt, updatedEntity.updatedAt)
    }
}
