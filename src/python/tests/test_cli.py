"""Tests for the CLI module.

Tests cover the three operation modes (migrate, serve, serve-only),
argument parsing, and error handling paths.
"""

import sys
from pathlib import Path
from unittest.mock import MagicMock, patch

import pytest

from src.openapi_server.cli import main, run_migrations_only, start_server


class TestRunMigrationsOnly:
    """Tests for the run_migrations_only function."""

    @patch("src.openapi_server.cli.DatabaseSettings")
    def test_skips_when_no_postgres(self, mock_settings_cls):
        """When no database_url is configured, migrations should be skipped."""
        mock_settings = MagicMock()
        mock_settings.use_postgres.return_value = False
        mock_settings_cls.return_value = mock_settings

        # Should return without error
        run_migrations_only()

    @patch("src.openapi_server.cli.command")
    @patch("src.openapi_server.cli.Config")
    @patch("src.openapi_server.cli.DatabaseSettings")
    def test_runs_alembic_upgrade(self, mock_settings_cls, mock_config_cls, mock_command):
        """When postgres is configured, should run alembic upgrade head."""
        mock_settings = MagicMock()
        mock_settings.use_postgres.return_value = True
        mock_settings.database_url = "postgresql://test:test@localhost/test"
        mock_settings.get_connection_string.return_value = (
            "postgresql+asyncpg://test:test@localhost/test"
        )
        mock_settings_cls.return_value = mock_settings

        mock_cfg = MagicMock()
        mock_config_cls.return_value = mock_cfg

        with patch.object(Path, "exists", return_value=True):
            run_migrations_only()

        mock_command.upgrade.assert_called_once_with(mock_cfg, "head")

    @patch("src.openapi_server.cli.DatabaseSettings")
    def test_exits_when_alembic_ini_missing(self, mock_settings_cls):
        """Should sys.exit(1) when alembic.ini is not found."""
        mock_settings = MagicMock()
        mock_settings.use_postgres.return_value = True
        mock_settings.database_url = "postgresql://test:test@localhost/test"
        mock_settings_cls.return_value = mock_settings

        with patch.object(Path, "exists", return_value=False):
            with pytest.raises(SystemExit) as exc_info:
                run_migrations_only()
            assert exc_info.value.code == 1

    @patch("src.openapi_server.cli.command")
    @patch("src.openapi_server.cli.Config")
    @patch("src.openapi_server.cli.DatabaseSettings")
    def test_exits_on_migration_error(self, mock_settings_cls, mock_config_cls, mock_command):
        """Should sys.exit(1) when migration fails."""
        mock_settings = MagicMock()
        mock_settings.use_postgres.return_value = True
        mock_settings.database_url = "postgresql://test:test@localhost/test"
        mock_settings.get_connection_string.return_value = (
            "postgresql+asyncpg://test:test@localhost/test"
        )
        mock_settings_cls.return_value = mock_settings

        mock_config_cls.return_value = MagicMock()
        mock_command.upgrade.side_effect = Exception("Migration failed")

        with patch.object(Path, "exists", return_value=True):
            with pytest.raises(SystemExit) as exc_info:
                run_migrations_only()
            assert exc_info.value.code == 1


class TestStartServer:
    """Tests for the start_server function."""

    @patch("src.openapi_server.cli.uvicorn")
    def test_serve_only_mode(self, mock_uvicorn):
        """serve-only mode should start server without migrations."""
        start_server(run_migrations=False, port=9000)

        mock_uvicorn.run.assert_called_once_with(
            "src.openapi_server.main:app",
            host="0.0.0.0",
            port=9000,
            log_level="info",
        )

    @patch("src.openapi_server.cli.uvicorn")
    @patch("src.openapi_server.cli.DatabaseSettings")
    def test_serve_mode_without_postgres(self, mock_settings_cls, mock_uvicorn):
        """serve mode without postgres should skip migrations and start server."""
        mock_settings = MagicMock()
        mock_settings.use_postgres.return_value = False
        mock_settings_cls.return_value = mock_settings

        start_server(run_migrations=True, port=8080)

        mock_uvicorn.run.assert_called_once()

    @patch("src.openapi_server.cli.uvicorn")
    @patch("src.openapi_server.cli.command")
    @patch("src.openapi_server.cli.Config")
    @patch("src.openapi_server.cli.DatabaseSettings")
    def test_serve_mode_with_postgres(
        self, mock_settings_cls, mock_config_cls, mock_command, mock_uvicorn
    ):
        """serve mode with postgres should run migrations then start server."""
        mock_settings = MagicMock()
        mock_settings.use_postgres.return_value = True
        mock_settings.get_connection_string.return_value = (
            "postgresql+asyncpg://test:test@localhost/test"
        )
        mock_settings_cls.return_value = mock_settings

        with patch.object(Path, "exists", return_value=True):
            start_server(run_migrations=True, port=8080)

        mock_command.upgrade.assert_called_once()
        mock_uvicorn.run.assert_called_once()

    @patch("src.openapi_server.cli.uvicorn")
    @patch("src.openapi_server.cli.DatabaseSettings")
    def test_serve_mode_alembic_ini_missing(self, mock_settings_cls, mock_uvicorn):
        """serve mode should warn and continue when alembic.ini missing."""
        mock_settings = MagicMock()
        mock_settings.use_postgres.return_value = True
        mock_settings.get_connection_string.return_value = (
            "postgresql+asyncpg://test:test@localhost/test"
        )
        mock_settings_cls.return_value = mock_settings

        with patch.object(Path, "exists", return_value=False):
            start_server(run_migrations=True, port=8080)

        # Should still start the server despite missing alembic.ini
        mock_uvicorn.run.assert_called_once()

    @patch("src.openapi_server.cli.command")
    @patch("src.openapi_server.cli.Config")
    @patch("src.openapi_server.cli.DatabaseSettings")
    def test_serve_mode_migration_error_exits(
        self, mock_settings_cls, mock_config_cls, mock_command
    ):
        """serve mode should sys.exit(1) when migration fails."""
        mock_settings = MagicMock()
        mock_settings.use_postgres.return_value = True
        mock_settings.get_connection_string.return_value = (
            "postgresql+asyncpg://test:test@localhost/test"
        )
        mock_settings_cls.return_value = mock_settings

        mock_config_cls.return_value = MagicMock()
        mock_command.upgrade.side_effect = Exception("Migration failed")

        with patch.object(Path, "exists", return_value=True):
            with pytest.raises(SystemExit) as exc_info:
                start_server(run_migrations=True, port=8080)
            assert exc_info.value.code == 1

    @patch("src.openapi_server.cli.uvicorn")
    @patch.dict("os.environ", {"PORT": "5000"}, clear=False)
    def test_port_from_env_variable(self, mock_uvicorn):
        """Should use PORT env var when no CLI port provided."""
        start_server(run_migrations=False, port=None)

        mock_uvicorn.run.assert_called_once_with(
            "src.openapi_server.main:app",
            host="0.0.0.0",
            port=5000,
            log_level="info",
        )

    @patch("src.openapi_server.cli.uvicorn")
    @patch.dict("os.environ", {}, clear=True)
    def test_default_port(self, mock_uvicorn):
        """Should use default port 8080 when no CLI port or env var."""
        start_server(run_migrations=False, port=None)

        mock_uvicorn.run.assert_called_once_with(
            "src.openapi_server.main:app",
            host="0.0.0.0",
            port=8080,
            log_level="info",
        )


class TestMain:
    """Tests for the main CLI entry point."""

    @patch("src.openapi_server.cli.run_migrations_only")
    def test_migrate_mode(self, mock_migrate):
        """--mode=migrate should call run_migrations_only."""
        with patch.object(sys, "argv", ["cli", "--mode", "migrate"]):
            main()
        mock_migrate.assert_called_once()

    @patch("src.openapi_server.cli.start_server")
    def test_serve_mode(self, mock_start):
        """--mode=serve should call start_server with migrations."""
        with patch.object(sys, "argv", ["cli", "--mode", "serve", "--port", "9000"]):
            main()
        mock_start.assert_called_once_with(run_migrations=True, port=9000)

    @patch("src.openapi_server.cli.start_server")
    def test_serve_only_mode(self, mock_start):
        """--mode=serve-only should call start_server without migrations."""
        with patch.object(sys, "argv", ["cli", "--mode", "serve-only"]):
            main()
        mock_start.assert_called_once_with(run_migrations=False, port=None)

    @patch("src.openapi_server.cli.start_server")
    def test_default_mode_is_serve_only(self, mock_start):
        """Default mode should be serve-only."""
        with patch.object(sys, "argv", ["cli"]):
            main()
        mock_start.assert_called_once_with(run_migrations=False, port=None)

    @patch("src.openapi_server.cli.start_server")
    def test_log_level_argument(self, mock_start):
        """--log-level should configure logging level."""
        with patch.object(sys, "argv", ["cli", "--log-level", "DEBUG"]):
            main()
        mock_start.assert_called_once()
