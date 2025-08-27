"""Default API implementation for the Lamp Control API."""

from datetime import datetime
from uuid import uuid4

from src.openapi_server.apis.default_api_base import BaseDefaultApi
from src.openapi_server.dependencies import get_lamp_repository
from src.openapi_server.models.lamp import Lamp
from src.openapi_server.models.lamp_create import LampCreate
from src.openapi_server.models.lamp_update import LampUpdate
from src.openapi_server.models.list_lamps200_response import ListLamps200Response
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
        now = datetime.now()
        lamp = Lamp(id=lamp_id, status=lamp_create.status, created_at=now, updated_at=now)
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

    async def list_lamps(self, cursor: str | None, page_size: int | None) -> ListLamps200Response:
        """List lamps with pagination.

        Args:
            cursor: Cursor for pagination (currently ignored for simplicity).
            page_size: Maximum number of items to return (currently ignored for simplicity).

        Returns:
            A paginated response containing lamps.
        """
        # For simplicity, we'll return all lamps and ignore pagination for now
        all_lamps = self._lamp_repository.list()
        return ListLamps200Response(
            data=all_lamps,
            next_cursor=None,  # No next page in this simple implementation
            has_more=False,  # No more items available
        )

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
            # Get existing lamp to preserve created_at timestamp
            existing_lamp = self._lamp_repository.get(lamp_id)
            if existing_lamp is None:
                raise LampNotFoundError(lamp_id)

            # Create updated lamp with new updated_at timestamp
            updated_lamp = Lamp(
                id=lamp_id,
                status=lamp_update.status,
                created_at=existing_lamp.created_at,
                updated_at=datetime.now(),
            )
            return self._lamp_repository.update(updated_lamp)
        except LampNotFoundError as err:
            from fastapi import HTTPException

            raise HTTPException(status_code=404, detail="Lamp not found") from err
