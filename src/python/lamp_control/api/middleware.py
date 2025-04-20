"""Middleware for FastAPI application."""

import uuid
from collections.abc import Callable

from fastapi import FastAPI, Request, Response
from starlette.middleware.base import BaseHTTPMiddleware, RequestResponseEndpoint

from lamp_control.utils.logging import bind_logging_context, clear_logging_context


class CorrelationIdMiddleware(BaseHTTPMiddleware):
    """Middleware to add correlation ID to each request."""

    def __init__(
        self,
        app: FastAPI,
        header_name: str = "X-Correlation-ID",
        context_key: str = "correlation_id",
        generate_id: Callable[[], str] = lambda: str(uuid.uuid4()),
    ) -> None:
        """
        Initialize the middleware.

        Args:
            app: FastAPI application
            header_name: Name of the header to look for/set correlation ID
            context_key: Key to use in the logging context
            generate_id: Function to generate correlation IDs
        """
        super().__init__(app)
        self.header_name = header_name
        self.context_key = context_key
        self.generate_id = generate_id

    async def dispatch(self, request: Request, call_next: RequestResponseEndpoint) -> Response:
        """
        Process the request/response cycle.

        Args:
            request: The incoming request
            call_next: The next middleware/endpoint to call

        Returns:
            The response from the next middleware/endpoint
        """
        # Clear any existing context
        clear_logging_context()

        # Get or generate correlation ID
        correlation_id = request.headers.get(self.header_name) or self.generate_id()

        # Add correlation ID to context
        bind_logging_context(**{self.context_key: correlation_id})

        # Add request information to context
        bind_logging_context(
            http_method=request.method,
            url=str(request.url),
            client_ip=request.client.host if request.client else None,
            user_agent=request.headers.get("User-Agent"),
        )

        # Process the request
        response = await call_next(request)

        # Add correlation ID to response headers
        response.headers[self.header_name] = correlation_id

        return response


def setup_middleware(app: FastAPI) -> None:
    """
    Set up all middleware for the FastAPI application.

    Args:
        app: FastAPI application instance
    """
    app.add_middleware(CorrelationIdMiddleware)