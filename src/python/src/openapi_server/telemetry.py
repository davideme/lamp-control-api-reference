"""OpenTelemetry instrumentation setup for the Lamp Control API.

Configures traces, metrics, and log correlation. All SDK setup is a no-op
when OTEL_EXPORTER_OTLP_ENDPOINT is not set. JSON structured logging is
always enabled.
"""

import logging
import os

from opentelemetry import trace
from opentelemetry._logs import set_logger_provider
from opentelemetry.baggage.propagation import W3CBaggagePropagator
from opentelemetry.exporter.otlp.proto.grpc._log_exporter import OTLPLogExporter
from opentelemetry.exporter.otlp.proto.grpc.metric_exporter import OTLPMetricExporter
from opentelemetry.exporter.otlp.proto.grpc.trace_exporter import OTLPSpanExporter
from opentelemetry.instrumentation.httpx import HTTPXClientInstrumentor
from opentelemetry.instrumentation.logging import LoggingInstrumentor
from opentelemetry.instrumentation.sqlalchemy import SQLAlchemyInstrumentor
from opentelemetry.metrics import set_meter_provider
from opentelemetry.propagate import set_global_textmap
from opentelemetry.propagators.composite import CompositePropagator
from opentelemetry.sdk._logs import LoggerProvider
from opentelemetry.sdk._logs.export import BatchLogRecordProcessor
from opentelemetry.sdk.metrics import MeterProvider
from opentelemetry.sdk.metrics.export import PeriodicExportingMetricReader
from opentelemetry.sdk.resources import Resource
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import BatchSpanProcessor
from opentelemetry.trace.propagation.tracecontext import TraceContextTextMapPropagator
from pythonjsonlogger import jsonlogger


def configure_telemetry() -> None:
    """Configure OpenTelemetry SDK.

    Always enables JSON structured logging on stdout.
    Sets W3C TraceContext + Baggage propagation unconditionally.
    Configures trace, metric, and log providers only when
    OTEL_EXPORTER_OTLP_ENDPOINT is set; otherwise a no-op.
    """
    handler = logging.StreamHandler()
    handler.setFormatter(
        jsonlogger.JsonFormatter(fmt="%(asctime)s %(levelname)s %(name)s %(message)s")
    )
    logging.basicConfig(handlers=[handler], level=logging.INFO, force=True)

    set_global_textmap(
        CompositePropagator(
            [
                TraceContextTextMapPropagator(),
                W3CBaggagePropagator(),
            ]
        )
    )

    endpoint = os.getenv("OTEL_EXPORTER_OTLP_ENDPOINT")
    if not endpoint:
        return

    service_name = os.getenv("OTEL_SERVICE_NAME", "lamp-control-api-python")
    resource = Resource.create({"service.name": service_name})

    # Traces
    tracer_provider = TracerProvider(resource=resource)
    tracer_provider.add_span_processor(BatchSpanProcessor(OTLPSpanExporter(endpoint=endpoint)))
    trace.set_tracer_provider(tracer_provider)

    # Metrics
    metric_reader = PeriodicExportingMetricReader(OTLPMetricExporter(endpoint=endpoint))
    meter_provider = MeterProvider(resource=resource, metric_readers=[metric_reader])
    set_meter_provider(meter_provider)

    # Logs
    logger_provider = LoggerProvider(resource=resource)
    logger_provider.add_log_record_processor(
        BatchLogRecordProcessor(OTLPLogExporter(endpoint=endpoint))
    )
    set_logger_provider(logger_provider)

    # Global instrumentors
    SQLAlchemyInstrumentor().instrument()
    HTTPXClientInstrumentor().instrument()
    LoggingInstrumentor().instrument()
