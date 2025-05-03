from dataclasses import dataclass
from datetime import datetime
from typing import Optional
from uuid import UUID, uuid4

@dataclass(frozen=True)
class Lamp:
    """Domain entity representing a lamp."""
    id: UUID
    name: str
    status: bool
    brightness: int
    color: str
    created_at: datetime
    updated_at: datetime

    @classmethod
    def create(
        cls,
        name: str,
        status: bool = False,
        brightness: int = 100,
        color: str = "#FFFFFF"
    ) -> "Lamp":
        """Factory method to create a new lamp."""
        now = datetime.utcnow()
        return cls(
            id=uuid4(),
            name=name,
            status=status,
            brightness=min(max(brightness, 0), 100),
            color=color,
            created_at=now,
            updated_at=now
        )

    def update(
        self,
        name: Optional[str] = None,
        status: Optional[bool] = None,
        brightness: Optional[int] = None,
        color: Optional[str] = None
    ) -> "Lamp":
        """Create a new instance with updated values."""
        return Lamp(
            id=self.id,
            name=name if name is not None else self.name,
            status=status if status is not None else self.status,
            brightness=min(max(brightness, 0), 100) if brightness is not None else self.brightness,
            color=color if color is not None else self.color,
            created_at=self.created_at,
            updated_at=datetime.utcnow()
        ) 