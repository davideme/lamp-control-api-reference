# ADR 008: OpenTelemetry Instrumentation for Python

## Status
Accepted

## Date
2026-03-17

## Context
The project-level Observability Strategy ADR ([docs/adr/007](../../../../docs/adr/007-observability-strategy.md)) mandates OpenTelemetry as the standard instrumentation framework and defines shared semantic conventions, trace propagation, and export requirements. This ADR describes how those requirements are met in the Python / FastAPI implementation.

Issue [#14](https://github.com/davideme/lamp-control-api-reference/issues/14) tracks the implementation work.

## Decision
Adopt the **OpenTelemetry Python SDK** with FastAPI / ASGI auto-instrumentation and SQLAlchemy instrumentation.

### Required Packages (Poetry)

```toml
# pyproject.toml
[tool.poetry.dependencies]
opentelemetry-api = "^1.29"
opentelemetry-sdk = "^1.29"
opentelemetry-instrumentation-fastapi = "^0.50b0"
opentelemetry-instrumentation-sqlalchemy = "^0.50b0"
opentelemetry-instrumentation-httpx = "^0.50b0"
opentelemetry-exporter-otlp-proto-grpc = "^1.29"

[tool.poetry.group.dev.dependencies]
opentelemetry-sdk = "^1.29"  # for InMemorySpanExporter in tests
```

### Instrumentation Scope

**Inbound HTTP spans:**
Use `FastAPIInstrumentor` to auto-instrument all routes:

```python
from opentelemetry.instrumentation.fastapi import FastAPIInstrumentor

FastAPIInstrumentor.instrument_app(app)
```

This records `http.request.method`, `http.route`, `http.response.status_code`, `url.path`, `server.address`, and `server.port` per OTel HTTP Semantic Conventions.

**Outbound HTTP spans:**
If `httpx` is used for outbound calls, instrument it:

```python
from opentelemetry.instrumentation.httpx import HTTPXClientInstrumentor

HTTPXClientInstrumentor().instrument()
```

**Database spans:**
SQLAlchemy async engine is instrumented via `SQLAlchemyInstrumentor`:

```python
from opentelemetry.instrumentation.sqlalchemy import SQLAlchemyInstrumentor

SQLAlchemyInstrumentor().instrument(engine=engine)
```

This records `db.system = "postgresql"`, `db.operation.name`, and (optionally) `db.statement`. The `db.statement` capture is **disabled by default** to prevent PII leakage; enable only in development via `enable_commenter=True` configuration if needed.

### Metrics Baseline

| Metric | Source |
|--------|--------|
| `http.server.request.duration` | `FastAPIInstrumentor` auto-instrumentation |
| `http.server.active_requests` | `FastAPIInstrumentor` auto-instrumentation |
| Python runtime metrics | `opentelemetry-instrumentation-system-metrics` (optional) |

### Log Correlation
Use the `opentelemetry-sdk` logging handler to bridge Python `logging` to OTel. Add the handler to the root logger:

```python
import logging
from opentelemetry.sdk._logs import LoggerProvider
from opentelemetry.sdk._logs.export import BatchLogRecordProcessor
from opentelemetry._logs import set_logger_provider

logger_provider = LoggerProvider(resource=resource)
logger_provider.add_log_record_processor(
    BatchLogRecordProcessor(OTLPLogExporter())
)
set_logger_provider(logger_provider)

# Attach OTel handler to Python logging
from opentelemetry.instrumentation.logging import LoggingInstrumentor
LoggingInstrumentor().instrument(set_logging_format=True)
```

When `LoggingInstrumentor` is active, `trace_id` and `span_id` are injected into every log record's `extra` fields and formatted into the log message.

### Propagation
Set W3C Trace Context as the global propagator at application startup:

```python
from opentelemetry.propagate import set_global_textmap
from opentelemetry.propagators.composite import CompositePropagator
from opentelemetry.trace.propagation.tracecontext import TraceContextTextMapPropagator
from opentelemetry.baggage.propagation import W3CBaggagePropagator

set_global_textmap(CompositePropagator([
    TraceContextTextMapPropagator(),
    W3CBaggagePropagator(),
]))
```

FastAPI's ASGI instrumentation reads and writes `traceparent` / `tracestate` automatically after this is set.

### Export and Collector Configuration

| Environment Variable | Default | Description |
|---------------------|---------|-------------|
| `OTEL_EXPORTER_OTLP_ENDPOINT` | *(none)* | OTLP gRPC endpoint; no-op when absent |
| `OTEL_SERVICE_NAME` | `lamp-control-api-python` | `service.name` resource attribute |
| `OTEL_RESOURCE_ATTRIBUTES` | *(none)* | Additional resource attributes |
| `OTEL_TRACES_SAMPLER` | `parentbased_always_on` | Sampling strategy |
| `OTEL_PYTHON_LOG_CORRELATION` | `true` | Enable trace context injection in Python logs |

Initialize the SDK using `opentelemetry-distro` auto-configuration:

```python
import os
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import BatchSpanProcessor
from opentelemetry.exporter.otlp.proto.grpc.trace_exporter import OTLPSpanExporter
from opentelemetry import trace

def configure_telemetry() -> None:
    endpoint = os.getenv("OTEL_EXPORTER_OTLP_ENDPOINT")
    if not endpoint:
        return  # No-op: use default noop TracerProvider

    exporter = OTLPSpanExporter(endpoint=endpoint)
    provider = TracerProvider(resource=resource)
    provider.add_span_processor(BatchSpanProcessor(exporter))
    trace.set_tracer_provider(provider)
```

### Testing and Verification
- Unit tests: the default `TracerProvider` is a no-op; no Collector needed.
- Integration tests: use `InMemorySpanExporter` and register it with a test `TracerProvider` to assert span names, attributes, and status codes.
- Local validation: set `OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317` and run `poetry run uvicorn openapi_server.main:app --reload` with the Docker Compose Collector stack.

## Consequences

### Positive
- `FastAPIInstrumentor` and `SQLAlchemyInstrumentor` provide near-zero-code HTTP and database instrumentation.
- Async-safe: `opentelemetry-instrumentation-fastapi` supports ASGI and async request handling natively.
- No-op default keeps `pytest` runs free of Collector infrastructure.

### Negative
- `opentelemetry-instrumentation-*` packages use beta versioning (`0.50b0`); API stability is not guaranteed until 1.0.
- `LoggingInstrumentor` modifies the global Python logging configuration, which can affect test output formatting.
- SQLAlchemy statement capture must remain disabled in production; requires explicit configuration awareness.

## References
- [docs/adr/007-observability-strategy.md](../../../../docs/adr/007-observability-strategy.md)
- Issue [#14](https://github.com/davideme/lamp-control-api-reference/issues/14)
- [OpenTelemetry Python SDK](https://opentelemetry.io/docs/languages/python/)
- [opentelemetry-instrumentation-fastapi](https://github.com/open-telemetry/opentelemetry-python-contrib/tree/main/instrumentation/opentelemetry-instrumentation-fastapi)
- [opentelemetry-instrumentation-sqlalchemy](https://github.com/open-telemetry/opentelemetry-python-contrib/tree/main/instrumentation/opentelemetry-instrumentation-sqlalchemy)
