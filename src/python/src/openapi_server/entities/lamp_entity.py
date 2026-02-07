"""Domain entities for the lamp control API.

Domain entities represent the core business objects and should be separate
from the HTTP API models to allow independent evolution.
"""

from dataclasses import dataclass, field
from datetime import UTC, datetime


@dataclass(eq=False)
class LampEntity:
    """Domain entity representing a lamp in the internal model.

    This is separate from the HTTP API model to allow independent evolution
    of the internal domain logic and external API contract.

    Equality is based on ID and status only (timestamps are excluded).
    Hashing is based on ID only.
    """

    id: str
    status: bool
    created_at: datetime = field(default_factory=lambda: datetime.now(UTC))
    updated_at: datetime = field(default_factory=lambda: datetime.now(UTC))

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

    def __hash__(self) -> int:
        """Generate hash based on ID."""
        return hash(self.id)
