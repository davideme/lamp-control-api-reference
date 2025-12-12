"""
Test module for the launcher script functionality.
"""

import os
from unittest.mock import Mock, patch

from launcher import main


class TestLauncher:
    """Test cases for the launcher module."""

    @patch("launcher.uvicorn.run")
    def test_main_with_default_values(self, mock_uvicorn_run: Mock) -> None:
        """Test main function uses default values when environment variables are not set."""
        # Clear environment variables if they exist
        with patch.dict(os.environ, {}, clear=True):
            main()

        mock_uvicorn_run.assert_called_once_with(
            "src.openapi_server.main:app",
            host="0.0.0.0",
            port=80,
            workers=1,
            log_level="info",
        )

    @patch("launcher.uvicorn.run")
    def test_main_with_custom_port(self, mock_uvicorn_run: Mock) -> None:
        """Test main function uses custom PORT from environment variable."""
        with patch.dict(os.environ, {"PORT": "8080"}, clear=True):
            main()

        mock_uvicorn_run.assert_called_once_with(
            "src.openapi_server.main:app",
            host="0.0.0.0",
            port=8080,
            workers=1,
            log_level="info",
        )

    @patch("launcher.uvicorn.run")
    def test_main_with_custom_workers(self, mock_uvicorn_run: Mock) -> None:
        """Test main function uses custom WORKERS from environment variable."""
        with patch.dict(os.environ, {"WORKERS": "5"}, clear=True):
            main()

        mock_uvicorn_run.assert_called_once_with(
            "src.openapi_server.main:app",
            host="0.0.0.0",
            port=80,
            workers=5,
            log_level="info",
        )

    @patch("launcher.uvicorn.run")
    def test_main_with_custom_port_and_workers(self, mock_uvicorn_run: Mock) -> None:
        """Test main function uses both custom PORT and WORKERS from environment variables."""
        with patch.dict(os.environ, {"PORT": "9000", "WORKERS": "7"}, clear=True):
            main()

        mock_uvicorn_run.assert_called_once_with(
            "src.openapi_server.main:app",
            host="0.0.0.0",
            port=9000,
            workers=7,
            log_level="info",
        )

    @patch("launcher.uvicorn.run")
    def test_main_with_string_conversion(self, mock_uvicorn_run: Mock) -> None:
        """Test main function properly converts environment variables to integers."""
        with patch.dict(os.environ, {"PORT": "4000", "WORKERS": "2"}, clear=True):
            main()

        mock_uvicorn_run.assert_called_once_with(
            "src.openapi_server.main:app",
            host="0.0.0.0",
            port=4000,
            workers=2,
            log_level="info",
        )
