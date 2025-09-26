#!/usr/bin/env python3
"""
Launcher script for FastAPI application with Cloud Run PORT support.
"""
import os
import sys


def main():
    port = os.environ.get("PORT", "80")

    # Build the fastapi run command
    cmd = [
        sys.executable,
        "-m",
        "fastapi",
        "run",
        "src/openapi_server/main.py",
        "--port",
        port,
        "--host",
        "0.0.0.0",
    ]

    # Execute the command, replacing the current process
    os.execv(sys.executable, cmd)


if __name__ == "__main__":
    main()
