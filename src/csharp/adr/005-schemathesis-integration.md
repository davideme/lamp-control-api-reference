# ADR 005: Integrate Schemathesis for API Testing in CI

## Status

Accepted

## Context

We need property-based API testing to catch regressions and schema violations early in the development cycle. Manual integration testing is time-consuming and often misses edge cases that property-based testing can discover automatically.

The C# implementation already has:
- Comprehensive CI/CD pipeline with GitHub Actions
- OpenAPI specification committed to the repository
- Manual integration tests in the test suite

We need to:
- Automatically detect schema violations and 5xx errors
- Catch regressions early without maintaining large test suites
- Validate that our API implementation matches our OpenAPI specification

## Decision

We will integrate **Schemathesis** into the CI pipeline as a new `schemathesis-testing` job that:

1. Uses the committed OpenAPI specification (`/docs/api/openapi.yaml`) as the source of truth
2. Runs property-based tests against the live C# API during CI
3. Validates status code conformance, server error absence, and response schema conformance
4. Generates JUnit XML reports for CI integration
5. Fails the build when API violations are found

**Configuration Details:**

- **Schema Source**: Committed `/docs/api/openapi.yaml` file for consistency and speed
- **Base URL**: `http://localhost:5169/v1` (matches the API's actual endpoint structure)
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

The integration is implemented as a new job in `.github/workflows/csharp-ci.yml`:

```yaml
schemathesis-testing:
  name: Schemathesis API Testing
  runs-on: ubuntu-latest
  needs: [code-quality]
  # ... (see workflow file for full configuration)
```

### Workflow Steps

1. **Setup**: Install .NET SDK and Python with Schemathesis
2. **Build**: Build the C# application
3. **Start API**: Run the API in background on port 5169
4. **Wait**: Ensure API is ready before testing
5. **Test**: Run Schemathesis with configured checks and limits
6. **Report**: Upload JUnit XML report as artifact
7. **Verify**: Check results and fail build if violations found
8. **Cleanup**: Stop the API process

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