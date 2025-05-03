from typing import Dict, List, Optional
from uuid import UUID

from ...domain.entities import Lamp
from ...interfaces.repositories import LampRepository

class InMemoryLampRepository(LampRepository):
    """In-memory implementation of the lamp repository."""
    
    def __init__(self):
        self._lamps: Dict[UUID, Lamp] = {}

    async def get_by_id(self, lamp_id: UUID) -> Optional[Lamp]:
        """Get a lamp by its ID."""
        return self._lamps.get(lamp_id)

    async def get_all(self) -> List[Lamp]:
        """Get all lamps."""
        return list(self._lamps.values())

    async def create(self, lamp: Lamp) -> Lamp:
        """Create a new lamp."""
        self._lamps[lamp.id] = lamp
        return lamp

    async def update(self, lamp: Lamp) -> Lamp:
        """Update an existing lamp."""
        if lamp.id not in self._lamps:
            raise ValueError(f"Lamp with ID {lamp.id} not found")
        self._lamps[lamp.id] = lamp
        return lamp

    async def delete(self, lamp_id: UUID) -> bool:
        """Delete a lamp by its ID."""
        if lamp_id not in self._lamps:
            return False
        del self._lamps[lamp_id]
        return True 