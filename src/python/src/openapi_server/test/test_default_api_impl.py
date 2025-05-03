"""Unit tests for the DefaultApiImpl class."""

from unittest.mock import Mock, patch

import pytest
from fastapi import HTTPException

from openapi_server.impl.default_api_impl import DefaultApiImpl
from openapi_server.models.lamp import Lamp
from openapi_server.models.lamp_create import LampCreate
from openapi_server.models.lamp_update import LampUpdate
from openapi_server.repositories.lamp_repository import LampNotFoundError


@pytest.fixture
def mock_lamp_repository():
    """Fixture that provides a mocked LampRepository."""
    return Mock()


@pytest.fixture
def api_impl(mock_lamp_repository):
    """Fixture that provides a DefaultApiImpl instance with mocked repository."""
    with patch(
        "openapi_server.impl.default_api_impl.get_lamp_repository",
        return_value=mock_lamp_repository,
    ):
        return DefaultApiImpl()


@pytest.fixture
def sample_lamp():
    """Fixture that provides a sample lamp for testing."""
    return Lamp(id="test-lamp-1", name="Test Lamp", is_on=False, brightness=50, color="white")


class TestDefaultApiImpl:
    """Test suite for DefaultApiImpl class."""

    async def test_create_lamp(self, api_impl, mock_lamp_repository):
        """Test creating a new lamp."""
        # Arrange
        lamp_create = LampCreate(name="New Lamp", is_on=True, brightness=75, color="blue")
        expected_lamp = Lamp(
            id="generated-uuid", name="New Lamp", is_on=True, brightness=75, color="blue"
        )
        mock_lamp_repository.create.return_value = expected_lamp

        # Act
        result = await api_impl.create_lamp(lamp_create)

        # Assert
        assert result == expected_lamp
        mock_lamp_repository.create.assert_called_once()
        created_lamp = mock_lamp_repository.create.call_args[0][0]
        assert created_lamp.name == lamp_create.name
        assert created_lamp.is_on == lamp_create.is_on
        assert created_lamp.brightness == lamp_create.brightness
        assert created_lamp.color == lamp_create.color

    async def test_get_lamp_success(self, api_impl, mock_lamp_repository, sample_lamp):
        """Test getting an existing lamp."""
        # Arrange
        mock_lamp_repository.get.return_value = sample_lamp

        # Act
        result = await api_impl.get_lamp(sample_lamp.id)

        # Assert
        assert result == sample_lamp
        mock_lamp_repository.get.assert_called_once_with(sample_lamp.id)

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

    async def test_list_lamps(self, api_impl, mock_lamp_repository, sample_lamp):
        """Test listing all lamps."""
        # Arrange
        expected_lamps = [sample_lamp]
        mock_lamp_repository.list.return_value = expected_lamps

        # Act
        result = await api_impl.list_lamps()

        # Assert
        assert result == expected_lamps
        mock_lamp_repository.list.assert_called_once()

    async def test_update_lamp_success(self, api_impl, mock_lamp_repository, sample_lamp):
        """Test updating an existing lamp."""
        # Arrange
        lamp_update = LampUpdate(name="Updated Lamp", is_on=True, brightness=100, color="red")
        updated_lamp = Lamp(
            id=sample_lamp.id, name="Updated Lamp", is_on=True, brightness=100, color="red"
        )
        mock_lamp_repository.update.return_value = updated_lamp

        # Act
        result = await api_impl.update_lamp(sample_lamp.id, lamp_update)

        # Assert
        assert result == updated_lamp
        mock_lamp_repository.update.assert_called_once()
        updated_lamp_arg = mock_lamp_repository.update.call_args[0][0]
        assert updated_lamp_arg.id == sample_lamp.id
        assert updated_lamp_arg.name == lamp_update.name
        assert updated_lamp_arg.is_on == lamp_update.is_on
        assert updated_lamp_arg.brightness == lamp_update.brightness
        assert updated_lamp_arg.color == lamp_update.color

    async def test_update_lamp_not_found(self, api_impl, mock_lamp_repository):
        """Test updating a non-existent lamp."""
        # Arrange
        lamp_update = LampUpdate(name="Updated Lamp", is_on=True, brightness=100, color="red")
        mock_lamp_repository.update.side_effect = LampNotFoundError("nonexistent-id")

        # Act & Assert
        with pytest.raises(HTTPException) as exc_info:
            await api_impl.update_lamp("nonexistent-id", lamp_update)
        assert exc_info.value.status_code == 404
        assert exc_info.value.detail == "Lamp not found"
        mock_lamp_repository.update.assert_called_once()

    async def test_delete_lamp_success(self, api_impl, mock_lamp_repository, sample_lamp):
        """Test deleting an existing lamp."""
        # Act
        await api_impl.delete_lamp(sample_lamp.id)

        # Assert
        mock_lamp_repository.delete.assert_called_once_with(sample_lamp.id)

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
