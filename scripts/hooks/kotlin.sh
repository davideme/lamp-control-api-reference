#!/usr/bin/env bash

set -euo pipefail

ROOT="$(git rev-parse --show-toplevel)"
# shellcheck source=scripts/hooks/common.sh
source "$ROOT/scripts/hooks/common.sh"

if [[ ! -x "$ROOT/src/kotlin/gradlew" ]]; then
  echo "[pre-commit] Gradle wrapper not found or not executable: $ROOT/src/kotlin/gradlew"
  exit 1
fi

cd "$ROOT/src/kotlin"
echo "[pre-commit] Running Kotlin format auto-fix"
./gradlew ktlintFormat --console=plain

cd "$ROOT"
restage_and_fail_if_changed "src/kotlin"

cd "$ROOT/src/kotlin"
echo "[pre-commit] Running Kotlin lint checks"
./gradlew ktlintCheck detekt --console=plain
