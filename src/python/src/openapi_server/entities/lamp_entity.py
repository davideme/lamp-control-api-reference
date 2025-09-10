"""Domain entities for the lamp control API.

Domain entities represent the core business objects and should be separate
from the HTTP API models to allow independent evolution.
"""

from datetime import UTC, datetime


class LampEntity:
    """Domain entity representing a lamp in the internal model.

    This is separate from the HTTP API model to allow independent evolution
    of the internal domain logic and external API contract.
    """

    def __init__(
        self,
        id: str,
        status: bool,
        created_at: datetime | None = None,
        updated_at: datetime | None = None,
    ) -> None:
        """Initialize a lamp entity.

        Args:
            id: Unique identifier for the lamp
            status: Whether the lamp is on (True) or off (False)
            created_at: When the lamp was created (defaults to now)
            updated_at: When the lamp was last updated (defaults to now)
        """
        self.id = id
        self.status = status
        self.created_at = created_at or datetime.now(UTC)
        self.updated_at = updated_at or datetime.now(UTC)

    def update_status(self, status: bool) -> None:
        """Update the lamp status and timestamp.

        Args:
            status: New status for the lamp
        """
        self.status = status
        self.updated_at = datetime.now(UTC)

    def __eq__(self, other: object) -> bool:
        """Check equality based on ID and status."""
        if not isinstance(other, LampEntity):
            return False
        return self.id == other.id and self.status == other.status

    def __repr__(self) -> str:
        """String representation of the lamp entity."""
        return (
            f"LampEntity(id={self.id!r}, status={self.status!r}, "
            f"created_at={self.created_at!r}, updated_at={self.updated_at!r})"
        )
