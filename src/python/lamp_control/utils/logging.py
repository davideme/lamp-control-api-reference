"""
Logging configuration using structlog.

This module configures structured logging with the following features:
- JSON output format for production
- Pretty printing for development
- Correlation IDs for request tracking
- Timestamp in ISO 8601 format
- Log level configuration
- Standard Python logging integration
- Exception formatting
"""

import logging
import sys
from typing import Any, cast

import structlog
from structlog.types import Processor


def setup_logging(
    *,
    json_format: bool = True,
    log_level: str = "INFO",
    enable_stdlib_logging: bool = True,
) -> None:
    """
    Configure structured logging with the given parameters.

    Args:
        json_format: Whether to output logs in JSON format (default: True)
        log_level: The minimum log level to output (default: "INFO")
        enable_stdlib_logging: Whether to configure stdlib logging (default: True)
    """
    # Configure standard library logging if requested
    if enable_stdlib_logging:
        logging.basicConfig(
            format="%(message)s",
            stream=sys.stdout,
            level=log_level,
        )

    # Default settings
    processors: list[Processor] = [
        structlog.contextvars.merge_contextvars,
        structlog.processors.add_log_level,
        structlog.processors.StackInfoRenderer(),
        structlog.dev.set_exc_info,
        structlog.processors.TimeStamper(fmt="%Y-%m-%d %H:%M:%S", utc=False),
    ]

    if json_format:
        # Production format: JSON
        processors.extend(
            [
                structlog.processors.JSONRenderer(),
            ]
        )
    else:
        # Development format: Pretty printing
        processors.extend(
            [
                structlog.dev.ConsoleRenderer(),
            ]
        )

    structlog.configure(
        processors=processors,
        wrapper_class=structlog.make_filtering_bound_logger(logging.NOTSET),
        context_class=dict,
        logger_factory=structlog.PrintLoggerFactory(),
        cache_logger_on_first_use=False
    )

def get_logger(name: str = "") -> structlog.stdlib.BoundLogger:
    """
    Get a logger instance with the given name.

    Args:
        name: The name for the logger (default: "")

    Returns:
        A configured structlog logger instance
    """
    return cast(structlog.stdlib.BoundLogger, structlog.get_logger(name))


class CorrelationIdFilter(logging.Filter):
    """Filter that adds correlation ID from context to log records."""

    def __init__(self, correlation_id_key: str = "correlation_id"):
        super().__init__()
        self.correlation_id_key = correlation_id_key

    def filter(self, record: logging.LogRecord) -> bool:
        """Add correlation ID to the log record if available in the context."""
        ctx = structlog.contextvars.get_contextvars()
        correlation_id = ctx.get(self.correlation_id_key, "unknown")
        record.correlation_id = correlation_id
        return True


def clear_logging_context() -> None:
    """Clear all thread-local logging context."""
    structlog.contextvars.clear_contextvars()


def bind_logging_context(**kwargs: Any) -> None:
    """
    Bind values to the thread-local logging context.

    Args:
        **kwargs: Key-value pairs to bind to the context
    """
    structlog.contextvars.bind_contextvars(**kwargs)
