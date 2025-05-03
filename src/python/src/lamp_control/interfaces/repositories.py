from abc import ABC, abstractmethod
from typing import List, Optional
from uuid import UUID

from ..domain.entities import Lamp

class LampRepository(ABC):
    """Interface for lamp repository operations."""
    
    @abstractmethod
    async def get_by_id(self, lamp_id: UUID) -> Optional[Lamp]:
        """Get a lamp by its ID."""
        pass

    @abstractmethod
    async def get_all(self) -> List[Lamp]:
        """Get all lamps."""
        pass

    @abstractmethod
    async def create(self, lamp: Lamp) -> Lamp:
        """Create a new lamp."""
        pass

    @abstractmethod
    async def update(self, lamp: Lamp) -> Lamp:
        """Update an existing lamp."""
        pass

    @abstractmethod
    async def delete(self, lamp_id: UUID) -> bool:
        """Delete a lamp by its ID."""
        pass 