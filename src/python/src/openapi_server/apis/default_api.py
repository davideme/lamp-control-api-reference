# coding: utf-8

from typing import Dict, List  # noqa: F401
import importlib
import pkgutil

from openapi_server.apis.default_api_base import BaseDefaultApi
import src.openapi_server.impl

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

from openapi_server.models.extra_models import TokenModel  # noqa: F401
from pydantic import Field, StrictStr
from typing import Any, Optional
from typing_extensions import Annotated
from openapi_server.models.error import Error
from openapi_server.models.lamp import Lamp
from openapi_server.models.lamp_create import LampCreate
from openapi_server.models.lamp_update import LampUpdate
from openapi_server.models.list_lamps200_response import ListLamps200Response


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
    lamp_create: LampCreate = Body(None, description=""),
) -> Lamp:
    if not BaseDefaultApi.subclasses:
        raise HTTPException(status_code=500, detail="Not implemented")
    return await BaseDefaultApi.subclasses[0]().create_lamp(lamp_create)


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
    lampId: StrictStr = Path(..., description=""),
) -> None:
    if not BaseDefaultApi.subclasses:
        raise HTTPException(status_code=500, detail="Not implemented")
    return await BaseDefaultApi.subclasses[0]().delete_lamp(lampId)


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
    lampId: StrictStr = Path(..., description=""),
) -> Lamp:
    if not BaseDefaultApi.subclasses:
        raise HTTPException(status_code=500, detail="Not implemented")
    return await BaseDefaultApi.subclasses[0]().get_lamp(lampId)


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
    cursor: Optional[StrictStr] = Query(None, description="", alias="cursor"),
    page_size: Optional[Annotated[int, Field(le=100, strict=True, ge=1)]] = Query(
        25, description="", alias="pageSize", ge=1, le=100
    ),
) -> ListLamps200Response:
    if not BaseDefaultApi.subclasses:
        raise HTTPException(status_code=500, detail="Not implemented")
    return await BaseDefaultApi.subclasses[0]().list_lamps(cursor, page_size)


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
    lampId: StrictStr = Path(..., description=""),
    lamp_update: LampUpdate = Body(None, description=""),
) -> Lamp:
    if not BaseDefaultApi.subclasses:
        raise HTTPException(status_code=500, detail="Not implemented")
    return await BaseDefaultApi.subclasses[0]().update_lamp(lampId, lamp_update)
