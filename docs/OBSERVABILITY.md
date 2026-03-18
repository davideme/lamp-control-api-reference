# Local Observability with OpenTelemetry

This guide covers running the full observability stack locally: an OTel Collector receiving traces and metrics from the language services, Jaeger for trace visualization, and Prometheus for metrics.

## Architecture

```
Language service  ──OTLP gRPC──▶  otel-collector:4317
                                       │
                          ┌────────────┴────────────┐
                          ▼                         ▼
                   Jaeger (traces)         Prometheus (metrics)
                   localhost:16686         localhost:9090
```

The collector is the single export target for all language services. It fans out:
- **Traces** → Jaeger via OTLP gRPC
- **Metrics** → Prometheus scrape endpoint on `:8889`

## Starting the Stack

Start only the observability services (no databases required):

```bash
docker-compose up -d otel-collector jaeger prometheus
```

Or start everything together:

```bash
docker-compose up -d
```

Verify all services are up:

```bash
docker-compose ps
```

## Accessing the UIs

| Service    | URL                    | Purpose                          |
|------------|------------------------|----------------------------------|
| Jaeger     | http://localhost:16686 | Trace search and waterfall view  |
| Prometheus | http://localhost:9090  | Metric queries and graphs        |

## Configuring a Language Service to Export Telemetry

Set the OTLP endpoint before starting any language server:

```bash
export OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317
export OTEL_SERVICE_NAME=lamp-control-api-<language>   # e.g. lamp-control-api-go
export OTEL_EXPORTER_OTLP_PROTOCOL=grpc
```

Then start the server normally. Examples:

```bash
# Go
cd src/go && make dev

# TypeScript
cd src/typescript && npm run dev

# Python
cd src/python && poetry run uvicorn openapi_server.main:app --reload

# Java
cd src/java && mvn spring-boot:run

# Kotlin
cd src/kotlin && ./gradlew run

# C#
cd src/csharp/LampControlApi && dotnet run
```

> The OTel SDKs default to a **no-op exporter** when `OTEL_EXPORTER_OTLP_ENDPOINT` is not set, so the server starts cleanly without a collector.

## Generating Some Traffic

```bash
# Health check
curl http://localhost:<port>/health

# Create a lamp
curl -X POST http://localhost:<port>/v1/lamps \
  -H "Content-Type: application/json" \
  -d '{"isOn": false}'

# List lamps
curl http://localhost:<port>/v1/lamps
```

Replace `<port>` with the port for your language (3000, 8000, 8080, 8081, 8082, or 8083).

## Viewing Traces in Jaeger

1. Open http://localhost:16686
2. Select the service (e.g. `lamp-control-api-go`) from the **Service** dropdown
3. Click **Find Traces**
4. Click any trace to see the full waterfall, span attributes, and timing breakdown

## Querying Metrics in Prometheus

1. Open http://localhost:9090
2. Use the **Graph** tab to query metrics, for example:

```promql
# HTTP request duration histogram (p99 per route)
histogram_quantile(0.99, sum by (le, http_route) (rate(http_server_request_duration_seconds_bucket[5m])))

# Active in-flight requests
http_server_active_requests

# Total request count by status code
sum by (http_response_status_code) (rate(http_server_request_duration_seconds_count[5m]))
```

The collector exposes its Prometheus endpoint at `http://localhost:8889/metrics` if you want to inspect the raw scrape output directly.

## Collector Health Check

The collector exposes a health endpoint on port `13133`:

```bash
curl http://localhost:13133
# returns: {"status":"Server available","upSince":"...","uptime":"..."}
```

## Stopping the Stack

```bash
docker-compose down
```

## Configuration Files

| File                          | Purpose                                              |
|-------------------------------|------------------------------------------------------|
| `otel-collector-config.yaml`  | Local collector: OTLP receiver → Jaeger + Prometheus |
| `otel-collector-config-gcp.yaml` | Cloud Run collector: OTLP receiver → Google Cloud |
| `prometheus.yml`              | Prometheus scrape config (targets the collector)     |
