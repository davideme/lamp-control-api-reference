#!/bin/bash
set -uo pipefail  # Removed -e to allow continuing on errors

# Only run in remote environment (Claude Code on the web)
if [ "${CLAUDE_CODE_REMOTE:-}" != "true" ]; then
  exit 0
fi

echo "🚀 Setting up development environment..."

# Go setup
echo "📦 Setting up Go dependencies..."
cd "$CLAUDE_PROJECT_DIR/src/go"
if go mod download >/dev/null 2>&1; then
  go mod tidy >/dev/null 2>&1 || true
  echo "✓ Go dependencies ready"
else
  echo "⚠ Network unavailable - Go dependencies will be downloaded on first use"
fi

# Install golangci-lint if not available
if ! command -v golangci-lint &> /dev/null; then
  echo "🔧 Installing golangci-lint..."
  if curl -sSfL https://raw.githubusercontent.com/golangci/golangci-lint/master/install.sh 2>/dev/null | sh -s -- -b $(go env GOPATH)/bin >/dev/null 2>&1; then
    # Add GOPATH/bin to PATH for this session
    echo "export PATH=\$PATH:$(go env GOPATH)/bin" >> "$CLAUDE_ENV_FILE"
    echo "✓ golangci-lint installed"
  else
    echo "⚠ Network unavailable - golangci-lint not installed"
  fi
fi

# Java/Maven setup
echo "📦 Setting up Java/Maven dependencies..."
cd "$CLAUDE_PROJECT_DIR/src/java"
if mvn dependency:resolve -q >/dev/null 2>&1; then
  echo "✓ Java dependencies ready"
else
  echo "⚠ Network unavailable - Maven dependencies will be downloaded on first use"
fi

# Kotlin/Gradle setup
echo "📦 Setting up Kotlin/Gradle dependencies..."
cd "$CLAUDE_PROJECT_DIR/src/kotlin"
if ./gradlew dependencies --quiet >/dev/null 2>&1; then
  echo "✓ Kotlin dependencies ready"
else
  echo "⚠ Network unavailable - Gradle dependencies will be downloaded on first use"
fi

# Python/Poetry setup
echo "📦 Setting up Python/Poetry dependencies..."
cd "$CLAUDE_PROJECT_DIR/src/python"
if poetry install --no-interaction >/dev/null 2>&1; then
  echo "✓ Python dependencies ready"
else
  echo "⚠ Network unavailable - Poetry dependencies will be installed on first use"
fi

# Return to project root
cd "$CLAUDE_PROJECT_DIR"

echo ""
echo "✅ Development environment setup complete!"
echo ""
echo "Available linters:"
echo "  - Go: golangci-lint (use 'golangci-lint run' in src/go)"
echo "  - Java: mvn spotless:check (use 'mvn spotless:check' in src/java)"
echo "  - Kotlin: ktlint + detekt (use './gradlew ktlintCheck detekt' in src/kotlin)"
echo "  - Python: black + ruff (use 'poetry run black . && poetry run ruff check . --fix' in src/python)"
echo ""
