#!/usr/bin/env bash
#
# verify-database.sh - Verify database state for mode testing
#
# Usage:
#   ./verify-database.sh <dbname> <state> [table]
#
# Arguments:
#   dbname  - Database name to check
#   state   - Expected state: 'exists' or 'not-exists'
#   table   - Table name to check (default: lamp)
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
NC='\033[0m' # No Color

# Logging functions
log_success() {
    echo -e "${GREEN}✓${NC} $1"
}

log_error() {
    echo -e "${RED}✗${NC} $1" >&2
}

# Verify table exists/not-exists
verify_table() {
    local dbname="$1"
    local expected_state="$2"
    local table="${3:-lamps}"

    # Query to count tables
    local count
    count=$(psql -h "$POSTGRES_HOST" -p "$POSTGRES_PORT" -U "$POSTGRES_USER" -d "$dbname" \
        -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = '${table}';" \
        2>/dev/null | tr -d ' ')

    if [ -z "$count" ]; then
        log_error "Failed to query database: $dbname"
        return 1
    fi

    case "$expected_state" in
        exists)
            if [ "$count" -eq 1 ]; then
                log_success "Table '${table}' exists in database '${dbname}'"
                return 0
            else
                log_error "Table '${table}' does not exist in database '${dbname}' (expected to exist)"
                return 1
            fi
            ;;
        not-exists)
            if [ "$count" -eq 0 ]; then
                log_success "Table '${table}' does not exist in database '${dbname}' (as expected)"
                return 0
            else
                log_error "Table '${table}' exists in database '${dbname}' (expected not to exist)"
                return 1
            fi
            ;;
        *)
            log_error "Invalid state: $expected_state (must be 'exists' or 'not-exists')"
            return 1
            ;;
    esac
}

# Main
main() {
    local dbname="${1:-}"
    local expected_state="${2:-}"
    local table="${3:-lamps}"

    if [ -z "$dbname" ] || [ -z "$expected_state" ]; then
        echo "Usage: $0 <dbname> <state> [table]"
        echo ""
        echo "Arguments:"
        echo "  dbname  Database name to check"
        echo "  state   Expected state: 'exists' or 'not-exists'"
        echo "  table   Table name to check (default: lamps)"
        echo ""
        echo "Environment variables:"
        echo "  POSTGRES_HOST     PostgreSQL host (default: localhost)"
        echo "  POSTGRES_PORT     PostgreSQL port (default: 5432)"
        echo "  POSTGRES_USER     PostgreSQL user (default: test)"
        echo "  POSTGRES_PASSWORD PostgreSQL password (default: test)"
        exit 1
    fi

    verify_table "$dbname" "$expected_state" "$table"
}

# Only run main if script is executed directly (not sourced)
if [ "${BASH_SOURCE[0]}" = "${0}" ]; then
    main "$@"
fi
