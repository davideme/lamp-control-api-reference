#!/bin/bash

# Script to extract code coverage percentages for different languages
# This script checks for coverage files in standard locations and extracts percentages

set -e

echo "Extracting code coverage percentages..."

# Helper: extract integer line coverage percentage from a JaCoCo XML report
# Usage: extract_jacoco_line_coverage /path/to/jacoco.xml
# Echos an integer percentage (0-100) or N/A
extract_jacoco_line_coverage() {
  local xml_path="$1"
  if [ ! -f "$xml_path" ]; then
    echo "N/A"
    return 0
  fi

  # Prefer XPath via xmllint to target the root-level counter precisely
  local missed covered total pct counter_line
  if command -v xmllint >/dev/null 2>&1; then
    # Try direct child of report
    missed=$(xmllint --xpath 'string(/report/counter[@type="LINE"]/@missed)' "$xml_path" 2>/dev/null || true)
    covered=$(xmllint --xpath 'string(/report/counter[@type="LINE"]/@covered)' "$xml_path" 2>/dev/null || true)
    # If not found, try under bundle (some JaCoCo versions place counters there)
    if ! [[ "$missed" =~ ^[0-9]+$ && "$covered" =~ ^[0-9]+$ ]]; then
      missed=$(xmllint --xpath 'string(/report/bundle/counter[@type="LINE"]/@missed)' "$xml_path" 2>/dev/null || true)
      covered=$(xmllint --xpath 'string(/report/bundle/counter[@type="LINE"]/@covered)' "$xml_path" 2>/dev/null || true)
    fi
  fi

  # Fallback: robust grep that works even when the XML is a single (very long) line.
  # Grab the last <counter type="LINE" .../> in the file, which is typically the aggregate at the root.
  if ! [[ "$missed" =~ ^[0-9]+$ && "$covered" =~ ^[0-9]+$ ]]; then
    counter_line=$(grep -o '<counter type="LINE"[^>]*/>' "$xml_path" | tail -n 1 || true)
    if [ -z "$counter_line" ]; then
      # As a fallback, split tags onto newlines then try again
      counter_line=$(sed 's/></>\n</g' "$xml_path" | grep -o '<counter type="LINE"[^>]*/>' | tail -n 1 || true)
    fi
    if [ -n "$counter_line" ]; then
      missed=$(printf '%s' "$counter_line" | sed -n 's/.*missed="\([0-9]\+\)".*/\1/p')
      covered=$(printf '%s' "$counter_line" | sed -n 's/.*covered="\([0-9]\+\)".*/\1/p')
    fi
  fi

  if [[ "$missed" =~ ^[0-9]+$ && "$covered" =~ ^[0-9]+$ ]]; then
    total=$((missed + covered))
    if [ "$total" -gt 0 ]; then
      pct=$(awk "BEGIN {printf \"%.2f\", ($covered/$total)*100}")
      echo "$pct"
      return 0
    else
      echo "0.00"
      return 0
    fi
  fi

  echo "N/A"
}

# TypeScript coverage
echo "Checking TypeScript coverage..."
if [ -f src/typescript/coverage/coverage-summary.json ]; then
  TS_RAW=$(jq '.total.lines.pct' src/typescript/coverage/coverage-summary.json)
  if [[ "$TS_RAW" =~ ^[0-9.]+$ ]]; then
    TS_COVERAGE=$(awk -v v="$TS_RAW" 'BEGIN { printf "%.2f", v }')
  else
    TS_COVERAGE="N/A"
  fi
else
  TS_COVERAGE="N/A"
fi
echo "TypeScript coverage: $TS_COVERAGE"

# Python coverage
echo "Checking Python coverage..."
if [ -f src/python/coverage/coverage.json ]; then
  PY_RAW=$(jq '.totals.percent_covered' src/python/coverage/coverage.json)
  if [[ "$PY_RAW" =~ ^[0-9.]+$ ]]; then
    PY_COVERAGE=$(awk -v v="$PY_RAW" 'BEGIN { printf "%.2f", v }')
  else
    PY_COVERAGE="N/A"
  fi
else
  PY_COVERAGE="N/A"
fi
echo "Python coverage: $PY_COVERAGE"

# Java coverage
echo "Checking Java coverage..."
JAVA_COVERAGE=$(extract_jacoco_line_coverage "src/java/target/site/jacoco/jacoco.xml")
echo "Java coverage: $JAVA_COVERAGE"

# C# coverage
echo "Checking C# coverage..."
CS_COVERAGE="N/A"

# Try different common locations for .NET coverage files
if [ -f src/csharp/TestResults/coverage.cobertura.xml ]; then
  # Extract line coverage from Cobertura XML using grep and sed
  CS_LINE_RATE=$(grep '<coverage' src/csharp/TestResults/coverage.cobertura.xml | sed 's/.*line-rate="\([0-9.]*\)".*/\1/')
  if [[ "$CS_LINE_RATE" =~ ^[0-9.]+$ && "$CS_LINE_RATE" != "0" ]]; then
    CS_COVERAGE=$(awk "BEGIN {printf \"%.2f\", $CS_LINE_RATE*100}")
  fi
elif [ -f src/csharp/TestResults/*/coverage.cobertura.xml ]; then
  # Try to find coverage file in subdirectories
  COVERAGE_FILE=$(find src/csharp/TestResults -name "coverage.cobertura.xml" | head -1)
  if [ -n "$COVERAGE_FILE" ]; then
    CS_LINE_RATE=$(grep '<coverage' "$COVERAGE_FILE" | sed 's/.*line-rate="\([0-9.]*\)".*/\1/')
    if [[ "$CS_LINE_RATE" =~ ^[0-9.]+$ && "$CS_LINE_RATE" != "0" ]]; then
      CS_COVERAGE=$(awk "BEGIN {printf \"%.2f\", $CS_LINE_RATE*100}")
    fi
  fi
elif [ -f src/csharp/coverage/coverage.json ]; then
  # Try JSON format (if using coverlet JSON output)
  CS_RAW=$(jq '.summary.linecoverage' src/csharp/coverage/coverage.json 2>/dev/null | sed 's/%//')
  if [[ "$CS_RAW" =~ ^[0-9.]+$ ]]; then
    CS_COVERAGE=$(awk -v v="$CS_RAW" 'BEGIN { printf "%.2f", v }')
  else
    CS_COVERAGE="N/A"
  fi
elif [ -d src/csharp/coverage ] && [ "$(ls -A src/csharp/coverage)" ]; then
  # Try to find any coverage files in the coverage directory
  COVERAGE_FILE=$(find src/csharp/coverage -name "*.xml" -o -name "*.json" | head -1)
  if [ -n "$COVERAGE_FILE" ] && [[ "$COVERAGE_FILE" == *.xml ]]; then
    CS_LINE_RATE=$(grep -o 'line-rate="[0-9.]*"' "$COVERAGE_FILE" | head -1 | sed 's/line-rate="\([0-9.]*\)"/\1/')
    if [[ "$CS_LINE_RATE" =~ ^[0-9.]+$ && "$CS_LINE_RATE" != "0" ]]; then
      CS_COVERAGE=$(awk "BEGIN {printf \"%.2f\", $CS_LINE_RATE*100}")
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
    PHP_COVERAGE=$(awk "BEGIN {printf \"%.2f\", ($PHP_COVERED_STATEMENTS/$PHP_TOTAL_STATEMENTS)*100}")
  fi
elif [ -f src/php/lamp-control-api/coverage/clover.xml ]; then
  # Try alternative location
  PHP_TOTAL_STATEMENTS=$(grep -o 'statements="[0-9]*"' src/php/lamp-control-api/coverage/clover.xml | head -1 | sed 's/statements="\([0-9]*\)"/\1/')
  PHP_COVERED_STATEMENTS=$(grep -o 'coveredstatements="[0-9]*"' src/php/lamp-control-api/coverage/clover.xml | head -1 | sed 's/coveredstatements="\([0-9]*\)"/\1/')
  
  if [[ "$PHP_TOTAL_STATEMENTS" =~ ^[0-9]+$ && "$PHP_COVERED_STATEMENTS" =~ ^[0-9]+$ && "$PHP_TOTAL_STATEMENTS" -gt 0 ]]; then
    PHP_COVERAGE=$(awk "BEGIN {printf \"%.2f\", ($PHP_COVERED_STATEMENTS/$PHP_TOTAL_STATEMENTS)*100}")
  fi
fi

echo "PHP coverage: $PHP_COVERAGE"

# Go coverage
echo "Checking Go coverage..."
if [ -f src/go/coverage.out ]; then
  # Use go tool cover to extract total coverage percentage
  # Check if go is available
  if command -v go >/dev/null 2>&1; then
    # Extract total coverage percentage from go tool cover output
    GO_COVERAGE_RAW=$(cd src/go && go tool cover -func=coverage.out | grep "total:" | awk '{print $3}' | sed 's/%//')
    if [[ "$GO_COVERAGE_RAW" =~ ^[0-9.]+$ ]]; then
      GO_COVERAGE=$(awk -v v="$GO_COVERAGE_RAW" 'BEGIN { printf "%.2f", v }')
    else
      GO_COVERAGE="N/A"
    fi
  else
    # Fallback: parse coverage.out file manually if go is not available
    # Go coverage format: file:line.column,line.column numstmt covered
    if [ -s src/go/coverage.out ]; then
      # Calculate coverage from coverage.out file
      TOTAL_STATEMENTS=$(grep -v "mode:" src/go/coverage.out | awk '{total += $2} END {print total}')
      COVERED_STATEMENTS=$(grep -v "mode:" src/go/coverage.out | awk '$3 > 0 {covered += $2} END {print covered}')
      
      if [[ "$TOTAL_STATEMENTS" =~ ^[0-9]+$ && "$COVERED_STATEMENTS" =~ ^[0-9]+$ && "$TOTAL_STATEMENTS" -gt 0 ]]; then
        GO_COVERAGE=$(awk "BEGIN {printf \"%.2f\", ($COVERED_STATEMENTS/$TOTAL_STATEMENTS)*100}")
      else
        GO_COVERAGE="N/A"
      fi
    else
      GO_COVERAGE="N/A"
    fi
  fi
else
  GO_COVERAGE="N/A"
fi
echo "Go coverage: $GO_COVERAGE"

# Kotlin coverage
echo "Checking Kotlin coverage..."
KOTLIN_COVERAGE="N/A"

# Default JaCoCo XML report path for the Kotlin (Gradle) project
KOTLIN_JACOCO_XML="src/kotlin/build/reports/jacoco/test/jacocoTestReport.xml"

if [ -f "$KOTLIN_JACOCO_XML" ]; then
  KOTLIN_COVERAGE=$(extract_jacoco_line_coverage "$KOTLIN_JACOCO_XML")
else
  # Fallback: search for any jacocoTestReport.xml under src/kotlin
  KOTLIN_XML_FOUND=$(find src/kotlin -maxdepth 8 -type f -path '*/build/reports/jacoco/test/jacocoTestReport.xml' | head -1 || true)
  if [ -n "$KOTLIN_XML_FOUND" ] && [ -f "$KOTLIN_XML_FOUND" ]; then
    KOTLIN_COVERAGE=$(extract_jacoco_line_coverage "$KOTLIN_XML_FOUND")
  fi
fi
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
