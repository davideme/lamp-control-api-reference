"""Unit tests for the InMemoryLampRepository class."""

import pytest

from src.openapi_server.entities.lamp_entity import LampEntity
from src.openapi_server.repositories.lamp_repository import (
    InMemoryLampRepository,
    LampNotFoundError,
)


@pytest.fixture
def repository():
    """Fixture that provides a fresh InMemoryLampRepository instance for each test."""
    return InMemoryLampRepository()


@pytest.fixture
def sample_lamp_entity():
    """Fixture that provides a sample lamp entity for testing."""
    return LampEntity(id="test-lamp-1", status=True)


class TestInMemoryLampRepository:
    """Test suite for InMemoryLampRepository class."""

    async def test_create_lamp(self, repository, sample_lamp_entity):
        """Test creating a new lamp."""
        # Act
        created_lamp = await repository.create(sample_lamp_entity)

        # Assert
        assert created_lamp == sample_lamp_entity
        retrieved = await repository.get(sample_lamp_entity.id)
        assert retrieved == sample_lamp_entity

    async def test_get_existing_lamp(self, repository, sample_lamp_entity):
        """Test retrieving an existing lamp."""
        # Arrange
        await repository.create(sample_lamp_entity)

        # Act
        retrieved_lamp = await repository.get(sample_lamp_entity.id)

        # Assert
        assert retrieved_lamp == sample_lamp_entity

    async def test_get_nonexistent_lamp(self, repository):
        """Test retrieving a non-existent lamp."""
        # Act
        retrieved_lamp = await repository.get("nonexistent-id")

        # Assert
        assert retrieved_lamp is None

    async def test_list_lamps(self, repository, sample_lamp_entity):
        """Test listing all lamps."""
        # Arrange
        await repository.create(sample_lamp_entity)

        another_lamp_entity = LampEntity(id="test-lamp-2", status=True)
        await repository.create(another_lamp_entity)

        # Act
        lamps = await repository.list()

        # Assert
        assert len(lamps) == 2
        assert sample_lamp_entity in lamps
        assert another_lamp_entity in lamps

    async def test_update_existing_lamp(self, repository, sample_lamp_entity):
        """Test updating an existing lamp."""
        # Arrange
        await repository.create(sample_lamp_entity)

        # Create updated entity with same ID but different status
        updated_lamp_entity = LampEntity(id=sample_lamp_entity.id, status=True)

        # Act
        result = await repository.update(updated_lamp_entity)

        # Assert
        assert result == updated_lamp_entity
        retrieved = await repository.get(sample_lamp_entity.id)
        assert retrieved == updated_lamp_entity

    async def test_update_nonexistent_lamp(self, repository, sample_lamp_entity):
        """Test updating a non-existent lamp."""
        # Act & Assert
        with pytest.raises(LampNotFoundError) as exc_info:
            await repository.update(sample_lamp_entity)
        assert str(exc_info.value) == f"Lamp with ID {sample_lamp_entity.id} not found"

    async def test_delete_existing_lamp(self, repository, sample_lamp_entity):
        """Test deleting an existing lamp."""
        # Arrange
        await repository.create(sample_lamp_entity)

        # Act
        await repository.delete(sample_lamp_entity.id)

        # Assert
        retrieved = await repository.get(sample_lamp_entity.id)
        assert retrieved is None

    async def test_delete_nonexistent_lamp(self, repository):
        """Test deleting a non-existent lamp."""
        # Act & Assert
        with pytest.raises(LampNotFoundError) as exc_info:
            await repository.delete("nonexistent-id")
        assert str(exc_info.value) == "Lamp with ID nonexistent-id not found"
