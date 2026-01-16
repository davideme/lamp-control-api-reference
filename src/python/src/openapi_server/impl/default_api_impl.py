"""Default API implementation for the Lamp Control API."""

from uuid import uuid4

from src.openapi_server.apis.default_api_base import BaseDefaultApi
from src.openapi_server.mappers.lamp_mapper import LampMapper
from src.openapi_server.models.lamp import Lamp
from src.openapi_server.models.lamp_create import LampCreate
from src.openapi_server.models.lamp_update import LampUpdate
from src.openapi_server.models.list_lamps200_response import ListLamps200Response
from src.openapi_server.repositories.lamp_repository import (
    InMemoryLampRepository,
    LampNotFoundError,
)
from src.openapi_server.repositories.postgres_lamp_repository import PostgresLampRepository


class DefaultApiImpl(BaseDefaultApi):  # type: ignore[no-untyped-call]
    """Implementation of the Default API with repository support.

    This implementation supports both in-memory and PostgreSQL storage
    based on configuration. The repository is injected via FastAPI's dependency
    injection system, ensuring proper session lifecycle management.
    """

    def __init__(self, repository: PostgresLampRepository | InMemoryLampRepository) -> None:
        """Initialize the API implementation with a repository.

        Args:
            repository: The lamp repository instance (injected by FastAPI).
        """
        self.repository = repository

    async def create_lamp(self, lamp_create: LampCreate) -> Lamp:
        """Create a new lamp.

        Args:
            lamp_create: The lamp creation data.

        Returns:
            The created lamp.

        Raises:
            HTTPException: If the lamp creation data is invalid.
        """
        if lamp_create is None:
            from fastapi import HTTPException

            raise HTTPException(status_code=400, detail="Invalid request data")

        lamp_id = str(uuid4())

        # Create domain entity from API model
        lamp_entity = LampMapper.create_from_api_model(lamp_create, lamp_id)
        created_entity = await self.repository.create(lamp_entity)

        # Convert domain entity back to API model
        return LampMapper.to_api_model(created_entity)

    async def delete_lamp(self, lamp_id: str) -> None:
        """Delete a lamp.

        Args:
            lamp_id: The ID of the lamp to delete.

        Raises:
            HTTPException: If the lamp is not found.
        """
        try:
            await self.repository.delete(lamp_id)
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
        lamp_entity = await self.repository.get(lamp_id)
        if lamp_entity is None:
            from fastapi import HTTPException

            raise HTTPException(status_code=404, detail="Lamp not found")

        # Convert domain entity to API model
        return LampMapper.to_api_model(lamp_entity)

    async def list_lamps(self, cursor: str | None, page_size: int | None) -> ListLamps200Response:
        """List lamps with pagination.

        Args:
            cursor: Cursor for pagination (currently ignored for simplicity).
            page_size: Maximum number of items to return (currently ignored for simplicity).

        Returns:
            A paginated response containing lamps.
        """
        # For simplicity, we'll return all lamps and ignore pagination for now
        all_lamp_entities = await self.repository.list()

        # Convert domain entities to API models
        all_lamps = [LampMapper.to_api_model(entity) for entity in all_lamp_entities]

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
            HTTPException: If the lamp is not found or update data is invalid.
        """
        if lamp_update is None:
            from fastapi import HTTPException

            raise HTTPException(status_code=400, detail="Invalid request data")

        try:
            # Get existing lamp entity
            existing_entity = await self.repository.get(lamp_id)
            if existing_entity is None:
                raise LampNotFoundError(lamp_id)

            # Update the domain entity using the mapper
            updated_entity = LampMapper.update_from_api_model(existing_entity, lamp_update)
            final_entity = await self.repository.update(updated_entity)

            # Convert domain entity back to API model
            return LampMapper.to_api_model(final_entity)
        except LampNotFoundError as err:
            from fastapi import HTTPException

            raise HTTPException(status_code=404, detail="Lamp not found") from err
