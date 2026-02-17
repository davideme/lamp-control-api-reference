#!/usr/bin/env bash

set -euo pipefail

ROOT="$(git rev-parse --show-toplevel)"
# shellcheck source=scripts/hooks/common.sh
source "$ROOT/scripts/hooks/common.sh"

require_cmd dotnet

cd "$ROOT/src/csharp"
echo "[pre-commit] Restoring C# dependencies"
dotnet restore --locked-mode

echo "[pre-commit] Running C# format auto-fix"
dotnet format

cd "$ROOT"
restage_and_fail_if_changed "src/csharp"

cd "$ROOT/src/csharp"
echo "[pre-commit] Running C# lint checks"
dotnet format --verify-no-changes --verbosity diagnostic
dotnet build --verbosity minimal --configuration Release --no-restore
