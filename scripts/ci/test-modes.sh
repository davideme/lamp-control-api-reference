#!/usr/bin/env bash
#
# test-modes.sh - Core mode testing logic for all languages
#
# Usage:
#   ./test-modes.sh <language> <build_cmd> <migrate_cmd> <serve_only_cmd> <serve_cmd> [health_port] [api_port]
#
# Arguments:
#   language        - Language name (go, python, java, csharp, kotlin, typescript)
#   build_cmd       - Command to build/prepare the application
#   migrate_cmd     - Command to run migrate mode
#   serve_only_cmd  - Command to run serve-only mode
#   serve_cmd       - Command to run serve mode
#   health_port     - Port for health endpoint (default: 8080)
#   api_port        - Port for API endpoint (default: same as health_port)
#
# Environment variables:
#   POSTGRES_HOST     - PostgreSQL host (default: localhost)
#   POSTGRES_PORT     - PostgreSQL port (default: 5432)
#   POSTGRES_USER     - PostgreSQL user (default: test)
#   POSTGRES_PASSWORD - PostgreSQL password (default: test)

set -euo pipefail

# Get script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Source helper scripts
source "$SCRIPT_DIR/setup-test-databases.sh" 2>/dev/null || true
source "$SCRIPT_DIR/verify-database.sh" 2>/dev/null || true

# Default configuration
POSTGRES_HOST="${POSTGRES_HOST:-localhost}"
POSTGRES_PORT="${POSTGRES_PORT:-5432}"
POSTGRES_USER="${POSTGRES_USER:-test}"
POSTGRES_PASSWORD="${POSTGRES_PASSWORD:-test}"

# Export for psql
export PGPASSWORD="${POSTGRES_PASSWORD}"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Logging
log_info() {
    echo -e "${GREEN}✓${NC} $1"
}

log_error() {
    echo -e "${RED}✗${NC} $1" >&2
}

log_section() {
    echo -e "\n${BLUE}===${NC} $1 ${BLUE}===${NC}\n"
}

# Cleanup function
cleanup_server() {
    local pid=$1
    if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
        kill "$pid" 2>/dev/null || true
        sleep 1
        kill -9 "$pid" 2>/dev/null || true
    fi
}

# Test migrate mode
test_migrate_mode() {
    local language=$1
    local migrate_cmd=$2

    log_section "Testing MIGRATE mode ($language)"

    # Create clean database
    "$SCRIPT_DIR/setup-test-databases.sh" create lampcontrol_prod

    # Set DATABASE_URL with sslmode=disable for test environments
    export DATABASE_URL="postgresql://$POSTGRES_USER:$POSTGRES_PASSWORD@$POSTGRES_HOST:$POSTGRES_PORT/lampcontrol_prod?sslmode=disable"

    # Enable PostgreSQL for applications that check this flag (TypeScript, Kotlin)
    export USE_POSTGRES="true"

    # Also set individual DB_* variables for applications that don't parse DATABASE_URL
    export DB_HOST="$POSTGRES_HOST"
    export DB_PORT="$POSTGRES_PORT"
    export DB_NAME="lampcontrol_prod"
    export DB_USER="$POSTGRES_USER"
    export DB_PASSWORD="$POSTGRES_PASSWORD"

    # Spring Boot specific variables (Java)
    export SPRING_DATASOURCE_URL="jdbc:postgresql://$POSTGRES_HOST:$POSTGRES_PORT/lampcontrol_prod?sslmode=disable"
    export FLYWAY_ENABLED="true"

    # C# specific variables (Npgsql connection string format)
    export ConnectionStrings__LampControl="Host=$POSTGRES_HOST;Port=$POSTGRES_PORT;Database=lampcontrol_prod;Username=$POSTGRES_USER;Password=$POSTGRES_PASSWORD"

    # Run migrations
    log_info "Running: $migrate_cmd"
    if eval "$migrate_cmd"; then
        log_info "Migrate command succeeded"
    else
        log_error "Migrate mode failed"
        return 1
    fi

    # Verify tables exist (check for 'lamps' table - plural)
    if "$SCRIPT_DIR/verify-database.sh" lampcontrol_prod exists lamps; then
        log_info "Migration created database schema"
    else
        log_error "Database schema not created"
        return 1
    fi

    log_info "✅ Migrate mode passed"
    return 0
}

# Test serve-only mode
test_serve_only_mode() {
    local language=$1
    local serve_only_cmd=$2
    local health_port=${3:-8081}
    local api_port=${4:-$health_port}

    log_section "Testing SERVE-ONLY mode ($language) - Production Pattern"

    # Use existing database from migrate test with sslmode=disable
    export DATABASE_URL="postgresql://$POSTGRES_USER:$POSTGRES_PASSWORD@$POSTGRES_HOST:$POSTGRES_PORT/lampcontrol_prod?sslmode=disable"

    # Enable PostgreSQL for applications that check this flag (TypeScript, Kotlin)
    export USE_POSTGRES="true"

    # Also set individual DB_* variables for applications that don't parse DATABASE_URL
    export DB_HOST="$POSTGRES_HOST"
    export DB_PORT="$POSTGRES_PORT"
    export DB_NAME="lampcontrol_prod"
    export DB_USER="$POSTGRES_USER"
    export DB_PASSWORD="$POSTGRES_PASSWORD"

    # Spring Boot specific variables (Java)
    export SPRING_DATASOURCE_URL="jdbc:postgresql://$POSTGRES_HOST:$POSTGRES_PORT/lampcontrol_prod?sslmode=disable"
    export FLYWAY_ENABLED="true"

    # C# specific variables (Npgsql connection string format)
    export ConnectionStrings__LampControl="Host=$POSTGRES_HOST;Port=$POSTGRES_PORT;Database=lampcontrol_prod;Username=$POSTGRES_USER;Password=$POSTGRES_PASSWORD"

    # Start server in background
    log_info "Starting server: $serve_only_cmd"
    eval "$serve_only_cmd" &
    local server_pid=$!

    # Wait for server to be ready (with retries)
    log_info "Waiting for server to be ready..."
    local max_attempts=15
    local attempt=0
    local server_ready=false

    while [ $attempt -lt $max_attempts ]; do
        sleep 1
        attempt=$((attempt + 1))

        # Check if server process is still alive
        if ! kill -0 "$server_pid" 2>/dev/null; then
            log_error "Server process died during startup"
            return 1
        fi

        # Try to connect to health endpoint
        if curl -f -s "http://localhost:$health_port/health" > /dev/null 2>&1; then
            server_ready=true
            log_info "Server ready after $attempt seconds"
            break
        fi
    done

    if [ "$server_ready" = false ]; then
        log_error "Health endpoint failed after $max_attempts seconds"
        cleanup_server "$server_pid"
        return 1
    fi

    log_info "Health endpoint responded"

    # Verify tables exist (from migrate step) - check for 'lamps' table (plural)
    if "$SCRIPT_DIR/verify-database.sh" lampcontrol_prod exists lamps; then
        log_info "Database schema exists (from migrate step)"
    else
        log_error "Database schema missing"
        cleanup_server "$server_pid"
        return 1
    fi

    # Test API endpoint
    log_info "Testing API endpoint: http://localhost:$api_port/v1/lamps"
    if curl -f -s "http://localhost:$api_port/v1/lamps" > /dev/null; then
        log_info "API endpoint responded"
    else
        log_error "API endpoint failed"
        cleanup_server "$server_pid"
        return 1
    fi

    # Cleanup
    cleanup_server "$server_pid"

    log_info "✅ Serve-only mode passed (production pattern: migrate → serve-only)"
    return 0
}

# Test serve mode
test_serve_mode() {
    local language=$1
    local serve_cmd=$2
    local health_port=${3:-8082}
    local api_port=${4:-$health_port}

    log_section "Testing SERVE mode ($language) - Local Development Pattern"

    # Create clean database
    "$SCRIPT_DIR/setup-test-databases.sh" create lampcontrol_serve

    # Set DATABASE_URL with sslmode=disable for test environments
    export DATABASE_URL="postgresql://$POSTGRES_USER:$POSTGRES_PASSWORD@$POSTGRES_HOST:$POSTGRES_PORT/lampcontrol_serve?sslmode=disable"

    # Enable PostgreSQL for applications that check this flag (TypeScript, Kotlin)
    export USE_POSTGRES="true"

    # Also set individual DB_* variables for applications that don't parse DATABASE_URL
    export DB_HOST="$POSTGRES_HOST"
    export DB_PORT="$POSTGRES_PORT"
    export DB_NAME="lampcontrol_serve"
    export DB_USER="$POSTGRES_USER"
    export DB_PASSWORD="$POSTGRES_PASSWORD"

    # Spring Boot specific variables (Java)
    export SPRING_DATASOURCE_URL="jdbc:postgresql://$POSTGRES_HOST:$POSTGRES_PORT/lampcontrol_serve?sslmode=disable"
    export FLYWAY_ENABLED="true"

    # C# specific variables (Npgsql connection string format)
    export ConnectionStrings__LampControl="Host=$POSTGRES_HOST;Port=$POSTGRES_PORT;Database=lampcontrol_serve;Username=$POSTGRES_USER;Password=$POSTGRES_PASSWORD"

    # Start server in background
    log_info "Starting server with migrations: $serve_cmd"
    eval "$serve_cmd" &
    local server_pid=$!

    # Wait for server to be ready (longer timeout for migrations)
    log_info "Waiting for server to be ready (with migrations)..."
    local max_attempts=20
    local attempt=0
    local server_ready=false

    while [ $attempt -lt $max_attempts ]; do
        sleep 1
        attempt=$((attempt + 1))

        # Check if server process is still alive
        if ! kill -0 "$server_pid" 2>/dev/null; then
            log_error "Server process died during startup"
            return 1
        fi

        # Try to connect to health endpoint
        if curl -f -s "http://localhost:$health_port/health" > /dev/null 2>&1; then
            server_ready=true
            log_info "Server ready after $attempt seconds"
            break
        fi
    done

    if [ "$server_ready" = false ]; then
        log_error "Server never became ready after $max_attempts seconds"
        cleanup_server "$server_pid"
        return 1
    fi

    # Verify tables exist (created by serve mode) - check for 'lamps' table (plural)
    if "$SCRIPT_DIR/verify-database.sh" lampcontrol_serve exists lamps; then
        log_info "Serve mode created database schema"
    else
        log_error "Database schema not created by serve mode"
        cleanup_server "$server_pid"
        return 1
    fi

    # Test health endpoint
    log_info "Testing health endpoint: http://localhost:$health_port/health"
    if curl -f -s "http://localhost:$health_port/health" > /dev/null; then
        log_info "Health endpoint responded"
    else
        log_error "Health endpoint failed"
        cleanup_server "$server_pid"
        return 1
    fi

    # Test API endpoint
    log_info "Testing API endpoint: http://localhost:$api_port/v1/lamps"
    if curl -f -s "http://localhost:$api_port/v1/lamps" > /dev/null; then
        log_info "API endpoint responded"
    else
        log_error "API endpoint failed"
        cleanup_server "$server_pid"
        return 1
    fi

    # Cleanup
    cleanup_server "$server_pid"

    log_info "✅ Serve mode passed"
    return 0
}

# Main
main() {
    local language="${1:-}"
    local build_cmd="${2:-}"
    local migrate_cmd="${3:-}"
    local serve_only_cmd="${4:-}"
    local serve_cmd="${5:-}"
    local health_port_serve_only="${6:-8081}"
    local health_port_serve="${7:-8082}"

    if [ -z "$language" ] || [ -z "$migrate_cmd" ] || [ -z "$serve_only_cmd" ] || [ -z "$serve_cmd" ]; then
        echo "Usage: $0 <language> <build_cmd> <migrate_cmd> <serve_only_cmd> <serve_cmd> [health_port_serve_only] [health_port_serve]"
        echo ""
        echo "Arguments:"
        echo "  language             Language name (go, python, java, etc.)"
        echo "  build_cmd            Command to build/prepare the application"
        echo "  migrate_cmd          Command to run migrate mode"
        echo "  serve_only_cmd       Command to run serve-only mode"
        echo "  serve_cmd            Command to run serve mode"
        echo "  health_port_serve_only  Port for serve-only mode (default: 8081)"
        echo "  health_port_serve    Port for serve mode (default: 8082)"
        exit 1
    fi

    log_section "Mode Testing for $language"

    # Run build command if provided
    if [ -n "$build_cmd" ]; then
        log_info "Building application: $build_cmd"
        if eval "$build_cmd"; then
            log_info "Build succeeded"
        else
            log_error "Build failed"
            exit 1
        fi
    fi

    # Test all modes
    local failed=0

    test_migrate_mode "$language" "$migrate_cmd" || failed=1
    test_serve_only_mode "$language" "$serve_only_cmd" "$health_port_serve_only" "$health_port_serve_only" || failed=1
    test_serve_mode "$language" "$serve_cmd" "$health_port_serve" "$health_port_serve" || failed=1

    # Summary
    if [ $failed -eq 0 ]; then
        log_section "✅ All mode tests passed for $language"
        exit 0
    else
        log_section "❌ Some mode tests failed for $language"
        exit 1
    fi
}

main "$@"
