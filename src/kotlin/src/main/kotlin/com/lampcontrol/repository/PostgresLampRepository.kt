package com.lampcontrol.repository

import com.lampcontrol.database.LampsTable
import com.lampcontrol.entity.LampEntity
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant
import java.util.*

/**
 * PostgreSQL implementation of LampRepository using Exposed ORM.
 * Implements soft deletes using the deleted_at column.
 */
class PostgresLampRepository : LampRepository {

    /**
     * Execute a database transaction with proper suspend support
     */
    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction { block() }

    override suspend fun getAllLamps(): List<LampEntity> = dbQuery {
        LampsTable
            .selectAll()
            .where { LampsTable.deletedAt.isNull() }
            .map { rowToEntity(it) }
    }

    override suspend fun getLampById(id: UUID): LampEntity? = dbQuery {
        LampsTable
            .selectAll()
            .where { (LampsTable.id eq id) and LampsTable.deletedAt.isNull() }
            .map { rowToEntity(it) }
            .singleOrNull()
    }

    override suspend fun createLamp(entity: LampEntity): LampEntity = dbQuery {
        LampsTable.insert {
            it[id] = entity.id
            it[isOn] = entity.status
            it[createdAt] = entity.createdAt
            it[updatedAt] = entity.updatedAt
            it[deletedAt] = null
        }
        entity
    }

    override suspend fun updateLamp(entity: LampEntity): LampEntity? = dbQuery {
        val updatedAt = Instant.now()
        val rowsUpdated = LampsTable.update(
            where = { (LampsTable.id eq entity.id) and LampsTable.deletedAt.isNull() }
        ) {
            it[isOn] = entity.status
            it[updatedAt] = updatedAt
        }

        if (rowsUpdated > 0) {
            // Return the updated entity with new updatedAt timestamp without extra DB round-trip
            LampEntity(
                id = entity.id,
                status = entity.status,
                createdAt = entity.createdAt,
                updatedAt = updatedAt
            )
        } else {
            null
        }
    }

    override suspend fun deleteLamp(id: UUID): Boolean = dbQuery {
        val rowsUpdated = LampsTable.update(
            where = { (LampsTable.id eq id) and LampsTable.deletedAt.isNull() }
        ) {
            it[deletedAt] = Instant.now()
        }
        rowsUpdated > 0
    }

    override suspend fun lampExists(id: UUID): Boolean = dbQuery {
        LampsTable
            .selectAll()
            .where { (LampsTable.id eq id) and LampsTable.deletedAt.isNull() }
            .count() > 0
    }

    /**
     * Convert a database row to a LampEntity
     */
    private fun rowToEntity(row: ResultRow): LampEntity {
        return LampEntity(
            id = row[LampsTable.id],
            status = row[LampsTable.isOn],
            createdAt = row[LampsTable.createdAt],
            updatedAt = row[LampsTable.updatedAt]
        )
    }
}
