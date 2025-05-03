# coding: utf-8

from typing import ClassVar, Dict, List, Tuple  # noqa: F401

from openapi_server.models.lamp import Lamp
from openapi_server.models.lamp_create import LampCreate
from openapi_server.models.lamp_update import LampUpdate


class BaseDefaultApi:
    subclasses: ClassVar[Tuple] = ()

    def __init_subclass__(cls, **kwargs):
        super().__init_subclass__(**kwargs)
        BaseDefaultApi.subclasses = BaseDefaultApi.subclasses + (cls,)
    def create_lamp(
        self,
        lamp_create: LampCreate,
    ) -> Lamp:
        ...


    def delete_lamp(
        self,
        lampId: str,
    ) -> None:
        ...


    def get_lamp(
        self,
        lampId: str,
    ) -> Lamp:
        ...


    def list_lamps(
        self,
    ) -> List[Lamp]:
        ...


    def update_lamp(
        self,
        lampId: str,
        lamp_update: LampUpdate,
    ) -> Lamp:
        ...
