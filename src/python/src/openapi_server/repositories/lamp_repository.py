"""Repository for managing lamp data."""

from openapi_server.models.lamp import Lamp


class LampNotFoundError(Exception):
    """Exception raised when a lamp is not found in the repository."""

    def __init__(self, lamp_id: str):
        """Initialize the exception with the lamp ID.

        Args:
            lamp_id: The ID of the lamp that was not found.
        """
        self.lamp_id = lamp_id
        super().__init__(f"Lamp with ID {lamp_id} not found")


class LampRepository:
    """Repository for managing lamp data with in-memory storage."""

    def __init__(self) -> None:
        """Initialize the repository with an empty lamp store."""
        self._lamps: dict[str, Lamp] = {}

    def create(self, lamp: Lamp) -> Lamp:
        """Create a new lamp.

        Args:
            lamp: The lamp to create.

        Returns:
            The created lamp.
        """
        self._lamps[lamp.id] = lamp
        return lamp

    def get(self, lamp_id: str) -> Lamp | None:
        """Get a specific lamp.

        Args:
            lamp_id: The ID of the lamp to get.

        Returns:
            The requested lamp, or None if not found.
        """
        return self._lamps.get(lamp_id)

    def list(self) -> list[Lamp]:
        """List all lamps.

        Returns:
            A list of all lamps.
        """
        return list(self._lamps.values())

    def update(self, lamp: Lamp) -> Lamp:
        """Update a lamp.

        Args:
            lamp: The lamp to update.

        Returns:
            The updated lamp.

        Raises:
            LampNotFoundError: If the lamp is not found.
        """
        if lamp.id not in self._lamps:
            raise LampNotFoundError(lamp.id)
        self._lamps[lamp.id] = lamp
        return lamp

    def delete(self, lamp_id: str) -> None:
        """Delete a lamp.

        Args:
            lamp_id: The ID of the lamp to delete.

        Raises:
            LampNotFoundError: If the lamp is not found.
        """
        if lamp_id not in self._lamps:
            raise LampNotFoundError(lamp_id)
        del self._lamps[lamp_id]
