"""Command-line interface for Lamp Control API.

This module provides CLI commands for running the application in different modes:
- serve-only: Start server without running migrations (default)
- serve: Run migrations and start the server
- migrate: Run migrations only
"""

import argparse
import logging
import os
import sys
from pathlib import Path

import uvicorn

from alembic import command
from alembic.config import Config
from src.openapi_server.infrastructure.config import DatabaseSettings

logger = logging.getLogger(__name__)


def run_migrations_only():
    """Run database migrations only and exit."""
    logger.info("Running migrations only...")

    settings = DatabaseSettings()
    if not settings.use_postgres():
        logger.warning("No PostgreSQL configuration found, nothing to migrate")
        return

    logger.info(f"Running migrations for database: {settings.database_url}")

    try:
        # Get the alembic.ini path (relative to this file)
        alembic_ini = Path(__file__).parent.parent.parent / "alembic.ini"
        if not alembic_ini.exists():
            logger.error(f"alembic.ini not found at {alembic_ini}")
            sys.exit(1)

        # Create Alembic config
        alembic_cfg = Config(str(alembic_ini))
        alembic_cfg.set_main_option("sqlalchemy.url", settings.database_url)

        # Run migrations
        command.upgrade(alembic_cfg, "head")

        logger.info("Migrations completed successfully")

    except Exception as e:
        logger.error(f"Migration failed: {e}")
        sys.exit(1)


def start_server(run_migrations: bool = True):
    """Start the FastAPI server.

    Args:
        run_migrations: Whether to run migrations before starting the server
    """
    if run_migrations:
        logger.info("Starting server with automatic migrations...")
        settings = DatabaseSettings()
        if settings.use_postgres():
            try:
                alembic_ini = Path(__file__).parent.parent.parent / "alembic.ini"
                if alembic_ini.exists():
                    alembic_cfg = Config(str(alembic_ini))
                    alembic_cfg.set_main_option("sqlalchemy.url", settings.database_url)
                    command.upgrade(alembic_cfg, "head")
                    logger.info("Migrations completed")
                else:
                    logger.warning(f"alembic.ini not found at {alembic_ini}, skipping migrations")
            except Exception as e:
                logger.error(f"Migration failed: {e}")
                sys.exit(1)
    else:
        logger.info("Starting server without running migrations...")

    # Start uvicorn server
    uvicorn.run(
        "src.openapi_server.main:app",
        host="0.0.0.0",
        port=int(os.getenv("PORT", "8080")),
        log_level="info",
    )


def main():
    """Main CLI entry point."""
    parser = argparse.ArgumentParser(
        description="Lamp Control API - FastAPI application for controlling lamps"
    )
    parser.add_argument(
        "--mode",
        choices=["serve", "migrate", "serve-only"],
        default="serve-only",
        help="Operation mode: serve-only (default, start server without migrations), "
        "serve (migrate and start server), migrate (run migrations only)",
    )
    parser.add_argument(
        "--log-level",
        choices=["DEBUG", "INFO", "WARNING", "ERROR", "CRITICAL"],
        default="INFO",
        help="Logging level (default: INFO)",
    )

    args = parser.parse_args()

    # Configure logging
    logging.basicConfig(
        level=getattr(logging, args.log_level),
        format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
    )

    # Execute based on mode
    if args.mode == "migrate":
        run_migrations_only()
    elif args.mode == "serve":
        start_server(run_migrations=True)
    elif args.mode == "serve-only":
        start_server(run_migrations=False)


if __name__ == "__main__":
    main()
