#!/usr/bin/env bash

set -euo pipefail

ROOT="$(git rev-parse --show-toplevel)"
# shellcheck source=scripts/hooks/common.sh
source "$ROOT/scripts/hooks/common.sh"

require_cmd npm

ts_files=()
for file in "$@"; do
  if [[ "$file" =~ ^src/typescript/.*\.ts$ ]]; then
    ts_files+=("${file#src/typescript/}")
  fi
done

if [[ ${#ts_files[@]} -eq 0 ]]; then
  exit 0
fi

cd "$ROOT/src/typescript"
echo "[pre-commit] Running TypeScript lint auto-fix on staged files"
npm exec eslint -- --fix "${ts_files[@]}"

cd "$ROOT"
restage_and_fail_if_changed "src/typescript"
