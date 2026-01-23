#!/usr/bin/env bash
#
# test-modes-local.sh - Local test runner for mode testing
#
# Usage:
#   ./test-modes-local.sh <language> [options]
#   ./test-modes-local.sh all [options]
#
# Options:
#   --start-postgres    Start PostgreSQL container before testing
#   --stop-postgres     Stop PostgreSQL container after testing
#   --keep-postgres     Keep PostgreSQL container running after tests
#
# Examples:
#   ./test-modes-local.sh go --start-postgres
#   ./test-modes-local.sh all --start-postgres
#   ./test-modes-local.sh python  # Use existing PostgreSQL

set -euo pipefail

# Get script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Source language configuration
source "$SCRIPT_DIR/ci/languages.sh"

# Container name
POSTGRES_CONTAINER="mode-test-postgres"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}✓${NC} $1"
}

log_error() {
    echo -e "${RED}✗${NC} $1" >&2
}

log_warn() {
    echo -e "${YELLOW}⚠${NC} $1"
}

log_section() {
    echo -e "\n${BLUE}===${NC} $1 ${BLUE}===${NC}\n"
}

# Check if PostgreSQL container is running
is_postgres_running() {
    docker ps --filter "name=$POSTGRES_CONTAINER" --format '{{.Names}}' 2>/dev/null | grep -q "^${POSTGRES_CONTAINER}$"
}

# Start PostgreSQL container
start_postgres() {
    if is_postgres_running; then
        log_info "PostgreSQL container already running"
        return 0
    fi

    log_info "Starting PostgreSQL container..."

    # Remove existing container if stopped
    docker rm -f "$POSTGRES_CONTAINER" 2>/dev/null || true

    # Start new container
    docker run -d \
        --name "$POSTGRES_CONTAINER" \
        -e POSTGRES_USER=test \
        -e POSTGRES_PASSWORD=test \
        -e POSTGRES_DB=lampcontrol \
        -p 5432:5432 \
        postgres:16-alpine

    # Wait for PostgreSQL to be ready
    log_info "Waiting for PostgreSQL to be ready..."
    for i in {1..30}; do
        if docker exec "$POSTGRES_CONTAINER" pg_isready -U test >/dev/null 2>&1; then
            log_info "PostgreSQL is ready"
            return 0
        fi
        sleep 1
    done

    log_error "PostgreSQL failed to start"
    return 1
}

# Stop PostgreSQL container
stop_postgres() {
    if is_postgres_running; then
        log_info "Stopping PostgreSQL container..."
        docker stop "$POSTGRES_CONTAINER" >/dev/null 2>&1
        docker rm "$POSTGRES_CONTAINER" >/dev/null 2>&1
        log_info "PostgreSQL container stopped"
    fi
}

# Test a single language
test_language() {
    local language="$1"

    log_section "Testing $language"

    # Get language configuration
    local workdir=$(get_language_config "$language" "workdir")
    local build_cmd=$(get_language_config "$language" "build")
    local migrate_cmd=$(get_language_config "$language" "migrate")
    local serve_only_cmd=$(get_language_config "$language" "serve-only")
    local serve_cmd=$(get_language_config "$language" "serve")
    local health_port_serve_only=$(get_language_config "$language" "health-port-serve-only")
    local health_port_serve=$(get_language_config "$language" "health-port-serve")

    if [ -z "$workdir" ]; then
        log_error "Unknown language: $language"
        return 1
    fi

    # Change to working directory
    cd "$PROJECT_ROOT/$workdir"

    # Run mode tests
    "$SCRIPT_DIR/ci/test-modes.sh" \
        "$language" \
        "$build_cmd" \
        "$migrate_cmd" \
        "$serve_only_cmd" \
        "$serve_cmd" \
        "$health_port_serve_only" \
        "$health_port_serve"
}

# Main
main() {
    local language="${1:-}"
    local start_pg=false
    local stop_pg=false
    local keep_pg=false

    # Parse arguments
    shift || true
    while [ $# -gt 0 ]; do
        case "$1" in
            --start-postgres)
                start_pg=true
                stop_pg=true
                ;;
            --stop-postgres)
                stop_pg=true
                ;;
            --keep-postgres)
                keep_pg=true
                stop_pg=false
                ;;
            *)
                log_error "Unknown option: $1"
                exit 1
                ;;
        esac
        shift
    done

    if [ -z "$language" ]; then
        echo "Usage: $0 <language|all> [options]"
        echo ""
        echo "Languages: $(list_languages)"
        echo ""
        echo "Options:"
        echo "  --start-postgres  Start PostgreSQL container before testing"
        echo "  --stop-postgres   Stop PostgreSQL container after testing"
        echo "  --keep-postgres   Keep PostgreSQL container running"
        exit 1
    fi

    # Start PostgreSQL if requested
    if [ "$start_pg" = true ]; then
        start_postgres
    fi

    # Trap to cleanup PostgreSQL on exit
    if [ "$stop_pg" = true ]; then
        trap 'stop_postgres' EXIT
    fi

    # Test language(s)
    if [ "$language" = "all" ]; then
        local failed=0
        for lang in $(list_languages); do
            if ! test_language "$lang"; then
                failed=1
            fi
        done

        if [ $failed -eq 0 ]; then
            log_section "✅ All languages passed"
            exit 0
        else
            log_section "❌ Some languages failed"
            exit 1
        fi
    else
        test_language "$language"
    fi
}

main "$@"
