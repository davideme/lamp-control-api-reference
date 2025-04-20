"""Tests for logging configuration."""

import json
import logging
from collections.abc import Generator
from typing import Any
from unittest.mock import patch

import pytest
import structlog

from lamp_control.utils.logging import (
    CorrelationIdFilter,
    bind_logging_context,
    clear_logging_context,
    get_logger,
    setup_logging,
)


@pytest.fixture
def capture_logs() -> Generator[list[dict[str, Any]], None, None]:
    """Fixture to capture log output for testing."""
    output: list[dict[str, Any]] = []

    def capture(_, __, event_dict: dict[str, Any]) -> dict[str, Any]:
        output.append(event_dict)
        return event_dict

    processors = structlog.get_config()["processors"]
    processors.insert(-1, capture)
    yield output
    processors.remove(capture)


def test_setup_logging_json_format() -> None:
    """Test logging setup with JSON format."""
    setup_logging(json_format=True, log_level="INFO")
    logger = get_logger("test")

    # Get the last processor which should be JSONRenderer
    processors = structlog.get_config()["processors"]
    assert isinstance(processors[-1], structlog.processors.JSONRenderer)

    # Test JSON output
    with patch("sys.stdout") as mock_stdout:
        logger.info("test_message", key="value")
        output = mock_stdout.write.call_args[0][0]
        log_entry = json.loads(output)
        assert log_entry["event"] == "test_message"
        assert log_entry["key"] == "value"
        assert "timestamp" in log_entry
        assert log_entry["log_level"] == "info"
        assert log_entry["logger"] == "test"


def test_setup_logging_dev_format() -> None:
    """Test logging setup with development format."""
    setup_logging(json_format=False, log_level="DEBUG")
    # Get the last processor which should be ConsoleRenderer
    processors = structlog.get_config()["processors"]
    assert isinstance(processors[-1], structlog.dev.ConsoleRenderer)


def test_get_logger_with_name() -> None:
    """Test getting a logger with a specific name."""
    setup_logging()
    logger = get_logger("test_logger")
    assert isinstance(logger, structlog.BoundLogger)
    assert logger._context.get("logger") == "test_logger"  # type: ignore


def test_correlation_id_filter() -> None:
    """Test the correlation ID filter."""
    correlation_filter = CorrelationIdFilter()
    record = logging.LogRecord(
        name="test",
        level=logging.INFO,
        pathname="test.py",
        lineno=1,
        msg="test message",
        args=(),
        exc_info=None,
    )

    # Test with no correlation ID in context
    correlation_filter.filter(record)
    assert record.correlation_id == "unknown"

    # Test with correlation ID in context
    bind_logging_context(correlation_id="test-id")
    correlation_filter.filter(record)
    assert record.correlation_id == "test-id"
    clear_logging_context()


def test_bind_and_clear_context(capture_logs: list[dict[str, Any]]) -> None:
    """Test binding and clearing context values."""
    setup_logging()
    logger = get_logger()

    # Test binding context
    bind_logging_context(user_id="123", action="test")
    logger.info("test_event")
    assert capture_logs[-1]["user_id"] == "123"
    assert capture_logs[-1]["action"] == "test"

    # Test clearing context
    clear_logging_context()
    logger.info("another_event")
    assert "user_id" not in capture_logs[-1]
    assert "action" not in capture_logs[-1]


def test_log_levels(capture_logs: list[dict[str, Any]]) -> None:
    """Test different log levels."""
    setup_logging(log_level="DEBUG")
    logger = get_logger()

    logger.debug("debug_message")
    assert capture_logs[-1]["log_level"] == "debug"

    logger.info("info_message")
    assert capture_logs[-1]["log_level"] == "info"

    logger.warning("warning_message")
    assert capture_logs[-1]["log_level"] == "warning"

    logger.error("error_message")
    assert capture_logs[-1]["log_level"] == "error"


def test_exception_logging(capture_logs: list[dict[str, Any]]) -> None:
    """Test exception logging."""
    setup_logging()
    logger = get_logger()

    try:
        raise ValueError("test error")
    except ValueError:
        logger.exception("error_occurred")

    assert capture_logs[-1]["log_level"] == "error"
    assert "ValueError: test error" in str(capture_logs[-1]["exc_info"])
