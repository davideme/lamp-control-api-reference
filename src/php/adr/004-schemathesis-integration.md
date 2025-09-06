# ADR 004: Integrate Schemathesis for API Testing in CI

## Status

Accepted

## Context

We need property-based API testing to catch regressions and schema violations early in the development cycle. Manual integration testing is time-consuming and often misses edge cases that property-based testing can discover automatically.

The PHP implementation already has:
- Comprehensive CI/CD pipeline with GitHub Actions
- OpenAPI specification committed to the repository
- Slim Framework with OpenAPI Data Mocker integration
- Comprehensive test suite with PHPUnit
- Manual integration tests in the test suite

We need to:
- Automatically detect schema violations and 5xx errors
- Catch regressions early without maintaining large test suites
- Validate that our API implementation matches our OpenAPI specification

## Decision

We will integrate **Schemathesis** into the CI pipeline as a new `schemathesis-testing` job that:

1. Uses the committed OpenAPI specification (`/docs/api/openapi.yaml`) as the source of truth
2. Runs property-based tests against the live PHP API during CI
3. Validates status code conformance, server error absence, and response schema conformance
4. Generates JUnit XML reports for CI integration
5. Fails the build when API violations are found

**Configuration Details:**

- **Schema Source**: Committed `/docs/api/openapi.yaml` file for consistency and speed
- **Base URL**: `http://localhost:8080/v1` (matches the PHP API's Slim Framework routing configuration)
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

The PHP Slim Framework application is configured with routes that have a base path of `/v1` as seen in the `RegisterRoutes.php` file:
- `'basePathWithoutHost' => '/v1'`
- Server runs on port 8080: `composer start` runs `php -S 0.0.0.0:8080 -t public`

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

The Schemathesis testing job will be added to `.github/workflows/php-ci.yml`:

```yaml
schemathesis-testing:
  name: Schemathesis API Testing
  runs-on: ubuntu-latest
  needs: [code-quality]
  defaults:
    run:
      working-directory: src/php/lamp-control-api
```

### Application Requirements

The PHP API must be started before testing and be accessible at `http://localhost:8080/v1`.

The API will be started using the composer script:
```bash
composer start
```

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

### Using Live Schema Discovery

We could fetch the schema from the running API endpoint, but this adds complexity and potential failure points during CI.

### Different Test Tools

- **Postman/Newman**: More manual setup, less property-based testing
- **RestAssured**: Requires more Java knowledge, manual test case creation
- **Custom scripts**: Higher maintenance burden, less comprehensive

### Different Check Configurations

We could include more checks like `response_headers_conformance`, but this may increase false positives for this simple API.

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
- [PHP Slim Framework Documentation](https://www.slimframework.com/)
- [Property-Based Testing](https://hypothesis.works/)
- [OpenAPI Specification](https://swagger.io/specification/)
- [Java Schemathesis Integration (ADR 009)](/src/java/adr/009-schemathesis-integration.md)
- [C# Schemathesis Integration (ADR 005)](/src/csharp/adr/005-schemathesis-integration.md)
- [TypeScript Schemathesis Integration (ADR 006)](/src/typescript/docs/adr/006-schemathesis-integration.md)