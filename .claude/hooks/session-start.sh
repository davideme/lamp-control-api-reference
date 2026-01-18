#!/bin/bash
set -euo pipefail

# Only run in remote environment (Claude Code on the web)
if [ "${CLAUDE_CODE_REMOTE:-}" != "true" ]; then
  exit 0
fi

echo "🚀 Setting up development environment..."

# Go setup
echo "📦 Setting up Go dependencies..."
cd "$CLAUDE_PROJECT_DIR/src/go"
go mod download
go mod tidy

# Install golangci-lint if not available
if ! command -v golangci-lint &> /dev/null; then
  echo "🔧 Installing golangci-lint..."
  curl -sSfL https://raw.githubusercontent.com/golangci/golangci-lint/master/install.sh | sh -s -- -b $(go env GOPATH)/bin
  # Add GOPATH/bin to PATH for this session
  echo "export PATH=\$PATH:$(go env GOPATH)/bin" >> "$CLAUDE_ENV_FILE"
fi

# Java/Maven setup
echo "📦 Setting up Java/Maven dependencies..."
cd "$CLAUDE_PROJECT_DIR/src/java"
mvn dependency:resolve -q

# Python/Poetry setup
echo "📦 Setting up Python/Poetry dependencies..."
cd "$CLAUDE_PROJECT_DIR/src/python"
poetry install --no-interaction

# Return to project root
cd "$CLAUDE_PROJECT_DIR"

echo "✅ Development environment ready!"
echo ""
echo "Available linters:"
echo "  - Go: golangci-lint"
echo "  - Java: mvn spotless:check"
echo "  - Python: poetry run black . && poetry run ruff check . --fix"
