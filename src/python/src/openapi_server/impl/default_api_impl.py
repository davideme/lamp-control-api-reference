"""Default API implementation for the Lamp Control API."""

from uuid import uuid4

from fastapi import HTTPException
from sqlalchemy.ext.asyncio import AsyncSession

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

    def __init__(
        self,
        repository: PostgresLampRepository | InMemoryLampRepository,
        db_session: AsyncSession | None = None,
    ) -> None:
        """Initialize the API implementation with a repository.

        Args:
            repository: The lamp repository instance (injected by FastAPI).
            db_session: Current request DB session for PostgreSQL mode.
        """
        self.repository = repository
        self.db_session = db_session

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
            raise HTTPException(status_code=400, detail="Invalid request data")

        lamp_id = str(uuid4())

        # Create domain entity from API model
        lamp_entity = LampMapper.create_from_api_model(lamp_create, lamp_id)
        if isinstance(self.repository, PostgresLampRepository):
            created_entity = await self.repository.create(self._require_db_session(), lamp_entity)
        else:
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
            if isinstance(self.repository, PostgresLampRepository):
                await self.repository.delete(self._require_db_session(), lamp_id)
            else:
                await self.repository.delete(lamp_id)
        except LampNotFoundError as err:
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
        if isinstance(self.repository, PostgresLampRepository):
            lamp_entity = await self.repository.get(self._require_db_session(), lamp_id)
        else:
            lamp_entity = await self.repository.get(lamp_id)
        if lamp_entity is None:
            raise HTTPException(status_code=404, detail="Lamp not found")

        # Convert domain entity to API model
        return LampMapper.to_api_model(lamp_entity)

    async def list_lamps(self, cursor: str | None, page_size: int | None) -> ListLamps200Response:
        """List lamps with pagination.

        Args:
            cursor: Cursor for pagination represented as a stringified offset.
            page_size: Maximum number of items to return.

        Returns:
            A paginated response containing lamps.
        """
        start_offset = self._parse_cursor(cursor)
        safe_page_size = self._normalize_page_size(page_size)

        if isinstance(self.repository, PostgresLampRepository):
            entities = await self.repository.list_paginated(
                self._require_db_session(),
                offset=start_offset,
                limit=safe_page_size + 1,
            )
        else:
            entities = await self.repository.list_paginated(
                offset=start_offset,
                limit=safe_page_size + 1,
            )
        has_more = len(entities) > safe_page_size
        page_entities = entities[:safe_page_size]
        next_cursor = str(start_offset + safe_page_size) if has_more else None

        # Convert domain entities to API models
        lamps = [LampMapper.to_api_model(entity) for entity in page_entities]

        return ListLamps200Response(
            data=lamps,
            next_cursor=next_cursor,
            has_more=has_more,
        )

    @staticmethod
    def _parse_cursor(cursor: str | None) -> int:
        """Parse a cursor value into a non-negative offset."""
        if cursor is None:
            return 0

        try:
            return max(0, int(cursor))
        except TypeError, ValueError:
            return 0

    @staticmethod
    def _normalize_page_size(page_size: int | None) -> int:
        """Normalize page size with a defensive default."""
        if page_size is None or page_size <= 0:
            return 25
        return page_size

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
            raise HTTPException(status_code=400, detail="Invalid request data")

        try:
            # Get existing lamp entity
            if isinstance(self.repository, PostgresLampRepository):
                existing_entity = await self.repository.get(self._require_db_session(), lamp_id)
            else:
                existing_entity = await self.repository.get(lamp_id)
            if existing_entity is None:
                raise LampNotFoundError(lamp_id)

            # Update the domain entity using the mapper
            updated_entity = LampMapper.update_from_api_model(existing_entity, lamp_update)
            if isinstance(self.repository, PostgresLampRepository):
                final_entity = await self.repository.update(
                    self._require_db_session(), updated_entity
                )
            else:
                final_entity = await self.repository.update(updated_entity)

            # Convert domain entity back to API model
            return LampMapper.to_api_model(final_entity)
        except LampNotFoundError as err:
            raise HTTPException(status_code=404, detail="Lamp not found") from err

    def _require_db_session(self) -> AsyncSession:
        """Return current DB session or fail fast for PostgreSQL operations."""
        if self.db_session is None:
            raise RuntimeError("Database session is required for PostgreSQL repository")
        return self.db_session
