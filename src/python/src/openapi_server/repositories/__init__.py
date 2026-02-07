"""Repositories package."""

from .lamp_repository import InMemoryLampRepository, LampNotFoundError
from .postgres_lamp_repository import PostgresLampRepository

__all__ = ["InMemoryLampRepository", "LampNotFoundError", "PostgresLampRepository"]
