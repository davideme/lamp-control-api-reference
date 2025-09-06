# ADR 004: Integrate Schemathesis for API Testing in CI

## Status

Accepted

## Context

We need property-based API testing to catch regressions and schema violations early in the development cycle. Manual integration testing is time-consuming and often misses edge cases that property-based testing can discover automatically.

The Python implementation already has:
- Comprehensive CI/CD pipeline with GitHub Actions
- OpenAPI specification committed to the repository
- FastAPI framework with automatic OpenAPI documentation
- Comprehensive test suite with pytest
- Manual integration tests in the test suite

We need to:
- Automatically detect schema violations and 5xx errors
- Catch regressions early without maintaining large test suites
- Validate that our API implementation matches our OpenAPI specification

## Decision

We will integrate **Schemathesis** into the CI pipeline as a new `schemathesis-testing` job that:

1. Uses the committed OpenAPI specification (`/docs/api/openapi.yaml`) as the source of truth
2. Runs property-based tests against the live Python API during CI
3. Validates status code conformance, server error absence, and response schema conformance
4. Generates JUnit XML reports for CI integration
5. Fails the build when API violations are found

**Configuration Details:**

- **Schema Source**: Committed `/docs/api/openapi.yaml` file for consistency and speed
- **Base URL**: `http://localhost:8000/v1` (matches the Python FastAPI server's default configuration)
- **Example Limits**: 100 examples per operation for comprehensive but fast testing
- **Checks**: `status_code_conformance`, `not_a_server_error`, `response_schema_conformance`
- **Reports**: JUnit XML format with 30-day retention
- **CI Integration**: Runs after test job passes, fails build on violations

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

### Base URL Selection

The Python FastAPI implementation runs on port 8000 by default with uvicorn. The API endpoints are prefixed with `/v1`, making the full base URL `http://localhost:8000/v1`. This follows the standard FastAPI pattern and matches the OpenAPI specification.

## Implementation

### CI Workflow Integration

The `schemathesis-testing` job is added to `.github/workflows/python-ci.yml`:

```yaml
schemathesis-testing:
  name: Schemathesis API Testing
  runs-on: ubuntu-latest
  needs: [test]
  
  steps:
  - name: Start API in background
    run: |
      poetry run uvicorn src.openapi_server.main:app --host 0.0.0.0 --port 8000 &
      echo $! > api.pid
      sleep 10
  
  - name: Wait for API to be ready
    run: |
      timeout 30 bash -c 'until curl -f -s http://localhost:8000/v1/lamps > /dev/null; do sleep 1; done'
      echo "API is ready!"
  
  - name: Run Schemathesis tests
    uses: schemathesis/action@v2
    with:
      schema: docs/api/openapi.yaml
      base-url: http://localhost:8000/v1
      max-examples: 100
      checks: status_code_conformance,not_a_server_error,response_schema_conformance
      report: junit
      report-name: schemathesis-report.xml
```

### Test Configuration

```yaml
- name: Run Schemathesis tests
  uses: schemathesis/action@v2
  with:
    schema: docs/api/openapi.yaml
    base-url: http://localhost:8000/v1
    max-examples: 100
    checks: status_code_conformance,not_a_server_error,response_schema_conformance
    report: junit
    report-name: schemathesis-report.xml
```

### Dependencies

- **Poetry**: For managing Python dependencies and virtual environments
- **Uvicorn**: ASGI server for running the FastAPI application
- **Schemathesis**: Property-based testing tool (installed via GitHub Action)

## Consequences

### Benefits

- **Early detection**: Catches API violations before deployment
- **Automatic coverage**: Tests edge cases that manual tests might miss
- **Low maintenance**: Automatically adapts to schema changes
- **CI integration**: Clear pass/fail status with detailed reports
- **Documentation validation**: Ensures implementation matches specification

### Trade-offs

- **Build time**: Adds ~1-2 minutes to CI pipeline
- **Test flakiness**: Property-based tests may find non-deterministic issues
- **False positives**: May flag legitimate behavior not documented in schema

### Mitigation Strategies

- **Reasonable limits**: 100 examples per operation balances coverage with speed
- **Specific checks**: Only run essential checks to minimize noise
- **Clear reporting**: JUnit XML provides actionable failure information
- **Documentation**: This ADR explains the integration for team understanding

## Alternatives Considered

### Manual Integration Testing Only

- **Pros**: Full control over test scenarios, easier debugging
- **Cons**: High maintenance, limited coverage, slow to adapt to schema changes
- **Decision**: Rejected for lack of edge case coverage

### Runtime Schema Discovery

- **Pros**: Always tests against current implementation
- **Cons**: Slower CI, dependency on running service, potential for network issues
- **Decision**: Rejected for reliability and speed concerns

### Different Property-Based Testing Tools

- **QuickCheck variants**: Limited OpenAPI support
- **Hypothesis**: Python-native but requires manual schema parsing
- **Decision**: Schemathesis chosen for OpenAPI-native support

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
- [Schemathesis GitHub Action](https://github.com/schemathesis/action)
- [Project OpenAPI Specification](/docs/api/openapi.yaml)
- [Python FastAPI Configuration](/src/python/src/openapi_server/main.py)
- [Java Schemathesis Integration (ADR 009)](/src/java/adr/009-schemathesis-integration.md)
- [C# Schemathesis Integration (ADR 005)](/src/csharp/adr/005-schemathesis-integration.md)
- [Kotlin Schemathesis Integration (ADR 006)](/src/kotlin/adr/006-schemathesis-integration.md)