"""Tests for the health endpoint."""

from fastapi.testclient import TestClient


def test_health_endpoint(client: TestClient):
    """Test case for health endpoint.

    Health check endpoint should return status ok and storage type.
    """
    response = client.request("GET", "/health")

    assert response.status_code == 200
    json_response = response.json()
    assert json_response["status"] == "ok"
    assert "storage" in json_response
    assert json_response["storage"] in ["memory", "postgres"]


def test_health_endpoint_content_type(client: TestClient):
    """Test that health endpoint returns proper JSON content type."""
    response = client.request("GET", "/health")

    assert response.status_code == 200
    assert response.headers.get("content-type") == "application/json"
    json_response = response.json()
    assert json_response["status"] == "ok"
    assert "storage" in json_response
