"""Dependency injection functions for FastAPI."""

from src.openapi_server.repositories.lamp_repository import LampRepository

repository = LampRepository()


def get_lamp_repository() -> LampRepository:
    """Get a lamp repository instance.

    Returns:
        A lamp repository instance.
    """
    return repository
