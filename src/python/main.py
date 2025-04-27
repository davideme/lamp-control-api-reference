#!/usr/bin/env python
"""Run the FastAPI application."""

import uvicorn

from lamp_control.api.app import create_app

app = create_app(debug=True)

if __name__ == "__main__":
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=8000,
        reload=True,
        log_level="debug",
    )