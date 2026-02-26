"""PostgreSQL implementation of the lamp repository.

This module provides the PostgreSQL-backed implementation of the lamp repository
using SQLAlchemy 2.0 with async support. It includes:
- Field mapping between entity (status) and database (is_on)
- Soft delete support using the deleted_at column
- Automatic timestamp handling via database triggers
"""

from __future__ import annotations

from datetime import UTC, datetime
from uuid import UUID

from sqlalchemy import insert, select, update
from sqlalchemy.ext.asyncio import AsyncSession

from src.openapi_server.entities.lamp_entity import LampEntity
from src.openapi_server.infrastructure.database.models import LampModel
from src.openapi_server.repositories.lamp_repository import LampNotFoundError


class PostgresLampRepository:
    """Repository for managing lamp data with PostgreSQL storage.

    This implementation uses SQLAlchemy async sessions to interact with
    PostgreSQL. It handles:
    - Field mapping: entity.status <-> database.is_on
    - Soft delete: sets deleted_at instead of removing records
    - Timestamp management: database trigger updates updated_at
    """

    async def create(self, db: AsyncSession, lamp_entity: LampEntity) -> LampEntity:
        """Create a new lamp in the database.

        Args:
            lamp_entity: The lamp entity to create.

        Returns:
            The created lamp entity with database-generated values.
        """
        stmt = (
            insert(LampModel)
            .values(
                id=UUID(lamp_entity.id),
                is_on=lamp_entity.status,  # Map status -> is_on
                created_at=lamp_entity.created_at,
                updated_at=lamp_entity.updated_at,
                deleted_at=None,
            )
            .returning(LampModel)
        )

        result = await db.execute(stmt)
        db_lamp = result.scalars().one()

        return self._to_entity(db_lamp)

    async def get(self, db: AsyncSession, lamp_id: str) -> LampEntity | None:
        """Get a lamp by ID, excluding soft-deleted lamps.

        Args:
            lamp_id: The ID of the lamp to retrieve.

        Returns:
            The lamp entity if found and not deleted, None otherwise.
        """
        stmt = select(LampModel).where(
            LampModel.id == UUID(lamp_id), LampModel.deleted_at.is_(None)
        )

        result = await db.execute(stmt)
        db_lamp = result.scalar_one_or_none()

        return self._to_entity(db_lamp) if db_lamp else None

    async def list(self, db: AsyncSession) -> list[LampEntity]:
        """List all active lamps (excluding soft-deleted ones).

        Returns:
            A list of all active lamp entities, ordered by created_at then id.
        """
        stmt = self._base_list_query()

        result = await db.execute(stmt)
        db_lamps = result.scalars().all()

        return [self._to_entity(lamp) for lamp in db_lamps]

    async def list_paginated(self, db: AsyncSession, offset: int, limit: int) -> list[LampEntity]:
        """List active lamps with bounded pagination.

        Ordering is deterministic: created_at ascending, then id ascending.

        Args:
            offset: Number of records to skip from the ordered active set.
            limit: Maximum number of records to return.

        Returns:
            A bounded list of active lamp entities.
        """
        safe_offset = max(0, offset)
        if limit <= 0:
            return []

        stmt = self._base_list_query().offset(safe_offset).limit(limit)
        result = await db.execute(stmt)
        db_lamps = result.scalars().all()

        return [self._to_entity(lamp) for lamp in db_lamps]

    async def update(self, db: AsyncSession, lamp_entity: LampEntity) -> LampEntity:
        """Update a lamp's status.

        The updated_at field will be automatically updated by the database
        trigger defined in the schema.

        Args:
            lamp_entity: The lamp entity with updated values.

        Returns:
            The updated lamp entity with trigger-updated timestamps.

        Raises:
            LampNotFoundError: If the lamp is not found or is soft-deleted.
        """
        stmt = (
            update(LampModel)
            .where(LampModel.id == UUID(lamp_entity.id), LampModel.deleted_at.is_(None))
            .values(is_on=lamp_entity.status)  # Map status -> is_on; updated_at set by trigger
            .returning(LampModel)
        )

        result = await db.execute(stmt)
        db_lamp = result.scalars().one_or_none()

        if db_lamp is None:
            raise LampNotFoundError(lamp_entity.id)

        return self._to_entity(db_lamp)

    async def delete(self, db: AsyncSession, lamp_id: str) -> None:
        """Soft delete a lamp by setting the deleted_at timestamp.

        Args:
            lamp_id: The ID of the lamp to delete.

        Raises:
            LampNotFoundError: If the lamp is not found or already deleted.
        """
        stmt = (
            update(LampModel)
            .where(LampModel.id == UUID(lamp_id), LampModel.deleted_at.is_(None))
            .values(deleted_at=datetime.now(UTC))
            .returning(LampModel.id)
        )

        result = await db.execute(stmt)
        deleted_id = result.scalar_one_or_none()

        if deleted_id is None:
            raise LampNotFoundError(lamp_id)

    @staticmethod
    def _base_list_query():
        """Build the base query used by list operations."""
        return (
            select(LampModel)
            .where(LampModel.deleted_at.is_(None))
            .order_by(LampModel.created_at, LampModel.id)
        )

    @staticmethod
    def _to_entity(model: LampModel) -> LampEntity:
        """Convert a database model to a domain entity.

        This method handles the field mapping:
        - model.is_on -> entity.status

        Args:
            model: The database model to convert.

        Returns:
            The corresponding domain entity.
        """
        return LampEntity(
            id=str(model.id),
            status=model.is_on,  # Map is_on -> status
            created_at=model.created_at,
            updated_at=model.updated_at,
        )
