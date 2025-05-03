# coding: utf-8

from typing import Dict, List  # noqa: F401
import importlib
import pkgutil

from openapi_server.apis.default_api_base import BaseDefaultApi
import openapi_server.impl

from fastapi import (  # noqa: F401
    APIRouter,
    Body,
    Cookie,
    Depends,
    Form,
    Header,
    Path,
    Query,
    Response,
    Security,
    status,
)

from openapi_server.models.extra_models import TokenModel  # noqa: F401
from openapi_server.models.lamp import Lamp
from openapi_server.models.lamp_create import LampCreate
from openapi_server.models.lamp_update import LampUpdate


router = APIRouter()

ns_pkg = openapi_server.impl
for _, name, _ in pkgutil.iter_modules(ns_pkg.__path__, ns_pkg.__name__ + "."):
    importlib.import_module(name)


@router.post(
    "/lamps",
    responses={
        201: {"model": Lamp, "description": "Lamp created successfully"},
    },
    tags=["default"],
    summary="Create a new lamp",
    response_model_by_alias=True,
)
async def create_lamp(
    lamp_create: LampCreate = Body(None, description=""),
) -> Lamp:
    ...


@router.delete(
    "/lamps/{lampId}",
    responses={
        204: {"description": "Lamp deleted successfully"},
        404: {"description": "Lamp not found"},
    },
    tags=["default"],
    summary="Delete a lamp",
    response_model_by_alias=True,
)
async def delete_lamp(
    lampId: str = Path(..., description=""),
) -> None:
    ...


@router.get(
    "/lamps/{lampId}",
    responses={
        200: {"model": Lamp, "description": "Lamp details"},
        404: {"description": "Lamp not found"},
    },
    tags=["default"],
    summary="Get a specific lamp",
    response_model_by_alias=True,
)
async def get_lamp(
    lampId: str = Path(..., description=""),
) -> Lamp:
    ...


@router.get(
    "/lamps",
    responses={
        200: {"model": List[Lamp], "description": "A list of lamps"},
    },
    tags=["default"],
    summary="List all lamps",
    response_model_by_alias=True,
)
async def list_lamps(
) -> List[Lamp]:
    ...


@router.put(
    "/lamps/{lampId}",
    responses={
        200: {"model": Lamp, "description": "Lamp updated successfully"},
        404: {"description": "Lamp not found"},
    },
    tags=["default"],
    summary="Update a lamp&#39;s status",
    response_model_by_alias=True,
)
async def update_lamp(
    lampId: str = Path(..., description=""),
    lamp_update: LampUpdate = Body(None, description=""),
) -> Lamp:
    ...
