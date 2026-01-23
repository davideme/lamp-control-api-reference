#!/usr/bin/env bash
#
# setup-test-databases.sh - Database management for mode testing
#
# Usage:
#   ./setup-test-databases.sh create <dbname>   # Create database
#   ./setup-test-databases.sh drop <dbname>     # Drop database
#   ./setup-test-databases.sh clean             # Drop all test databases
#
# Environment variables:
#   POSTGRES_HOST     - PostgreSQL host (default: localhost)
#   POSTGRES_PORT     - PostgreSQL port (default: 5432)
#   POSTGRES_USER     - PostgreSQL user (default: test)
#   POSTGRES_PASSWORD - PostgreSQL password (default: test)

set -euo pipefail

# Default configuration
POSTGRES_HOST="${POSTGRES_HOST:-localhost}"
POSTGRES_PORT="${POSTGRES_PORT:-5432}"
POSTGRES_USER="${POSTGRES_USER:-test}"
POSTGRES_PASSWORD="${POSTGRES_PASSWORD:-test}"

# Export password for psql
export PGPASSWORD="${POSTGRES_PASSWORD}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${GREEN}✓${NC} $1"
}

log_error() {
    echo -e "${RED}✗${NC} $1" >&2
}

log_warn() {
    echo -e "${YELLOW}⚠${NC} $1"
}

# Check if PostgreSQL is accessible
check_postgres() {
    if ! psql -h "$POSTGRES_HOST" -p "$POSTGRES_PORT" -U "$POSTGRES_USER" -d postgres -c '\q' 2>/dev/null; then
        log_error "Cannot connect to PostgreSQL at $POSTGRES_HOST:$POSTGRES_PORT"
        log_error "Make sure PostgreSQL is running and credentials are correct"
        return 1
    fi
    return 0
}

# Create database
create_database() {
    local dbname="$1"

    if ! check_postgres; then
        return 1
    fi

    # Drop if exists
    psql -h "$POSTGRES_HOST" -p "$POSTGRES_PORT" -U "$POSTGRES_USER" -d postgres \
        -c "DROP DATABASE IF EXISTS ${dbname};" >/dev/null 2>&1 || true

    # Create database
    if psql -h "$POSTGRES_HOST" -p "$POSTGRES_PORT" -U "$POSTGRES_USER" -d postgres \
        -c "CREATE DATABASE ${dbname};" >/dev/null 2>&1; then
        log_info "Created database: ${dbname}"
        return 0
    else
        log_error "Failed to create database: ${dbname}"
        return 1
    fi
}

# Drop database
drop_database() {
    local dbname="$1"

    if ! check_postgres; then
        return 1
    fi

    if psql -h "$POSTGRES_HOST" -p "$POSTGRES_PORT" -U "$POSTGRES_USER" -d postgres \
        -c "DROP DATABASE IF EXISTS ${dbname};" >/dev/null 2>&1; then
        log_info "Dropped database: ${dbname}"
        return 0
    else
        log_error "Failed to drop database: ${dbname}"
        return 1
    fi
}

# Clean all test databases
clean_databases() {
    if ! check_postgres; then
        return 1
    fi

    local test_dbs=("lampcontrol_prod" "lampcontrol_serve" "lampcontrol_clean" "lampcontrol_test")

    log_info "Cleaning test databases..."
    for db in "${test_dbs[@]}"; do
        psql -h "$POSTGRES_HOST" -p "$POSTGRES_PORT" -U "$POSTGRES_USER" -d postgres \
            -c "DROP DATABASE IF EXISTS ${db};" >/dev/null 2>&1 || true
    done
    log_info "Cleaned all test databases"
}

# Main
main() {
    local action="${1:-}"
    local dbname="${2:-}"

    case "$action" in
        create)
            if [ -z "$dbname" ]; then
                log_error "Usage: $0 create <dbname>"
                exit 1
            fi
            create_database "$dbname"
            ;;
        drop)
            if [ -z "$dbname" ]; then
                log_error "Usage: $0 drop <dbname>"
                exit 1
            fi
            drop_database "$dbname"
            ;;
        clean)
            clean_databases
            ;;
        *)
            echo "Usage: $0 {create|drop|clean} [dbname]"
            echo ""
            echo "Commands:"
            echo "  create <dbname>  Create a test database"
            echo "  drop <dbname>    Drop a test database"
            echo "  clean            Drop all test databases"
            echo ""
            echo "Environment variables:"
            echo "  POSTGRES_HOST     PostgreSQL host (default: localhost)"
            echo "  POSTGRES_PORT     PostgreSQL port (default: 5432)"
            echo "  POSTGRES_USER     PostgreSQL user (default: test)"
            echo "  POSTGRES_PASSWORD PostgreSQL password (default: test)"
            exit 1
            ;;
    esac
}

# Only run main if script is executed directly (not sourced)
if [ "${BASH_SOURCE[0]}" = "${0}" ]; then
    main "$@"
fi
