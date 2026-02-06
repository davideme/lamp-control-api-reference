"""Tests for HTTP exception handlers in main.py.

Tests cover the error_map for various HTTP status codes (401, 403, 404, 405, 409, 500)
and the fallback for unmapped codes.
"""

import pytest
from fastapi.testclient import TestClient
from starlette.exceptions import HTTPException as StarletteHTTPException

from src.openapi_server.main import app


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

    def test_error_map_returns_correct_codes(self, client):
        """The error_map should map known status codes to error strings."""
        # We test 404 and 405 through real routes above.
        # For other codes we verify the handler logic via a custom route injection.

        # Test the handler directly by triggering exceptions from an endpoint
        @app.get("/test-500")
        async def raise_500():
            raise StarletteHTTPException(status_code=500, detail="Internal error")

        @app.get("/test-409")
        async def raise_409():
            raise StarletteHTTPException(status_code=409, detail="Conflict")

        @app.get("/test-403")
        async def raise_403():
            raise StarletteHTTPException(status_code=403, detail="Forbidden")

        @app.get("/test-401")
        async def raise_401():
            raise StarletteHTTPException(status_code=401, detail="Unauthorized")

        @app.get("/test-418")
        async def raise_418():
            raise StarletteHTTPException(status_code=418, detail="Teapot")

        test_client = TestClient(app)

        response = test_client.get("/test-500")
        assert response.status_code == 500
        assert response.json() == {"error": "INTERNAL_SERVER_ERROR"}

        response = test_client.get("/test-409")
        assert response.status_code == 409
        assert response.json() == {"error": "CONFLICT"}

        response = test_client.get("/test-403")
        assert response.status_code == 403
        assert response.json() == {"error": "FORBIDDEN"}

        response = test_client.get("/test-401")
        assert response.status_code == 401
        assert response.json() == {"error": "UNAUTHORIZED"}

        # Unmapped code should use fallback format
        response = test_client.get("/test-418")
        assert response.status_code == 418
        assert response.json() == {"error": "HTTP_ERROR_418"}
