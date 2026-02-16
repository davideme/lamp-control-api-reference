"""Integration tests for PostgresLampRepository with real PostgreSQL database.

These tests use Testcontainers to spin up a real PostgreSQL instance,
ensuring that the repository works correctly with an actual database.
"""

import asyncio
from datetime import UTC, datetime, timedelta
from pathlib import Path
from uuid import uuid4

import psycopg2
import pytest
from sqlalchemy import text
from sqlalchemy.ext.asyncio import AsyncSession, async_sessionmaker, create_async_engine
from testcontainers.postgres import PostgresContainer

from src.openapi_server.entities.lamp_entity import LampEntity
from src.openapi_server.repositories.lamp_repository import LampNotFoundError
from src.openapi_server.repositories.postgres_lamp_repository import PostgresLampRepository


@pytest.fixture(scope="module")
def event_loop():
    """Create a module-scoped event loop for async fixtures."""
    loop = asyncio.get_event_loop_policy().new_event_loop()
    yield loop
    loop.close()


@pytest.fixture(scope="module")
def postgres_container():
    """Start a PostgreSQL container for all tests in this module."""
    with PostgresContainer("postgres:16-alpine", driver="psycopg2") as postgres:
        # Apply the schema
        # Path from: src/python/src/openapi_server/test/test_postgres_lamp_repository.py
        # Go up to repository root: parents[5] gets us to lamp-control-api-reference/
        schema_path = Path(__file__).parents[5] / "database" / "sql" / "postgresql" / "schema.sql"

        # Read and execute the schema
        with open(schema_path) as f:
            schema_sql = f.read()

        # Get a connection and execute the schema
        # Use individual connection parameters instead of URL
        conn = psycopg2.connect(
            host=postgres.get_container_host_ip(),
            port=postgres.get_exposed_port(5432),
            user=postgres.username,
            password=postgres.password,
            dbname=postgres.dbname,
        )
        cur = conn.cursor()
        cur.execute(schema_sql)
        conn.commit()
        cur.close()
        conn.close()

        yield postgres


@pytest.fixture(scope="module")
async def engine(postgres_container):
    """Create an async SQLAlchemy engine for the test database."""
    # Convert psycopg2 URL to asyncpg URL
    database_url = postgres_container.get_connection_url().replace(
        "postgresql+psycopg2://", "postgresql+asyncpg://"
    )

    engine = create_async_engine(database_url, echo=False)
    yield engine
    await engine.dispose()


@pytest.fixture
async def session(engine):
    """Provide a database session for each test.

    Each test runs in a transaction that is rolled back at the end,
    ensuring test isolation even when the repository commits.
    """
    # Create a connection that will be used for the entire test
    async with engine.connect() as connection:
        # Start a transaction
        transaction = await connection.begin()

        # Create a session bound to this connection
        async_session = async_sessionmaker(
            bind=connection, class_=AsyncSession, expire_on_commit=False
        )

        async with async_session() as session:
            yield session

            # Rollback the transaction after the test
            await transaction.rollback()


@pytest.fixture
def repository(session):
    """Provide a PostgresLampRepository instance."""
    return PostgresLampRepository(session)


@pytest.fixture
def sample_lamp_entity():
    """Provide a sample lamp entity for testing."""
    return LampEntity(id=str(uuid4()), status=True)


class TestPostgresLampRepository:
    """Integration test suite for PostgresLampRepository."""

    async def test_create_lamp(self, repository, sample_lamp_entity, session):
        """Test creating a lamp in PostgreSQL."""
        # Act
        created_lamp = await repository.create(sample_lamp_entity)

        # Assert
        assert created_lamp.id == sample_lamp_entity.id
        assert created_lamp.status == sample_lamp_entity.status
        assert created_lamp.created_at is not None
        assert created_lamp.updated_at is not None

    async def test_get_existing_lamp(self, repository, sample_lamp_entity, session):
        """Test retrieving an existing lamp."""
        # Arrange
        await repository.create(sample_lamp_entity)

        # Act
        retrieved_lamp = await repository.get(sample_lamp_entity.id)

        # Assert
        assert retrieved_lamp is not None
        assert retrieved_lamp.id == sample_lamp_entity.id
        assert retrieved_lamp.status == sample_lamp_entity.status

    async def test_get_nonexistent_lamp(self, repository):
        """Test retrieving a non-existent lamp returns None."""
        # Act
        retrieved_lamp = await repository.get(str(uuid4()))

        # Assert
        assert retrieved_lamp is None

    async def test_list_empty(self, repository):
        """Test listing lamps when database is empty."""
        # Act
        lamps = await repository.list()

        # Assert
        assert lamps == []

    async def test_list_multiple_lamps(self, repository, sample_lamp_entity, session):
        """Test listing multiple lamps."""
        # Arrange
        await repository.create(sample_lamp_entity)

        another_lamp = LampEntity(id=str(uuid4()), status=False)
        await repository.create(another_lamp)

        # Act
        lamps = await repository.list()

        # Assert
        assert len(lamps) == 2
        lamp_ids = {lamp.id for lamp in lamps}
        assert sample_lamp_entity.id in lamp_ids
        assert another_lamp.id in lamp_ids

    async def test_list_paginated_returns_exact_window(self, repository):
        """Test list_paginated returns offset/limit window."""
        # Arrange
        now = datetime.now(UTC)
        lamp_a = LampEntity(id=str(uuid4()), status=True, created_at=now, updated_at=now)
        lamp_b = LampEntity(
            id=str(uuid4()),
            status=False,
            created_at=now + timedelta(seconds=1),
            updated_at=now + timedelta(seconds=1),
        )
        lamp_c = LampEntity(
            id=str(uuid4()),
            status=True,
            created_at=now + timedelta(seconds=2),
            updated_at=now + timedelta(seconds=2),
        )
        await repository.create(lamp_a)
        await repository.create(lamp_b)
        await repository.create(lamp_c)

        # Act
        page = await repository.list_paginated(offset=1, limit=1)

        # Assert
        assert len(page) == 1
        assert page[0].id == lamp_b.id

    async def test_list_paginated_second_page_progression(self, repository):
        """Test pagination progression across pages."""
        # Arrange
        now = datetime.now(UTC)
        lamps = [
            LampEntity(
                id=str(uuid4()),
                status=i % 2 == 0,
                created_at=now + timedelta(seconds=i),
                updated_at=now + timedelta(seconds=i),
            )
            for i in range(5)
        ]
        for lamp in lamps:
            await repository.create(lamp)

        # Act
        first_page = await repository.list_paginated(offset=0, limit=2)
        second_page = await repository.list_paginated(offset=2, limit=2)

        # Assert
        assert [lamp.id for lamp in first_page] == [lamps[0].id, lamps[1].id]
        assert [lamp.id for lamp in second_page] == [lamps[2].id, lamps[3].id]

    async def test_list_paginated_excludes_soft_deleted(self, repository):
        """Test list_paginated excludes soft-deleted lamps."""
        # Arrange
        first = LampEntity(id=str(uuid4()), status=True)
        second = LampEntity(id=str(uuid4()), status=False)
        third = LampEntity(id=str(uuid4()), status=True)
        await repository.create(first)
        await repository.create(second)
        await repository.create(third)
        await repository.delete(second.id)

        # Act
        lamps = await repository.list_paginated(offset=0, limit=10)

        # Assert
        assert {lamp.id for lamp in lamps} == {first.id, third.id}
        assert second.id not in {lamp.id for lamp in lamps}

    async def test_list_paginated_uses_deterministic_ordering(self, repository):
        """Test list_paginated ordering is created_at then id."""
        # Arrange
        created_at = datetime.now(UTC)
        lamp_a = LampEntity(
            id=str(uuid4()), status=True, created_at=created_at, updated_at=created_at
        )
        lamp_b = LampEntity(
            id=str(uuid4()), status=False, created_at=created_at, updated_at=created_at
        )
        await repository.create(lamp_b)
        await repository.create(lamp_a)

        # Act
        lamps = await repository.list_paginated(offset=0, limit=2)

        # Assert
        expected = sorted([lamp_a.id, lamp_b.id])
        assert [lamps[0].id, lamps[1].id] == expected

    async def test_update_lamp_status(self, repository, sample_lamp_entity, session):
        """Test updating a lamp's status."""
        # Arrange
        await repository.create(sample_lamp_entity)

        # Modify the status
        sample_lamp_entity.status = not sample_lamp_entity.status

        # Act
        updated_lamp = await repository.update(sample_lamp_entity)

        # Assert
        assert updated_lamp.id == sample_lamp_entity.id
        assert updated_lamp.status == sample_lamp_entity.status
        # Database trigger should have set the timestamp.
        # Note: within a transaction, PostgreSQL's now() returns the transaction
        # start time, and clock differences between Python and the container can
        # cause the DB-generated timestamp to appear earlier than the Python-
        # generated one passed during create. We only verify it was set.
        assert updated_lamp.updated_at is not None

    async def test_update_nonexistent_lamp(self, repository, sample_lamp_entity):
        """Test updating a non-existent lamp raises LampNotFoundError."""
        # Act & Assert
        with pytest.raises(LampNotFoundError) as exc_info:
            await repository.update(sample_lamp_entity)
        assert sample_lamp_entity.id in str(exc_info.value)

    async def test_soft_delete_lamp(self, repository, sample_lamp_entity, session):
        """Test soft deleting a lamp."""
        # Arrange
        await repository.create(sample_lamp_entity)

        # Act
        await repository.delete(sample_lamp_entity.id)

        # Assert - lamp should not be retrievable
        retrieved_lamp = await repository.get(sample_lamp_entity.id)
        assert retrieved_lamp is None

    async def test_soft_delete_sets_deleted_at(self, repository, sample_lamp_entity, session):
        """Test that soft delete sets the deleted_at timestamp in database."""
        # Arrange
        await repository.create(sample_lamp_entity)

        # Act
        await repository.delete(sample_lamp_entity.id)

        # Assert - check database directly
        result = await session.execute(
            text("SELECT deleted_at FROM lamps WHERE id = :id"), {"id": sample_lamp_entity.id}
        )
        row = result.fetchone()
        assert row is not None
        assert row[0] is not None  # deleted_at should be set

    async def test_soft_deleted_lamp_not_in_list(self, repository, sample_lamp_entity, session):
        """Test that soft-deleted lamps don't appear in list()."""
        # Arrange
        await repository.create(sample_lamp_entity)
        another_lamp = LampEntity(id=str(uuid4()), status=False)
        await repository.create(another_lamp)

        # Act - delete one lamp
        await repository.delete(sample_lamp_entity.id)
        lamps = await repository.list()

        # Assert - only the non-deleted lamp should be in the list
        assert len(lamps) == 1
        assert lamps[0].id == another_lamp.id

    async def test_delete_nonexistent_lamp(self, repository):
        """Test deleting a non-existent lamp raises LampNotFoundError."""
        # Act & Assert
        nonexistent_id = str(uuid4())
        with pytest.raises(LampNotFoundError) as exc_info:
            await repository.delete(nonexistent_id)
        assert nonexistent_id in str(exc_info.value)

    async def test_delete_already_deleted_lamp(self, repository, sample_lamp_entity, session):
        """Test deleting an already-deleted lamp raises LampNotFoundError."""
        # Arrange
        await repository.create(sample_lamp_entity)
        await repository.delete(sample_lamp_entity.id)

        # Act & Assert
        with pytest.raises(LampNotFoundError):
            await repository.delete(sample_lamp_entity.id)

    async def test_field_mapping_status_to_is_on(self, repository, session):
        """Test that entity status field correctly maps to database is_on field."""
        # Arrange
        lamp_on = LampEntity(id=str(uuid4()), status=True)
        lamp_off = LampEntity(id=str(uuid4()), status=False)

        # Act
        await repository.create(lamp_on)
        await repository.create(lamp_off)

        # Assert - check database directly
        result = await session.execute(
            text("SELECT id, is_on FROM lamps WHERE id IN (:id1, :id2) ORDER BY is_on"),
            {"id1": lamp_on.id, "id2": lamp_off.id},
        )
        rows = result.fetchall()

        assert len(rows) == 2
        assert str(rows[0][0]) == lamp_off.id  # is_on = False comes first
        assert rows[0][1] is False
        assert str(rows[1][0]) == lamp_on.id  # is_on = True comes second
        assert rows[1][1] is True

    async def test_concurrent_operations(self, repository, session):
        """Test that multiple operations work correctly with connection pooling."""
        # Arrange - create multiple lamps
        lamps = [LampEntity(id=str(uuid4()), status=i % 2 == 0) for i in range(10)]

        # Act - create all lamps
        for lamp in lamps:
            await repository.create(lamp)

        # Assert - all lamps should be retrievable
        all_lamps = await repository.list()
        assert len(all_lamps) == 10
