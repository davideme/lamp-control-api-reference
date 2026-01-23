#!/usr/bin/env bash
#
# languages.sh - Language-specific configuration for mode testing
#
# This file defines build and run commands for each language implementation.
# Source this file to access language-specific commands.

# Build commands
declare -gA LANGUAGE_BUILD=(
  ["go"]="go build -o bin/lamp-control-api ./cmd/lamp-control-api"
  ["python"]="poetry install --no-interaction --no-root"
  ["java"]="mvn clean package -DskipTests"
  ["csharp"]="dotnet build --no-restore --configuration Release"
  ["kotlin"]="./gradlew build --no-daemon"
  ["typescript"]="npm ci && npm run build"
)

# Migrate mode commands
declare -gA LANGUAGE_MIGRATE=(
  ["go"]="./bin/lamp-control-api --mode=migrate"
  ["python"]="poetry run python -m openapi_server.cli --mode=migrate"
  ["java"]="java -jar target/openapi-spring-1.0.0.jar --mode=migrate"
  ["csharp"]="cd LampControlApi && dotnet run --configuration Release --no-build -- --mode=migrate"
  ["kotlin"]="./gradlew run --args='--mode=migrate' --no-daemon"
  ["typescript"]="npm start -- --mode=migrate"
)

# Serve-only mode commands (production pattern)
declare -gA LANGUAGE_SERVE_ONLY=(
  ["go"]="./bin/lamp-control-api --mode=serve-only --port=8081"
  ["python"]="poetry run python -m openapi_server.cli --mode=serve-only"
  ["java"]="java -jar target/openapi-spring-1.0.0.jar --mode=serve-only --server.port=8081"
  ["csharp"]="cd LampControlApi && dotnet run --configuration Release --no-build -- --mode=serve-only --urls http://localhost:5170"
  ["kotlin"]="KTOR_PORT=8081 ./gradlew run --args='--mode=serve-only' --no-daemon"
  ["typescript"]="PORT=8081 npm start -- --mode=serve-only"
)

# Serve mode commands (local development pattern)
declare -gA LANGUAGE_SERVE=(
  ["go"]="./bin/lamp-control-api --mode=serve --port=8082"
  ["python"]="poetry run python -m openapi_server.cli --mode=serve"
  ["java"]="java -jar target/openapi-spring-1.0.0.jar --mode=serve --server.port=8082"
  ["csharp"]="cd LampControlApi && dotnet run --configuration Release --no-build -- --mode=serve --urls http://localhost:5171"
  ["kotlin"]="KTOR_PORT=8082 ./gradlew run --args='--mode=serve' --no-daemon"
  ["typescript"]="PORT=8082 npm start -- --mode=serve"
)

# Health endpoint ports for serve-only mode
declare -gA LANGUAGE_HEALTH_PORT_SERVE_ONLY=(
  ["go"]="8081"
  ["python"]="8000"  # Python uses default 8000, ignores our port in env
  ["java"]="8081"
  ["csharp"]="5170"
  ["kotlin"]="8081"
  ["typescript"]="8081"
)

# Health endpoint ports for serve mode
declare -gA LANGUAGE_HEALTH_PORT_SERVE=(
  ["go"]="8082"
  ["python"]="8000"  # Python uses default 8000, ignores our port in env
  ["java"]="8082"
  ["csharp"]="5171"
  ["kotlin"]="8082"
  ["typescript"]="8082"
)

# Working directories
declare -gA LANGUAGE_WORKDIR=(
  ["go"]="src/go"
  ["python"]="src/python"
  ["java"]="src/java"
  ["csharp"]="src/csharp"
  ["kotlin"]="src/kotlin"
  ["typescript"]="src/typescript"
)

# Get language configuration
get_language_config() {
    local language="$1"
    local config_type="$2"

    case "$config_type" in
        build)
            echo "${LANGUAGE_BUILD[$language]:-}"
            ;;
        migrate)
            echo "${LANGUAGE_MIGRATE[$language]:-}"
            ;;
        serve-only)
            echo "${LANGUAGE_SERVE_ONLY[$language]:-}"
            ;;
        serve)
            echo "${LANGUAGE_SERVE[$language]:-}"
            ;;
        health-port-serve-only)
            echo "${LANGUAGE_HEALTH_PORT_SERVE_ONLY[$language]:-8081}"
            ;;
        health-port-serve)
            echo "${LANGUAGE_HEALTH_PORT_SERVE[$language]:-8082}"
            ;;
        workdir)
            echo "${LANGUAGE_WORKDIR[$language]:-}"
            ;;
        *)
            echo ""
            return 1
            ;;
    esac
}

# List all supported languages
list_languages() {
    echo "go python java csharp kotlin typescript"
}
