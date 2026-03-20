# ADR 008: OpenTelemetry Instrumentation for Kotlin

## Status
Accepted

## Date
2026-03-17

## Context
The project-level Observability Strategy ADR ([docs/adr/007](../../../../docs/adr/007-observability-strategy.md)) mandates OpenTelemetry as the standard instrumentation framework and defines shared semantic conventions, trace propagation, and export requirements. This ADR describes how those requirements are met in the Kotlin / Ktor implementation.

Issue [#14](https://github.com/davideme/lamp-control-api-reference/issues/14) tracks the implementation work.

## Decision
Adopt the **OpenTelemetry Kotlin / JVM SDK** combined with **Ktor OpenTelemetry plugin** for the Ktor server instrumentation.

### Instrumentation Summary

| Signal | Library / Mechanism | Official OTel? | Instrumentation Type |
|--------|---------------------|----------------|----------------------|
| Traces – Inbound HTTP | `opentelemetry-ktor-3.0` (`KtorServerTelemetry` plugin) | ✅ Yes | Config only |
| Traces – Outbound HTTP | `opentelemetry-ktor-3.0` (`KtorClientTelemetry` plugin) | ✅ Yes | Config only |
| Traces – Database (Exposed) | OTel Tracer API — manual span wrapping (Exposed has no auto-instrumentation library) | ✅ Yes (API only) | Custom code required |
| Metrics – HTTP server | `KtorServerTelemetry` plugin (auto-emitted with HTTP spans) | ✅ Yes | Config only |
| Metrics – JVM runtime | `opentelemetry-runtime-telemetry-java8` | ✅ Yes | Config only |
| Logs | `opentelemetry-logback-appender-1.0` (Logback → OTel bridge) | ✅ Yes | Config only |

> **Config only**: install the Ktor plugin with `install(KtorServerTelemetry)` or add the Logback appender in `logback.xml` — no per-handler changes needed.  
> **Custom code required**: Exposed ORM has no auto-instrumentation library, so every repository method must be manually wrapped with `tracer.spanBuilder`/`span.end`. All I/O signals (inbound HTTP, outbound HTTP, database) are covered, though database spans require explicit instrumentation code.

### Required Gradle Dependencies

```kotlin
// build.gradle.kts
val otelVersion = "1.44.0"
val otelInstrumentationVersion = "2.10.0"

dependencies {
    implementation("io.opentelemetry:opentelemetry-api:$otelVersion")
    implementation("io.opentelemetry:opentelemetry-sdk:$otelVersion")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp:$otelVersion")
    implementation("io.opentelemetry.instrumentation:opentelemetry-ktor-3.0:$otelInstrumentationVersion")
    implementation("io.opentelemetry:opentelemetry-sdk-extension-autoconfigure:$otelVersion")
    // Runtime metrics
    implementation("io.opentelemetry.instrumentation:opentelemetry-runtime-telemetry-java8:${otelInstrumentationVersion}-alpha")
}
```

### Instrumentation Scope

**Inbound HTTP spans:**
Install the `KtorServerTelemetry` plugin on the Ktor `Application`:

```kotlin
install(KtorServerTelemetry) {
    setOpenTelemetry(openTelemetry)
}
```

This records `http.request.method`, `http.route`, `http.response.status_code`, `url.path`, and `server.address` per OTel HTTP Semantic Conventions.

**Outbound HTTP spans:**
Wrap Ktor's `HttpClient` with the OTel client plugin:

```kotlin
val client = HttpClient(CIO) {
    install(KtorClientTelemetry) {
        setOpenTelemetry(openTelemetry)
    }
}
```

**Database spans:**
Exposed ORM does not have an official OTel auto-instrumentation library. Database operations MUST be wrapped manually using `tracer.spanBuilder` with `db.system`, `db.operation.name`, and table name attributes. The full SQL query MUST NOT be recorded in production to avoid PII leakage.

Example:

```kotlin
suspend fun createLamp(request: CreateLampRequest): Lamp = withContext(Dispatchers.IO) {
    val span = tracer.spanBuilder("db.lamp.insert")
        .setAttribute(SemanticAttributes.DB_SYSTEM, "postgresql")
        .setAttribute(SemanticAttributes.DB_OPERATION, "INSERT")
        .setAttribute(SemanticAttributes.DB_SQL_TABLE, "lamps")
        .startSpan()
    try {
        span.makeCurrent().use {
            transaction {
                LampTable.insert { /* ... */ }
                // ...
            }
        }
    } catch (e: Exception) {
        span.recordException(e)
        span.setStatus(StatusCode.ERROR, e.message ?: "error")
        throw e
    } finally {
        span.end()
    }
}
```

### Metrics Baseline

| Metric | Source |
|--------|--------|
| `http.server.request.duration` | `KtorServerTelemetry` plugin |
| `http.server.active_requests` | `KtorServerTelemetry` plugin |
| JVM runtime metrics | `opentelemetry-runtime-telemetry-java8` |

### Log Correlation
Use the `opentelemetry-logback-appender-1.0` with SLF4J / Logback (the default logging backend for Ktor):

```xml
<!-- logback.xml -->
<appender name="OpenTelemetry"
          class="io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender">
    <captureExperimentalAttributes>true</captureExperimentalAttributes>
</appender>
```

When a span is active, `trace_id` and `span_id` are injected into the MDC and emitted with each log record.

### Propagation
W3C Trace Context is configured as the default propagator via the OTel SDK autoconfigure module:

```kotlin
// Set globally at application start
GlobalOpenTelemetry.resetForTest() // only in tests
openTelemetry = AutoConfiguredOpenTelemetrySdk.initialize().openTelemetrySdk
```

`OTEL_PROPAGATORS=tracecontext,baggage` (default) ensures `traceparent` / `tracestate` headers are read and written.

### Export and Collector Configuration

| Environment Variable | Default | Description |
|---------------------|---------|-------------|
| `OTEL_EXPORTER_OTLP_ENDPOINT` | *(none)* | OTLP gRPC endpoint used when one or more exporters are set to `otlp` |
| `OTEL_SERVICE_NAME` | `lamp-control-api-kotlin` | `service.name` resource attribute |
| `OTEL_RESOURCE_ATTRIBUTES` | *(none)* | Additional resource attributes (e.g., `deployment.environment=production`) |
| `OTEL_TRACES_SAMPLER` | `parentbased_always_on` | Sampling strategy |
| `OTEL_TRACES_EXPORTER` | `none` | Trace exporter; `none` guarantees no-op-by-default, set to `otlp` when an OTLP endpoint is configured |
| `OTEL_METRICS_EXPORTER` | `none` | Metrics exporter; `none` guarantees no-op-by-default, set to `otlp` when an OTLP endpoint is configured |
| `OTEL_LOGS_EXPORTER` | `none` | Log exporter; `none` guarantees no-op-by-default, set to `otlp` when an OTLP endpoint is configured |

Use `AutoConfiguredOpenTelemetrySdk` to read all `OTEL_*` environment variables automatically:
```kotlin
val sdk = AutoConfiguredOpenTelemetrySdk.initialize().openTelemetrySdk
GlobalOpenTelemetry.set(sdk)
```

By default (local development, unit tests, CI), we do **not** set `OTEL_EXPORTER_OTLP_ENDPOINT` and we explicitly configure
`OTEL_TRACES_EXPORTER=none`, `OTEL_METRICS_EXPORTER=none`, and `OTEL_LOGS_EXPORTER=none` (via environment or runtime configuration).
In this configuration, `AutoConfiguredOpenTelemetrySdk` initializes no-op exporters and performs no network export.
When an OpenTelemetry Collector is available and export should be enabled, deployment manifests must set
`OTEL_EXPORTER_OTLP_ENDPOINT` (for example `http://otel-collector:4317`) and switch the relevant exporters to `otlp`
(for example `OTEL_TRACES_EXPORTER=otlp`, `OTEL_METRICS_EXPORTER=otlp`, `OTEL_LOGS_EXPORTER=otlp`).

### Testing and Verification
- Unit tests: use `OpenTelemetry.noop()` or `GlobalOpenTelemetry.resetForTest()` to avoid actual export.
- Integration tests: register `InMemorySpanExporter` and `InMemoryMetricExporter` in the test application module to assert spans and metric data points.
- Local validation: `./gradlew run` with `OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317` and a running OTel Collector (Docker Compose) to view traces in Jaeger.

## Consequences

### Positive
- `KtorServerTelemetry` plugin handles HTTP instrumentation with a single `install` call.
- Coroutine-safe: the OTel context propagates correctly through Kotlin coroutines via `makeCurrent()`.
- Autoconfigure module reads all standard `OTEL_*` env vars, reducing boilerplate.

### Negative
- Exposed ORM lacks an official auto-instrumentation library; manual span wrapping adds boilerplate around every database operation.
- `GlobalOpenTelemetry` singleton pattern may complicate parallel test execution; each test that modifies global state must call `resetForTest()`.
- Alpha instrumentation packages (`opentelemetry-runtime-telemetry-java8`) may have breaking changes between minor versions.

## References
- [docs/adr/007-observability-strategy.md](../../../../docs/adr/007-observability-strategy.md)
- Issue [#14](https://github.com/davideme/lamp-control-api-reference/issues/14)
- [OpenTelemetry Kotlin / JVM](https://opentelemetry.io/docs/languages/java/)
- [opentelemetry-ktor instrumentation](https://github.com/open-telemetry/opentelemetry-java-instrumentation/tree/main/instrumentation/ktor)
- [OpenTelemetry SDK Autoconfigure](https://github.com/open-telemetry/opentelemetry-java/tree/main/sdk-extensions/autoconfigure)
