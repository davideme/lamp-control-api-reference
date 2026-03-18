"""Tests for the database configuration module.

Tests cover connection string generation, URL conversion for asyncpg,
and sslmode parameter handling.
"""

from unittest.mock import patch

from src.openapi_server.infrastructure.config import DatabaseSettings


class TestDatabaseSettings:
    """Tests for DatabaseSettings configuration."""

    def test_use_postgres_with_url(self):
        """Should return True when database_url is set."""
        settings = DatabaseSettings(database_url="postgresql://user:pass@localhost/db")
        assert settings.use_postgres() is True

    def test_use_postgres_without_url(self):
        """Should return False when database_url is not set."""
        with patch.dict("os.environ", {}, clear=True):
            settings = DatabaseSettings(database_url=None)
        assert settings.use_postgres() is False

    def test_use_postgres_with_empty_url(self):
        """Should return False when database_url is empty string."""
        settings = DatabaseSettings(database_url="")
        assert settings.use_postgres() is False

    def test_use_postgres_with_whitespace_url(self):
        """Should return False when database_url is whitespace only."""
        settings = DatabaseSettings(database_url="   ")
        assert settings.use_postgres() is False

    def test_get_connection_string_converts_scheme(self):
        """Should convert postgresql:// to postgresql+asyncpg://."""
        settings = DatabaseSettings(database_url="postgresql://user:pass@localhost:5432/db")
        result = settings.get_connection_string()
        assert result == "postgresql+asyncpg://user:pass@localhost:5432/db"

    def test_get_connection_string_removes_sslmode_disable(self):
        """Should remove sslmode=disable parameter."""
        settings = DatabaseSettings(
            database_url="postgresql://user:pass@localhost/db?sslmode=disable"
        )
        result = settings.get_connection_string()
        assert "sslmode" not in result
        assert result == "postgresql+asyncpg://user:pass@localhost/db"

    def test_get_connection_string_removes_sslmode_require(self):
        """Should remove sslmode=require parameter."""
        settings = DatabaseSettings(
            database_url="postgresql://user:pass@localhost/db?sslmode=require"
        )
        result = settings.get_connection_string()
        assert "sslmode" not in result

    def test_get_connection_string_removes_sslmode_and_timeout_params(self):
        """Should remove sslmode and timeout from query parameters."""
        settings = DatabaseSettings(
            database_url="postgresql://user:pass@localhost/db?sslmode=disable&timeout=30"
        )
        result = settings.get_connection_string()
        assert "sslmode" not in result
        assert "timeout=" not in result

    def test_get_connection_string_removes_connect_timeout(self):
        """Should remove connect_timeout from URL query parameters."""
        settings = DatabaseSettings(
            database_url="postgresql://user:pass@localhost/db?connect_timeout=10"
        )
        result = settings.get_connection_string()
        assert "connect_timeout" not in result
        assert "timeout=" not in result

    def test_get_connection_string_removes_connect_timeout_and_sslmode(self):
        """Should remove connect_timeout and sslmode together."""
        settings = DatabaseSettings(
            database_url="postgresql://user:pass@localhost/db?sslmode=disable&connect_timeout=5"
        )
        result = settings.get_connection_string()
        assert "sslmode" not in result
        assert "connect_timeout" not in result
        assert "timeout=" not in result

    def test_get_connect_timeout_from_connect_timeout(self):
        """Should parse connect_timeout as float."""
        settings = DatabaseSettings(
            database_url="postgresql://user:pass@localhost/db?connect_timeout=10"
        )
        assert settings.get_connect_timeout() == 10.0

    def test_get_connect_timeout_from_timeout(self):
        """Should parse timeout as float when connect_timeout is absent."""
        settings = DatabaseSettings(database_url="postgresql://user:pass@localhost/db?timeout=3.5")
        assert settings.get_connect_timeout() == 3.5

    def test_get_connect_timeout_prefers_connect_timeout(self):
        """Should prefer connect_timeout when both parameters are present."""
        settings = DatabaseSettings(
            database_url="postgresql://user:pass@localhost/db?timeout=9&connect_timeout=2"
        )
        assert settings.get_connect_timeout() == 2.0

    def test_get_connect_timeout_invalid_or_non_positive(self):
        """Should return None for invalid, zero, or negative timeout values."""
        assert (
            DatabaseSettings(
                database_url="postgresql://user:pass@localhost/db?connect_timeout=abc"
            ).get_connect_timeout()
            is None
        )
        assert (
            DatabaseSettings(
                database_url="postgresql://user:pass@localhost/db?connect_timeout=0"
            ).get_connect_timeout()
            is None
        )
        assert (
            DatabaseSettings(
                database_url="postgresql://user:pass@localhost/db?connect_timeout=-1"
            ).get_connect_timeout()
            is None
        )

    def test_get_connection_string_fallback_to_individual_params(self):
        """Should build connection string from individual params when no database_url."""
        settings = DatabaseSettings(
            database_url=None,
            db_user="myuser",
            db_password="mypass",
            db_host="myhost",
            db_port=5433,
            db_name="mydb",
        )
        result = settings.get_connection_string()
        assert result == "postgresql+asyncpg://myuser:mypass@myhost:5433/mydb"

    def test_get_connection_string_non_postgresql_url(self):
        """Should return URL as-is if it doesn't start with postgresql://."""
        settings = DatabaseSettings(database_url="postgresql+asyncpg://user:pass@localhost/db")
        result = settings.get_connection_string()
        assert result == "postgresql+asyncpg://user:pass@localhost/db"
