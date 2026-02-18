"""SQLAlchemy ORM models for the Lamp Control API.

These models map to the PostgreSQL schema defined in database/sql/postgresql/schema.sql.
They use SQLAlchemy 2.0 syntax with modern type annotations.
"""

from datetime import datetime
from uuid import UUID

from sqlalchemy import Boolean, DateTime
from sqlalchemy.dialects.postgresql import UUID as PGUUID
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column


class Base(DeclarativeBase):
    """Base class for all SQLAlchemy models."""

    pass


class LampModel(Base):
    """SQLAlchemy model for the lamps table.

    This model matches the PostgreSQL schema and includes:
    - UUID primary key
    - Boolean status field (is_on)
    - Timestamps with timezone support
    - Soft delete support via deleted_at column

    The updated_at column is automatically updated by a PostgreSQL trigger
    defined in the schema.sql file.
    """

    __tablename__ = "lamps"

    id: Mapped[UUID] = mapped_column(PGUUID(as_uuid=True), primary_key=True)
    is_on: Mapped[bool] = mapped_column(Boolean, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    updated_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    deleted_at: Mapped[datetime | None] = mapped_column(
        DateTime(timezone=True), nullable=True, default=None
    )

    def __repr__(self) -> str:
        """String representation of the lamp model."""
        return f"<LampModel(id={self.id}, is_on={self.is_on}, deleted_at={self.deleted_at})>"
