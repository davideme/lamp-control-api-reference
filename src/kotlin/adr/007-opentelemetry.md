# ADR 007: OpenTelemetry Instrumentation for Kotlin

## Status
Accepted

## Date
2026-03-17

## Context
The project-level Observability Strategy ADR ([docs/adr/007](../../../docs/adr/007-observability-strategy.md)) mandates OpenTelemetry as the standard instrumentation framework and defines shared semantic conventions, trace propagation, and export requirements. This ADR describes how those requirements are met in the Kotlin / Ktor implementation.

Issue [#14](https://github.com/davideme/lamp-control-api-reference/issues/14) tracks the implementation work.

## Decision
Adopt the **OpenTelemetry Kotlin / JVM SDK** combined with **Ktor OpenTelemetry plugin** for the Ktor server instrumentation.

### Instrumentation Summary

| Signal | Library / Mechanism | Official OTel? | Instrumentation Type |
|--------|---------------------|----------------|----------------------|
| Traces – Inbound HTTP | `opentelemetry-ktor-3.0` (`KtorServerTelemetry` plugin) | ✅ Yes | Code-based |
| Traces – Outbound HTTP | `opentelemetry-ktor-3.0` (`KtorClientTelemetry` plugin) | ✅ Yes | Code-based |
| Traces – Database (Exposed) | OTel Tracer API — manual span wrapping (Exposed has no auto-instrumentation library) | ✅ Yes (API only) | Code-based |
| Metrics – HTTP server | `KtorServerTelemetry` plugin (auto-emitted with HTTP spans) | ✅ Yes | Code-based |
| Metrics – JVM runtime | `opentelemetry-runtime-telemetry-java8` | ✅ Yes | Code-based |
| Logs | `opentelemetry-logback-appender-1.0` (Logback → OTel bridge) | ✅ Yes | Code-based (logback.xml) |

> **Code-based** means installing the Ktor plugin and configuring the Logback appender. Exposed database spans require manual wrapping because no auto-instrumentation library exists for Exposed ORM.

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
    implementation("io.opentelemetry.instrumentation:opentelemetry-runtime-telemetry-java8:$otelInstrumentationVersion-alpha")
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
| `OTEL_EXPORTER_OTLP_ENDPOINT` | *(none)* | OTLP gRPC endpoint; no-op when absent |
| `OTEL_SERVICE_NAME` | `lamp-control-api-kotlin` | `service.name` resource attribute |
| `OTEL_RESOURCE_ATTRIBUTES` | *(none)* | Additional resource attributes (e.g., `deployment.environment=production`) |
| `OTEL_TRACES_SAMPLER` | `parentbased_always_on` | Sampling strategy |
| `OTEL_LOGS_EXPORTER` | `otlp` | Log export; set to `none` to disable |

Use `AutoConfiguredOpenTelemetrySdk` to read all `OTEL_*` environment variables automatically:
```kotlin
val sdk = AutoConfiguredOpenTelemetrySdk.initialize().openTelemetrySdk
GlobalOpenTelemetry.set(sdk)
```

When `OTEL_EXPORTER_OTLP_ENDPOINT` is unset, the SDK defaults to a no-op exporter, keeping unit and CI tests free of Collector dependencies.

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
- [docs/adr/007-observability-strategy.md](../../../docs/adr/007-observability-strategy.md)
- Issue [#14](https://github.com/davideme/lamp-control-api-reference/issues/14)
- [OpenTelemetry Kotlin / JVM](https://opentelemetry.io/docs/languages/java/)
- [opentelemetry-ktor instrumentation](https://github.com/open-telemetry/opentelemetry-java-instrumentation/tree/main/instrumentation/ktor)
- [OpenTelemetry SDK Autoconfigure](https://github.com/open-telemetry/opentelemetry-java/tree/main/sdk-extensions/autoconfigure)
