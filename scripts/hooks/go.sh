#!/usr/bin/env bash

set -euo pipefail

ROOT="$(git rev-parse --show-toplevel)"
# shellcheck source=scripts/hooks/common.sh
source "$ROOT/scripts/hooks/common.sh"

require_cmd go
require_cmd gofmt
require_cmd golangci-lint

go_files=()
for file in "$@"; do
  if [[ "$file" =~ ^src/go/.*\.go$ ]]; then
    go_files+=("${file#src/go/}")
  fi
done

if [[ ${#go_files[@]} -eq 0 ]]; then
  exit 0
fi

cd "$ROOT/src/go"
echo "[pre-commit] Running gofmt auto-fix"
gofmt -w "${go_files[@]}"

cd "$ROOT"
restage_and_fail_if_changed "src/go"

cd "$ROOT/src/go"
echo "[pre-commit] Running Go lint checks"
go_pkgs=()
for file in "${go_files[@]}"; do
  dir="$(dirname "$file")"
  if [[ "$dir" == "." ]]; then
    pkg="./"
  else
    pkg="./$dir"
  fi

  exists=0
  for existing in "${go_pkgs[@]}"; do
    if [[ "$existing" == "$pkg" ]]; then
      exists=1
      break
    fi
  done
  if [[ $exists -eq 0 ]]; then
    go_pkgs+=("$pkg")
  fi
done

golangci-lint run --timeout=5m "${go_pkgs[@]}"
go vet "${go_pkgs[@]}"
