"""FastAPI application configuration."""

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from lamp_control.api.middleware import setup_middleware
from lamp_control.utils.logging import get_logger, setup_logging

logger = get_logger(__name__)


def create_app(
    *,
    debug: bool = False,
    enable_cors: bool = True,
) -> FastAPI:
    """
    Create and configure the FastAPI application.

    Args:
        debug: Enable debug mode (default: False)
        enable_cors: Enable CORS middleware (default: True)

    Returns:
        Configured FastAPI application
    """
    # Configure logging first
    setup_logging(
        json_format=not debug,  # Use JSON in production, pretty print in debug
        log_level="DEBUG" if debug else "INFO",
    )

    logger.info("creating_application", debug=debug, enable_cors=enable_cors)

    app = FastAPI(
        title="Lamp Control API",
        description="API for controlling smart lamps",
        version="0.1.0",
        debug=debug,
    )

    # Set up middleware
    setup_middleware(app)

    if enable_cors:
        app.add_middleware(
            CORSMiddleware,
            allow_origins=["*"],  # Configure appropriately for production
            allow_credentials=True,
            allow_methods=["*"],
            allow_headers=["*"],
        )

    # Log application startup
    @app.on_event("startup")
    async def startup_event() -> None:
        """Log application startup."""
        logger.info("application_started")

    # Log application shutdown
    @app.on_event("shutdown")
    async def shutdown_event() -> None:
        """Log application shutdown."""
        logger.info("application_shutdown")

    return app
