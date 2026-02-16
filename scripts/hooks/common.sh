#!/usr/bin/env bash

set -euo pipefail

repo_root() {
  git rev-parse --show-toplevel
}

restage_and_fail_if_changed() {
  local prefix="$1"
  local changed

  changed=$(git diff --name-only -- "$prefix")
  if [[ -z "$changed" ]]; then
    return 0
  fi

  echo "$changed" | while IFS= read -r file; do
    [[ -z "$file" ]] && continue
    git add "$file"
  done

  echo "[pre-commit] Auto-fixed files were re-staged under $prefix."
  echo "[pre-commit] Review changes and run commit again."
  exit 1
}

require_cmd() {
  local cmd="$1"
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "[pre-commit] Required command not found: $cmd"
    exit 1
  fi
}
