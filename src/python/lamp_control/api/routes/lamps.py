"""API routes for lamp control."""

from typing import List
from uuid import UUID, uuid4

from fastapi import APIRouter, HTTPException, status

from lamp_control.schemas import Lamp, LampCreate, LampUpdate

# In-memory database for demonstration purposes
# In a real application, this would be replaced with a proper database
LAMPS_DB: dict[UUID, Lamp] = {}

# Create router with prefix that matches the OpenAPI specification
router = APIRouter(prefix="/lamps", tags=["lamps"])


@router.post("", response_model=Lamp, status_code=status.HTTP_201_CREATED)
async def create_lamp(lamp: LampCreate) -> Lamp:
    """
    Create a new lamp.
    
    Args:
        lamp: The lamp data to create
        
    Returns:
        The created lamp
    """
    lamp_id = uuid4()
    db_lamp = Lamp(id=lamp_id, status=lamp.status)
    LAMPS_DB[lamp_id] = db_lamp
    return db_lamp


@router.get("", response_model=List[Lamp])
async def list_lamps() -> List[Lamp]:
    """
    List all lamps.
    
    Returns:
        List of all lamps
    """
    return list(LAMPS_DB.values())


@router.get("/{lamp_id}", response_model=Lamp)
async def get_lamp(lamp_id: UUID) -> Lamp:
    """
    Get a specific lamp by ID.
    
    Args:
        lamp_id: The ID of the lamp to retrieve
        
    Returns:
        The requested lamp
        
    Raises:
        HTTPException: If the lamp is not found
    """
    if lamp_id not in LAMPS_DB:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Lamp with ID {lamp_id} not found"
        )
    return LAMPS_DB[lamp_id]


@router.put("/{lamp_id}", response_model=Lamp)
async def update_lamp(lamp_id: UUID, lamp: LampUpdate) -> Lamp:
    """
    Update a lamp.
    
    Args:
        lamp_id: The ID of the lamp to update
        lamp: The updated lamp data
        
    Returns:
        The updated lamp
        
    Raises:
        HTTPException: If the lamp is not found
    """
    if lamp_id not in LAMPS_DB:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Lamp with ID {lamp_id} not found"
        )
    
    db_lamp = LAMPS_DB[lamp_id]
    db_lamp.status = lamp.status
    
    return db_lamp


@router.delete("/{lamp_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_lamp(lamp_id: UUID) -> None:
    """
    Delete a lamp.
    
    Args:
        lamp_id: The ID of the lamp to delete
        
    Raises:
        HTTPException: If the lamp is not found
    """
    if lamp_id not in LAMPS_DB:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Lamp with ID {lamp_id} not found"
        )
    
    del LAMPS_DB[lamp_id]