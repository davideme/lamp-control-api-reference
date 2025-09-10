"""Mappers to convert between domain entities and API models.

This separation allows the internal domain model to evolve independently
from the external API contract.
"""

from src.openapi_server.entities.lamp_entity import LampEntity
from src.openapi_server.models.lamp import Lamp
from src.openapi_server.models.lamp_create import LampCreate
from src.openapi_server.models.lamp_update import LampUpdate


class LampMapper:
    """Mapper to convert between domain entities and API models."""

    @staticmethod
    def to_api_model(entity: LampEntity) -> Lamp:
        """Convert from domain entity to API model.

        Args:
            entity: Domain entity to convert

        Returns:
            API model representation
        """
        return Lamp(
            id=entity.id,
            status=entity.status,
            created_at=entity.created_at,
            updated_at=entity.updated_at,
        )

    @staticmethod
    def to_domain_entity(api_model: Lamp) -> LampEntity:
        """Convert from API model to domain entity.

        Args:
            api_model: API model to convert

        Returns:
            Domain entity representation
        """
        return LampEntity(
            id=api_model.id,
            status=api_model.status,
            created_at=api_model.created_at,
            updated_at=api_model.updated_at,
        )

    @staticmethod
    def create_from_api_model(lamp_create: LampCreate, lamp_id: str) -> LampEntity:
        """Create a domain entity from API create model.

        Args:
            lamp_create: API create model
            lamp_id: Generated ID for the lamp

        Returns:
            New domain entity
        """
        return LampEntity(
            id=lamp_id,
            status=lamp_create.status,
        )

    @staticmethod
    def update_from_api_model(entity: LampEntity, lamp_update: LampUpdate) -> LampEntity:
        """Update a domain entity from API update model.

        Args:
            entity: Existing domain entity
            lamp_update: API update model

        Returns:
            Updated domain entity
        """
        entity.update_status(lamp_update.status)
        return entity
