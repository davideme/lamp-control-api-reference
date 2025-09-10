# GitHub Copilot Coding Agent Instructions

This repository serves as a comprehensive reference implementation of a Lamp Control API across multiple programming languages and frameworks. Each implementation demonstrates best practices for modern API development while maintaining consistency in functionality and design.

## Repository Purpose

This is a multi-language reference implementation designed for:
- **Learning and Comparison**: Study how the same API is implemented across different languages
- **Technology Evaluation**: Compare metrics and implementation approaches for technology decisions
- **Best Practice Reference**: Demonstrate modern API development patterns in each language
- **Development Team Training**: Provide examples of well-structured, tested code

## General Guidelines

When working on this repository:

### Code Quality Standards
- Follow the DORA Core Model development practices documented in `CONTRIBUTING.md`
- Maintain high test coverage (80%+ minimum across all implementations)
- Use language-specific linters and static analysis tools
- Follow conventional commit messages for version control
- Ensure all implementations maintain consistency with the OpenAPI specification

### API Consistency Requirements
- All implementations must support the same core lamp control functionality
- Maintain consistent domain model: lamps with `id` and `isOn`/`status` properties
- Use proper HTTP status codes across all implementations
- Follow RESTful principles for endpoint design
- Ensure OpenAPI specification compliance

### Development Workflow
- Create feature branches using format: `[language]-[feature]` 
- Write comprehensive tests before implementing features
- Use CI/CD pipelines for automated testing and quality checks
- Review code coverage and quality metrics in pull requests
- Maintain documentation alongside code changes

### Implementation Requirements
Each language implementation should include:
- **REST API**: Core HTTP endpoints for lamp control
- **Testing**: Comprehensive unit and integration tests
- **Documentation**: Clear setup and usage instructions
- **CI/CD**: Automated testing and quality checks
- **Dependencies**: Proper dependency management with lock files
- **Error Handling**: Graceful error responses with appropriate status codes

## Architecture Patterns

### Repository Pattern
Most implementations follow a repository pattern for data access:
- Repository interfaces for data operations
- Service layer for business logic
- Controller/handler layer for HTTP endpoints
- Dependency injection for loose coupling

### Storage Implementations
Implementations vary in storage approach:
- **In-memory**: Thread-safe maps for development/testing
- **Database**: Some implementations support persistent storage
- **Mock/Stub**: For testing scenarios

### Testing Strategy
All implementations should include:
- **Unit Tests**: Individual component testing
- **Integration Tests**: End-to-end API testing
- **Property-Based Testing**: Using tools like Schemathesis
- **Concurrency Tests**: For thread-safe implementations
- **Coverage Reports**: Minimum 80% code coverage

## Language-Specific Notes

Each implementation has detailed instructions in `.github/instructions/[language].instructions.md`:
- **TypeScript**: Express.js with strict TypeScript, Jest testing
- **Python**: FastAPI with Poetry, pytest, and Pydantic models
- **Java**: Spring Boot with comprehensive quality tools
- **C#**: ASP.NET Core with StyleCop analyzers
- **PHP**: Slim Framework with PSR standards
- **Go**: Chi Router with oapi-codegen, high test coverage
- **Kotlin**: Ktor with coroutines and serialization
- **Ruby**: Rails API with multi-database support (planned)

## Quality Metrics and Comparison

The repository maintains detailed metrics in `docs/COMPARISON.md` including:
- Lines of code (ULOC) measurements
- Test coverage percentages
- Build and test performance
- Code complexity analysis
- Framework-specific observations

## Contributing

When contributing to this repository:
1. Read the language-specific instructions for your target implementation
2. Follow the DORA guidelines in `CONTRIBUTING.md`
3. Ensure your changes maintain consistency across implementations
4. Include comprehensive tests with your changes
5. Update documentation as needed
6. Verify CI/CD pipelines pass before submitting pull requests

## Observability and Monitoring

Implementations should include:
- **Structured Logging**: Use appropriate logging frameworks
- **Health Checks**: Endpoint for monitoring system health
- **Metrics**: Basic application metrics where applicable
- **Error Tracking**: Proper error handling and reporting

This repository serves as both a learning resource and a practical reference for building high-quality APIs across different technology stacks.