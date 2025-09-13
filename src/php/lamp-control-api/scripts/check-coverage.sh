#!/bin/bash

# Script to check PHP code coverage and ensure it meets the minimum threshold
# Usage: ./scripts/check-coverage.sh [threshold] [coverage-file]

set -e

# Default values
THRESHOLD=${1:-80}  # 80% minimum coverage requirement
COVERAGE_FILE=${2:-coverage.xml}

echo "🔍 Checking PHP code coverage..."

# Run tests with coverage if coverage file doesn't exist
if [ ! -f "$COVERAGE_FILE" ]; then
    echo "Running tests with coverage..."
    vendor/bin/phpunit --coverage-clover="$COVERAGE_FILE"
fi

# Check if coverage file exists
if [ ! -f "$COVERAGE_FILE" ]; then
    echo "❌ Coverage file $COVERAGE_FILE not found"
    exit 1
fi

echo "📊 Extracting coverage data from $COVERAGE_FILE..."

# Extract coverage percentage from the clover XML
METRICS_LINE=$(grep '<metrics files=' "$COVERAGE_FILE")
if [ -n "$METRICS_LINE" ]; then
    TOTAL_STATEMENTS=$(echo "$METRICS_LINE" | sed -n 's/.*\bstatements="\([0-9]*\)".*/\1/p')
    COVERED_STATEMENTS=$(echo "$METRICS_LINE" | sed -n 's/.*coveredstatements="\([0-9]*\)".*/\1/p')
    
    if [[ "$TOTAL_STATEMENTS" =~ ^[0-9]+$ && "$COVERED_STATEMENTS" =~ ^[0-9]+$ && "$TOTAL_STATEMENTS" -gt 0 ]]; then
        COVERAGE=$(awk "BEGIN {printf \"%.2f\", ($COVERED_STATEMENTS/$TOTAL_STATEMENTS)*100}")
        
        echo ""
        echo "📋 Coverage Results:"
        echo "Total statements: $TOTAL_STATEMENTS"
        echo "Covered statements: $COVERED_STATEMENTS"
        echo "Current coverage: ${COVERAGE}%"
        echo "Required threshold: ${THRESHOLD}%"
        
        # Check if coverage meets threshold
        MEETS_THRESHOLD=$(awk -v cov="$COVERAGE" -v thresh="$THRESHOLD" 'BEGIN {print (cov >= thresh)}')
        
        if [ "$MEETS_THRESHOLD" -eq 0 ]; then
            echo ""
            echo "❌ Code coverage is ${COVERAGE}%, which is below the required ${THRESHOLD}%"
            echo ""
            echo "💡 To improve coverage:"
            echo "   1. Add tests for uncovered code paths"
            echo "   2. Review the HTML coverage report: coverage-report/index.html"
            echo "   3. Focus on testing business logic and error handling"
            echo ""
            echo "Please add more tests to increase coverage."
            exit 1
        else
            echo ""
            echo "✅ Code coverage is ${COVERAGE}%, which meets the required ${THRESHOLD}%"
            echo ""
            echo "💡 To view detailed coverage report:"
            echo "   Open coverage-report/index.html in your browser"
        fi
    else
        echo "❌ Could not parse coverage data from clover XML"
        echo "Total statements: '$TOTAL_STATEMENTS'"
        echo "Covered statements: '$COVERED_STATEMENTS'"
        exit 1
    fi
else
    echo "❌ Could not find project metrics in coverage XML"
    exit 1
fi

echo ""
echo "🎉 Coverage check completed successfully!"