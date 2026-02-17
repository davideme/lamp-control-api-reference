#!/usr/bin/env bash

set -euo pipefail

ROOT="$(git rev-parse --show-toplevel)"
# shellcheck source=scripts/hooks/common.sh
source "$ROOT/scripts/hooks/common.sh"

require_cmd sqlfluff

sql_files=()
for file in "$@"; do
  if [[ "$file" =~ ^database/sql/postgresql/.*\.sql$ ]]; then
    sql_files+=("$file")
  fi
done

if [[ ${#sql_files[@]} -eq 0 ]]; then
  exit 0
fi

cd "$ROOT"
echo "[pre-commit] Running SQLFluff auto-fix"
sqlfluff fix "${sql_files[@]}" --dialect postgres --exclude-rules LT01

restage_and_fail_if_changed "database/sql/postgresql"

echo "[pre-commit] Running SQLFluff lint"
sqlfluff lint "${sql_files[@]}" --dialect postgres --exclude-rules LT01
