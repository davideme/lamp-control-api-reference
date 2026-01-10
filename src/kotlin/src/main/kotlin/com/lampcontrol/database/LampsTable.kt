package com.lampcontrol.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

/**
 * Exposed table definition for the lamps table.
 * Maps to the schema defined in database/sql/postgresql/schema.sql
 */
object LampsTable : Table("lamps") {
    val id = uuid("id")
    val isOn = bool("is_on")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    val deletedAt = timestamp("deleted_at").nullable()

    override val primaryKey = PrimaryKey(id)
}
