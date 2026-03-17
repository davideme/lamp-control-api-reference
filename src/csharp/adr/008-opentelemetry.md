# ADR 008: OpenTelemetry Instrumentation for C#

## Status
Accepted

## Date
2026-03-17

## Context
The project-level Observability Strategy ADR ([docs/adr/007](../../../docs/adr/007-observability-strategy.md)) mandates OpenTelemetry as the standard instrumentation framework and defines shared semantic conventions, trace propagation, and export requirements. This ADR describes how those requirements are met in the C# / ASP.NET Core implementation.

Issue [#14](https://github.com/davideme/lamp-control-api-reference/issues/14) tracks the implementation work.

## Decision
Adopt the **OpenTelemetry .NET SDK** (`OpenTelemetry.*` NuGet packages) to instrument the ASP.NET Core application.

### Instrumentation Summary

| Signal | Library / Mechanism | Official OTel? | Instrumentation Type |
|--------|---------------------|----------------|----------------------|
| Traces – Inbound HTTP | `OpenTelemetry.Instrumentation.AspNetCore` | ✅ Yes | Code-based |
| Traces – Outbound HTTP | `OpenTelemetry.Instrumentation.Http` | ✅ Yes | Code-based |
| Traces – Database (EF Core) | `OpenTelemetry.Instrumentation.EntityFrameworkCore` | ✅ Yes | Code-based |
| Metrics – HTTP server | `OpenTelemetry.Instrumentation.AspNetCore` | ✅ Yes | Code-based |
| Metrics – .NET runtime | `OpenTelemetry.Instrumentation.Runtime` | ✅ Yes | Code-based |
| Logs | `OpenTelemetry` log bridge (`AddOpenTelemetry()` on `ILogger`) | ✅ Yes | Code-based |

> **Code-based** means registering the instrumentation package in `Program.cs` (a few lines). No application business logic changes are required.

### Required NuGet Packages

| Package | Purpose |
|---------|---------|
| `OpenTelemetry.Extensions.Hosting` | `AddOpenTelemetry()` host integration |
| `OpenTelemetry.Instrumentation.AspNetCore` | Inbound HTTP server spans |
| `OpenTelemetry.Instrumentation.Http` | Outbound `HttpClient` spans |
| `OpenTelemetry.Instrumentation.EntityFrameworkCore` | Database spans via EF Core |
| `OpenTelemetry.Instrumentation.Runtime` | .NET runtime metrics (GC, threads, heap) |
| `OpenTelemetry.Exporter.OpenTelemetryProtocol` | OTLP gRPC/HTTP export |

### Instrumentation Scope

**Inbound HTTP spans:**
ASP.NET Core instrumentation (`AddAspNetCoreInstrumentation`) automatically creates spans for every incoming request and populates `http.request.method`, `http.route`, `http.response.status_code`, and `url.path` per the OTel HTTP Semantic Conventions.

**Outbound HTTP spans:**
`HttpClient` instrumentation (`AddHttpClientInstrumentation`) instruments any `HttpClient` call made during request handling.

**Database spans:**
`AddEntityFrameworkCoreInstrumentation` adds spans for EF Core queries, capturing `db.system`, `db.statement`, and related attributes. The `SetDbStatementForText` option MUST be `false` in production to avoid capturing PII in SQL statements.

### Metrics Baseline

Register the following meters with `AddMeter` in the OTel builder:

| Metric | Instrument | Notes |
|--------|-----------|-------|
| `http.server.request.duration` | Histogram | Provided by `AddAspNetCoreInstrumentation` |
| `http.server.active_requests` | UpDownCounter | Provided by `AddAspNetCoreInstrumentation` |
| Runtime metrics | Various | Provided by `AddRuntimeInstrumentation` |

### Log Correlation
Configure the `ILogger` pipeline to include `TraceId` and `SpanId` in structured log output:

```csharp
builder.Logging.AddOpenTelemetry(logging =>
{
    logging.IncludeFormattedMessage = true;
    logging.IncludeScopes = true;
});
```

When using structured logging (e.g., Serilog, NLog, or `Microsoft.Extensions.Logging`), `trace_id` and `span_id` are injected into the log scope automatically by the OTel .NET SDK when an active span exists.

### Propagation
W3C Trace Context propagation is the default in the OTel .NET SDK. No additional configuration is needed; `traceparent` / `tracestate` headers are read and written automatically.

### Export and Collector Configuration
Configure OTLP export via environment variables following the OTel SDK standard:

| Variable | Default | Description |
|----------|---------|-------------|
| `OTEL_EXPORTER_OTLP_ENDPOINT` | *(none)* | OTLP gRPC endpoint; if absent, uses no-op exporter |
| `OTEL_EXPORTER_OTLP_PROTOCOL` | `grpc` | `grpc` or `http/protobuf` |
| `OTEL_SERVICE_NAME` | `lamp-control-api-csharp` | Overrides `service.name` resource attribute |
| `OTEL_RESOURCE_ATTRIBUTES` | *(none)* | Additional resource attributes |

Configuration in `Program.cs`:
```csharp
builder.Services.AddOpenTelemetry()
    .ConfigureResource(r => r
        .AddService(
            serviceName: "lamp-control-api-csharp",
            serviceVersion: typeof(Program).Assembly.GetName().Version?.ToString() ?? "0.0.0")
        .AddAttributes([
            new("deployment.environment",
                builder.Environment.EnvironmentName.ToLowerInvariant())
        ]))
    .WithTracing(tracing => tracing
        .AddAspNetCoreInstrumentation()
        .AddHttpClientInstrumentation()
        .AddEntityFrameworkCoreInstrumentation(o => o.SetDbStatementForText = false)
        .AddOtlpExporter())
    .WithMetrics(metrics => metrics
        .AddAspNetCoreInstrumentation()
        .AddHttpClientInstrumentation()
        .AddRuntimeInstrumentation()
        .AddOtlpExporter())
    .WithLogging(logging => logging
        .AddOtlpExporter());
```

The OTLP exporter reads `OTEL_EXPORTER_OTLP_ENDPOINT` automatically; when the variable is not set, the SDK uses a no-op exporter so tests and CI runs without a Collector continue to pass.

### Testing and Verification
- Unit tests for service/repository methods MUST NOT require a running OTel Collector.
- Integration tests MAY use `TestActivityListener` or `InMemoryExporter` to assert that expected spans and metrics are emitted.
- A smoke test (manual or automated) using the local Docker Compose Collector setup SHOULD verify end-to-end trace propagation to Jaeger.

## Consequences

### Positive
- Full three-pillar observability (logs, metrics, traces) out of the box with minimal boilerplate via ASP.NET Core instrumentation.
- OTLP export decouples application from backend choice.
- No-op default keeps CI green without infra dependencies.

### Negative
- Adds several NuGet dependencies; increases build time slightly.
- EF Core statement capture must be explicitly disabled in production to avoid PII leakage.

## References
- [docs/adr/007-observability-strategy.md](../../../docs/adr/007-observability-strategy.md)
- Issue [#14](https://github.com/davideme/lamp-control-api-reference/issues/14)
- [OpenTelemetry .NET SDK](https://github.com/open-telemetry/opentelemetry-dotnet)
- [OpenTelemetry .NET Instrumentation](https://github.com/open-telemetry/opentelemetry-dotnet-contrib)
- [ASP.NET Core Instrumentation](https://github.com/open-telemetry/opentelemetry-dotnet/tree/main/src/OpenTelemetry.Instrumentation.AspNetCore)
