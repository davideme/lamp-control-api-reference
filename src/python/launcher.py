#!/usr/bin/env python3
"""
Launcher script for FastAPI application with Cloud Run PORT support.
"""
import os

import uvicorn


def main() -> None:
    port = int(os.environ.get("PORT", "80"))
    workers = int(os.environ.get("WORKERS", "3"))

    # Start the FastAPI application directly with uvicorn
    # This is more reliable than using the `fastapi run` command in containers
    uvicorn.run(
        "src.openapi_server.main:app",
        host="0.0.0.0",
        port=port,
        workers=workers,
        log_level="info",
    )


if __name__ == "__main__":
    main()
