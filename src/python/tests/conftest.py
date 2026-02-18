import pytest
from fastapi import FastAPI
from fastapi.testclient import TestClient

from src.openapi_server import dependencies
from src.openapi_server.main import app as application


@pytest.fixture
def app() -> FastAPI:
    application.dependency_overrides = {}

    return application


@pytest.fixture(autouse=True)
def clear_in_memory_repository() -> None:
    """Reset in-memory repository state between tests."""
    dependencies.in_memory_repository._lamps.clear()


@pytest.fixture
def client(app) -> TestClient:
    return TestClient(app)
