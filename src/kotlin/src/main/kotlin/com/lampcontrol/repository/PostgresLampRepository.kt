package com.lampcontrol.repository

import com.lampcontrol.database.LampsTable
import com.lampcontrol.entity.LampEntity
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant
import java.util.UUID

/**
 * PostgreSQL implementation of LampRepository using Exposed ORM.
 * Implements soft deletes using the deleted_at column.
 */
class PostgresLampRepository : LampRepository {
    /**
     * Execute a database transaction with proper suspend support
     */
    private suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction { block() }

    override suspend fun getAllLamps(): List<LampEntity> =
        dbQuery {
            LampsTable
                .selectAll()
                .where { LampsTable.deletedAt.isNull() }
                .map { rowToEntity(it) }
        }

    override suspend fun getLampsPage(
        offset: Int,
        limit: Int,
    ): List<LampEntity> =
        dbQuery {
            LampsTable
                .selectAll()
                .where { LampsTable.deletedAt.isNull() }
                .orderBy(LampsTable.createdAt to SortOrder.ASC, LampsTable.id to SortOrder.ASC)
                .limit(limit)
                .offset(offset.toLong())
                .map { rowToEntity(it) }
        }

    override suspend fun getLampById(id: UUID): LampEntity? =
        dbQuery {
            LampsTable
                .selectAll()
                .where { (LampsTable.id eq id) and LampsTable.deletedAt.isNull() }
                .map { rowToEntity(it) }
                .singleOrNull()
        }

    override suspend fun createLamp(entity: LampEntity): LampEntity =
        dbQuery {
            LampsTable.insertReturning {
                it[id] = entity.id
                it[isOn] = entity.status
                it[deletedAt] = null
                // created_at and updated_at set by DB DEFAULT CURRENT_TIMESTAMP
            }.single().let { rowToEntity(it) }
        }

    override suspend fun updateLamp(
        id: UUID,
        status: Boolean,
    ): LampEntity? =
        dbQuery {
            LampsTable.updateReturning(
                where = { (LampsTable.id eq id) and LampsTable.deletedAt.isNull() },
            ) {
                it[isOn] = status
                // updated_at set by DB BEFORE UPDATE trigger
            }.singleOrNull()?.let { rowToEntity(it) }
        }

    override suspend fun deleteLamp(id: UUID): Boolean =
        dbQuery {
            LampsTable.updateReturning(
                returning = listOf(LampsTable.id),
                where = { (LampsTable.id eq id) and LampsTable.deletedAt.isNull() },
            ) {
                it[deletedAt] = Instant.now()
            }.any()
        }

    override suspend fun lampExists(id: UUID): Boolean =
        dbQuery {
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
            updatedAt = row[LampsTable.updatedAt],
        )
    }
}
