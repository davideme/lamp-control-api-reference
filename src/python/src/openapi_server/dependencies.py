"""Dependency injection functions for FastAPI.

This module provides dependency injection for repositories and database sessions.
It supports conditional activation of PostgreSQL vs in-memory storage based on
the DATABASE_URL environment variable. If DATABASE_URL is set, PostgreSQL is used;
otherwise, in-memory storage is used.
"""

from collections.abc import AsyncGenerator

from sqlalchemy.ext.asyncio import AsyncSession

from src.openapi_server.infrastructure.config import DatabaseSettings
from src.openapi_server.infrastructure.database.database import DatabaseManager
from src.openapi_server.repositories.lamp_repository import InMemoryLampRepository
from src.openapi_server.repositories.postgres_lamp_repository import PostgresLampRepository

# Global configuration and instances
settings = DatabaseSettings()
db_manager: DatabaseManager | None = None
in_memory_repository = InMemoryLampRepository()


def initialize_database() -> None:
    """Initialize the database manager if PostgreSQL is enabled.

    This function should be called during application startup.
    It creates the database manager only if DATABASE_URL is configured.
    """
    global db_manager
    if settings.use_postgres():
        db_manager = DatabaseManager(settings)


async def get_db_session() -> AsyncGenerator[AsyncSession, None]:
    """Provide a database session for dependency injection.

    This is used only when PostgreSQL is enabled.

    Yields:
        AsyncSession: A database session for executing queries.

    Raises:
        RuntimeError: If database manager is not initialized.
    """
    if db_manager is None:
        raise RuntimeError("Database not initialized")

    async for session in db_manager.get_session():
        yield session


async def get_lamp_repository() -> (
    AsyncGenerator[PostgresLampRepository | InMemoryLampRepository, None]
):
    """FastAPI dependency that provides a lamp repository instance.

    If DATABASE_URL is configured, returns a PostgreSQL repository with a database session.
    Otherwise, returns the in-memory repository for development/testing.

    This is a proper FastAPI dependency using the generator pattern. The session lifecycle
    is managed by FastAPI - the session is created when the dependency is resolved at the
    start of the request and automatically closed when the request completes.

    Yields:
        A lamp repository instance (PostgreSQL or in-memory).
    """
    if settings.use_postgres():
        # Create session and repository, FastAPI handles cleanup automatically
        async for session in get_db_session():
            yield PostgresLampRepository(session)
            # Session is automatically closed here by FastAPI after request completes
    else:
        # In-memory repository doesn't need session management
        yield in_memory_repository
