"""API routes package."""

from lamp_control.api.routes.lamps import router as lamps_router

__all__ = ["lamps_router"]