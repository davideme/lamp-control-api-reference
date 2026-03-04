# JIT Warmup Profiling Suite

Measures how latency evolves over time as JVM (Java/Kotlin), V8 (TypeScript), and .NET (C#) JIT compilers warm up. Go and Python serve as flat-baseline references — they have no JIT.

The benchmark runs sequential 30-second windows of load against all 6 Cloud Run services in parallel. Because Cloud Run containers stay alive between windows, JIT state accumulates across the entire run duration.

## How it works

```
run-profile.sh
  └─ for each window (default: 10 × 30s = 5 min total):
       k6 run profile.js   ← hits all 6 services simultaneously at TARGET_RPS each
       writes results/window_N.json
  └─ node report.js        ← reads all window_*.json, writes report.md + report.csv
```

Each window JSON contains p50/p95/p99/avg latencies per language. `report.js` then
annotates the tables with expected JIT tier transitions (C1, C2, TurboFan, etc.) based
on the thresholds in `config.json`.

## Prerequisites

| Tool | Install |
|------|---------|
| `k6` | `brew install k6` or [k6.io/docs/getting-started/installation](https://k6.io/docs/getting-started/installation/) |
| `jq` | `brew install jq` |
| `curl` | pre-installed on macOS/Linux |
| `node` (≥18) | for `report.js` only |
| `gcloud` CLI | only required with `--cold-start` or `--setup-memory` |

## Quick start

```bash
cd benchmarks/jit-profile

# Standard run (warm services, default config: 50 RPS × 10 windows × 30s)
./run-profile.sh

# Results appear in:
#   results/report.md   ← Markdown tables with JIT annotations
#   results/report.csv  ← Raw data for further analysis
#   results/run-meta.json
#   results/window_0.json … window_9.json
```

## Options

```
./run-profile.sh [--cold-start] [--rps N] [--windows N] [--setup-memory]
```

| Flag | Description | Default |
|------|-------------|---------|
| `--rps N` | Requests per second per language | `50` (from `config.json`) |
| `--windows N` | Number of 30-second measurement windows | `10` (from `config.json`) |
| `--cold-start` | Scale all Cloud Run services to 0 instances first, forcing a fresh JVM/runtime | `false` |
| `--setup-memory` | Re-configure all services to in-memory mode before starting | `false` |

Both `--cold-start` and `--setup-memory` require:
- `gcloud` CLI authenticated (`gcloud auth login`)
- `GOOGLE_CLOUD_PROJECT` environment variable set

### Examples

```bash
# Force cold start to see full JIT ramp-up from zero
GOOGLE_CLOUD_PROJECT=my-project ./run-profile.sh --cold-start

# Higher load, more windows
./run-profile.sh --rps 100 --windows 20

# Full reset: in-memory mode + cold start + extended run
GOOGLE_CLOUD_PROJECT=my-project ./run-profile.sh --setup-memory --cold-start --windows 15

# Re-generate the report from existing window files (no k6 run)
node report.js
```

## Configuration (`config.json`)

```json
{
  "target_rps_per_language": 50,
  "num_windows": 10,
  "window_seconds": 30
}
```

Edit this file to change defaults. CLI flags always override `config.json` values.

The `jit_thresholds` array defines the expected JIT tier activation points (in method
invocations). At 50 RPS these translate to:

| Tier | ~Time | Window |
|------|-------|--------|
| C# Tier 1 | < 1s | t=0s |
| Java/Kotlin C1 | ~40s | t=30s |
| V8 TurboFan (TypeScript) | ~120s | t=90–120s |
| Java/Kotlin C2 | ~200s | t=180–210s |

## Understanding the output

`report.md` contains three latency tables (p95, p50, p99) plus a warmup summary:

```
| Language   |   t=0s |  t=30s |  t=60s | ...
|------------|--------|--------|--------|
| Java       |  245ms |  89ms ← Java/Kotlin C1 | ...
```

The `← Tier name` annotations mark windows where a JIT compiler tier is expected to
have activated, explaining latency drops visible in the data.

Go and Python show flat latency throughout — they are AOT/interpreted and serve as
stable baselines.

## Troubleshooting

### "No window_*.json files found"

This error comes from `report.js` when the `results/` directory contains no
`window_N.json` files. Common causes:

**1. k6 never ran — run `run-profile.sh`, not `report.js` directly.**

`report.js` is only a report generator. The window files are produced by the k6 runs
orchestrated by `run-profile.sh`. Running `node report.js` standalone before a benchmark
run will always produce this error.

**2. k6 exited before writing results.**

`run-profile.sh` uses `set -euo pipefail` so any prerequisite failure (missing k6,
missing `services.json`, service warmup failure returning non-zero) will abort the
script before any k6 window runs. Check the terminal output for the specific error.

**3. k6 ran but `handleSummary` silently failed to write files.**

`profile.js` writes window files from k6's `handleSummary` callback using the
`RESULTS_DIR` env var (absolute path). If the `results/` directory doesn't exist or
isn't writable, k6 will not error out but the files won't appear.

Verify:
```bash
ls -la benchmarks/jit-profile/results/
# The directory must exist (the .gitkeep file ensures it is committed)
# Confirm write permissions if running as a different user
```

**4. Wrong working directory.**

The script must be run from its own directory or called with its full path. It uses
`SCRIPT_DIR` internally so calling it from any location is fine:

```bash
# Both of these work:
./benchmarks/jit-profile/run-profile.sh
cd benchmarks/jit-profile && ./run-profile.sh
```

**5. Services did not respond during warmup.**

If all services fail the warmup probe, the script prints a warning and continues. k6
will then run against unreachable URLs and `handleSummary` may produce empty metric
objects. The window files are still written but all values will be `null`, which is
valid — `report.js` renders them as `—`.

Check that the service URLs in `../k6/services.json` are reachable:
```bash
curl -s https://go-lamp-control-api-<id>.europe-west1.run.app/v1/lamps?pageSize=1
```

### k6 reports "no active scenarios"

All service URLs are empty strings. Verify `../k6/services.json` has `memoryUrl` values
populated for each language.

### Report shows all `—` values

The window files exist but all metric values are null. This means k6 ran but received
no valid responses. Check that:
- Services are deployed and healthy (`/health` endpoint returns 200)
- Services are in **in-memory mode** (no `DATABASE_URL` env var) if you want to isolate
  JIT performance from database latency. Use `--setup-memory` to configure this.
