# Cloud Run Single-Instance Benchmark Harness

This directory contains a benchmark harness for comparing the six language implementations with Cloud Run autoscaling neutralized (`max instances = 1`).

## What it implements

- Two benchmark passes:
  - `memory` pass (runtime/framework signal)
  - `db` pass (realistic signal)
- Fixed, fairness-first Cloud Run settings for all services
- Main ranking at fixed concurrency pressure with `concurrency=80`
- Optional non-ranking extreme run at concurrency `1000`
- Sequential service execution (no parallel cross-service load)
- Raw k6 result exports and generated markdown summary

## Files

- `config.json`: benchmark parameters and Cloud Run parity settings
- `services.json`: service URLs and per-service DB seed/reset hooks
- `scenarios.js`: k6 workload script (read-heavy CRUD mix)
- `run-benchmarks.js`: orchestration script for full memory+db benchmark execution
- `configure-cloud-run.js`: applies identical Cloud Run settings (dry-run by default)
- `generate-summary.js`: regenerates `benchmarks/results/summary.md` from `run-report.json`

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
- `dbSeedCommand`: optional shell command to reset+seed DB before each DB run

Example `dbSeedCommand`:

```bash
./scripts/seed-benchmark-db.sh typescript 10000
```

If memory and DB use the same URL with an env-var mode switch, set both URLs equal and use setup commands.
Example:

```bash
gcloud run services update typescript-lamp-control-api --region europe-west1 --update-env-vars STORAGE_MODE=memory
gcloud run services update typescript-lamp-control-api --region europe-west1 --update-env-vars STORAGE_MODE=db
```

## 2) Validate or apply Cloud Run parity settings

Dry run (prints commands):

```bash
node benchmarks/k6/configure-cloud-run.js --project <gcp-project-id>
```

Apply settings:

```bash
node benchmarks/k6/configure-cloud-run.js --project <gcp-project-id> --execute
```

Settings come from `benchmarks/k6/config.json` under `cloudRun`.

## 3) Run benchmark

Run both passes (`memory`,`db`) with settings from `config.json`:

```bash
node benchmarks/k6/run-benchmarks.js
```

Run only memory pass:

```bash
node benchmarks/k6/run-benchmarks.js --passes memory
```

Outputs:

- Raw k6 JSON: `benchmarks/results/raw/<run-id>/...`
- Structured run report: `benchmarks/results/run-report.json`
- Ranked markdown summary: `benchmarks/results/summary.md`

## 4) Rebuild summary only

```bash
node benchmarks/k6/generate-summary.js benchmarks/results/run-report.json benchmarks/results/summary.md
```

## Notes on fairness and interpretation

- Keep Cloud Run settings identical across all six languages in the ranking run.
- Concurrency is a major factor even with `max instances=1`; it controls in-container contention.
- Use memory pass ranking to isolate runtime/framework signal.
- Use DB pass ranking to understand production-like behavior and DB bottleneck impact.
- Treat concurrency `1000` as saturation appendix, not primary ranking.
