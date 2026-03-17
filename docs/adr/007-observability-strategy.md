# ADR 007: Observability Strategy

## Status
Accepted

## Date
2026-03-17

## Context
As the lamp-control-api-reference project grows to demonstrate consistent API patterns across six languages (TypeScript, Python, Java, C#, Go, Kotlin), operational visibility becomes critical. Without a shared observability strategy, each implementation would instrument itself differently, making it impossible to compare behaviour, diagnose cross-cutting issues, or build reusable dashboards and alerts.

Issue [#14](https://github.com/davideme/lamp-control-api-reference/issues/14) raised the need to define an observability approach. This ADR captures the high-level strategy; language-specific implementation details are deferred to language-local ADRs.

## Decision
We adopt a **three-pillar observability strategy** covering **logs**, **metrics**, and **traces**, underpinned by the following cross-cutting principles:

### 1. Pillars

| Pillar | Purpose |
|--------|---------|
| **Structured Logs** | Human- and machine-readable event records with consistent fields |
| **Metrics** | Numeric time-series data for SLO measurement and alerting |
| **Distributed Traces** | End-to-end request flow across service boundaries |

### 2. Instrumentation Standard
**OpenTelemetry** (OTel) is the standard instrumentation framework across all implementations. OpenTelemetry is vendor-neutral and CNCF-graduated; it unifies collection of all three pillars through a single SDK and Collector. Each language implementation MUST adopt the OpenTelemetry SDK appropriate for its ecosystem (see language-specific ADRs).

### 3. Semantic Conventions
All implementations MUST emit the following resource and span attributes using [OpenTelemetry Semantic Conventions](https://opentelemetry.io/docs/specs/semconv/):

**Resource attributes (set once at startup):**
- `service.name` – e.g. `lamp-control-api-typescript`
- `service.version` – from the application build/release
- `deployment.environment` – `development`, `staging`, or `production`

**HTTP span attributes (inbound requests):**
- `http.request.method`
- `http.route`
- `http.response.status_code`
- `url.path`
- `server.address`, `server.port`

**Error attributes:**
- `error.type` – exception class or error code
- `exception.message`, `exception.stacktrace` on exception events

**Lamp domain attributes (custom spans):**
- `lamp.id` – UUID of the lamp resource
- `lamp.operation` – `create`, `get`, `list`, `update`, `delete`

### 4. Trace Context Propagation
All implementations MUST support **W3C Trace Context** (`traceparent` / `tracestate` headers) for distributed trace propagation across API boundaries.

### 5. Log Correlation
Structured log records MUST include `trace_id` and `span_id` fields (hex-encoded) when emitted within an active span, enabling log–trace correlation in any backend.

### 6. Metrics Baseline
Every implementation MUST expose at minimum:

| Metric | Type | Description |
|--------|------|-------------|
| `http.server.request.duration` | Histogram | Latency per route/method/status |
| `http.server.active_requests` | UpDownCounter | Concurrent in-flight requests (where the framework supports it) |
| `lamp.operations.total` | Counter | Lamp CRUD operations by type and outcome |
| Runtime metrics | Varies | GC, heap, goroutine/thread counts where practical |

### 7. Export and Collector

**Local / development:**
- Run an **OpenTelemetry Collector** (Docker Compose service) that accepts OTLP (gRPC or HTTP) from all language servers.
- The Collector fans out to a local Jaeger instance (traces) and a Prometheus scrape endpoint (metrics).
- Logs may be emitted to stdout in JSON format and collected by the Collector's `filelog` or Docker log receiver.

**Production:**
- The OTLP endpoint is configured via the standard environment variable `OTEL_EXPORTER_OTLP_ENDPOINT` (and optionally `OTEL_EXPORTER_OTLP_HEADERS` for auth).
- Implementations MUST default to **no-op** exporting when the endpoint variable is absent, so tests and local runs without a Collector do not fail.
- Sampling policy baseline: **head-based sampling at 100%** for development; production deployments SHOULD configure tail-based or probabilistic sampling via Collector processor or `OTEL_TRACES_SAMPLER` env var.

### 8. PII and Data Sensitivity
- HTTP request/response bodies MUST NOT be captured as span attributes or logged unless explicitly opted in via configuration.
- Query parameters that may carry sensitive data (e.g., API keys) MUST be redacted before recording.
- Log levels MUST default to `INFO` in production; `DEBUG` must be disabled unless explicitly enabled.

### 9. SLO-Aligned Metrics
Implementations SHOULD define the following SLO-relevant metrics to enable consistent alerting across languages:

- **Availability**: percentage of non-5xx responses over a rolling window.
- **Latency**: p50/p95/p99 of `http.server.request.duration`.
- **Error rate**: rate of `error.type` attributed spans over total spans.

### 10. Dashboards and Alerting Consistency
- A shared Grafana dashboard template (JSON) SHOULD be maintained in `docs/observability/` covering all three pillars.
- Alert rules SHOULD be defined as code (e.g., Prometheus alert rules YAML) and stored alongside the dashboard template.
- All language implementations map to the same dashboard panels via consistent metric and attribute names defined in §3 above.

## Rationale
- **OpenTelemetry** was chosen over vendor SDKs (Datadog, New Relic, etc.) because it is vendor-neutral, CNCF-graduated, and supported by all major observability backends.
- **W3C Trace Context** was chosen over B3 or proprietary formats as it is the current IETF standard and natively supported by OTel and all modern proxies.
- **Baseline metrics** are aligned with the OpenTelemetry HTTP Semantic Conventions to maximise out-of-the-box dashboard compatibility.
- **No-op default** ensures that adding instrumentation does not break existing CI pipelines that run without a Collector.

## Consequences

### Positive
- Uniform observability across all six language implementations.
- Vendor-neutral: teams can swap backends (Jaeger → Tempo, Prometheus → Mimir) without changing application code.
- Correlation of logs, metrics, and traces within a single request lifecycle.
- Shared dashboards and runbooks reduce operational overhead.

### Negative
- Adds a runtime dependency (OTel SDK) to every implementation.
- Local development setup requires a Docker Compose service for the Collector (opt-in; not required for unit tests).
- Initial instrumentation effort per language (tracked in issue #14).

## ADR Governance Note

### Folder Structure and Alignment
Going forward:

- **Global / cross-cutting ADRs** (API design, infrastructure, observability, testing strategy) live in `docs/adr/`.
- **Language implementation ADRs** (framework selection, build tooling, linting, language-specific patterns) live in the language's local ADR directory:
  - `src/csharp/adr/`
  - `src/go/adr/`
  - `src/java/adr/`
  - `src/kotlin/adr/`
  - `src/python/docs/adr/`
  - `src/typescript/docs/adr/`

The current layout reflects organic growth and is intentionally left in place for this PR to minimise disruption. A follow-up PR may consolidate or cross-link these folders if the team agrees. Any new ADR MUST be placed according to the convention above.

## References
- Issue [#14 – OpenTelemetry Integration](https://github.com/davideme/lamp-control-api-reference/issues/14)
- [OpenTelemetry](https://opentelemetry.io/)
- [W3C Trace Context](https://www.w3.org/TR/trace-context/)
- [OpenTelemetry Semantic Conventions](https://opentelemetry.io/docs/specs/semconv/)
- [CNCF OpenTelemetry](https://www.cncf.io/projects/opentelemetry/)
