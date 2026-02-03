"""Tests for default_api.py error handling.

This module tests the error handling in the API routes when no implementation
is registered.
"""

import pytest
from fastapi import HTTPException

from src.openapi_server.apis import default_api
from src.openapi_server.apis.default_api_base import BaseDefaultApi
from src.openapi_server.models.lamp_create import LampCreate
from src.openapi_server.models.lamp_update import LampUpdate
from src.openapi_server.repositories.lamp_repository import InMemoryLampRepository


@pytest.fixture
def mock_repository():
    """Provide a mock repository for testing."""
    return InMemoryLampRepository()


@pytest.fixture(autouse=True)
def clear_subclasses():
    """Clear BaseDefaultApi subclasses before each test."""
    original_subclasses = BaseDefaultApi.subclasses
    BaseDefaultApi.subclasses = ()
    yield
    BaseDefaultApi.subclasses = original_subclasses


@pytest.mark.asyncio
async def test_create_lamp_no_implementation(mock_repository):
    """Test create_lamp raises HTTPException when no implementation is registered."""
    lamp_create = LampCreate(status=True)

    with pytest.raises(HTTPException) as exc_info:
        await default_api.create_lamp(lamp_create=lamp_create, repository=mock_repository)

    assert exc_info.value.status_code == 500
    assert exc_info.value.detail == "Not implemented"


@pytest.mark.asyncio
async def test_delete_lamp_no_implementation(mock_repository):
    """Test delete_lamp raises HTTPException when no implementation is registered."""
    with pytest.raises(HTTPException) as exc_info:
        await default_api.delete_lamp(lampId="test-id", repository=mock_repository)

    assert exc_info.value.status_code == 500
    assert exc_info.value.detail == "Not implemented"


@pytest.mark.asyncio
async def test_get_lamp_no_implementation(mock_repository):
    """Test get_lamp raises HTTPException when no implementation is registered."""
    with pytest.raises(HTTPException) as exc_info:
        await default_api.get_lamp(lampId="test-id", repository=mock_repository)

    assert exc_info.value.status_code == 500
    assert exc_info.value.detail == "Not implemented"


@pytest.mark.asyncio
async def test_list_lamps_no_implementation(mock_repository):
    """Test list_lamps raises HTTPException when no implementation is registered."""
    with pytest.raises(HTTPException) as exc_info:
        await default_api.list_lamps(cursor=None, page_size=25, repository=mock_repository)

    assert exc_info.value.status_code == 500
    assert exc_info.value.detail == "Not implemented"


@pytest.mark.asyncio
async def test_update_lamp_no_implementation(mock_repository):
    """Test update_lamp raises HTTPException when no implementation is registered."""
    lamp_update = LampUpdate(status=False)

    with pytest.raises(HTTPException) as exc_info:
        await default_api.update_lamp(
            lampId="test-id", lamp_update=lamp_update, repository=mock_repository
        )

    assert exc_info.value.status_code == 500
    assert exc_info.value.detail == "Not implemented"
