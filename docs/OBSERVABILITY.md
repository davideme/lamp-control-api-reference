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
| Jaeger     | http://localhost:16686 | Trace search and waterfall view (v2.16) |
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

The collector exposes its Prometheus endpoint at `:8889/metrics` inside the `otel-collector` container. To inspect the raw scrape output when running via Docker Compose, you can exec into the container and curl the endpoint:

```bash
docker-compose exec otel-collector curl http://localhost:8889/metrics
```

## Collector Health Check

The collector exposes a health endpoint on port `13133` inside the container. You can check it with:

```bash
docker-compose exec otel-collector curl http://localhost:13133
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

> **Jaeger 2.x note**: Starting with Jaeger 2.0, the project ships a single unified image (`cr.jaegertracing.io/jaegertracing/jaeger`). The OTel Collector is kept as a dedicated intermediate step — language services export to the collector, and the collector forwards to Jaeger — to mirror the production Cloud Run sidecar topology and keep buffering, batching, and retry logic in one place.

---

## Cloud Run: service.yaml Setup

Each language has a `src/<language>/service.yaml` that defines the Cloud Run service as a Knative resource with two containers: the **app** and an **OTel Collector sidecar**. This file is managed manually — Cloud Build only updates the app image; the full service definition (sidecar, secrets, probes) is applied separately.

### Architecture on Cloud Run

```
Cloud Run Service
├── app container          ← your language service (built by Cloud Build)
└── collector container    ← otelcol-google sidecar (fixed image, config from Secret Manager)
        │
        ▼
  Google Cloud Trace + Google Managed Prometheus
```

The app sends OTLP gRPC to `localhost:4317` (the sidecar). The sidecar exports to Google Cloud using the `googlecloud` and `googlemanagedprometheus` exporters.

### Prerequisites

1. **Enable Secret Manager** and store the collector config:

   ```bash
   gcloud secrets create otel-collector-config \
     --replication-policy=automatic \

   gcloud secrets versions add otel-collector-config \
     --data-file=otel-collector-config-gcp.yaml \
   ```

2. **Grant the Cloud Run service account** access to the secret:

   ```bash
   gcloud secrets add-iam-policy-binding otel-collector-config \
     --member=serviceAccount:<SERVICE_ACCOUNT_EMAIL> \
     --role=roles/secretmanager.secretAccessor \
   ```

### Deploying for the First Time (or Updating the Sidecar)

Use `gcloud run services replace` to apply the full service definition:

```bash
gcloud run services replace src/<language>/service.yaml \
  --region=YOUR_REGION
```

| Language   | File                        |
|------------|-----------------------------|
| TypeScript | `src/typescript/service.yaml` |
| Go         | `src/go/service.yaml`         |
| Python     | `src/python/service.yaml`     |
| Java       | `src/java/service.yaml`       |
| Kotlin     | `src/kotlin/service.yaml`     |
| C#         | `src/csharp/service.yaml`     |

> Run this whenever you change the sidecar image, environment variables, probes, secret mounts, or any structural part of the service. Cloud Build does **not** run this step automatically.

### What Cloud Build Updates (app image only)

On every push, Cloud Build runs:

```bash
gcloud run services update <service-name> \
  --container=app \
  --image=<new-image> \
  --update-labels=... \
  --region=YOUR_REGION \
  --quiet
```

This only swaps the `app` container image. The `collector` sidecar and all other service settings remain exactly as defined in `service.yaml`.

### service.yaml Structure

```yaml
apiVersion: serving.knative.dev/v1
kind: Service
metadata:
  name: <language>-lamp-control-api          # Cloud Run service name
  annotations:
    run.googleapis.com/launch-stage: ALPHA   # required for multi-container support
spec:
  template:
    metadata:
      annotations:
        autoscaling.knative.dev/maxScale: '1'
        run.googleapis.com/startup-cpu-boost: 'true'
        # collector must be ready before app starts
        run.googleapis.com/container-dependencies: "{app:[collector]}"
    spec:
      containers:
        - name: app
          image: us-docker.pkg.dev/cloudrun/container/hello:latest  # placeholder; replaced by Cloud Build
          ports:
            - containerPort: <port>          # language-specific (see table below)
          startupProbe:
            tcpSocket:
              port: <port>
            periodSeconds: 10
            failureThreshold: 3
            timeoutSeconds: 1
          env:
            - name: OTEL_EXPORTER_OTLP_ENDPOINT
              value: http://localhost:4317    # sidecar on loopback
            - name: OTEL_SERVICE_NAME
              value: lamp-control-api-<language>
        - name: collector
          image: us-docker.pkg.dev/cloud-ops-agents-artifacts/google-cloud-opentelemetry-collector/otelcol-google:0.147.0
          args:
            - --config=/etc/otelcol-google/config.yaml
          startupProbe:
            httpGet:
              path: /
              port: 13133                    # collector health endpoint
            timeoutSeconds: 30
            periodSeconds: 30
          livenessProbe:
            httpGet:
              path: /
              port: 13133
            timeoutSeconds: 30
            periodSeconds: 30
          volumeMounts:
            - mountPath: /etc/otelcol-google/
              name: config
      volumes:
        - name: config
          secret:
            secretName: otel-collector-config
            items:
              - key: latest
                path: config.yaml
```

### Port Reference

| Language   | Service Name                   | containerPort |
|------------|--------------------------------|---------------|
| TypeScript | `typescript-lamp-control-api`  | 3000          |
| Python     | `python-lamp-control-api`      | 8000          |
| Go         | `go-lamp-control-api`          | 8080          |
| Java       | `java-lamp-control-api`        | 8081          |
| Kotlin     | `kotlin-lamp-control-api`      | 8082          |
| C#         | `csharp-lamp-control-api`      | 8083          |

### Updating the Collector Config

If you change `otel-collector-config-gcp.yaml`, push a new secret version and redeploy:

```bash
gcloud secrets versions add otel-collector-config \
  --data-file=otel-collector-config-gcp.yaml \

# No service.yaml change needed — the secret mount always uses 'latest' version
# Restart the service to pick up the new config:
gcloud run services update <service-name> \
  --region=YOUR_REGION \
  --quiet
```
