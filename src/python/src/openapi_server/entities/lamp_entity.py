"""Domain entities for the lamp control API.

Domain entities represent the core business objects and should be separate
from the HTTP API models to allow independent evolution.
"""

from __future__ import annotations

from dataclasses import dataclass, field
from datetime import datetime


@dataclass(eq=False)
class LampEntity:
    """Domain entity representing a lamp in the internal model.

    This is separate from the HTTP API model to allow independent evolution
    of the internal domain logic and external API contract.

    Equality is based on ID and status only (timestamps are excluded).
    Hashing is based on ID only.

    Timestamps are managed by the database (DEFAULT CURRENT_TIMESTAMP and
    BEFORE UPDATE trigger) and populated from the RETURNING clause after
    each write. Before a DB write, both fields are None.
    """

    id: str
    status: bool
    created_at: datetime | None = field(default=None)
    updated_at: datetime | None = field(default=None)

    def update_status(self, status: bool) -> None:
        """Update the lamp status.

        Args:
            status: New status for the lamp
        """
        self.status = status

    def __eq__(self, other: object) -> bool:
        """Check equality based on ID and status."""
        if not isinstance(other, LampEntity):
            return False
        return self.id == other.id and self.status == other.status

    def __hash__(self) -> int:
        """Generate hash based on ID."""
        return hash(self.id)
