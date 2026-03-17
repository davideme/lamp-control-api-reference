# ADR 008: OpenTelemetry Instrumentation for TypeScript

## Status
Accepted

## Date
2026-03-17

## Context
The project-level Observability Strategy ADR ([docs/adr/007](../../../../docs/adr/007-observability-strategy.md)) mandates OpenTelemetry as the standard instrumentation framework and defines shared semantic conventions, trace propagation, and export requirements. This ADR describes how those requirements are met in the TypeScript / Fastify implementation.

Issue [#14](https://github.com/davideme/lamp-control-api-reference/issues/14) tracks the implementation work.

## Decision
Adopt the **OpenTelemetry JavaScript/Node.js SDK** with Fastify auto-instrumentation and Prisma instrumentation for the TypeScript implementation.

### Required npm Packages

```jsonc
// package.json
{
  "dependencies": {
    "@opentelemetry/api": "^1.9.0",
    "@opentelemetry/sdk-node": "^0.57.0",
    "@opentelemetry/instrumentation-fastify": "^0.42.0",
    "@opentelemetry/instrumentation-http": "^0.57.0",
    "@opentelemetry/instrumentation-undici": "^0.10.0",
    "@opentelemetry/exporter-trace-otlp-grpc": "^0.57.0",
    "@opentelemetry/exporter-metrics-otlp-grpc": "^0.57.0",
    "@opentelemetry/resources": "^1.29.0",
    "@opentelemetry/semantic-conventions": "^1.28.0",
    "@prisma/instrumentation": "^6.0.0"
  }
}
```

### Instrumentation Approach: SDK Init File (loaded before app code)

Create `src/infrastructure/telemetry/instrumentation.ts` that is loaded before any other module using the `--import` Node.js flag (Node 18.19+) or `--require`:

```typescript
// src/infrastructure/telemetry/instrumentation.ts
import { NodeSDK } from '@opentelemetry/sdk-node';
import { OTLPTraceExporter } from '@opentelemetry/exporter-trace-otlp-grpc';
import { OTLPMetricExporter } from '@opentelemetry/exporter-metrics-otlp-grpc';
import { Resource } from '@opentelemetry/resources';
import { ATTR_SERVICE_NAME, ATTR_SERVICE_VERSION } from '@opentelemetry/semantic-conventions';
import { FastifyInstrumentation } from '@opentelemetry/instrumentation-fastify';
import { HttpInstrumentation } from '@opentelemetry/instrumentation-http';
import { PrismaInstrumentation } from '@prisma/instrumentation';

const endpoint = process.env.OTEL_EXPORTER_OTLP_ENDPOINT;

const sdk = new NodeSDK({
  resource: new Resource({
    [ATTR_SERVICE_NAME]: process.env.OTEL_SERVICE_NAME ?? 'lamp-control-api-typescript',
    [ATTR_SERVICE_VERSION]: process.env.npm_package_version ?? '0.0.0',
    'deployment.environment': process.env.NODE_ENV ?? 'development',
  }),
  traceExporter: endpoint ? new OTLPTraceExporter({ url: endpoint }) : undefined,
  metricReader: endpoint
    ? new PeriodicExportingMetricReader({ exporter: new OTLPMetricExporter({ url: endpoint }) })
    : undefined,
  instrumentations: [
    new HttpInstrumentation(),
    new FastifyInstrumentation(),
    new PrismaInstrumentation(),
  ],
});

if (endpoint) {
  sdk.start();
}
```

When `OTEL_EXPORTER_OTLP_ENDPOINT` is not set, the SDK is not started and all OTel API calls fall back to no-ops.

### Instrumentation Scope

**Inbound HTTP spans:**
`FastifyInstrumentation` and `HttpInstrumentation` together record `http.request.method`, `http.route`, `http.response.status_code`, `url.path`, `server.address`, and `server.port` for every incoming request, per OTel HTTP Semantic Conventions.

**Outbound HTTP spans:**
`HttpInstrumentation` intercepts Node.js `http`/`https` modules. For `fetch`-based clients (Node 18+), `@opentelemetry/instrumentation-undici` provides equivalent coverage.

**Database spans:**
`PrismaInstrumentation` (from `@prisma/instrumentation`) creates spans for every Prisma query with `db.system = "postgresql"`, `db.operation.name`, and `db.statement` (sanitised by default).

### Metrics Baseline

| Metric | Source |
|--------|--------|
| `http.server.request.duration` | `FastifyInstrumentation` / `HttpInstrumentation` |
| `http.server.active_requests` | `HttpInstrumentation` |

### Log Correlation
Use `@opentelemetry/winston-transport` (if Winston is the logger) or inject trace context manually into Pino/Fastify logger via a custom hook:

```typescript
// Fastify request hook – inject trace context into log fields
fastify.addHook('onRequest', (request, _reply, done) => {
  const span = trace.getActiveSpan();
  if (span) {
    const ctx = span.spanContext();
    request.log = request.log.child({
      trace_id: ctx.traceId,
      span_id: ctx.spanId,
    });
  }
  done();
});
```

This ensures every request log record includes `trace_id` and `span_id`, enabling log–trace correlation in any backend.

### Propagation
The OTel Node.js SDK defaults to W3C Trace Context propagation. No additional configuration is required. The `traceparent` and `tracestate` headers are read from inbound requests and written to outbound requests automatically.

Override via environment variable if needed:
```
OTEL_PROPAGATORS=tracecontext,baggage
```

### Export and Collector Configuration

| Environment Variable | Default | Description |
|---------------------|---------|-------------|
| `OTEL_EXPORTER_OTLP_ENDPOINT` | *(none)* | OTLP gRPC endpoint; SDK not started when absent |
| `OTEL_SERVICE_NAME` | `lamp-control-api-typescript` | `service.name` resource attribute |
| `OTEL_RESOURCE_ATTRIBUTES` | *(none)* | Additional resource attributes |
| `OTEL_TRACES_SAMPLER` | `parentbased_always_on` | Sampling strategy |
| `NODE_ENV` | `development` | Mapped to `deployment.environment` |

Start the server with the instrumentation file pre-loaded:
```bash
node --import ./dist/infrastructure/telemetry/instrumentation.js ./dist/server.js
```

Or in `package.json`:
```json
{
  "scripts": {
    "start": "node --import ./dist/infrastructure/telemetry/instrumentation.js ./dist/server.js"
  }
}
```

### Testing and Verification
- Unit and integration tests: omit `OTEL_EXPORTER_OTLP_ENDPOINT` so the SDK is not started; no Collector needed.
- OTel span assertion in tests: use `@opentelemetry/sdk-trace-base`'s `InMemorySpanExporter` with a `NodeTracerProvider` configured in a test helper.
- Local smoke test: set `OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317` and run with the Docker Compose Collector stack; verify traces in Jaeger UI.

## Consequences

### Positive
- `FastifyInstrumentation` + `HttpInstrumentation` + `PrismaInstrumentation` provide three-pillar coverage with minimal boilerplate.
- `--import` flag ensures instrumentation is loaded before Fastify and Prisma, avoiding "too late to patch" issues.
- No-op default keeps CI pipelines and unit tests free of Collector infrastructure.

### Negative
- `--import` requires Node.js 18.19+ (ESM) or a `--require` alternative for CommonJS builds; the project must confirm its module format.
- `@opentelemetry/sdk-node` and related packages are still in `0.x` (pre-1.0 stability) for some sub-packages.
- Prisma spans include query text by default; review and configure `PrismaInstrumentation` to sanitise if PII is a concern.
- Fastify's logger (Pino) requires a custom hook for trace context injection since Pino does not natively integrate with OTel log bridge.

## References
- [docs/adr/007-observability-strategy.md](../../../../docs/adr/007-observability-strategy.md)
- Issue [#14](https://github.com/davideme/lamp-control-api-reference/issues/14)
- [OpenTelemetry JavaScript SDK](https://opentelemetry.io/docs/languages/js/)
- [@opentelemetry/sdk-node](https://github.com/open-telemetry/opentelemetry-js/tree/main/experimental/packages/opentelemetry-sdk-node)
- [@opentelemetry/instrumentation-fastify](https://github.com/open-telemetry/opentelemetry-js-contrib/tree/main/plugins/node/opentelemetry-instrumentation-fastify)
- [@prisma/instrumentation](https://www.prisma.io/docs/orm/prisma-client/observability-and-logging/opentelemetry-tracing)
