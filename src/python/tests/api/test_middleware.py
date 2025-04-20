"""Tests for API middleware."""

import uuid
from http import HTTPStatus
from typing import Any, AsyncGenerator, Generator

import pytest
import structlog
from fastapi import FastAPI
from httpx import AsyncClient

from lamp_control.api.middleware import CorrelationIdMiddleware
from lamp_control.utils.logging import setup_logging


@pytest.fixture
def capture_logs() -> Generator[list[dict[str, Any]], None, None]:
    """Fixture to capture log output for testing."""
    output: list[dict[str, Any]] = []

    def capture(_, __, event_dict: dict[str, Any]) -> dict[str, Any]:
        output.append(event_dict)
        return event_dict

    processors = structlog.get_config()["processors"]
    processors.insert(-1, capture)
    yield output
    processors.remove(capture)


@pytest.fixture
def app() -> FastAPI:
    """Create a test FastAPI application."""
    setup_logging()
    app = FastAPI()
    app.add_middleware(CorrelationIdMiddleware)

    @app.get("/test")
    async def test_endpoint() -> dict[str, str]:
        logger = structlog.get_logger()
        logger.info("test_endpoint_called")
        return {"message": "test"}

    return app


@pytest.fixture
async def client(app: FastAPI) -> AsyncGenerator[AsyncClient, None]:
    """Create an async test client."""
    async with AsyncClient(app=app, base_url="http://test") as client:
        yield client


@pytest.mark.asyncio
async def test_correlation_id_generation(
    client: AsyncClient, capture_logs: list[dict[str, Any]]
) -> None:
    """Test that correlation ID is generated when not provided."""
    response = await client.get("/test")
    assert response.status_code == HTTPStatus.OK

    # Check correlation ID in response headers
    correlation_id = response.headers.get("X-Correlation-ID")
    assert correlation_id is not None
    assert uuid.UUID(correlation_id)  # Validates UUID format

    # Check correlation ID in logs
    assert capture_logs[-1]["correlation_id"] == correlation_id


@pytest.mark.asyncio
async def test_correlation_id_propagation(
    client: AsyncClient, capture_logs: list[dict[str, Any]]
) -> None:
    """Test that provided correlation ID is propagated."""
    test_correlation_id = "test-correlation-id"
    response = await client.get(
        "/test", headers={"X-Correlation-ID": test_correlation_id}
    )
    assert response.status_code == HTTPStatus.OK

    # Check correlation ID in response headers
    assert response.headers["X-Correlation-ID"] == test_correlation_id

    # Check correlation ID in logs
    assert capture_logs[-1]["correlation_id"] == test_correlation_id


@pytest.mark.asyncio
async def test_request_context_logging(
    client: AsyncClient, capture_logs: list[dict[str, Any]]
) -> None:
    """Test that request context is added to logs."""
    test_user_agent = "test-user-agent"
    response = await client.get(
        "/test", headers={"User-Agent": test_user_agent}
    )
    assert response.status_code == HTTPStatus.OK

    # Check request context in logs
    log_entry = capture_logs[-1]
    assert log_entry["http_method"] == "GET"
    assert log_entry["url"] == "http://test/test"
    assert log_entry["user_agent"] == test_user_agent
    assert "client_ip" in log_entry


@pytest.mark.asyncio
async def test_context_clearing(
    client: AsyncClient, capture_logs: list[dict[str, Any]]
) -> None:
    """Test that context is cleared between requests."""
    # First request
    first_response = await client.get("/test")
    first_correlation_id = first_response.headers["X-Correlation-ID"]

    # Second request
    second_response = await client.get("/test")
    second_correlation_id = second_response.headers["X-Correlation-ID"]

    # Check that correlation IDs are different
    assert first_correlation_id != second_correlation_id

    # Check that logs have correct correlation IDs
    assert capture_logs[-2]["correlation_id"] == first_correlation_id
    assert capture_logs[-1]["correlation_id"] == second_correlation_id


@pytest.mark.asyncio
async def test_custom_correlation_id_config(
    app: FastAPI, capture_logs: list[dict[str, Any]]
) -> None:
    """Test custom correlation ID configuration."""
    # Add middleware with custom configuration
    app.middleware_stack = None  # Clear existing middleware
    app.add_middleware(
        CorrelationIdMiddleware,
        header_name="Custom-Correlation-ID",
        context_key="custom_correlation_id",
        generate_id=lambda: "custom-id",
    )

    # Create new client with updated app
    async with AsyncClient(app=app, base_url="http://test") as client:
        response = await client.get("/test")
        assert response.status_code == HTTPStatus.OK

        # Check custom header name
        assert response.headers["Custom-Correlation-ID"] == "custom-id"

        # Check custom context key in logs
        assert capture_logs[-1]["custom_correlation_id"] == "custom-id" 