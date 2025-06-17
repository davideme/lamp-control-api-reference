#!/bin/bash

# Script to extract code coverage percentages for different languages
# This script checks for coverage files in standard locations and extracts percentages

set -e

echo "Extracting code coverage percentages..."

# TypeScript coverage
echo "Checking TypeScript coverage..."
if [ -f src/typescript/coverage/coverage-summary.json ]; then
  TS_COVERAGE=$(jq '.total.lines.pct' src/typescript/coverage/coverage-summary.json)
else
  TS_COVERAGE="N/A"
fi
echo "TypeScript coverage: $TS_COVERAGE"

# Python coverage
echo "Checking Python coverage..."
if [ -f src/python/coverage/coverage.json ]; then
  PY_COVERAGE=$(jq '.totals.percent_covered | floor' src/python/coverage/coverage.json)
else
  PY_COVERAGE="N/A"
fi
echo "Python coverage: $PY_COVERAGE"

# Java coverage
echo "Checking Java coverage..."
if [ -f src/java/target/site/jacoco/jacoco.xml ]; then
  # Extract line coverage from JaCoCo XML using grep and sed
  JAVA_MISSED=$(grep '<counter type="LINE"' src/java/target/site/jacoco/jacoco.xml | tail -1 | sed 's/.*missed="\([0-9]*\)".*/\1/')
  JAVA_COVERED=$(grep '<counter type="LINE"' src/java/target/site/jacoco/jacoco.xml | tail -1 | sed 's/.*covered="\([0-9]*\)".*/\1/')
  if [[ "$JAVA_MISSED" =~ ^[0-9]+$ && "$JAVA_COVERED" =~ ^[0-9]+$ ]]; then
    JAVA_TOTAL=$((JAVA_MISSED + JAVA_COVERED))
    if [ "$JAVA_TOTAL" -gt 0 ]; then
      JAVA_COVERAGE=$(awk "BEGIN {printf \"%.0f\", ($JAVA_COVERED/$JAVA_TOTAL)*100}")
    else
      JAVA_COVERAGE="0"
    fi
  else
    JAVA_COVERAGE="N/A"
  fi
else
  JAVA_COVERAGE="N/A"
fi
echo "Java coverage: $JAVA_COVERAGE"

# C# coverage
echo "Checking C# coverage..."
CS_COVERAGE="N/A"

# Try different common locations for .NET coverage files
if [ -f src/csharp/TestResults/coverage.cobertura.xml ]; then
  # Extract line coverage from Cobertura XML using grep and sed
  CS_LINE_RATE=$(grep '<coverage' src/csharp/TestResults/coverage.cobertura.xml | sed 's/.*line-rate="\([0-9.]*\)".*/\1/')
  if [[ "$CS_LINE_RATE" =~ ^[0-9.]+$ && "$CS_LINE_RATE" != "0" ]]; then
    CS_COVERAGE=$(awk "BEGIN {printf \"%.0f\", $CS_LINE_RATE*100}")
  fi
elif [ -f src/csharp/TestResults/*/coverage.cobertura.xml ]; then
  # Try to find coverage file in subdirectories
  COVERAGE_FILE=$(find src/csharp/TestResults -name "coverage.cobertura.xml" | head -1)
  if [ -n "$COVERAGE_FILE" ]; then
    CS_LINE_RATE=$(grep '<coverage' "$COVERAGE_FILE" | sed 's/.*line-rate="\([0-9.]*\)".*/\1/')
    if [[ "$CS_LINE_RATE" =~ ^[0-9.]+$ && "$CS_LINE_RATE" != "0" ]]; then
      CS_COVERAGE=$(awk "BEGIN {printf \"%.0f\", $CS_LINE_RATE*100}")
    fi
  fi
elif [ -f src/csharp/coverage/coverage.json ]; then
  # Try JSON format (if using coverlet JSON output)
  CS_COVERAGE=$(jq '.summary.linecoverage' src/csharp/coverage/coverage.json 2>/dev/null | sed 's/%//' || echo "N/A")
elif [ -d src/csharp/coverage ] && [ "$(ls -A src/csharp/coverage)" ]; then
  # Try to find any coverage files in the coverage directory
  COVERAGE_FILE=$(find src/csharp/coverage -name "*.xml" -o -name "*.json" | head -1)
  if [ -n "$COVERAGE_FILE" ] && [[ "$COVERAGE_FILE" == *.xml ]]; then
    CS_LINE_RATE=$(grep -o 'line-rate="[0-9.]*"' "$COVERAGE_FILE" | head -1 | sed 's/line-rate="\([0-9.]*\)"/\1/')
    if [[ "$CS_LINE_RATE" =~ ^[0-9.]+$ && "$CS_LINE_RATE" != "0" ]]; then
      CS_COVERAGE=$(awk "BEGIN {printf \"%.0f\", $CS_LINE_RATE*100}")
    fi
  fi
fi

echo "C# coverage: $CS_COVERAGE"

# PHP coverage
echo "Checking PHP coverage..."
PHP_COVERAGE="N/A"

# Try to find PHP coverage XML files
if [ -f src/php/lamp-control-api/coverage.xml ]; then
  # Extract line coverage from Clover XML - get total statements and covered statements
  PHP_TOTAL_STATEMENTS=$(grep -o 'statements="[0-9]*"' src/php/lamp-control-api/coverage.xml | head -1 | sed 's/statements="\([0-9]*\)"/\1/')
  PHP_COVERED_STATEMENTS=$(grep -o 'coveredstatements="[0-9]*"' src/php/lamp-control-api/coverage.xml | head -1 | sed 's/coveredstatements="\([0-9]*\)"/\1/')
  
  if [[ "$PHP_TOTAL_STATEMENTS" =~ ^[0-9]+$ && "$PHP_COVERED_STATEMENTS" =~ ^[0-9]+$ && "$PHP_TOTAL_STATEMENTS" -gt 0 ]]; then
    PHP_COVERAGE=$(awk "BEGIN {printf \"%.0f\", ($PHP_COVERED_STATEMENTS/$PHP_TOTAL_STATEMENTS)*100}")
  fi
elif [ -f src/php/lamp-control-api/coverage/clover.xml ]; then
  # Try alternative location
  PHP_TOTAL_STATEMENTS=$(grep -o 'statements="[0-9]*"' src/php/lamp-control-api/coverage/clover.xml | head -1 | sed 's/statements="\([0-9]*\)"/\1/')
  PHP_COVERED_STATEMENTS=$(grep -o 'coveredstatements="[0-9]*"' src/php/lamp-control-api/coverage/clover.xml | head -1 | sed 's/coveredstatements="\([0-9]*\)"/\1/')
  
  if [[ "$PHP_TOTAL_STATEMENTS" =~ ^[0-9]+$ && "$PHP_COVERED_STATEMENTS" =~ ^[0-9]+$ && "$PHP_TOTAL_STATEMENTS" -gt 0 ]]; then
    PHP_COVERAGE=$(awk "BEGIN {printf \"%.0f\", ($PHP_COVERED_STATEMENTS/$PHP_TOTAL_STATEMENTS)*100}")
  fi
fi

echo "PHP coverage: $PHP_COVERAGE"

# Go coverage (placeholder for future implementation)
echo "Checking Go coverage..."
GO_COVERAGE="N/A"
echo "Go coverage: $GO_COVERAGE"

# Kotlin coverage (placeholder for future implementation)
echo "Checking Kotlin coverage..."
KOTLIN_COVERAGE="N/A"
echo "Kotlin coverage: $KOTLIN_COVERAGE"

# Ruby coverage (placeholder for future implementation)
echo "Checking Ruby coverage..."
RUBY_COVERAGE="N/A"
echo "Ruby coverage: $RUBY_COVERAGE"

echo "Coverage extraction completed!"

# Output coverage values for capture
echo "TS_COVERAGE=$TS_COVERAGE"
echo "PY_COVERAGE=$PY_COVERAGE"
echo "JAVA_COVERAGE=$JAVA_COVERAGE"
echo "CS_COVERAGE=$CS_COVERAGE"
echo "PHP_COVERAGE=$PHP_COVERAGE"
echo "GO_COVERAGE=$GO_COVERAGE"
echo "KOTLIN_COVERAGE=$KOTLIN_COVERAGE"
echo "RUBY_COVERAGE=$RUBY_COVERAGE"
