
from typing import ClassVar, Dict, List, Tuple  # noqa: F401

from lamp_control_api.models.lamp import Lamp
from lamp_control_api.models.lamp_create import LampCreate
from lamp_control_api.models.lamp_update import LampUpdate


class BaseDefaultApi:
    subclasses: ClassVar[tuple] = ()

    def __init_subclass__(cls, **kwargs):
        super().__init_subclass__(**kwargs)
        BaseDefaultApi.subclasses = BaseDefaultApi.subclasses + (cls,)

    def create_lamp(
        self,
        lamp_create: LampCreate,
    ) -> Lamp: ...

    def delete_lamp(
        self,
        lampId: str,
    ) -> None: ...

    def get_lamp(
        self,
        lampId: str,
    ) -> Lamp: ...

    def list_lamps(
        self,
    ) -> list[Lamp]: ...

    def update_lamp(
        self,
        lampId: str,
        lamp_update: LampUpdate,
    ) -> Lamp: ...
