"""Dependency injection functions for FastAPI.

This module provides dependency injection for repositories and database sessions.
It supports conditional activation of PostgreSQL vs in-memory storage based on
the USE_POSTGRES environment variable.
"""

from collections.abc import AsyncGenerator
from typing import Union

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
    It creates the database manager only if USE_POSTGRES is true.
    """
    global db_manager
    if settings.use_postgres:
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


async def get_lamp_repository(
    session: AsyncSession | None = None,
) -> Union[PostgresLampRepository, InMemoryLampRepository]:
    """Get a lamp repository instance based on configuration.

    If USE_POSTGRES is true, returns a PostgreSQL repository with a database session.
    Otherwise, returns the in-memory repository for development/testing.

    Args:
        session: Optional database session (injected when using PostgreSQL).

    Returns:
        A lamp repository instance (PostgreSQL or in-memory).
    """
    # When PostgreSQL is enabled, session must be provided via dependency injection
    if settings.use_postgres:
        if session is None:
            # This is a fallback; in production, session should be injected
            async for s in get_db_session():
                return PostgresLampRepository(s)
        return PostgresLampRepository(session)

    # Default to in-memory repository
    return in_memory_repository
