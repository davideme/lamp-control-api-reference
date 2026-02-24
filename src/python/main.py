"""Entrypoint shim for Cloud Run / buildpack deployments.

This file enables the standard `uvicorn main:app` invocation expected by
Google Cloud Buildpacks and documented Cloud Run FastAPI patterns.
"""

from src.openapi_server.main import app  # noqa: F401
