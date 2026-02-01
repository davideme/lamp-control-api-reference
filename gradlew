#!/bin/bash
# Wrapper script to delegate to the Kotlin Gradle wrapper

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
exec "$SCRIPT_DIR/src/kotlin/gradlew" -p "$SCRIPT_DIR/src/kotlin" "$@"
