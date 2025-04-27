"""Tests for logging configuration."""

import json
import logging

import pytest
import structlog

from lamp_control.utils.logging import (
    CorrelationIdFilter,
    bind_logging_context,
    clear_logging_context,
    get_logger,
    setup_logging,
)


def test_setup_logging_json_format(capsys: pytest.CaptureFixture[str]) -> None:
    """Test logging setup with JSON format."""
    setup_logging(json_format=True, log_level="INFO")
    logger = get_logger("test")

    # Get the last processor which should be JSONRenderer
    processors = structlog.get_config()["processors"]
    assert isinstance(processors[-1], structlog.processors.JSONRenderer)

    # Test JSON output
    logger.info("test_message", key="value")
    captured = capsys.readouterr()
    log_entry = json.loads(captured.out)

    assert log_entry["event"] == "test_message"
    assert log_entry["key"] == "value"
    assert "timestamp" in log_entry
    assert log_entry["level"] == "info"


def test_setup_logging_dev_format() -> None:
    """Test logging setup with development format."""
    setup_logging(json_format=False, log_level="DEBUG")

    # Get the last processor which should be ConsoleRenderer
    processors = structlog.get_config()["processors"]
    assert isinstance(processors[-1], structlog.dev.ConsoleRenderer)


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
    assert record.correlation_id == "unknown"  # type: ignore

    # Test with correlation ID in context
    bind_logging_context(correlation_id="test-id")
    correlation_filter.filter(record)
    assert record.correlation_id == "test-id"  # type: ignore
    clear_logging_context()


def test_bind_and_clear_context(capsys: pytest.CaptureFixture[str]) -> None:
    """Test binding and clearing context values."""
    setup_logging(json_format=True)
    logger = get_logger()

    # Test binding context
    bind_logging_context(user_id="123", action="test")
    logger.info("test_event")
    captured = capsys.readouterr()
    log_entry = json.loads(captured.out)
    assert log_entry["user_id"] == "123"
    assert log_entry["action"] == "test"

    # Test clearing context
    clear_logging_context()
    logger.info("another_event")
    captured = capsys.readouterr()
    log_entry = json.loads(captured.out)
    assert "user_id" not in log_entry
    assert "action" not in log_entry


def test_log_levels(capsys: pytest.CaptureFixture[str]) -> None:
    """Test different log levels."""
    setup_logging(json_format=True, log_level="DEBUG")
    logger = get_logger()

    logger.debug("debug_message")
    captured = capsys.readouterr()
    log_entry = json.loads(captured.out)
    assert log_entry["level"] == "debug"

    logger.info("info_message")
    captured = capsys.readouterr()
    log_entry = json.loads(captured.out)
    assert log_entry["level"] == "info"

    logger.warning("warning_message")
    captured = capsys.readouterr()
    log_entry = json.loads(captured.out)
    assert log_entry["level"] == "warning"

    logger.error("error_message")
    captured = capsys.readouterr()
    log_entry = json.loads(captured.out)
    assert log_entry["level"] == "error"
