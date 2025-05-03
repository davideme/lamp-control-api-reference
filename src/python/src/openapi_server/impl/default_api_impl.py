"""Default API implementation for the Lamp Control API."""
from typing import Dict, List
from uuid import uuid4

from openapi_server.apis.default_api_base import BaseDefaultApi
from openapi_server.models.lamp import Lamp
from openapi_server.models.lamp_create import LampCreate
from openapi_server.models.lamp_update import LampUpdate


class DefaultApiImpl(BaseDefaultApi):
    """Implementation of the Default API with in-memory storage."""

    def __init__(self):
        """Initialize the API implementation with an empty lamp store."""
        self._lamps: Dict[str, Lamp] = {}

    async def create_lamp(self, lamp_create: LampCreate) -> Lamp:
        """Create a new lamp.

        Args:
            lamp_create: The lamp creation data.

        Returns:
            The created lamp.
        """
        lamp_id = str(uuid4())
        lamp = Lamp(id=lamp_id, status=lamp_create.status)
        self._lamps[lamp_id] = lamp
        return lamp

    async def delete_lamp(self, lamp_id: str) -> None:
        """Delete a lamp.

        Args:
            lamp_id: The ID of the lamp to delete.

        Raises:
            HTTPException: If the lamp is not found.
        """
        if lamp_id not in self._lamps:
            from fastapi import HTTPException
            raise HTTPException(status_code=404, detail="Lamp not found")
        del self._lamps[lamp_id]

    async def get_lamp(self, lamp_id: str) -> Lamp:
        """Get a specific lamp.

        Args:
            lamp_id: The ID of the lamp to get.

        Returns:
            The requested lamp.

        Raises:
            HTTPException: If the lamp is not found.
        """
        if lamp_id not in self._lamps:
            from fastapi import HTTPException
            raise HTTPException(status_code=404, detail="Lamp not found")
        return self._lamps[lamp_id]

    async def list_lamps(self) -> List[Lamp]:
        """List all lamps.

        Returns:
            A list of all lamps.
        """
        return list(self._lamps.values())

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
        if lamp_id not in self._lamps:
            from fastapi import HTTPException
            raise HTTPException(status_code=404, detail="Lamp not found")
        
        lamp = self._lamps[lamp_id]
        lamp.status = lamp_update.status
        return lamp 