#!/usr/bin/env bash
# run-profile.sh — JIT warmup profiling benchmark orchestrator
#
# Runs sequential 30-second k6 windows against all 6 Cloud Run services in
# parallel within each window. Cloud Run containers stay alive between windows,
# so JIT state accumulates across the full benchmark duration.
#
# Usage:
#   ./run-profile.sh [--cold-start] [--rps N] [--windows N] [--setup-memory]
#
# Options:
#   --cold-start      Scale services to 0 instances before starting (forces fresh JVM)
#   --rps N           Requests per second per language (default: from config.json)
#   --windows N       Number of 30-second windows (default: from config.json)
#   --setup-memory    Configure all Cloud Run services for in-memory mode first
#
# Requirements:
#   - k6 installed (https://k6.io/docs/getting-started/installation/)
#   - jq installed
#   - gcloud CLI authenticated (for --cold-start or --setup-memory)
#   - GOOGLE_CLOUD_PROJECT env var set (for gcloud commands)

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SERVICES_JSON="${SCRIPT_DIR}/../k6/services.json"
RESULTS_DIR="${SCRIPT_DIR}/results"
CONFIG_JSON="${SCRIPT_DIR}/config.json"

# ─── Defaults (loaded from config.json, then overridable via CLI flags) ────────
TARGET_RPS=50
NUM_WINDOWS=10
WINDOW_DURATION=30
COLD_START=false
SETUP_MEMORY=false

# Wire defaults from config.json (single source of truth for benchmark settings).
# CLI flags parsed below still take precedence.
if [[ -f "$CONFIG_JSON" ]] && command -v jq &>/dev/null; then
  cfg=$(jq -r '[.target_rps_per_language, .num_windows, .window_seconds] | @sh' "$CONFIG_JSON" 2>/dev/null || true)
  if [[ -n "$cfg" ]]; then
    read -r cfg_rps cfg_windows cfg_wsec <<< "$cfg"
    TARGET_RPS="${cfg_rps//\'/}"
    NUM_WINDOWS="${cfg_windows//\'/}"
    WINDOW_DURATION="${cfg_wsec//\'/}"
  fi
fi

# ─── Argument parsing ─────────────────────────────────────────────────────────
while [[ $# -gt 0 ]]; do
  case "$1" in
    --cold-start) COLD_START=true; shift ;;
    --setup-memory) SETUP_MEMORY=true; shift ;;
    --rps) TARGET_RPS="$2"; shift 2 ;;
    --windows) NUM_WINDOWS="$2"; shift 2 ;;
    *) echo "Unknown option: $1" >&2; exit 1 ;;
  esac
done

# ─── Prerequisites check ─────────────────────────────────────────────────────
if ! command -v k6 &>/dev/null; then
  echo "ERROR: k6 not found. Install from https://k6.io/docs/getting-started/installation/" >&2
  exit 1
fi

if ! command -v jq &>/dev/null; then
  echo "ERROR: jq not found. Install with: brew install jq" >&2
  exit 1
fi

# gcloud is only required when --cold-start or --setup-memory is set.
if [[ "$COLD_START" == "true" || "$SETUP_MEMORY" == "true" ]]; then
  if ! command -v gcloud &>/dev/null; then
    echo "ERROR: gcloud CLI not found. Install from https://cloud.google.com/sdk/docs/install and ensure you are authenticated." >&2
    exit 1
  fi
fi

if [[ ! -f "$SERVICES_JSON" ]]; then
  echo "ERROR: services.json not found at $SERVICES_JSON" >&2
  exit 1
fi

# ─── Read service URLs ────────────────────────────────────────────────────────
get_memory_url() {
  jq -r --arg name "$1" '.[] | select(.name == $name) | .memoryUrl' "$SERVICES_JSON"
}

get_service_name() {
  jq -r --arg name "$1" '.[] | select(.name == $name) | .cloudRunService' "$SERVICES_JSON"
}

get_region() {
  jq -r --arg name "$1" '.[] | select(.name == $name) | .cloudRunRegion' "$SERVICES_JSON"
}

TS_URL=$(get_memory_url "typescript")
PY_URL=$(get_memory_url "python")
GO_URL=$(get_memory_url "go")
JAVA_URL=$(get_memory_url "java")
KT_URL=$(get_memory_url "kotlin")
CS_URL=$(get_memory_url "csharp")

echo "=== JIT Warmup Profile ==="
echo "RPS per language : ${TARGET_RPS}"
echo "Windows          : ${NUM_WINDOWS} × ${WINDOW_DURATION}s = $((NUM_WINDOWS * WINDOW_DURATION))s total"
echo "Services:"
echo "  TypeScript : ${TS_URL}"
echo "  Python     : ${PY_URL}"
echo "  Go         : ${GO_URL}"
echo "  Java       : ${JAVA_URL}"
echo "  Kotlin     : ${KT_URL}"
echo "  C#         : ${CS_URL}"
echo ""

# ─── Optional: configure services for in-memory mode ────────────────────────
if [[ "$SETUP_MEMORY" == "true" ]]; then
  if [[ -z "${GOOGLE_CLOUD_PROJECT:-}" ]]; then
    echo "ERROR: GOOGLE_CLOUD_PROJECT must be set for --setup-memory" >&2
    exit 1
  fi

  echo ">>> Configuring all services for in-memory mode..."
  while IFS= read -r cmd; do
    bash -c "$cmd"
  done < <(jq -r '.[].memorySetupCommand' "$SERVICES_JSON")
  echo ">>> Memory mode configured. Waiting 30s for changes to propagate..."
  sleep 30
fi

# ─── Optional: cold start (scale all services to 0 instances) ────────────────
if [[ "$COLD_START" == "true" ]]; then
  if [[ -z "${GOOGLE_CLOUD_PROJECT:-}" ]]; then
    echo "ERROR: GOOGLE_CLOUD_PROJECT must be set for --cold-start" >&2
    exit 1
  fi

  echo ">>> Scaling all services to 0 instances for cold start..."
  for lang in typescript python go java kotlin csharp; do
    svc=$(get_service_name "$lang")
    region=$(get_region "$lang")
    echo "  Scaling ${svc} to 0..."
    gcloud run services update "$svc" \
      --project "$GOOGLE_CLOUD_PROJECT" \
      --region "$region" \
      --min-instances=0 \
      --max-instances=1 \
      --quiet
  done

  echo ">>> Waiting 60s for instances to drain..."
  sleep 60
  echo ">>> Services are cold. First k6 request will trigger cold start."
fi

# ─── Prepare results directory ────────────────────────────────────────────────
mkdir -p "$RESULTS_DIR"

# Save run metadata
cat > "${RESULTS_DIR}/run-meta.json" <<EOF
{
  "timestamp": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")",
  "target_rps": ${TARGET_RPS},
  "num_windows": ${NUM_WINDOWS},
  "window_seconds": ${WINDOW_DURATION},
  "cold_start": ${COLD_START},
  "services": {
    "typescript": "${TS_URL}",
    "python": "${PY_URL}",
    "go": "${GO_URL}",
    "java": "${JAVA_URL}",
    "kotlin": "${KT_URL}",
    "csharp": "${CS_URL}"
  }
}
EOF

# ─── Run measurement windows ──────────────────────────────────────────────────
echo ">>> Starting benchmark at $(date -u +"%H:%M:%S UTC")"
echo ""

for window in $(seq 0 $((NUM_WINDOWS - 1))); do
  elapsed=$((window * WINDOW_DURATION))
  echo "--- Window ${window} (elapsed: ${elapsed}s) ---"

  # Run k6 from SCRIPT_DIR so the relative results/ path in profile.js resolves
  # correctly regardless of the caller's working directory.
  ( cd "${SCRIPT_DIR}" && k6 run \
      --env "TS_URL=${TS_URL}" \
      --env "PY_URL=${PY_URL}" \
      --env "GO_URL=${GO_URL}" \
      --env "JAVA_URL=${JAVA_URL}" \
      --env "KT_URL=${KT_URL}" \
      --env "CS_URL=${CS_URL}" \
      --env "TARGET_RPS=${TARGET_RPS}" \
      --env "WINDOW=${window}" \
      --env "WINDOW_DURATION=${WINDOW_DURATION}s" \
      --env "RESULTS_DIR=${RESULTS_DIR}" \
      --no-summary \
      "${SCRIPT_DIR}/profile.js" \
  )

  echo "  Window ${window} saved to ${RESULTS_DIR}/window_${window}.json"
done

echo ""
echo ">>> All windows complete at $(date -u +"%H:%M:%S UTC")"
echo ""

# ─── Generate report ─────────────────────────────────────────────────────────
echo ">>> Generating report..."
node "${SCRIPT_DIR}/report.js"

echo ""
echo "=== Done ==="
echo "Results : ${RESULTS_DIR}/report.md"
echo "CSV     : ${RESULTS_DIR}/report.csv"
