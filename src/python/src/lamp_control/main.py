from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from .application.use_cases import LampUseCases
from .infrastructure.repositories.in_memory import InMemoryLampRepository
from lamp_control_api.api import router as api_router

app = FastAPI(
    title="Lamp Control API",
    description="API for controlling lamps",
    version="0.1.0"
)

# Configure CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Initialize repository and use cases
repository = InMemoryLampRepository()
use_cases = LampUseCases(repository)

# Mount the generated API router
app.include_router(api_router, prefix="/api/v1")

# Store the use cases in the app state for access in route handlers
app.state.use_cases = use_cases

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000) 