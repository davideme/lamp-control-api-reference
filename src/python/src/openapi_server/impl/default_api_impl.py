"""Default API implementation for the Lamp Control API."""

from uuid import uuid4

from src.openapi_server.apis.default_api_base import BaseDefaultApi
from src.openapi_server.dependencies import get_lamp_repository
from src.openapi_server.models.lamp import Lamp
from src.openapi_server.models.lamp_create import LampCreate
from src.openapi_server.models.lamp_update import LampUpdate
from src.openapi_server.repositories.lamp_repository import LampNotFoundError


class DefaultApiImpl(BaseDefaultApi):  # type: ignore[no-untyped-call]
    """Implementation of the Default API with in-memory storage."""

    def __init__(self) -> None:
        """Initialize the API implementation with a lamp repository."""
        self._lamp_repository = get_lamp_repository()

    async def create_lamp(self, lamp_create: LampCreate) -> Lamp:
        """Create a new lamp.

        Args:
            lamp_create: The lamp creation data.

        Returns:
            The created lamp.
        """
        lamp_id = str(uuid4())
        lamp = Lamp(id=lamp_id, status=lamp_create.status)
        return self._lamp_repository.create(lamp)

    async def delete_lamp(self, lamp_id: str) -> None:
        """Delete a lamp.

        Args:
            lamp_id: The ID of the lamp to delete.

        Raises:
            HTTPException: If the lamp is not found.
        """
        try:
            self._lamp_repository.delete(lamp_id)
        except LampNotFoundError as err:
            from fastapi import HTTPException

            raise HTTPException(status_code=404, detail="Lamp not found") from err

    async def get_lamp(self, lamp_id: str) -> Lamp:
        """Get a specific lamp.

        Args:
            lamp_id: The ID of the lamp to get.

        Returns:
            The requested lamp.

        Raises:
            HTTPException: If the lamp is not found.
        """
        lamp = self._lamp_repository.get(lamp_id)
        if lamp is None:
            from fastapi import HTTPException

            raise HTTPException(status_code=404, detail="Lamp not found")
        return lamp

    async def list_lamps(self) -> list[Lamp]:
        """List all lamps.

        Returns:
            A list of all lamps.
        """
        return self._lamp_repository.list()

    async def update_lamp(self, lamp_id: str, lamp_update: LampUpdate) -> Lamp:
        """Update a lamp's status.

        Args:
            lamp_id: The ID of the lamp to update.
            lamp_update: The update data.

        Returns:
            The updated lamp.

        Raises:
            HTTPException: If the lamp is not found.
        """
        try:
            updated_lamp = Lamp(id=lamp_id, status=lamp_update.status)
            return self._lamp_repository.update(updated_lamp)
        except LampNotFoundError as err:
            from fastapi import HTTPException

            raise HTTPException(status_code=404, detail="Lamp not found") from err
