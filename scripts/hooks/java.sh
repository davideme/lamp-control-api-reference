#!/usr/bin/env bash

set -euo pipefail

ROOT="$(git rev-parse --show-toplevel)"
# shellcheck source=scripts/hooks/common.sh
source "$ROOT/scripts/hooks/common.sh"

require_cmd mvn

cd "$ROOT/src/java"
echo "[pre-commit] Running Java format auto-fix"
mvn spotless:apply

cd "$ROOT"
restage_and_fail_if_changed "src/java"

cd "$ROOT/src/java"
echo "[pre-commit] Running Java lint checks"
mvn spotless:check
mvn pmd:check
mvn checkstyle:check
