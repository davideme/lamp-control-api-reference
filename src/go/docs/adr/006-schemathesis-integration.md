# ADR 007: Integrate Schemathesis for API Testing in CI

## Status

Proposed

## Date

2025-01-27

## Context

The Go implementation of the Lamp Control API needs automated API testing to validate that the implementation conforms to the OpenAPI specification. Manual testing is insufficient for catching regressions and ensuring API contract compliance across different scenarios.

Property-based testing tools like Schemathesis can automatically generate test cases from OpenAPI specifications, providing comprehensive coverage of edge cases and ensuring the API implementation matches its specification.

## Decision

We will integrate **Schemathesis** into the CI pipeline as a new `schemathesis-testing` job that:

1. Uses the committed OpenAPI specification (`/docs/api/openapi.yaml`) as the source of truth
2. Runs property-based tests against the live Go API during CI
3. Validates status code conformance, server error absence, and response schema conformance
4. Generates JUnit XML reports for CI integration
5. Fails the build when API violations are found

**Configuration Details:**

- **Schema Source**: Committed `/docs/api/openapi.yaml` file for consistency and speed
- **Base URL**: `http://localhost:8080` (matches the Go API's current routing without v1 prefix)
- **Example Limits**: 100 examples per operation for comprehensive but fast testing
- **Checks**: `status_code_conformance`, `not_a_server_error`, `response_schema_conformance`
- **Reports**: JUnit XML format with 30-day retention
- **CI Integration**: Runs after code quality checks pass, fails build on violations

## Rationale

### Why Schemathesis?

- **Property-based testing**: Automatically generates diverse test cases
- **OpenAPI native**: Understands OpenAPI specifications natively
- **CI-friendly**: Supports JUnit XML reporting and appropriate exit codes
- **Comprehensive checks**: Validates status codes, schemas, and server errors
- **Low maintenance**: Automatically adapts to schema changes

### Why Committed OpenAPI Specification?

- **Consistency**: Same schema used across all implementations
- **Speed**: No dependency on running services for schema discovery
- **Reliability**: Avoids network issues during CI
- **Version control**: Schema changes are tracked and reviewable

### Why These Specific Checks?

- **status_code_conformance**: Ensures API returns documented status codes
- **not_a_server_error**: Catches 5xx errors that indicate implementation bugs
- **response_schema_conformance**: Validates response structure matches specification

### Why Go-Specific Configuration?

- **Port 8080**: Matches the default port configured in the Go application
- **Base URL**: The Go implementation currently serves directly on `/lamps` without the `/v1` prefix specified in the OpenAPI spec. This is a known discrepancy that should be addressed in future versions.
- **Chi Router**: The Go implementation uses Chi router with proper OpenAPI middleware
- **Thread Safety**: Go implementation uses sync.RWMutex for concurrent access, important for testing
- **In-Memory Storage**: Fast test execution due to in-memory storage implementation

## Consequences

### Benefits

- **Early detection**: Catches API violations before deployment
- **Automatic coverage**: Tests edge cases that manual tests might miss
- **Low maintenance**: Automatically adapts to schema changes
- **CI integration**: Clear pass/fail status with detailed reports
- **Documentation validation**: Ensures implementation matches specification
- **Regression prevention**: Catches breaking changes in API behavior

### Trade-offs

- **Build time**: Adds ~1-2 minutes to CI pipeline
- **Test flakiness**: Property-based tests may find non-deterministic issues
- **False positives**: May flag legitimate behavior not documented in schema
- **Resource usage**: Requires starting full Go application during CI

### Mitigation Strategies

- **Reasonable limits**: 100 examples per operation balances coverage with speed
- **Specific checks**: Only run essential checks to minimize noise
- **Clear reporting**: JUnit XML provides actionable failure information
- **Documentation**: This ADR explains the integration for team understanding
- **Dependencies**: Run after code quality checks to fail fast on basic issues

## Implementation

### CI Workflow Integration

The schemathesis testing will be implemented as a new job in `.github/workflows/go-ci.yml`:

```yaml
schemathesis-testing:
  name: Schemathesis API Testing
  runs-on: ubuntu-latest
  needs: [lint, test]
  defaults:
    run:
      working-directory: src/go
  
  steps:
  - name: Checkout code
    uses: actions/checkout@v4
  
  - name: Set up Go
    uses: actions/setup-go@v4
    with:
      go-version: '1.24.3'
      cache-dependency-path: src/go/go.sum
  
  - name: Install dependencies
    run: |
      go mod download
      go mod tidy
  
  - name: Build application
    run: go build -o bin/lamp-control-api ./cmd/lamp-control-api
  
  - name: Start API in background
    run: |
      ./bin/lamp-control-api --port=8080 &
      echo $! > api.pid
      sleep 10
  
  - name: Wait for API to be ready
    run: |
      timeout 30 bash -c 'until curl -f -s http://localhost:8080/lamps > /dev/null; do sleep 1; done'
      echo "API is ready!"
  
  - name: Run Schemathesis tests
    uses: schemathesis/action@v2
    with:
      schema: docs/api/openapi.yaml
      base-url: http://localhost:8080
      max-examples: 100
      checks: status_code_conformance,not_a_server_error,response_schema_conformance
      args: '--report junit --report-junit-path schemathesis-report.xml'
  
  - name: Stop API
    run: |
      if [ -f api.pid ]; then
        kill $(cat api.pid) || true
        rm api.pid
      fi
  
  - name: Upload Schemathesis report
    uses: actions/upload-artifact@v4
    if: always()
    with:
      name: go-schemathesis-report
      path: |
        schemathesis-report.xml
      retention-days: 30
  
  - name: Check Schemathesis results
    run: |
      if [ -f schemathesis-report.xml ]; then
        if grep -q 'failures="[1-9]' schemathesis-report.xml || grep -q 'errors="[1-9]' schemathesis-report.xml; then
          echo "❌ Schemathesis found API issues!"
          echo "Check the uploaded report for details."
          exit 1
        else
          echo "✅ Schemathesis tests passed!"
        fi
      else
        echo "⚠️ No Schemathesis report found"
      fi
```

### Application Requirements

The Go application must:
- Start successfully on port 8080
- Respond to HTTP requests on `/lamps` endpoint for health checking
- Handle concurrent requests properly (already implemented with sync.RWMutex)
- Return appropriate HTTP status codes as documented in OpenAPI spec

## Monitoring and Maintenance

- **Report Review**: Check uploaded artifacts for test details
- **Schema Updates**: Update OpenAPI spec when API changes
- **Limit Adjustment**: Modify `--max-examples` if needed for CI performance
- **Check Addition**: Add more checks as requirements evolve

## Future Considerations

- **Stateful Testing**: Consider adding stateful test phases for complex workflows
- **Authentication**: Add API key testing if authentication is implemented
- **Performance Testing**: Consider load testing integration for performance regression detection
- **Multiple Environments**: Run against staging environments for comprehensive validation

## References

- [Schemathesis Documentation](https://schemathesis.readthedocs.io/)
- [Property-Based Testing](https://hypothesis.works/)
- [OpenAPI Specification](https://swagger.io/specification/)
- [Chi Router Documentation](https://go-chi.io/)
- [Go Testing Best Practices](https://golang.org/doc/tutorial/add-a-test)