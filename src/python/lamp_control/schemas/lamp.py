"""Schemas for lamp-related API endpoints."""

from uuid import UUID

from pydantic import BaseModel, Field


class LampBase(BaseModel):
    """Base schema with common lamp attributes."""

    status: bool = Field(description="Whether the lamp is turned on (true) or off (false)")


class LampCreate(LampBase):
    """Schema for creating a new lamp."""

    pass


class LampUpdate(LampBase):
    """Schema for updating an existing lamp."""

    pass


class Lamp(LampBase):
    """Schema for a lamp resource."""

    id: UUID = Field(description="Unique identifier for the lamp")

    model_config = {
        "from_attributes": True,
        "populate_by_name": True,
    }