"""Tests for HTTP exception handlers in main.py.

Tests cover the error_map for various HTTP status codes (401, 403, 404, 405, 409, 500)
and the fallback for unmapped codes.
"""

import pytest
from fastapi.testclient import TestClient
from starlette.exceptions import HTTPException as StarletteHTTPException

from src.openapi_server.main import app, http_exception_handler


@pytest.fixture
def client():
    return TestClient(app)


class TestHttpExceptionHandler:
    """Tests for the HTTP exception handler covering non-400 status codes."""

    def test_404_returns_not_found(self, client):
        """GET to non-existent route should return 404 NOT_FOUND."""
        response = client.get("/v1/lamps/nonexistent-uuid-that-does-not-exist")
        assert response.status_code == 404
        assert response.json() == {"error": "NOT_FOUND"}

    def test_405_returns_method_not_allowed(self, client):
        """PATCH to a route that doesn't support it should return 405."""
        response = client.patch("/v1/lamps")
        assert response.status_code == 405
        assert response.json() == {"error": "METHOD_NOT_ALLOWED"}

    @pytest.mark.asyncio
    @pytest.mark.parametrize(
        ("status_code", "expected_error"),
        [
            (401, "UNAUTHORIZED"),
            (403, "FORBIDDEN"),
            (409, "CONFLICT"),
            (500, "INTERNAL_SERVER_ERROR"),
            (418, "HTTP_ERROR_418"),
        ],
    )
    async def test_error_map_codes(self, status_code, expected_error):
        """The exception handler should map status codes to error strings."""
        exc = StarletteHTTPException(status_code=status_code)
        response = await http_exception_handler(request=None, exc=exc)

        assert response.status_code == status_code
        assert response.body == f'{{"error":"{expected_error}"}}'.encode()
