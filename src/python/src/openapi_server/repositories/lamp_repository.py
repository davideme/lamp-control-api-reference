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
            A list of all lamp entities.
        """
        return list(self._lamps.values())

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
