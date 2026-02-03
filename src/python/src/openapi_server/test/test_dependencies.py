"""Tests for dependency injection functions.

This module tests the dependency injection system for repositories and database sessions.
"""

from unittest.mock import MagicMock, patch

import pytest
from unittest.mock import patch, MagicMock
from src.openapi_server import dependencies
from src.openapi_server.repositories.lamp_repository import InMemoryLampRepository
from src.openapi_server.repositories.postgres_lamp_repository import PostgresLampRepository


@pytest.mark.asyncio
async def test_get_db_session_not_initialized():
    """Test get_db_session raises RuntimeError when database is not initialized."""
    # Save original db_manager
    original_db_manager = dependencies.db_manager
    try:
        # Set db_manager to None to simulate uninitialized state
        dependencies.db_manager = None

        # Attempt to get session should raise RuntimeError
        with pytest.raises(RuntimeError, match="Database not initialized"):
            async for _ in dependencies.get_db_session():
                pass  # Should not reach here
    finally:
        # Restore original db_manager
        dependencies.db_manager = original_db_manager


@pytest.mark.asyncio
async def test_get_lamp_repository_postgres():
    """Test get_lamp_repository returns PostgresLampRepository when PostgreSQL is enabled."""
    # Save original state
    original_db_manager = dependencies.db_manager
    original_settings = dependencies.settings

    try:
        # Mock settings to enable PostgreSQL
        mock_settings = MagicMock()
        mock_settings.use_postgres.return_value = True
        dependencies.settings = mock_settings

        # Mock database manager and session
        mock_session = MagicMock()
        mock_db_manager = MagicMock()

        async def mock_get_session():
            yield mock_session

        mock_db_manager.get_session.return_value = mock_get_session()
        dependencies.db_manager = mock_db_manager

        # Get repository
        repo_generator = dependencies.get_lamp_repository()
        repo = await repo_generator.__anext__()

        # Verify it's a PostgresLampRepository
        assert isinstance(repo, PostgresLampRepository)

        # Clean up the generator
        try:
            await repo_generator.__anext__()
        except StopAsyncIteration:
            pass
    finally:
        # Restore original state
        dependencies.db_manager = original_db_manager
        dependencies.settings = original_settings


@pytest.mark.asyncio
async def test_get_lamp_repository_in_memory():
    """Test get_lamp_repository returns InMemoryLampRepository when PostgreSQL is disabled."""
    # Save original state
    original_settings = dependencies.settings

    try:
        # Mock settings to disable PostgreSQL
        mock_settings = MagicMock()
        mock_settings.use_postgres.return_value = False
        dependencies.settings = mock_settings

        # Get repository
        repo_generator = dependencies.get_lamp_repository()
        repo = await repo_generator.__anext__()

        # Verify it's the in-memory repository
        assert isinstance(repo, InMemoryLampRepository)
        assert repo is dependencies.in_memory_repository

        # Clean up the generator
        try:
            await repo_generator.__anext__()
        except StopAsyncIteration:
            pass
    finally:
        # Restore original state
        dependencies.settings = original_settings


def test_initialize_database_with_postgres():
    """Test initialize_database creates DatabaseManager when PostgreSQL is enabled."""
    # Save original state
    original_db_manager = dependencies.db_manager
    original_settings = dependencies.settings

    try:
        # Mock settings to enable PostgreSQL
        mock_settings = MagicMock()
        mock_settings.use_postgres.return_value = True
        dependencies.settings = mock_settings

        # Mock DatabaseManager
        with patch("src.openapi_server.dependencies.DatabaseManager") as mock_db_manager_class:
            mock_instance = MagicMock()
            mock_db_manager_class.return_value = mock_instance

            # Initialize database
            dependencies.initialize_database()

            # Verify DatabaseManager was created
            mock_db_manager_class.assert_called_once_with(mock_settings)
            assert dependencies.db_manager is mock_instance
    finally:
        # Restore original state
        dependencies.db_manager = original_db_manager
        dependencies.settings = original_settings


def test_initialize_database_without_postgres():
    """Test initialize_database does not create DatabaseManager when PostgreSQL is disabled."""
    # Save original state
    original_db_manager = dependencies.db_manager
    original_settings = dependencies.settings

    try:
        # Reset db_manager
        dependencies.db_manager = None

        # Mock settings to disable PostgreSQL
        mock_settings = MagicMock()
        mock_settings.use_postgres.return_value = False
        dependencies.settings = mock_settings

        # Initialize database
        dependencies.initialize_database()

        # Verify DatabaseManager was not created
        assert dependencies.db_manager is None
    finally:
        # Restore original state
        dependencies.db_manager = original_db_manager
        dependencies.settings = original_settings
