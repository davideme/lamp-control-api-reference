from typing import List, Optional
from uuid import UUID

from ..domain.entities import Lamp
from ..interfaces.repositories import LampRepository

class LampUseCases:
    """Application use cases for lamp operations."""
    
    def __init__(self, repository: LampRepository):
        self.repository = repository

    async def get_lamp(self, lamp_id: UUID) -> Optional[Lamp]:
        """Get a lamp by its ID."""
        return await self.repository.get_by_id(lamp_id)

    async def get_all_lamps(self) -> List[Lamp]:
        """Get all lamps."""
        return await self.repository.get_all()

    async def create_lamp(
        self,
        name: str,
        status: bool = False,
        brightness: int = 100,
        color: str = "#FFFFFF"
    ) -> Lamp:
        """Create a new lamp."""
        lamp = Lamp.create(
            name=name,
            status=status,
            brightness=brightness,
            color=color
        )
        return await self.repository.create(lamp)

    async def update_lamp(
        self,
        lamp_id: UUID,
        name: Optional[str] = None,
        status: Optional[bool] = None,
        brightness: Optional[int] = None,
        color: Optional[str] = None
    ) -> Optional[Lamp]:
        """Update an existing lamp."""
        lamp = await self.repository.get_by_id(lamp_id)
        if not lamp:
            return None
            
        updated_lamp = lamp.update(
            name=name,
            status=status,
            brightness=brightness,
            color=color
        )
        return await self.repository.update(updated_lamp)

    async def delete_lamp(self, lamp_id: UUID) -> bool:
        """Delete a lamp by its ID."""
        return await self.repository.delete(lamp_id) 