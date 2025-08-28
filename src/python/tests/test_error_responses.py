"""Test error response formats to ensure they match OpenAPI specification."""

import pytest
from fastapi.testclient import TestClient
from src.openapi_server.main import app

client = TestClient(app)


def test_post_lamps_with_null_data():
    """Test POST /lamps with null data returns correct error format."""
    response = client.post("/v1/lamps", json=None)
    assert response.status_code == 400
    assert response.json() == {"error": "INVALID_ARGUMENT"}


def test_put_lamps_with_null_data():
    """Test PUT /lamps/{lampId} with null data returns correct error format."""
    response = client.put("/v1/lamps/0", json=None)
    assert response.status_code == 400
    assert response.json() == {"error": "INVALID_ARGUMENT"}


def test_post_lamps_with_invalid_json():
    """Test POST /lamps with invalid JSON structure returns correct error format."""
    response = client.post("/v1/lamps", json={"invalid": "data"})
    assert response.status_code == 400
    assert response.json() == {"error": "INVALID_ARGUMENT"}


def test_post_lamps_with_empty_json():
    """Test POST /lamps with empty JSON object returns correct error format."""
    response = client.post("/v1/lamps", json={})
    assert response.status_code == 400
    assert response.json() == {"error": "INVALID_ARGUMENT"}


def test_error_response_schema_compliance():
    """Test that error responses only contain the 'error' field as per OpenAPI spec."""
    response = client.post("/v1/lamps", json=None)
    assert response.status_code == 400
    response_data = response.json()
    
    # Should only have 'error' field
    assert set(response_data.keys()) == {"error"}
    assert isinstance(response_data["error"], str)
    assert response_data["error"] == "INVALID_ARGUMENT"