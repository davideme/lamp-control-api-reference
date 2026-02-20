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

    def test_get_connection_string_removes_sslmode_with_other_params(self):
        """Should remove sslmode while preserving other query parameters."""
        settings = DatabaseSettings(
            database_url="postgresql://user:pass@localhost/db?sslmode=disable&timeout=30"
        )
        result = settings.get_connection_string()
        assert "sslmode" not in result
        assert "timeout=30" in result

    def test_get_connection_string_maps_connect_timeout_to_timeout(self):
        """Should map connect_timeout to asyncpg's timeout parameter."""
        settings = DatabaseSettings(
            database_url="postgresql://user:pass@localhost/db?connect_timeout=10"
        )
        result = settings.get_connection_string()
        assert "connect_timeout" not in result
        assert "timeout=10" in result

    def test_get_connection_string_maps_connect_timeout_and_removes_sslmode(self):
        """Should normalize connect_timeout and remove sslmode together."""
        settings = DatabaseSettings(
            database_url=(
                "postgresql://user:pass@localhost/db?sslmode=disable&connect_timeout=5"
            )
        )
        result = settings.get_connection_string()
        assert "sslmode" not in result
        assert "connect_timeout" not in result
        assert "timeout=5" in result

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
