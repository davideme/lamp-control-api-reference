# ADR 006: Integrate Schemathesis for API Testing in CI

## Status

Accepted

## Context

We need property-based API testing to catch regressions and schema violations early in the development cycle. Manual integration testing is time-consuming and often misses edge cases that property-based testing can discover automatically.

The TypeScript implementation already has:
- Comprehensive CI/CD pipeline with GitHub Actions
- OpenAPI specification committed to the repository
- Comprehensive test suite with Jest and comprehensive coverage (85%)
- Manual integration tests in the test suite
- Fastify-based REST API with OpenAPI integration

We need to:
- Automatically detect schema violations and 5xx errors
- Catch regressions early without maintaining large test suites
- Validate that our API implementation matches our OpenAPI specification

## Decision

We will integrate **Schemathesis** into the CI pipeline as a new `schemathesis-testing` job that:

1. Uses the committed OpenAPI specification (`/docs/api/openapi.yaml`) as the source of truth
2. Runs property-based tests against the live TypeScript API during CI
3. Validates status code conformance, server error absence, and response schema conformance
4. Generates JUnit XML reports for CI integration
5. Fails the build when API violations are found

**Configuration Details:**

- **Schema Source**: Committed `/docs/api/openapi.yaml` file for consistency and speed
- **Base URL**: `http://localhost:8080/v1` (matches the TypeScript Fastify API's server configuration)
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

### Base URL Selection

The TypeScript Fastify application is configured with:
- `server.listen({ port: 8080 })` in `src/index.ts`
- `prefix: 'v1'` in `src/infrastructure/app.ts`

This results in the API being available at `http://localhost:8080/v1`, which matches the OpenAPI specification's server configuration.

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

The implementation adds a new `schemathesis-testing` job to `.github/workflows/typescript-ci.yml` that:

1. **Dependencies**: Runs after `build-and-test` job passes
2. **Application startup**: Uses `npm run dev` to start the API in background
3. **Health check**: Waits for API readiness before running tests
4. **Test execution**: Uses `schemathesis/action@v2` with configured parameters
5. **Cleanup**: Stops the API process after testing
6. **Reporting**: Uploads JUnit XML reports and fails on violations

### Application Requirements

The TypeScript Fastify application must:
- Start successfully with `npm run dev` or `npm start`
- Expose endpoints at `/v1` prefix on port 8080
- Respond to health checks for readiness verification
- Implement all OpenAPI specification endpoints correctly

### Test Configuration

```yaml
- name: Run Schemathesis tests
  uses: schemathesis/action@v2
  with:
    schema: docs/api/openapi.yaml
    base-url: http://localhost:8080/v1
    max-examples: 100
    checks: status_code_conformance,not_a_server_error,response_schema_conformance
    args: '--report junit --report-junit-path schemathesis-report.xml'
```

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
- **Hypothesis**: Python-specific, would require additional tooling
- **Decision**: Schemathesis chosen for OpenAPI-native support

## References

- [Schemathesis Documentation](https://schemathesis.readthedocs.io/)
- [Schemathesis GitHub Action](https://github.com/schemathesis/action)
- [Project OpenAPI Specification](/docs/api/openapi.yaml)
- [TypeScript Application Configuration](/src/typescript/src/infrastructure/app.ts)
- [Java Schemathesis Integration (ADR 009)](/src/java/adr/009-schemathesis-integration.md)
- [C# Schemathesis Integration (ADR 005)](/src/csharp/adr/005-schemathesis-integration.md)
- [Kotlin Schemathesis Integration (ADR 006)](/src/kotlin/adr/006-schemathesis-integration.md)