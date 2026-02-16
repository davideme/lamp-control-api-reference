"""Unit tests for the DefaultApiImpl class."""

from datetime import datetime, timedelta
from unittest.mock import AsyncMock

import pytest
from fastapi import HTTPException

from src.openapi_server.entities.lamp_entity import LampEntity
from src.openapi_server.impl.default_api_impl import DefaultApiImpl
from src.openapi_server.models.lamp import Lamp
from src.openapi_server.models.lamp_create import LampCreate
from src.openapi_server.models.lamp_update import LampUpdate
from src.openapi_server.repositories.lamp_repository import LampNotFoundError


@pytest.fixture
def mock_lamp_repository():
    """Fixture that provides a mocked LampRepository."""
    mock = AsyncMock()
    mock.get.return_value = None
    mock.list_paginated.return_value = []
    return mock


@pytest.fixture
def api_impl(mock_lamp_repository):
    """Fixture that provides a DefaultApiImpl instance with mocked repository."""
    return DefaultApiImpl(repository=mock_lamp_repository)


@pytest.fixture
def sample_lamp():
    """Fixture that provides a sample lamp API model for testing."""
    return Lamp(id="test-lamp-1", status=True, created_at=datetime.now(), updated_at=datetime.now())


@pytest.fixture
def sample_lamp_entity():
    """Fixture that provides a sample lamp domain entity for testing."""
    return LampEntity(id="test-lamp-1", status=True)


class TestDefaultApiImpl:
    """Test suite for DefaultApiImpl class."""

    @pytest.mark.asyncio
    async def test_create_lamp(self, api_impl, mock_lamp_repository, sample_lamp_entity):
        """Test creating a new lamp."""
        # Arrange
        lamp_create = LampCreate(status=True)
        # Mock repository to return a domain entity
        mock_lamp_repository.create.return_value = sample_lamp_entity

        # Act
        result = await api_impl.create_lamp(lamp_create)

        # Assert
        assert result.status == sample_lamp_entity.status
        assert result.id == sample_lamp_entity.id
        assert result.created_at is not None
        assert result.updated_at is not None
        # Verify repository was called with a domain entity
        mock_lamp_repository.create.assert_called_once()

    @pytest.mark.asyncio
    async def test_create_lamp_null_request(self, api_impl, mock_lamp_repository):
        """Test creating a lamp with null request data."""
        # Act & Assert
        with pytest.raises(HTTPException) as exc_info:
            await api_impl.create_lamp(None)
        assert exc_info.value.status_code == 400
        assert exc_info.value.detail == "Invalid request data"

    @pytest.mark.asyncio
    async def test_get_lamp_success(self, api_impl, mock_lamp_repository, sample_lamp_entity):
        """Test getting an existing lamp."""
        # Arrange
        mock_lamp_repository.get.return_value = sample_lamp_entity

        # Act
        result = await api_impl.get_lamp(sample_lamp_entity.id)

        # Assert
        assert result.id == sample_lamp_entity.id
        assert result.status == sample_lamp_entity.status
        assert result.created_at is not None
        assert result.updated_at is not None
        mock_lamp_repository.get.assert_called_once_with(sample_lamp_entity.id)

    @pytest.mark.asyncio
    async def test_get_lamp_not_found(self, api_impl, mock_lamp_repository):
        """Test getting a non-existent lamp."""
        # Arrange
        mock_lamp_repository.get.return_value = None

        # Act & Assert
        with pytest.raises(HTTPException) as exc_info:
            await api_impl.get_lamp("nonexistent-id")
        assert exc_info.value.status_code == 404
        assert exc_info.value.detail == "Lamp not found"
        mock_lamp_repository.get.assert_called_once_with("nonexistent-id")

    @pytest.mark.asyncio
    async def test_list_lamps_respects_page_size_and_cursor(
        self, api_impl, mock_lamp_repository, sample_lamp_entity
    ):
        """Test listing lamps with bounded pagination."""
        # Arrange
        second_entity = LampEntity(
            id="test-lamp-2",
            status=False,
            created_at=sample_lamp_entity.created_at + timedelta(seconds=1),
            updated_at=sample_lamp_entity.updated_at + timedelta(seconds=1),
        )
        extra_entity = LampEntity(
            id="test-lamp-3",
            status=True,
            created_at=sample_lamp_entity.created_at + timedelta(seconds=2),
            updated_at=sample_lamp_entity.updated_at + timedelta(seconds=2),
        )
        mock_lamp_repository.list_paginated.return_value = [
            sample_lamp_entity,
            second_entity,
            extra_entity,
        ]

        # Act
        result = await api_impl.list_lamps(cursor="10", page_size=2)

        # Assert
        assert len(result.data) == 2
        assert result.data[0].id == sample_lamp_entity.id
        assert result.data[1].id == second_entity.id
        assert result.has_more is True
        assert result.next_cursor == "12"
        mock_lamp_repository.list_paginated.assert_called_once_with(offset=10, limit=3)

    @pytest.mark.asyncio
    async def test_list_lamps_terminal_page(
        self, api_impl, mock_lamp_repository, sample_lamp_entity
    ):
        """Test listing lamps on terminal page."""
        # Arrange
        mock_lamp_repository.list_paginated.return_value = [sample_lamp_entity]

        # Act
        result = await api_impl.list_lamps(cursor="5", page_size=2)

        # Assert
        assert len(result.data) == 1
        assert result.data[0].id == sample_lamp_entity.id
        assert result.has_more is False
        assert result.next_cursor is None
        mock_lamp_repository.list_paginated.assert_called_once_with(offset=5, limit=3)

    @pytest.mark.asyncio
    async def test_list_lamps_invalid_cursor_defaults_to_zero(
        self, api_impl, mock_lamp_repository, sample_lamp_entity
    ):
        """Test invalid cursor fallback behavior."""
        # Arrange
        mock_lamp_repository.list_paginated.return_value = [sample_lamp_entity]

        # Act
        result = await api_impl.list_lamps(cursor="bad-cursor", page_size=1)

        # Assert
        assert len(result.data) == 1
        assert result.data[0].id == sample_lamp_entity.id
        mock_lamp_repository.list_paginated.assert_called_once_with(offset=0, limit=2)

    @pytest.mark.asyncio
    async def test_list_lamps_negative_cursor_defaults_to_zero(
        self, api_impl, mock_lamp_repository, sample_lamp_entity
    ):
        """Test negative cursor fallback behavior."""
        # Arrange
        mock_lamp_repository.list_paginated.return_value = []

        # Act
        result = await api_impl.list_lamps(cursor="-7", page_size=None)

        # Assert
        assert result.data == []
        assert result.has_more is False
        assert result.next_cursor is None
        mock_lamp_repository.list_paginated.assert_called_once_with(offset=0, limit=26)

    @pytest.mark.asyncio
    async def test_update_lamp_success(self, api_impl, mock_lamp_repository, sample_lamp_entity):
        """Test updating an existing lamp."""
        # Arrange
        lamp_update = LampUpdate(status=True)
        updated_entity = LampEntity(id=sample_lamp_entity.id, status=True)
        mock_lamp_repository.get.return_value = sample_lamp_entity
        mock_lamp_repository.update.return_value = updated_entity

        # Act
        result = await api_impl.update_lamp(sample_lamp_entity.id, lamp_update)

        # Assert
        assert result.id == updated_entity.id
        assert result.status == updated_entity.status
        mock_lamp_repository.get.assert_called_once_with(sample_lamp_entity.id)
        mock_lamp_repository.update.assert_called_once()
        # Check that the entity passed to update has the correct status
        updated_lamp_arg = mock_lamp_repository.update.call_args[0][0]
        assert updated_lamp_arg.status == lamp_update.status

    @pytest.mark.asyncio
    async def test_update_lamp_null_request(self, api_impl, mock_lamp_repository):
        """Test updating a lamp with null request data."""
        # Act & Assert
        with pytest.raises(HTTPException) as exc_info:
            await api_impl.update_lamp("test-lamp-1", None)
        assert exc_info.value.status_code == 400
        assert exc_info.value.detail == "Invalid request data"

    @pytest.mark.asyncio
    async def test_update_lamp_not_found(self, api_impl, mock_lamp_repository):
        """Test updating a non-existent lamp."""
        # Arrange
        lamp_update = LampUpdate(status=True)
        mock_lamp_repository.get.return_value = None

        # Act & Assert
        with pytest.raises(HTTPException) as exc_info:
            await api_impl.update_lamp("nonexistent-id", lamp_update)
        assert exc_info.value.status_code == 404
        assert exc_info.value.detail == "Lamp not found"
        mock_lamp_repository.get.assert_called_once_with("nonexistent-id")

    @pytest.mark.asyncio
    async def test_delete_lamp_success(self, api_impl, mock_lamp_repository, sample_lamp_entity):
        """Test deleting an existing lamp."""
        # Arrange

        # Act
        await api_impl.delete_lamp(sample_lamp_entity.id)

        # Assert
        mock_lamp_repository.delete.assert_called_once_with(sample_lamp_entity.id)

    @pytest.mark.asyncio
    async def test_delete_lamp_not_found(self, api_impl, mock_lamp_repository):
        """Test deleting a non-existent lamp."""
        # Arrange
        mock_lamp_repository.delete.side_effect = LampNotFoundError("nonexistent-id")

        # Act & Assert
        with pytest.raises(HTTPException) as exc_info:
            await api_impl.delete_lamp("nonexistent-id")
        assert exc_info.value.status_code == 404
        assert exc_info.value.detail == "Lamp not found"
        mock_lamp_repository.delete.assert_called_once_with("nonexistent-id")
