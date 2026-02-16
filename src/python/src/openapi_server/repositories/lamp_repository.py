from __future__ import annotations

"""Repository for managing lamp data with in-memory storage.

This module provides the in-memory implementation of the lamp repository.
The repository interface is async to maintain compatibility with the
PostgreSQL repository implementation.
"""

from src.openapi_server.entities.lamp_entity import LampEntity


class LampNotFoundError(Exception):
    """Exception raised when a lamp is not found in the repository."""

    def __init__(self, lamp_id: str):
        """Initialize the exception with the lamp ID.

        Args:
            lamp_id: The ID of the lamp that was not found.
        """
        self.lamp_id = lamp_id
        super().__init__(f"Lamp with ID {lamp_id} not found")


class InMemoryLampRepository:
    """Repository for managing lamp data with in-memory storage.

    This implementation uses an in-memory dictionary for storage and is
    suitable for development and testing. All methods are async to maintain
    compatibility with the PostgreSQL repository implementation, even though
    the operations are synchronous.
    """

    def __init__(self) -> None:
        """Initialize the repository with an empty lamp store."""
        self._lamps: dict[str, LampEntity] = {}

    async def create(self, lamp_entity: LampEntity) -> LampEntity:
        """Create a new lamp.

        Args:
            lamp_entity: The lamp entity to create.

        Returns:
            The created lamp entity.
        """
        self._lamps[lamp_entity.id] = lamp_entity
        return lamp_entity

    async def get(self, lamp_id: str) -> LampEntity | None:
        """Get a specific lamp.

        Args:
            lamp_id: The ID of the lamp to get.

        Returns:
            The requested lamp entity, or None if not found.
        """
        return self._lamps.get(lamp_id)

    async def list(self) -> list[LampEntity]:
        """List all lamps.

        Returns:
            A list of all lamp entities ordered by created_at then id.
        """
        return self._sorted_lamps()

    async def list_paginated(self, offset: int, limit: int) -> list[LampEntity]:
        """List lamps using a bounded window for cursor pagination.

        Ordering is deterministic and mirrors the PostgreSQL repository:
        created_at ascending, then id ascending.

        Args:
            offset: Number of records to skip from the start of the ordered set.
            limit: Maximum number of records to return.

        Returns:
            A bounded list of lamp entities for the requested window.
        """
        safe_offset = max(0, offset)
        if limit <= 0:
            return []
        return self._sorted_lamps()[safe_offset : safe_offset + limit]

    async def update(self, lamp_entity: LampEntity) -> LampEntity:
        """Update a lamp.

        Args:
            lamp_entity: The lamp entity to update.

        Returns:
            The updated lamp entity.

        Raises:
            LampNotFoundError: If the lamp is not found.
        """
        if lamp_entity.id not in self._lamps:
            raise LampNotFoundError(lamp_entity.id)
        self._lamps[lamp_entity.id] = lamp_entity
        return lamp_entity

    async def delete(self, lamp_id: str) -> None:
        """Delete a lamp.

        Args:
            lamp_id: The ID of the lamp to delete.

        Raises:
            LampNotFoundError: If the lamp is not found.
        """
        if lamp_id not in self._lamps:
            raise LampNotFoundError(lamp_id)
        del self._lamps[lamp_id]

    def _sorted_lamps(self) -> list[LampEntity]:
        """Return lamps in deterministic pagination order."""
        return sorted(self._lamps.values(), key=lambda lamp: (lamp.created_at, lamp.id))
