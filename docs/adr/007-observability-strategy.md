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

#### Alternatives Considered for Instrumentation Standard

| Option | Reason Not Chosen |
|--------|------------------|
| **Vendor SDKs** (Datadog Agent, New Relic APM, Dynatrace OneAgent, Honeycomb `libhoney`) | Lock application code to a specific backend. Switching vendors requires rewriting instrumentation in all six language implementations. |
| **OpenCensus** | The predecessor to OpenTelemetry; officially archived in July 2023 in favour of OTel. No new features or security patches. |
| **Prometheus client libs + OpenTracing** | Two separate APIs with no unified context model; traces and metrics cannot be correlated natively. Each language would require two distinct integrations. |
| **Custom / ad-hoc logging + metrics** | Cannot produce distributed traces, lacks cross-language consistency, and requires bespoke dashboards per implementation. |

**Why OpenTelemetry?**
- It is the industry-standard, CNCF-graduated project merging OpenCensus and OpenTracing.
- A single SDK covers all three pillars (logs, metrics, traces) with a unified context model enabling log–trace correlation.
- Vendor-neutral OTLP protocol: the same application code exports to Jaeger, Tempo, Zipkin, Datadog, Honeycomb, and any other OTLP-compatible backend by changing an environment variable.
- Native SDKs exist for all six languages used in this project (TypeScript, Python, Java, C#, Go, Kotlin/JVM).
- Auto-instrumentation libraries cover the most common frameworks in each language, minimising hand-written boilerplate.

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

### 4. Trace Context Propagation
All implementations MUST support **W3C Trace Context** (`traceparent` / `tracestate` headers) for distributed trace propagation across API boundaries.

#### Alternatives Considered for Trace Propagation

| Standard | Description | Reason Not Chosen |
|----------|-------------|------------------|
| **B3 (Zipkin)** | Headers: `X-B3-TraceId`, `X-B3-SpanId`, `X-B3-Sampled`. Used by Zipkin, Brave, older Spring Cloud. | Non-standard (Zipkin-specific). Two variants (single-header vs multi-header) cause interoperability confusion. W3C supersedes it for new projects. |
| **AWS X-Ray** | Header: `X-Amzn-Trace-Id`. Used natively within AWS services. | Proprietary to AWS. Not supported outside AWS infrastructure without additional bridge configuration. |
| **Datadog `x-datadog-trace-id`** | Proprietary Datadog header format. | Vendor-locked; not interoperable with any other system without active conversion. |
| **Jaeger `uber-trace-id`** | Jaeger-specific header format. | Backend-specific; deprecated in Jaeger in favour of W3C. |

**Why W3C Trace Context?**
- Ratified as a [W3C Recommendation](https://www.w3.org/TR/trace-context/) — the only propagation format that is a formal web standard.
- Natively supported as the default propagator in the OpenTelemetry SDKs for all six languages.
- Understood by all modern reverse proxies, service meshes, and cloud load balancers (Nginx, Envoy, AWS ALB, GCP Cloud Load Balancing) without additional plugin configuration.
- Eliminates the need for a "bridge" between propagation formats when integrating with third-party systems.

### 5. Log Correlation
Structured log records MUST include `trace_id` and `span_id` fields (hex-encoded) when emitted within an active span, enabling log–trace correlation in any backend.

### 6. Metrics Baseline
Every implementation MUST expose at minimum:

**HTTP / Application metrics**

| Metric | Type | Description |
|--------|------|-------------|
| `http.server.request.duration` | Histogram | Latency per route/method/status — drives p50/p95/p99 SLOs |
| `http.server.active_requests` | UpDownCounter | Concurrent in-flight requests (where framework supports it) |

**Runtime / Process metrics** (where the language runtime exposes them)

| Metric | Type | Description |
|--------|------|-------------|
| `process.runtime.jvm.threads.count` (state: `runnable`) / `go.goroutines` / `dotnet.thread_pool.thread.count` | UpDownCounter | Active platform threads or goroutines; reveals thread pool exhaustion |
| `process.runtime.jvm.threads.count` (state: `waiting`/`blocked`) / `dotnet.thread_pool.queue.length` | Gauge | Waiting/blocked threads; leading indicator of thread pool saturation |
| `process.runtime.jvm.memory.heap.used` / `go.memory.heap.alloc` / `process.runtime.dotnet.gc.heap.size` | Gauge | Heap memory in use; alerts before OOM kills |
| GC pause duration / GC count | Histogram / Counter | Garbage collection pressure; high GC is a common latency root cause |
| `process.cpu.time` | Counter | CPU consumption; detect hot loops and run-away processes |

**Database connection pool metrics** (where the driver/ORM exposes them)

| Metric | Type | Description |
|--------|------|-------------|
| `db.client.connection.pool.size` | Gauge | Maximum pool capacity |
| `db.client.connection.count` (state: `used`) | Gauge | Active connections currently executing queries |
| `db.client.connection.count` (state: `idle`) | Gauge | Idle connections available; low idle + high used signals pool exhaustion |
| `db.client.connection.wait_duration` | Histogram | Time spent waiting to acquire a connection; key signal for pool saturation |
| `db.client.connection.timeouts` | Counter | Connection acquisition timeouts |

> **Notes:**
> - Metric names in the runtime and DB pool rows follow [OTel Semantic Conventions for runtime](https://opentelemetry.io/docs/specs/semconv/runtime/) and [database client metrics](https://opentelemetry.io/docs/specs/semconv/database/database-metrics/). Some names differ per language runtime — language-specific ADRs SHOULD document the exact metric names and the package used to emit them.
> - Implementations for which a runtime or pool metric is not available SHOULD document the gap in their language-specific ADR.

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
