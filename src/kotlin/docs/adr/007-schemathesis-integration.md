# ADR 006: Integrate Schemathesis for API Testing in CI

## Status

Accepted

## Date

2025-08-17

## Context

The Kotlin implementation of the lamp control API uses Ktor framework and follows the same OpenAPI specification as other language implementations in this repository. To ensure API contract compliance and catch regressions early, we need automated property-based testing that validates the implementation against the OpenAPI specification.

Manual testing and unit tests alone are insufficient to catch all possible API contract violations, especially edge cases and schema mismatches that can occur with diverse input data. The project already has successful Schemathesis integration for the C# implementation, providing a proven pattern to follow.

## Decision

We will integrate **Schemathesis** into the CI pipeline as a new `schemathesis-testing` job that:

1. Uses the committed OpenAPI specification (`/docs/api/openapi.yaml`) as the source of truth
2. Runs property-based tests against the live Kotlin API during CI
3. Validates status code conformance, server error absence, and response schema conformance
4. Generates JUnit XML reports for CI integration
5. Fails the build when API violations are found

**Configuration Details:**

- **Schema Source**: Committed `/docs/api/openapi.yaml` file for consistency and speed
- **Base URL**: `http://localhost:8080/v1` (matches the Kotlin API's Ktor server endpoint structure)
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

### Why Kotlin-Specific Configuration?

- **Port 8080**: Ktor server runs on port 8080 by default (as configured in Application.kt)
- **Health endpoint**: Uses `/health` endpoint for readiness checks (defined in Routing.kt)
- **Gradle integration**: Uses `./gradlew run --no-daemon` for starting the application
- **JDK 21**: Matches the runtime environment specified in ADR 002

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

## Implementation

### CI Workflow Integration

The `schemathesis-testing` job is added to `.github/workflows/kotlin-ci.yml`:

```yaml
schemathesis-testing:
  name: Schemathesis API Testing
  runs-on: ubuntu-latest
  needs: [code-quality]
  
  steps:
  - name: Start API in background
    run: |
      ./gradlew run --no-daemon &
      echo $! > api.pid
      sleep 15
  
  - name: Wait for API to be ready
    run: |
      timeout 30 bash -c 'until curl -f -s http://localhost:8080/health > /dev/null; do sleep 1; done'
      echo "API is ready!"
  
  - name: Run Schemathesis tests
    uses: schemathesis/action@v2
    with:
      schema: docs/api/openapi.yaml
      base-url: http://localhost:8080/v1
      max-examples: 100
      checks: status_code_conformance,not_a_server_error,response_schema_conformance
      report: junit
      report-name: schemathesis-report.xml
```

### Application Requirements

The Kotlin application must provide:

1. **Health Check Endpoint**: `/health` endpoint for readiness verification
2. **API Endpoints**: All endpoints under `/v1` matching the OpenAPI specification
3. **Port Configuration**: Server running on port 8080 (default Ktor configuration)
4. **Graceful Startup**: Application ready within 30 seconds of startup

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
- [Ktor Framework Documentation](https://ktor.io/)
- [C# Schemathesis Integration ADR](../csharp/adr/005-schemathesis-integration.md)