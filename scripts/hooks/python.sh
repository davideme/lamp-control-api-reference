#!/usr/bin/env bash

set -euo pipefail

ROOT="$(git rev-parse --show-toplevel)"
# shellcheck source=scripts/hooks/common.sh
source "$ROOT/scripts/hooks/common.sh"

require_cmd poetry

py_files=()
for file in "$@"; do
  if [[ "$file" == src/python/*.py ]]; then
    py_files+=("${file#src/python/}")
  fi
done

if [[ ${#py_files[@]} -eq 0 ]]; then
  exit 0
fi

cd "$ROOT/src/python"
echo "[pre-commit] Running Python format auto-fix"
poetry run black "${py_files[@]}"
poetry run ruff check "${py_files[@]}" --fix

cd "$ROOT"
restage_and_fail_if_changed "src/python"

cd "$ROOT/src/python"
echo "[pre-commit] Running Python lint checks"
poetry run black --check --diff "${py_files[@]}"
poetry run ruff check "${py_files[@]}"
