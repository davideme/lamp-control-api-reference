"""Tests for database manager engine configuration."""

from unittest.mock import MagicMock, patch

from src.openapi_server.infrastructure.database.database import DatabaseManager


def test_database_manager_passes_numeric_connect_timeout():
    """Should pass timeout via connect_args as a float."""
    settings = MagicMock()
    settings.get_connection_string.return_value = "postgresql+asyncpg://user:pass@localhost/db"
    settings.get_connect_timeout.return_value = 4.5
    settings.db_pool_min_size = 5
    settings.db_pool_max_size = 20

    with patch(
        "src.openapi_server.infrastructure.database.database.create_async_engine"
    ) as mock_create_engine:
        DatabaseManager(settings)

    connect_args = mock_create_engine.call_args.kwargs["connect_args"]
    assert connect_args == {"timeout": 4.5}


def test_database_manager_omits_timeout_when_not_configured():
    """Should pass empty connect_args when timeout is not configured."""
    settings = MagicMock()
    settings.get_connection_string.return_value = "postgresql+asyncpg://user:pass@localhost/db"
    settings.get_connect_timeout.return_value = None
    settings.db_pool_min_size = 5
    settings.db_pool_max_size = 20

    with patch("src.openapi_server.infrastructure.database.database.create_async_engine") as mock:
        DatabaseManager(settings)

    connect_args = mock.call_args.kwargs["connect_args"]
    assert connect_args == {}
