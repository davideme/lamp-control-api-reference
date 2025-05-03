"""Unit tests for the LampRepository class."""

import pytest
from openapi_server.models.lamp import Lamp
from openapi_server.repositories.lamp_repository import LampNotFoundError, LampRepository


@pytest.fixture
def repository():
    """Fixture that provides a fresh LampRepository instance for each test."""
    return LampRepository()


@pytest.fixture
def sample_lamp():
    """Fixture that provides a sample lamp for testing."""
    return Lamp(id="test-lamp-1", name="Test Lamp", is_on=False, brightness=50, color="white")


class TestLampRepository:
    """Test suite for LampRepository class."""

    def test_create_lamp(self, repository, sample_lamp):
        """Test creating a new lamp."""
        # Act
        created_lamp = repository.create(sample_lamp)

        # Assert
        assert created_lamp == sample_lamp
        assert repository.get(sample_lamp.id) == sample_lamp

    def test_get_existing_lamp(self, repository, sample_lamp):
        """Test retrieving an existing lamp."""
        # Arrange
        repository.create(sample_lamp)

        # Act
        retrieved_lamp = repository.get(sample_lamp.id)

        # Assert
        assert retrieved_lamp == sample_lamp

    def test_get_nonexistent_lamp(self, repository):
        """Test retrieving a non-existent lamp."""
        # Act
        retrieved_lamp = repository.get("nonexistent-id")

        # Assert
        assert retrieved_lamp is None

    def test_list_lamps(self, repository, sample_lamp):
        """Test listing all lamps."""
        # Arrange
        repository.create(sample_lamp)
        another_lamp = Lamp(
            id="test-lamp-2", name="Another Lamp", is_on=True, brightness=75, color="red"
        )
        repository.create(another_lamp)

        # Act
        lamps = repository.list()

        # Assert
        assert len(lamps) == 2
        assert sample_lamp in lamps
        assert another_lamp in lamps

    def test_update_existing_lamp(self, repository, sample_lamp):
        """Test updating an existing lamp."""
        # Arrange
        repository.create(sample_lamp)
        updated_lamp = Lamp(
            id=sample_lamp.id, name="Updated Lamp", is_on=True, brightness=100, color="blue"
        )

        # Act
        result = repository.update(updated_lamp)

        # Assert
        assert result == updated_lamp
        assert repository.get(sample_lamp.id) == updated_lamp

    def test_update_nonexistent_lamp(self, repository, sample_lamp):
        """Test updating a non-existent lamp."""
        # Act & Assert
        with pytest.raises(LampNotFoundError) as exc_info:
            repository.update(sample_lamp)
        assert str(exc_info.value) == f"Lamp with ID {sample_lamp.id} not found"

    def test_delete_existing_lamp(self, repository, sample_lamp):
        """Test deleting an existing lamp."""
        # Arrange
        repository.create(sample_lamp)

        # Act
        repository.delete(sample_lamp.id)

        # Assert
        assert repository.get(sample_lamp.id) is None

    def test_delete_nonexistent_lamp(self, repository):
        """Test deleting a non-existent lamp."""
        # Act & Assert
        with pytest.raises(LampNotFoundError) as exc_info:
            repository.delete("nonexistent-id")
        assert str(exc_info.value) == "Lamp with ID nonexistent-id not found"
