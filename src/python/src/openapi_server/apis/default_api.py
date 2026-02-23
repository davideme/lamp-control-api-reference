import importlib
import pkgutil
from typing import Annotated, Any, Optional  # noqa: F401

from fastapi import (  # noqa: F401
    APIRouter,
    Body,
    Cookie,
    Depends,
    Form,
    Header,
    HTTPException,
    Path,
    Query,
    Response,
    Security,
    status,
)
from pydantic import StrictStr
from sqlalchemy.ext.asyncio import AsyncSession

import src.openapi_server.impl
from src.openapi_server.apis.default_api_base import BaseDefaultApi
from src.openapi_server.dependencies import get_lamp_repository, get_optional_db_session
from src.openapi_server.models.error import Error
from src.openapi_server.models.extra_models import TokenModel  # noqa: F401
from src.openapi_server.models.lamp import Lamp
from src.openapi_server.models.lamp_create import LampCreate
from src.openapi_server.models.lamp_update import LampUpdate
from src.openapi_server.models.list_lamps200_response import ListLamps200Response
from src.openapi_server.repositories.lamp_repository import InMemoryLampRepository
from src.openapi_server.repositories.postgres_lamp_repository import PostgresLampRepository

router = APIRouter()

ns_pkg = src.openapi_server.impl
for _, name, _ in pkgutil.iter_modules(ns_pkg.__path__, ns_pkg.__name__ + "."):
    importlib.import_module(name)


@router.post(
    "/lamps",
    responses={
        201: {"model": Lamp, "description": "Lamp created successfully"},
        400: {"model": Error, "description": "Invalid request data"},
    },
    tags=["default"],
    summary="Create a new lamp",
    response_model_by_alias=True,
    status_code=201,
)
async def create_lamp(
    lamp_create: Annotated[LampCreate | None, Body(description="")] = None,
    repository: Annotated[
        PostgresLampRepository | InMemoryLampRepository, Depends(get_lamp_repository)
    ] = None,
    db_session: Annotated[AsyncSession | None, Depends(get_optional_db_session)] = None,
) -> Lamp:
    if not BaseDefaultApi.subclasses:
        raise HTTPException(status_code=500, detail="Not implemented")
    return await BaseDefaultApi.subclasses[0](repository, db_session).create_lamp(lamp_create)


@router.delete(
    "/lamps/{lampId}",
    responses={
        204: {"description": "Lamp deleted successfully"},
        400: {"model": Error, "description": "Invalid lamp ID format"},
        404: {"description": "Lamp not found"},
    },
    tags=["default"],
    summary="Delete a lamp",
    response_model_by_alias=True,
    status_code=204,
)
async def delete_lamp(
    lamp_id: Annotated[StrictStr, Path(description="", alias="lampId")],
    repository: Annotated[
        PostgresLampRepository | InMemoryLampRepository, Depends(get_lamp_repository)
    ] = None,
    db_session: Annotated[AsyncSession | None, Depends(get_optional_db_session)] = None,
) -> None:
    if not BaseDefaultApi.subclasses:
        raise HTTPException(status_code=500, detail="Not implemented")
    return await BaseDefaultApi.subclasses[0](repository, db_session).delete_lamp(lamp_id)


@router.get(
    "/lamps/{lampId}",
    responses={
        200: {"model": Lamp, "description": "Lamp details"},
        304: {"description": "Not Modified"},
        400: {"model": Error, "description": "Invalid lamp ID format"},
        404: {"description": "Lamp not found"},
    },
    tags=["default"],
    summary="Get a specific lamp",
    response_model_by_alias=True,
)
async def get_lamp(
    lamp_id: Annotated[StrictStr, Path(description="", alias="lampId")],
    repository: Annotated[
        PostgresLampRepository | InMemoryLampRepository, Depends(get_lamp_repository)
    ] = None,
    db_session: Annotated[AsyncSession | None, Depends(get_optional_db_session)] = None,
) -> Lamp:
    if not BaseDefaultApi.subclasses:
        raise HTTPException(status_code=500, detail="Not implemented")
    return await BaseDefaultApi.subclasses[0](repository, db_session).get_lamp(lamp_id)


@router.get(
    "/lamps",
    responses={
        200: {"model": ListLamps200Response, "description": "A list of lamps with pagination"},
        304: {"description": "Not Modified"},
        400: {"model": Error, "description": "Invalid request parameters"},
    },
    tags=["default"],
    summary="List all lamps",
    response_model_by_alias=True,
)
async def list_lamps(
    cursor: Annotated[StrictStr | None, Query(description="", alias="cursor")] = None,
    page_size: Annotated[int | None, Query(description="", alias="pageSize", ge=1, le=100)] = 25,
    repository: Annotated[
        PostgresLampRepository | InMemoryLampRepository, Depends(get_lamp_repository)
    ] = None,
    db_session: Annotated[AsyncSession | None, Depends(get_optional_db_session)] = None,
) -> ListLamps200Response:
    if not BaseDefaultApi.subclasses:
        raise HTTPException(status_code=500, detail="Not implemented")
    return await BaseDefaultApi.subclasses[0](repository, db_session).list_lamps(cursor, page_size)


@router.put(
    "/lamps/{lampId}",
    responses={
        200: {"model": Lamp, "description": "Lamp updated successfully"},
        400: {"model": Error, "description": "Invalid request data or lamp ID format"},
        404: {"description": "Lamp not found"},
    },
    tags=["default"],
    summary="Update a lamp&#39;s status",
    response_model_by_alias=True,
)
async def update_lamp(
    lamp_id: Annotated[StrictStr, Path(description="", alias="lampId")],
    lamp_update: Annotated[LampUpdate | None, Body(description="")] = None,
    repository: Annotated[
        PostgresLampRepository | InMemoryLampRepository, Depends(get_lamp_repository)
    ] = None,
    db_session: Annotated[AsyncSession | None, Depends(get_optional_db_session)] = None,
) -> Lamp:
    if not BaseDefaultApi.subclasses:
        raise HTTPException(status_code=500, detail="Not implemented")
    return await BaseDefaultApi.subclasses[0](repository, db_session).update_lamp(
        lamp_id, lamp_update
    )
