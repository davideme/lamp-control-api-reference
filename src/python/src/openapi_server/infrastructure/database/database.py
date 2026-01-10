"""Database connection and session management.

This module provides the DatabaseManager class which handles:
- Creating and configuring the async SQLAlchemy engine
- Connection pooling with health checks
- Session lifecycle management for FastAPI dependency injection
- Proper cleanup on application shutdown
"""

from collections.abc import AsyncGenerator

from sqlalchemy.ext.asyncio import AsyncSession, async_sessionmaker, create_async_engine

from src.openapi_server.infrastructure.config import DatabaseSettings


class DatabaseManager:
    """Manages database connections and sessions for the application.

    This class creates and configures an async SQLAlchemy engine with
    connection pooling, and provides async session factories for use
    with FastAPI's dependency injection system.
    """

    def __init__(self, settings: DatabaseSettings) -> None:
        """Initialize the database manager with configuration settings.

        Args:
            settings: Database configuration including connection details
                     and pool settings.
        """
        self.settings = settings

        # Create async engine with connection pooling
        self.engine = create_async_engine(
            settings.get_connection_string(),
            pool_pre_ping=True,  # Verify connections before using
            pool_size=settings.db_pool_min_size,
            max_overflow=settings.db_pool_max_size - settings.db_pool_min_size,
            pool_recycle=3600,  # Recycle connections after 1 hour
            echo=False,  # Set to True for SQL query logging during development
        )

        # Create session factory
        self.async_session_factory = async_sessionmaker(
            self.engine,
            class_=AsyncSession,
            expire_on_commit=False,  # Prevent lazy loading issues after commit
        )

    async def close(self) -> None:
        """Close the database engine and release all connections.

        This should be called during application shutdown to properly
        clean up database connections.
        """
        await self.engine.dispose()

    async def get_session(self) -> AsyncGenerator[AsyncSession, None]:
        """Provide an async database session for dependency injection.

        This is an async generator that yields a database session and
        ensures proper cleanup via context management.

        Yields:
            AsyncSession: A database session for executing queries.

        Example:
            async def my_endpoint(session: AsyncSession = Depends(db_manager.get_session)):
                # Use session here
                pass
        """
        async with self.async_session_factory() as session:
            yield session
