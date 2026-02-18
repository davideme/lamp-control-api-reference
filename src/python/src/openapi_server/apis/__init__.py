"""APIs package."""

from typing import TYPE_CHECKING, Any

if TYPE_CHECKING:
    from .default_api import router
    from .default_api_base import BaseDefaultApi

__all__ = ["BaseDefaultApi", "router"]


def __getattr__(name: str) -> Any:
    if name == "router":
        from .default_api import router as _router

        return _router
    if name == "BaseDefaultApi":
        from .default_api_base import BaseDefaultApi as _BaseDefaultApi

        return _BaseDefaultApi

    raise AttributeError(f"module {__name__!r} has no attribute {name!r}")
