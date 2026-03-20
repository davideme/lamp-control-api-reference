import logging
from unittest.mock import patch

from opentelemetry.sdk.trace import TracerProvider
from pythonjsonlogger import jsonlogger

from src.openapi_server.telemetry import configure_telemetry


class TestConfigureTelemetry:
    def test_no_op_without_endpoint(self, monkeypatch):
        """configure_telemetry does not set up providers when endpoint is absent."""
        monkeypatch.delenv("OTEL_EXPORTER_OTLP_ENDPOINT", raising=False)

        with patch("opentelemetry.trace.set_tracer_provider") as mock_set:
            configure_telemetry()
            mock_set.assert_not_called()

    def test_sets_w3c_propagators(self, monkeypatch):
        """W3C TraceContext + Baggage propagation is always configured."""
        monkeypatch.delenv("OTEL_EXPORTER_OTLP_ENDPOINT", raising=False)

        with patch("src.openapi_server.telemetry.set_global_textmap") as mock_propagator:
            configure_telemetry()
            mock_propagator.assert_called_once()

    def test_configures_tracer_provider_when_endpoint_set(self, monkeypatch):
        """TracerProvider is configured when OTEL_EXPORTER_OTLP_ENDPOINT is set."""
        monkeypatch.setenv("OTEL_EXPORTER_OTLP_ENDPOINT", "http://localhost:4317")
        monkeypatch.delenv("OTEL_SERVICE_NAME", raising=False)

        with (
            patch("opentelemetry.trace.set_tracer_provider") as mock_set,
            patch("src.openapi_server.telemetry.OTLPSpanExporter"),
            patch("src.openapi_server.telemetry.OTLPMetricExporter"),
            patch("src.openapi_server.telemetry.OTLPLogExporter"),
            patch("src.openapi_server.telemetry.SQLAlchemyInstrumentor"),
            patch("src.openapi_server.telemetry.HTTPXClientInstrumentor"),
            patch("src.openapi_server.telemetry.LoggingInstrumentor"),
            patch("src.openapi_server.telemetry.set_meter_provider"),
            patch("src.openapi_server.telemetry.set_logger_provider"),
        ):
            configure_telemetry()
            mock_set.assert_called_once()
            assert isinstance(mock_set.call_args[0][0], TracerProvider)

    def test_default_service_name(self, monkeypatch):
        """Service name defaults to lamp-control-api-python."""
        monkeypatch.setenv("OTEL_EXPORTER_OTLP_ENDPOINT", "http://localhost:4317")
        monkeypatch.delenv("OTEL_SERVICE_NAME", raising=False)

        with (
            patch("opentelemetry.trace.set_tracer_provider") as mock_set,
            patch("src.openapi_server.telemetry.OTLPSpanExporter"),
            patch("src.openapi_server.telemetry.OTLPMetricExporter"),
            patch("src.openapi_server.telemetry.OTLPLogExporter"),
            patch("src.openapi_server.telemetry.SQLAlchemyInstrumentor"),
            patch("src.openapi_server.telemetry.HTTPXClientInstrumentor"),
            patch("src.openapi_server.telemetry.LoggingInstrumentor"),
            patch("src.openapi_server.telemetry.set_meter_provider"),
            patch("src.openapi_server.telemetry.set_logger_provider"),
        ):
            configure_telemetry()
            provider = mock_set.call_args[0][0]
            assert provider.resource.attributes["service.name"] == "lamp-control-api-python"

    def test_custom_service_name(self, monkeypatch):
        """Service name is read from OTEL_SERVICE_NAME env var."""
        monkeypatch.setenv("OTEL_EXPORTER_OTLP_ENDPOINT", "http://localhost:4317")
        monkeypatch.setenv("OTEL_SERVICE_NAME", "my-custom-service")

        with (
            patch("opentelemetry.trace.set_tracer_provider") as mock_set,
            patch("src.openapi_server.telemetry.OTLPSpanExporter"),
            patch("src.openapi_server.telemetry.OTLPMetricExporter"),
            patch("src.openapi_server.telemetry.OTLPLogExporter"),
            patch("src.openapi_server.telemetry.SQLAlchemyInstrumentor"),
            patch("src.openapi_server.telemetry.HTTPXClientInstrumentor"),
            patch("src.openapi_server.telemetry.LoggingInstrumentor"),
            patch("src.openapi_server.telemetry.set_meter_provider"),
            patch("src.openapi_server.telemetry.set_logger_provider"),
        ):
            configure_telemetry()
            provider = mock_set.call_args[0][0]
            assert provider.resource.attributes["service.name"] == "my-custom-service"

    def test_instruments_global_libraries_when_endpoint_set(self, monkeypatch):
        """SQLAlchemy, HTTPX, and Logging instrumentors are enabled when endpoint is set."""
        monkeypatch.setenv("OTEL_EXPORTER_OTLP_ENDPOINT", "http://localhost:4317")

        with (
            patch("opentelemetry.trace.set_tracer_provider"),
            patch("src.openapi_server.telemetry.OTLPSpanExporter"),
            patch("src.openapi_server.telemetry.OTLPMetricExporter"),
            patch("src.openapi_server.telemetry.OTLPLogExporter"),
            patch("src.openapi_server.telemetry.SQLAlchemyInstrumentor") as mock_sqla,
            patch("src.openapi_server.telemetry.HTTPXClientInstrumentor") as mock_httpx,
            patch("src.openapi_server.telemetry.LoggingInstrumentor") as mock_logging,
            patch("src.openapi_server.telemetry.set_meter_provider"),
            patch("src.openapi_server.telemetry.set_logger_provider"),
        ):
            configure_telemetry()
            mock_sqla.return_value.instrument.assert_called_once()
            mock_httpx.return_value.instrument.assert_called_once()
            mock_logging.return_value.instrument.assert_called_once_with()

    def test_json_logging_is_always_configured(self, monkeypatch):
        """Root logger uses a JSON formatter regardless of OTEL_EXPORTER_OTLP_ENDPOINT."""
        monkeypatch.delenv("OTEL_EXPORTER_OTLP_ENDPOINT", raising=False)

        configure_telemetry()

        root_handlers = logging.getLogger().handlers
        assert any(isinstance(h.formatter, jsonlogger.JsonFormatter) for h in root_handlers)

    def test_does_not_instrument_global_libraries_without_endpoint(self, monkeypatch):
        """Instrumentors are not called when endpoint is absent."""
        monkeypatch.delenv("OTEL_EXPORTER_OTLP_ENDPOINT", raising=False)

        with (
            patch("src.openapi_server.telemetry.SQLAlchemyInstrumentor") as mock_sqla,
            patch("src.openapi_server.telemetry.HTTPXClientInstrumentor") as mock_httpx,
            patch("src.openapi_server.telemetry.LoggingInstrumentor") as mock_logging,
        ):
            configure_telemetry()
            mock_sqla.return_value.instrument.assert_not_called()
            mock_httpx.return_value.instrument.assert_not_called()
            mock_logging.return_value.instrument.assert_not_called()
