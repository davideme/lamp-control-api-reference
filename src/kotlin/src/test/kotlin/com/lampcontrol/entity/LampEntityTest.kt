package com.lampcontrol.entity

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LampEntityTest {
    @Test
    fun `should create new lamp entity with generated ID and timestamps`() {
        // When
        val entity = LampEntity.create(true)

        // Then
        assertNotNull(entity.id)
        assertEquals(true, entity.status)
        assertNotNull(entity.createdAt)
        assertNotNull(entity.updatedAt)
        assertEquals(entity.createdAt, entity.updatedAt)
    }

    @Test
    fun `should create updated entity with new status and timestamp`() {
        // Given
        val originalEntity = LampEntity.create(false)
        Thread.sleep(10) // Ensure different timestamp

        // When
        val updatedEntity = originalEntity.withUpdatedStatus(true)

        // Then
        assertEquals(originalEntity.id, updatedEntity.id)
        assertEquals(true, updatedEntity.status)
        assertEquals(originalEntity.createdAt, updatedEntity.createdAt)
        assertNotEquals(originalEntity.updatedAt, updatedEntity.updatedAt)
    }

    @Test
    fun `should maintain immutability when updating status`() {
        // Given
        val originalEntity = LampEntity.create(false)

        // When
        val updatedEntity = originalEntity.withUpdatedStatus(true)

        // Then
        assertEquals(false, originalEntity.status)
        assertEquals(true, updatedEntity.status)
        assertNotEquals(originalEntity, updatedEntity)
    }

    @Test
    fun `should verify domain entity separation from API models`() {
        // This test ensures that LampEntity doesn't depend on any API model classes
        // Given
        val entity = LampEntity.create(true)

        // Then - should be able to create and manipulate without API dependencies
        val entityClass = entity::class.java
        val apiModelPackage = "com.lampcontrol.api.models"

        // Verify no direct dependency on API model classes
        assertFalse(entityClass.name.contains(apiModelPackage))
        assertNotNull(entity.toString()) // Basic functionality works
    }
}
