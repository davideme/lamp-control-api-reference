# Code Coverage Setup for Go Lamp Control API

This document explains the code coverage configuration and requirements for the Go implementation of the Lamp Control API.

## Overview

The project enforces a **minimum 80% code coverage** requirement that is checked in both CI/CD pipeline and local development.

## Coverage Configuration

### What's Included
- All business logic in the `api` package
- Unit tests and integration tests
- Error handling paths
- Concurrent access scenarios

### What's Excluded
- Generated files (`.gen.go`) - These are auto-generated OpenAPI code
- Main packages - These are difficult to unit test and contain mostly boilerplate
- Vendor dependencies

## CI/CD Integration

### GitHub Actions Workflow
The coverage check is integrated into the CI pipeline in `.github/workflows/go-ci.yml`:

```yaml
- name: Check code coverage
  working-directory: src/go
  run: |
    # ... filter generated files and main packages ...
    coverage=$(go tool cover -func=coverage.out | grep total: | awk '{print $3}' | sed 's/%//')
    if [ "$meets_threshold" -eq 0 ]; then
      echo "❌ Code coverage is ${coverage}%, which is below the required 80%"
      exit 1
    fi
```

### Coverage Artifacts
- Coverage profile: `coverage.out`
- HTML report: `coverage.html` (uploaded as artifact)
- Summary displayed in CI logs

## Local Development

### Available Commands

1. **Run tests with coverage**:
   ```bash
   make test-coverage
   ```

2. **Check coverage meets 80% threshold**:
   ```bash
   make coverage-check
   ```

3. **Generate and view HTML coverage report**:
   ```bash
   make coverage-report
   ```

4. **Show coverage summary**:
   ```bash
   make coverage-summary
   ```

### Manual Coverage Check
```bash
# Run tests with coverage
go test -coverprofile=coverage.out ./...

# View coverage summary
go tool cover -func=coverage.out

# Generate HTML report
go tool cover -html=coverage.out -o coverage.html
```

## Coverage Script

The `scripts/check-coverage.sh` script provides automated coverage checking:

```bash
# Use default 80% threshold
./scripts/check-coverage.sh

# Use custom threshold
./scripts/check-coverage.sh 75

# Use custom coverage file
./scripts/check-coverage.sh 80 my-coverage.out
```

### Script Features
- ✅ Filters out generated files automatically
- ✅ Excludes main packages from calculation
- ✅ Provides detailed function-level coverage report
- ✅ Generates HTML reports
- ✅ Configurable thresholds
- ✅ Clear pass/fail indication

## Current Coverage Status

As of the latest implementation:
- **Core API Logic**: 100% coverage
- **Error Handling**: 100% coverage
- **Concurrent Operations**: Tested with 1000 concurrent operations
- **Edge Cases**: All 404 scenarios covered

## Maintaining Coverage

### When Adding New Code
1. Write unit tests for new functions
2. Test error paths and edge cases
3. Run `make coverage-check` before committing
4. Ensure CI passes before merging

### Best Practices
- Test both happy path and error scenarios
- Use table-driven tests for multiple inputs
- Test concurrent access where applicable
- Mock external dependencies
- Focus on business logic over boilerplate

## Troubleshooting

### Coverage Below Threshold
1. Check which functions lack coverage:
   ```bash
   go tool cover -func=coverage.out | grep -E "(0.0%|[1-7][0-9]\.[0-9]%)"
   ```

2. Add tests for uncovered functions
3. Verify error paths are tested
4. Consider if code can be simplified

### CI Failures
- Local tests pass but CI fails: Check for race conditions
- Coverage varies between runs: Ensure deterministic tests
- Build failures: Verify all imports and dependencies

## Integration with IDEs

### VS Code
Install the Go extension for coverage highlighting:
1. Install Go extension
2. Run `Go: Test Package` with coverage
3. View coverage in editor gutters

### Coverage Display
- 🟢 Green: Covered lines
- 🔴 Red: Uncovered lines
- 🟡 Yellow: Partially covered lines

This comprehensive coverage setup ensures high code quality and catches regressions early in the development process.
