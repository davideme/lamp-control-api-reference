# coding: utf-8

from typing import ClassVar, Dict, List, Tuple  # noqa: F401

from pydantic import StrictStr
from typing import Any, List
from openapi_server.models.lamp import Lamp
from openapi_server.models.lamp_create import LampCreate
from openapi_server.models.lamp_update import LampUpdate


class BaseDefaultApi:
    subclasses: ClassVar[Tuple] = ()

    def __init_subclass__(cls, **kwargs):
        super().__init_subclass__(**kwargs)
        BaseDefaultApi.subclasses = BaseDefaultApi.subclasses + (cls,)
    async def create_lamp(
        self,
        lamp_create: LampCreate,
    ) -> Lamp:
        ...


    async def delete_lamp(
        self,
        lampId: StrictStr,
    ) -> None:
        ...


    async def get_lamp(
        self,
        lampId: StrictStr,
    ) -> Lamp:
        ...


    async def list_lamps(
        self,
    ) -> List[Lamp]:
        ...


    async def update_lamp(
        self,
        lampId: StrictStr,
        lamp_update: LampUpdate,
    ) -> Lamp:
        ...
