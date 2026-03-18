package com.lampcontrol.database

import org.junit.jupiter.api.Test
import kotlin.test.*

class LampsTableTest {
    @Test
    fun `LampsTable has correct table name`() {
        assertEquals("lamps", LampsTable.tableName)
    }

    @Test
    fun `LampsTable has id column`() {
        assertNotNull(LampsTable.id)
        assertEquals("id", LampsTable.id.name)
    }

    @Test
    fun `LampsTable has isOn column`() {
        assertNotNull(LampsTable.isOn)
        assertEquals("is_on", LampsTable.isOn.name)
    }

    @Test
    fun `LampsTable has createdAt column`() {
        assertNotNull(LampsTable.createdAt)
        assertEquals("created_at", LampsTable.createdAt.name)
    }

    @Test
    fun `LampsTable has updatedAt column`() {
        assertNotNull(LampsTable.updatedAt)
        assertEquals("updated_at", LampsTable.updatedAt.name)
    }

    @Test
    fun `LampsTable has deletedAt column`() {
        assertNotNull(LampsTable.deletedAt)
        assertEquals("deleted_at", LampsTable.deletedAt.name)
    }

    @Test
    fun `LampsTable has primary key`() {
        assertNotNull(LampsTable.primaryKey)
        assertEquals(1, LampsTable.primaryKey!!.columns.size)
        assertEquals(LampsTable.id, LampsTable.primaryKey!!.columns.first())
    }

    @Test
    fun `LampsTable columns collection is not empty`() {
        val columns = LampsTable.columns
        assertNotNull(columns)
        assert(columns.isNotEmpty())
        assert(columns.size >= 5) // At least id, isOn, createdAt, updatedAt, deletedAt
    }
}
