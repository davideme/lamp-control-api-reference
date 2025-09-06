"""Unit tests for the DefaultApiImpl class."""

from unittest.mock import Mock, patch

import pytest
from fastapi import HTTPException

from src.openapi_server.impl.default_api_impl import DefaultApiImpl
from src.openapi_server.models.lamp import Lamp
from src.openapi_server.models.lamp_create import LampCreate
from src.openapi_server.models.lamp_update import LampUpdate
from src.openapi_server.repositories.lamp_repository import LampNotFoundError


@pytest.fixture
def mock_lamp_repository():
    """Fixture that provides a mocked LampRepository."""
    return Mock()


@pytest.fixture
def api_impl(mock_lamp_repository):
    """Fixture that provides a DefaultApiImpl instance with mocked repository."""
    with patch(
        "src.openapi_server.impl.default_api_impl.get_lamp_repository",
        return_value=mock_lamp_repository,
    ):
        return DefaultApiImpl()


@pytest.fixture
def sample_lamp():
    """Fixture that provides a sample lamp for testing."""
    from datetime import datetime

    return Lamp(id="test-lamp-1", status=True, created_at=datetime.now(), updated_at=datetime.now())


class TestDefaultApiImpl:
    """Test suite for DefaultApiImpl class."""

    @pytest.mark.asyncio
    async def test_create_lamp(self, api_impl, mock_lamp_repository):
        """Test creating a new lamp."""
        # Arrange
        lamp_create = LampCreate(status=True)
        from datetime import datetime

        expected_lamp = Lamp(
            id="generated-uuid", status=True, created_at=datetime.now(), updated_at=datetime.now()
        )
        mock_lamp_repository.create.return_value = expected_lamp

        # Act
        result = await api_impl.create_lamp(lamp_create)

        # Assert
        assert result.status == expected_lamp.status
        assert result.id is not None

    @pytest.mark.asyncio
    async def test_create_lamp_null_request(self, api_impl, mock_lamp_repository):
        """Test creating a lamp with null request data."""
        # Act & Assert
        with pytest.raises(HTTPException) as exc_info:
            await api_impl.create_lamp(None)
        assert exc_info.value.status_code == 400
        assert exc_info.value.detail == "Invalid request data"
        mock_lamp_repository.create.assert_not_called()

    @pytest.mark.asyncio
    async def test_get_lamp_success(self, api_impl, mock_lamp_repository, sample_lamp):
        """Test getting an existing lamp."""
        # Arrange
        mock_lamp_repository.get.return_value = sample_lamp

        # Act
        result = await api_impl.get_lamp(sample_lamp.id)

        # Assert
        assert result == sample_lamp
        mock_lamp_repository.get.assert_called_once_with(sample_lamp.id)

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
    async def test_list_lamps(self, api_impl, mock_lamp_repository, sample_lamp):
        """Test listing all lamps."""
        # Arrange
        expected_lamps = [sample_lamp]
        mock_lamp_repository.list.return_value = expected_lamps

        # Act
        result = await api_impl.list_lamps(cursor=None, page_size=None)

        # Assert
        assert result.data == expected_lamps
        assert result.has_more is False
        assert result.next_cursor is None
        mock_lamp_repository.list.assert_called_once()

    @pytest.mark.asyncio
    async def test_update_lamp_success(self, api_impl, mock_lamp_repository, sample_lamp):
        """Test updating an existing lamp."""
        # Arrange
        lamp_update = LampUpdate(status=True)
        updated_lamp = Lamp(
            id=sample_lamp.id,
            status=True,
            created_at=sample_lamp.created_at,
            updated_at=sample_lamp.updated_at,
        )
        mock_lamp_repository.get.return_value = sample_lamp
        mock_lamp_repository.update.return_value = updated_lamp

        # Act
        result = await api_impl.update_lamp(sample_lamp.id, lamp_update)

        # Assert
        assert result == updated_lamp
        mock_lamp_repository.get.assert_called_once_with(sample_lamp.id)
        mock_lamp_repository.update.assert_called_once()
        updated_lamp_arg = mock_lamp_repository.update.call_args[0][0]
        assert updated_lamp_arg.id == sample_lamp.id
        assert updated_lamp_arg.status == lamp_update.status

    @pytest.mark.asyncio
    async def test_update_lamp_null_request(self, api_impl, mock_lamp_repository):
        """Test updating a lamp with null request data."""
        # Act & Assert
        with pytest.raises(HTTPException) as exc_info:
            await api_impl.update_lamp("test-lamp-1", None)
        assert exc_info.value.status_code == 400
        assert exc_info.value.detail == "Invalid request data"
        mock_lamp_repository.get.assert_not_called()
        mock_lamp_repository.update.assert_not_called()

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
    async def test_delete_lamp_success(self, api_impl, mock_lamp_repository, sample_lamp):
        """Test deleting an existing lamp."""
        # Act
        await api_impl.delete_lamp(sample_lamp.id)

        # Assert
        mock_lamp_repository.delete.assert_called_once_with(sample_lamp.id)

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
