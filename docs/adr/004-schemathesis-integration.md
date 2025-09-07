# ADR 004: Schemathesis Integration for Property-Based API Testing

## Status

Accepted

## Context

The Lamp Control API Reference project includes multiple language implementations (C#, TypeScript, Java, Python, PHP, Go, Kotlin, and Ruby) that all need to conform to the same OpenAPI specification. Manual testing alone is insufficient for ensuring:

1. **API Contract Compliance**: All implementations must return responses that match the OpenAPI schema exactly
2. **Edge Case Coverage**: Manual tests often miss boundary conditions and unexpected input combinations
3. **Regression Detection**: Changes to implementations should not break existing API contracts
4. **Consistency Across Languages**: Different implementations should behave identically for the same inputs
5. **Specification Accuracy**: The OpenAPI specification should accurately reflect the actual API behavior

Traditional testing approaches have limitations:
- **Manual Testing**: Time-intensive and doesn't scale with multiple implementations
- **Static Testing**: Unit tests don't validate the full request-response cycle
- **Limited Test Cases**: Hand-written integration tests miss many edge cases
- **Maintenance Burden**: Test suites require constant updates as APIs evolve

## Decision

We will adopt **Schemathesis** as the standard property-based API testing framework across all language implementations of the Lamp Control API.

### Strategic Implementation

1. **Unified Testing Approach**: All applicable language implementations will use Schemathesis for property-based API testing
2. **Single Source of Truth**: The committed `/docs/api/openapi.yaml` specification serves as the contract for all implementations
3. **Comprehensive Coverage**: Generate hundreds of test cases automatically to validate edge cases and boundary conditions
4. **CI Integration**: Integrate Schemathesis testing into all CI/CD pipelines as a quality gate
5. **Consistent Configuration**: Standardize testing parameters across all implementations for comparable results

### Core Configuration Standards

- **Schema Source**: Committed `/docs/api/openapi.yaml` for consistency and version control
- **Test Volume**: 100 examples per operation for thorough coverage balanced with CI performance
- **Essential Checks**: `status_code_conformance`, `not_a_server_error`, `response_schema_conformance`
- **Reporting Format**: JUnit XML for standardized CI integration and result aggregation
- **Build Integration**: Run after code quality checks, fail builds on violations

## Rationale

### Why Property-Based Testing?

Property-based testing automatically generates diverse test inputs based on the OpenAPI specification, providing:

- **Exhaustive Coverage**: Tests combinations and edge cases that manual testing would miss
- **Automatic Adaptation**: Test cases evolve automatically as the API specification changes  
- **Bug Discovery**: Finds implementation issues through comprehensive input generation
- **Documentation Validation**: Ensures the specification accurately describes the actual API behavior

### Why Schemathesis?

Schemathesis was selected over alternatives because it:

- **OpenAPI Native**: Built specifically for OpenAPI/Swagger specifications
- **Language Agnostic**: Can test any HTTP API regardless of implementation language
- **CI Friendly**: Provides proper exit codes and reporting formats for automation
- **Mature Tooling**: Well-established with comprehensive documentation and GitHub Action integration
- **Flexible Configuration**: Allows customization of test parameters and validation rules

### Why Committed Specification?

Using the committed OpenAPI specification rather than runtime discovery provides:

- **Consistency**: All implementations test against the identical contract
- **Speed**: No dependency on running services during CI
- **Reliability**: Eliminates network-related test failures
- **Version Control**: Specification changes are tracked and reviewable
- **Cross-Implementation Validation**: Ensures all languages implement the same contract

## Consequences

### Benefits

- **Early Issue Detection**: Catches API contract violations before deployment across all implementations
- **Reduced Maintenance**: Automatically adapts to specification changes without manual test updates  
- **Improved Quality**: Comprehensive testing increases confidence in all implementations
- **Standardized Validation**: Consistent testing approach across different programming languages
- **Documentation Accuracy**: Validates that specifications match actual implementation behavior
- **Cross-Language Consistency**: Ensures all implementations behave identically for the same inputs

### Trade-offs

- **Increased Build Time**: Adds 1-2 minutes to CI pipelines across all implementations
- **Potential Test Flakiness**: Property-based tests may occasionally find non-deterministic issues
- **False Positives**: May flag legitimate behavior that isn't properly documented in the specification
- **Learning Curve**: Development teams need to understand property-based testing concepts
- **Tool Dependency**: Introduces dependency on Schemathesis tool and GitHub Action

### Mitigation Strategies

- **Reasonable Limits**: 100 examples per operation balances comprehensive testing with CI performance
- **Focused Checks**: Only run essential validation checks to minimize noise and false positives
- **Clear Documentation**: Each implementation includes detailed ADRs explaining the integration
- **Standardized Configuration**: Consistent setup across implementations reduces complexity
- **Artifact Retention**: JUnit XML reports provide actionable information for debugging failures

## Implementation

Each language implementation includes its own detailed ADR documenting the specific integration approach:

### Language-Specific Implementation ADRs

The following implementations currently have Schemathesis integration:

- **Python**: [ADR 004: Schemathesis Integration](/src/python/docs/adr/004-schemathesis-integration.md)
- **C#**: [ADR 005: Schemathesis Integration](/src/csharp/adr/005-schemathesis-integration.md)  
- **TypeScript**: [ADR 006: Schemathesis Integration](/src/typescript/docs/adr/006-schemathesis-integration.md)
- **Kotlin**: [ADR 006: Schemathesis Integration](/src/kotlin/adr/006-schemathesis-integration.md)
- **Go**: [ADR 007: Schemathesis Integration](/src/go/adr/007-schemathesis-integration.md)
- **PHP**: [ADR 004: Schemathesis Integration](/src/php/adr/004-schemathesis-integration.md)
- **Java**: [ADR 009: Schemathesis Integration](/src/java/adr/009-schemathesis-integration.md)

*Note: Ruby implementation does not yet have Schemathesis integration.*

### Common Implementation Pattern

Each language implementation follows this standard pattern:

1. **CI Workflow Integration**: New `schemathesis-testing` job in language-specific CI workflows
2. **Application Startup**: Start the API implementation in background during CI
3. **Health Verification**: Ensure API readiness before running tests
4. **Test Execution**: Use `schemathesis/action@v2` with standardized configuration
5. **Result Processing**: Upload JUnit XML reports and fail builds on violations
6. **Cleanup**: Stop API processes after testing completes

## Monitoring and Maintenance

- **Report Analysis**: Regularly review JUnit XML artifacts for trends and issues across implementations
- **Specification Updates**: Update OpenAPI schema when API contracts change, triggering automatic test adaptation
- **Performance Monitoring**: Monitor CI build times and adjust test parameters if needed
- **Cross-Implementation Comparison**: Compare test results across different language implementations for consistency
- **Tool Updates**: Keep Schemathesis GitHub Action and dependencies updated across all implementations

## Future Considerations

- **Stateful Testing**: Consider adding multi-step workflow testing for complex API interactions
- **Authentication Testing**: Integrate API authentication validation when security is implemented
- **Performance Integration**: Add performance regression testing using similar property-based approaches  
- **Environment Testing**: Expand testing to staging and pre-production environments
- **Advanced Validation**: Consider additional checks for response headers, timing, and other quality attributes

## References

- [Schemathesis Documentation](https://schemathesis.readthedocs.io/)
- [Schemathesis GitHub Action](https://github.com/schemathesis/action)  
- [Project OpenAPI Specification](/docs/api/openapi.yaml)
- [Property-Based Testing Principles](https://hypothesis.works/)
- [OpenAPI Specification v3.0](https://swagger.io/specification/)