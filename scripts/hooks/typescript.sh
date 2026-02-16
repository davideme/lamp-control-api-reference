#!/usr/bin/env bash

set -euo pipefail

ROOT="$(git rev-parse --show-toplevel)"
# shellcheck source=scripts/hooks/common.sh
source "$ROOT/scripts/hooks/common.sh"

require_cmd npm

cd "$ROOT/src/typescript"
echo "[pre-commit] Running TypeScript lint auto-fix"
npm run lint -- --fix

cd "$ROOT"
restage_and_fail_if_changed "src/typescript"
