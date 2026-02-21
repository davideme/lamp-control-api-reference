# Cloud Run Single-Instance Benchmark Harness

This directory contains a benchmark harness for comparing the six language implementations with Cloud Run autoscaling neutralized (`max instances = 1`).

## What it implements

- Two benchmark passes:
  - `memory` pass (runtime/framework signal)
  - `db` pass (realistic signal)
- Fixed, fairness-first Cloud Run settings for all services
- Main ranking at fixed concurrency pressure with `concurrency=80`
- Separate cold-start appendix (not used in primary ranking)
- Optional non-ranking extreme run at `1000 RPS` (disabled by default)
- Sequential service execution (no parallel cross-service load)
- Raw k6 result exports and generated markdown summary

## Files

- `config.json`: benchmark parameters and Cloud Run parity settings
- `config.fast.json`: shorter benchmark profile for quicker p95-focused comparisons
- `services.json`: service URLs and per-service DB seed/reset hooks
- `scenarios.js`: k6 workload script (read-heavy CRUD mix)
- `run-benchmarks.js`: orchestration script for full memory+db benchmark execution
- `configure-cloud-run.js`: applies identical Cloud Run settings (dry-run by default)
- `generate-summary.js`: regenerates `benchmarks/results/summary.md` from `run-report.json`
- `summary.js`: shared summary rendering used by benchmark scripts

## Prerequisites

- `k6` installed and in `PATH`
- Node.js 20+ (uses built-in `fetch`)
- Network access from benchmark runner to all Cloud Run URLs
- If running configuration step: authenticated `gcloud` CLI and project access

## 1) Fill service map

Edit `benchmarks/k6/services.json`:

- `memoryUrl`: Cloud Run URL for memory-backed deployment (no `/v1` suffix needed)
- `dbUrl`: Cloud Run URL for DB-backed deployment (no `/v1` suffix needed)
- `cloudRunService`: Cloud Run service name for settings updates
- `cloudRunRegion`: region (default `us-central1`)
- `memorySetupCommand`: optional command run before memory pass for this service
- `dbSetupCommand`: optional command run before DB pass for this service
- `dbSeedCommand`: optional per-service override shell command to reset+seed DB before each DB run

Default seeding is configured once in `benchmarks/k6/config.json` as `defaultDbSeedCommand`:

```bash
psql "$BENCHMARK_DATABASE_URL" -v ON_ERROR_STOP=1 -c "TRUNCATE TABLE lamps RESTART IDENTITY CASCADE; INSERT INTO lamps (id, is_on, created_at, updated_at, deleted_at) SELECT gen_random_uuid(), (g % 2 = 0), NOW() - ((10001 - g) * INTERVAL '1 second'), NOW() - ((10001 - g) * INTERVAL '1 second'), NULL FROM generate_series(1, 10000) AS g;"
```

Set `dbSeedCommand` in a service entry only when that service needs a custom seed/reset flow.

If memory and DB use the same URL, run passes sequentially and toggle env vars via setup commands.
For this repository, DB mode is enabled by connection settings (for example `DATABASE_URL` or language-specific equivalents).
Example (TypeScript/Go/Python/Kotlin):

```bash
gcloud run services update typescript-lamp-control-api --region europe-west1 --remove-env-vars DATABASE_URL
gcloud run services update typescript-lamp-control-api --region europe-west1 --update-env-vars DATABASE_URL="$BENCHMARK_DATABASE_URL"
```

Before running benchmarks, export required variables:

```bash
export BENCHMARK_DATABASE_URL='postgresql://<user>:<pass>@<host>:5432/<database>?sslmode=require'
export BENCHMARK_JDBC_DATABASE_URL='jdbc:postgresql://<host>:5432/<database>'
export BENCHMARK_DB_USER='<db-user>'
export BENCHMARK_DB_PASSWORD='<db-password>'
export BENCHMARK_CSHARP_CONNECTION_STRING='Host=<host>;Port=5432;Database=<database>;Username=<db-user>;Password=<db-password>'
```

`GOOGLE_CLOUD_PROJECT` is optional for `run-benchmarks.js`; if unset, it uses `cloudRun.projectId` from `benchmarks/k6/config.json`.

## 2) Run from a GCP VM (recommended)

Create a runner VM in the same region and install required tools:

```bash
gcloud compute instances create lamp-bench-runner \
  --project=<YOUR_PROJECT_ID> \
  --zone=europe-west1-b \
  --machine-type=e2-standard-4 \
  --image-family=ubuntu-2204-lts \
  --image-project=ubuntu-os-cloud \
  --boot-disk-size=30GB \
  --metadata=startup-script='#!/usr/bin/env bash
set -euxo pipefail
export DEBIAN_FRONTEND=noninteractive

apt-get update
apt-get install -y ca-certificates curl gnupg git jq postgresql-client

curl -fsSL https://deb.nodesource.com/setup_20.x | bash -
apt-get install -y nodejs

curl -fsSL https://dl.k6.io/key.gpg | gpg --dearmor -o /usr/share/keyrings/k6-archive-keyring.gpg
echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" > /etc/apt/sources.list.d/k6.list
apt-get update
apt-get install -y k6

node --version
npm --version
k6 version
psql --version
'
```

Then connect:

```bash
gcloud compute ssh lamp-bench-runner --project=<YOUR_PROJECT_ID> --zone=europe-west1-b
```

## 3) Validate or apply Cloud Run parity settings

Dry run (prints commands):

```bash
node benchmarks/k6/configure-cloud-run.js
```

Apply settings:

```bash
node benchmarks/k6/configure-cloud-run.js --execute
```

Settings come from `benchmarks/k6/config.json` under `cloudRun`.
Project resolution order is: `--project`, `cloudRun.projectId`, `cloudRun.projectNumber`, `GOOGLE_CLOUD_PROJECT`.
Startup probe applied to all services:

```yaml
startupProbe:
  timeoutSeconds: 1
  periodSeconds: 10
  failureThreshold: 3
  tcpSocket:
    port: 8080
```

For meaningful cold-start sampling, keep `cloudRun.minInstances=0`.

## 4) Run benchmark

Run both passes (`memory`,`db`) with settings from `config.json`:

```bash
node benchmarks/k6/run-benchmarks.js
```

Run both passes with the faster profile:

```bash
node benchmarks/k6/run-benchmarks.js --config benchmarks/k6/config.fast.json
```

Run only memory pass:

```bash
node benchmarks/k6/run-benchmarks.js --passes memory
```

Run benchmark without running setup commands (`memorySetupCommand` / `dbSetupCommand`):

```bash
node benchmarks/k6/run-benchmarks.js --passes memory --skip-setup
```

Run from macOS without sleep interruptions:

```bash
caffeinate -i node benchmarks/k6/run-benchmarks.js
```

Fast profile on macOS:

```bash
caffeinate -i node benchmarks/k6/run-benchmarks.js --config benchmarks/k6/config.fast.json
```

Disable cold-start appendix for quick local iterations:

```bash
node benchmarks/k6/run-benchmarks.js --config benchmarks/k6/config.fast.json
# then set coldStart.enabled=false in the selected config
```

Runtime behavior:
- Cold-start probe runs before warmup/fixed/stress and is reported separately.
- Cold-start probe waits optional cooldown (`coldStart.cooldownSeconds`) to improve scale-to-zero likelihood.
- Precheck CRUD uses retry with exponential backoff (up to 7 attempts total).
- If an iteration still fails (precheck, k6, or setup error), the runner logs the error, records the failed iteration in `run-report.json`, and continues with the next iteration/service.

Enable the extreme appendix run:

```bash
# Set benchmarks/k6/config.json -> extreme.enabled to true
```

Outputs:

- Raw k6 JSON: `benchmarks/results/raw/<run-id>/...`
- Structured run report: `benchmarks/results/run-report.json`
- Ranked markdown summary: `benchmarks/results/summary.md`
- Cold-start artifact per sampled iteration: `benchmarks/results/raw/<run-id>/<pass>/<service>/iter-*/cold-start.json`

## 5) Rebuild summary only

```bash
node benchmarks/k6/generate-summary.js benchmarks/results/run-report.json benchmarks/results/summary.md
```

## Notes on fairness and interpretation

- Keep Cloud Run settings identical across all six languages in the ranking run.
- Concurrency is a major factor even with `max instances=1`; it controls in-container contention.
- Use memory pass ranking to isolate runtime/framework signal.
- Use DB pass ranking to understand production-like behavior and DB bottleneck impact.
- Treat extreme `1000 RPS` run as saturation appendix, not primary ranking.
- Use `config.fast.json` for iterative checks and `config.json` for final publication-quality runs.
