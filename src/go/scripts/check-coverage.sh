#!/bin/bash

# Script to check Go code coverage and ensure it meets the minimum threshold
# Usage: ./scripts/check-coverage.sh [threshold] [coverage-file]

set -e

# Default values
THRESHOLD=${1:-80}  # Back to 80% since we're excluding main functions
COVERAGE_FILE=${2:-coverage.out}

echo "🔍 Checking Go code coverage..."

# Run tests with coverage
echo "Running tests with coverage..."
go test -v -race -coverprofile="$COVERAGE_FILE" ./...

# Check if coverage file exists
if [ ! -f "$COVERAGE_FILE" ]; then
    echo "❌ Coverage file $COVERAGE_FILE not found"
    exit 1
fi

# Filter out generated files and query files from coverage
echo "Filtering out generated files and queries from coverage calculation..."
grep -vE '(\.gen\.go:|/queries/)' "$COVERAGE_FILE" > "${COVERAGE_FILE}.filtered" || true

# Also filter out main functions/packages which are hard to unit test
echo "Filtering out main packages from coverage calculation..."
grep -v "/main.go:" "${COVERAGE_FILE}.filtered" > "${COVERAGE_FILE}.filtered2" || true

if [ -s "${COVERAGE_FILE}.filtered2" ]; then
    mv "${COVERAGE_FILE}.filtered2" "$COVERAGE_FILE"
    rm -f "${COVERAGE_FILE}.filtered"
elif [ -s "${COVERAGE_FILE}.filtered" ]; then
    mv "${COVERAGE_FILE}.filtered" "$COVERAGE_FILE"
else
    echo "⚠️  Warning: No non-generated/non-main files found in coverage report"
fi

# Extract coverage percentage
coverage=$(go tool cover -func="$COVERAGE_FILE" | grep total: | awk '{print $3}' | sed 's/%//')

echo ""
echo "📊 Coverage Results:"
echo "Current coverage: ${coverage}%"
echo "Required threshold: ${THRESHOLD}%"

# Check if coverage meets threshold (using awk for floating point comparison)
meets_threshold=$(awk -v cov="$coverage" -v thresh="$THRESHOLD" 'BEGIN {print (cov >= thresh)}')

if [ "$meets_threshold" -eq 0 ]; then
    echo ""
    echo "❌ Code coverage is ${coverage}%, which is below the required ${THRESHOLD}%"
    echo ""
    echo "📋 Coverage by function:"
    go tool cover -func="$COVERAGE_FILE"
    echo ""
    echo "💡 To see detailed coverage in your browser, run:"
    echo "   go tool cover -html=$COVERAGE_FILE"
    echo ""
    echo "Please add more tests to increase coverage."
    exit 1
else
    echo ""
    echo "✅ Code coverage is ${coverage}%, which meets the required ${THRESHOLD}%"
    echo ""
    echo "📋 Coverage by function:"
    go tool cover -func="$COVERAGE_FILE"
    echo ""
    echo "💡 To view detailed coverage report in your browser, run:"
    echo "   go tool cover -html=$COVERAGE_FILE"
fi

echo ""
echo "🎉 Coverage check completed successfully!"
