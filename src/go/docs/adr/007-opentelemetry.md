# ADR 007: OpenTelemetry Instrumentation for Go

## Status
Accepted

## Date
2026-03-17

## Context
The project-level Observability Strategy ADR ([docs/adr/007](../../../../docs/adr/007-observability-strategy.md)) mandates OpenTelemetry as the standard instrumentation framework and defines shared semantic conventions, trace propagation, and export requirements. This ADR describes how those requirements are met in the Go / Chi Router implementation.

Issue [#14](https://github.com/davideme/lamp-control-api-reference/issues/14) tracks the implementation work.

## Decision
Adopt the **OpenTelemetry Go SDK** to instrument the Chi-based HTTP server.

### Instrumentation Summary

| Signal | Library / Mechanism | Official OTel? | Instrumentation Type |
|--------|---------------------|----------------|----------------------|
| Traces – Inbound HTTP | `go.opentelemetry.io/contrib/.../otelhttp` (`otelhttp.NewHandler`) | ✅ Yes | Config only |
| Traces – Outbound HTTP | `otelhttp.NewTransport` | ✅ Yes | Config only |
| Traces – Database (sqlc) | OTel Tracer API — manual span wrapping (sqlc has no auto-instrumentation library) | ✅ Yes (API only) | Custom code required |
| Metrics – HTTP server | `otelhttp` (auto-emitted alongside HTTP spans) | ✅ Yes | Config only |
| Metrics – Go runtime | `go.opentelemetry.io/contrib/instrumentation/runtime` (goroutines, heap, GC) | ✅ Yes | Config only |
| Logs | Manual `trace.SpanFromContext` + `log/slog` injection | ✅ Yes (OTel API) | Custom code required |

> **Config only**: wrap the router / transport with `otelhttp` once at startup — no per-handler changes needed.  
> **Custom code required**: sqlc does not have an auto-instrumentation library, so every repository method must be manually wrapped with `tracer.Start`/`span.End`. Log correlation similarly requires a small helper function. All I/O signals (inbound HTTP, outbound HTTP, database) are covered, though database spans require explicit instrumentation code.

### Required Go Modules

| Module | Purpose |
|--------|---------|
| `go.opentelemetry.io/otel` | Core OTel API |
| `go.opentelemetry.io/otel/sdk` | SDK (TracerProvider, MeterProvider) |
| `go.opentelemetry.io/otel/exporters/otlp/otlptrace/otlptracegrpc` | OTLP gRPC trace export |
| `go.opentelemetry.io/otel/exporters/otlp/otlpmetric/otlpmetricgrpc` | OTLP gRPC metric export |
| `go.opentelemetry.io/contrib/instrumentation/net/http/otelhttp` | Inbound HTTP server spans |
| `go.opentelemetry.io/contrib/instrumentation/runtime` | Go runtime metrics (goroutines, heap, GC) |
| `go.opentelemetry.io/otel/semconv/v1.26.0` | Semantic convention constants |
| `go.opentelemetry.io/otel/metric` | Metrics API |

### Instrumentation Scope

**Inbound HTTP spans:**
Wrap the Chi router with `otelhttp.NewHandler`:

```go
func spanNameFromRequest(req *http.Request) string {
    if routeCtx := chi.RouteContext(req.Context()); routeCtx != nil {
        if pattern := routeCtx.RoutePattern(); pattern != "" {
            return req.Method + " " + pattern
        }
    }

    return req.Method + " " + req.URL.Path
}

handler := otelhttp.NewHandler(r, "lamp-control-api",
    otelhttp.WithSpanNameFormatter(func(_ string, req *http.Request) string {
        return spanNameFromRequest(req)
    }),
)
```

This automatically records `http.request.method`, `http.route`, `http.response.status_code`, `url.path`, and request duration metrics.

**Outbound HTTP spans:**
Wrap any `http.Client` transport:

```go
client := &http.Client{
    Transport: otelhttp.NewTransport(http.DefaultTransport),
}
```

**Database spans:**
sqlc-generated queries do not provide automatic OTel instrumentation (unlike ORMs such as GORM or sqlx wrappers). Database calls MUST be wrapped manually using `tracer.Start` / `span.End` with `db.system = "postgresql"`, `db.operation.name`, and the table name. The full query string MUST NOT be recorded in production to avoid PII leakage.

Example:

```go
var tracer = otel.Tracer("lamp-control-api")

func (r *LampRepository) CreateLamp(ctx context.Context, ...) (*Lamp, error) {
    ctx, span := tracer.Start(ctx, "db.lamp.create",
        oteltrace.WithAttributes(
            semconv.DBSystemPostgreSQL,
            semconv.DBOperationName("INSERT"),
            semconv.DBCollectionName("lamps"),
        ),
    )
    defer span.End()
    lamp, err := r.queries.CreateLamp(ctx, ...)
    if err != nil {
        span.RecordError(err)
        span.SetStatus(codes.Error, err.Error())
        return nil, err
    }
    return lamp, nil
}
```

### Metrics Baseline

| Metric | Instrument | Notes |
|--------|-----------|-------|
| `http.server.request.duration` | Histogram | Provided by `otelhttp` |
| `http.server.active_requests` | UpDownCounter | Provided by `otelhttp` |
| `go.goroutine.count` | UpDownCounter | Provided by `go.opentelemetry.io/contrib/instrumentation/runtime` |
| `go.memory.used` | UpDownCounter | Provided by `go.opentelemetry.io/contrib/instrumentation/runtime` |
| `go.gc.duration` | Histogram | Provided by `go.opentelemetry.io/contrib/instrumentation/runtime` |

### Log Correlation
The current Go implementation uses the standard `log` package. As part of our observability migration, the Go standard `log/slog` package (Go 1.21+) WILL be adopted for structured logging. When using `slog`, include `trace_id` and `span_id` by extracting them from the active span:

```go
func logWithTrace(ctx context.Context, msg string, args ...any) {
    span := trace.SpanFromContext(ctx)
    sc := span.SpanContext()
    slog.InfoContext(ctx, msg,
        append(args,
            "trace_id", sc.TraceID().String(),
            "span_id", sc.SpanID().String(),
        )...,
    )
}
```

### Propagation
W3C Trace Context is the default propagator. Set it globally at startup:

```go
otel.SetTextMapPropagator(
    propagation.NewCompositeTextMapPropagator(
        propagation.TraceContext{},
        propagation.Baggage{},
    ),
)
```

### Export and Collector Configuration
Configure OTLP export via environment variables:

| Variable | Default | Description |
|----------|---------|-------------|
| `OTEL_EXPORTER_OTLP_ENDPOINT` | *(none)* | OTLP gRPC endpoint; if absent, uses no-op exporter |
| `OTEL_SERVICE_NAME` | `lamp-control-api-go` | Service name resource attribute |
| `OTEL_RESOURCE_ATTRIBUTES` | *(none)* | Additional resource attributes |
| `OTEL_TRACES_SAMPLER` | `parentbased_always_on` | Sampling strategy |

Initialization example in `main.go`:

```go
func initOTel(ctx context.Context) (func(context.Context) error, error) {
    endpoint := os.Getenv("OTEL_EXPORTER_OTLP_ENDPOINT")
    if endpoint == "" {
        // No-op: return a noop shutdown function
        otel.SetTracerProvider(trace.NewNoopTracerProvider())
        return func(context.Context) error { return nil }, nil
    }
    // ... configure OTLP exporters and providers
}
```

### Testing and Verification
- Unit tests MUST NOT require a running OTel Collector; initialize a no-op provider in test helpers.
- Integration tests MAY use `go.opentelemetry.io/otel/sdk/trace/tracetest` (`NewInMemoryExporter`) to assert spans are created with expected attributes.
- The `make dev` target SHOULD start the Docker Compose Collector alongside the API server for local end-to-end validation.

## Consequences

### Positive
- Lightweight: `otelhttp` middleware adds minimal overhead.
- No-op default keeps CI and unit tests free of infra dependencies.
- Consistent attribute naming via `semconv` constants prevents typos.

### Negative
- sqlc-generated queries require manual wrapping for database spans (no auto-instrumentation library exists for sqlc).
- Go's structured logging integration requires a small utility function to inject trace context.
- OTLP gRPC adds a dependency on `google.golang.org/grpc` transitively.

## References
- [docs/adr/007-observability-strategy.md](../../../../docs/adr/007-observability-strategy.md)
- Issue [#14](https://github.com/davideme/lamp-control-api-reference/issues/14)
- [OpenTelemetry Go SDK](https://github.com/open-telemetry/opentelemetry-go)
- [otelhttp middleware](https://github.com/open-telemetry/opentelemetry-go-contrib/tree/main/instrumentation/net/http/otelhttp)
- [OTel Go Getting Started](https://opentelemetry.io/docs/languages/go/getting-started/)
