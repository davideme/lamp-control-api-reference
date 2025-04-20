# ADR 007: TypeScript Observability (Logging, Metrics & Tracing)

**Status:** Proposed

**Date:** 2025-04-18

## Context

Effective observability is essential for monitoring application health, diagnosing issues, and understanding system behavior, especially request flows across different components. This requires standardizing tools for structured logging, metrics collection, and distributed tracing within the TypeScript implementation.

## Decision

We will adopt the following libraries for observability:

-   **Logging:** **Winston**
    -   _Rationale:_ A highly flexible and popular logging library for Node.js. It supports multiple configurable transports (console, file, external services), various log formats (including JSON for structured logging), and custom log levels. Its maturity and widespread use make it a safe choice.
-   **Metrics:** **Prometheus** (via client library/middleware like `prom-client` or `express-prometheus-middleware`)
    -   _Rationale:_ Prometheus is the de facto open-source standard for time-series based monitoring and alerting. Using a Prometheus client library allows the application to expose standard and custom metrics (e.g., HTTP request duration, error rates, business-specific metrics) via an HTTP endpoint (`/metrics`) for scraping by a Prometheus server.
-   **Tracing:** **OpenTelemetry (OTel)**
    -   _Rationale:_ OpenTelemetry is the vendor-neutral CNCF standard for observability, providing APIs and SDKs for traces, metrics, and logs. Its Node.js SDK offers automatic instrumentation for many common libraries (Express, gRPC, database clients), simplifying the process of generating distributed traces. It supports exporting data via OTLP (OpenTelemetry Protocol) to various backends (Jaeger, Tempo, commercial platforms), providing flexibility.

## Consequences

-   **Pros:**
    -   Provides a comprehensive observability solution covering the three pillars: logs, metrics, and traces.
    -   **Logging:** Enables structured, configurable logging.
    -   **Metrics:** Integrates with the standard Prometheus ecosystem.
    -   **Tracing:** Enables visualization of request flows, identification of bottlenecks, and error propagation analysis using the OpenTelemetry standard.
    -   Leverages popular, well-supported, and standard-based tools.
    -   OTel's auto-instrumentation reduces initial setup effort for tracing.
-   **Cons:**
    -   Increases the number of dependencies and configuration complexity (Winston, Prometheus client, OTel SDK, instrumentation packages, exporters).
    -   Requires understanding concepts of all three observability pillars.
    -   Effective use requires setting up external systems (log aggregation, Prometheus server, trace backend like Jaeger/Tempo, visualization like Grafana), though the libraries function independently.
    -   Auto-instrumentation might not cover all scenarios, potentially requiring manual span creation.

## Alternatives Considered

-   **Logging Alternatives:**
    -   _Pino:_ High-performance JSON logger.
    -   _Bunyan:_ Another mature JSON logger.
-   **Metrics Alternatives:**
    -   _StatsD:_ Different metrics protocol.
    -   _OTel Metrics SDK:_ Could potentially replace the Prometheus client library for a more unified OTel approach, though Prometheus scraping is very common.
-   **Tracing Alternatives:**
    -   _Jaeger/Zipkin specific clients:_ Older approaches before OTel became the standard; vendor lock-in.
    -   _Vendor-specific APM agents:_ (e.g., Datadog, Dynatrace) Provide integrated solutions but create vendor lock-in.
    -   _No Tracing:_ Misses critical insights into request flows and performance bottlenecks.

## References

-   [Winston](https://github.com/winstonjs/winston)
-   [Prometheus](https://prometheus.io/)
-   [`prom-client`](https://github.com/siimon/prom-client)
-   [`express-prometheus-middleware`](https://github.com/joaozielasko/express-prometheus-middleware)
-   [OpenTelemetry](https://opentelemetry.io/)
-   [OpenTelemetry JS SDK](https://github.com/open-telemetry/opentelemetry-js)
-   [OpenTelemetry JS Contrib (Instrumentation)](https://github.com/open-telemetry/opentelemetry-js-contrib)
-   [Pino](https://getpino.io/)
-   [Jaeger Tracing](https://www.jaegertracing.io/) 