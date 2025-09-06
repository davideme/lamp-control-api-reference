# Lamp Control API Reference

[![Database Schema Tests](https://github.com/davideme/lamp-control-api-reference/actions/workflows/database-tests.yml/badge.svg)](https://github.com/davideme/lamp-control-api-reference/actions/workflows/database-tests.yml)

A comprehensive reference implementation of a simple lamp control API in multiple popular web programming languages, showcasing different API styles (REST, GraphQL, and gRPC) and database technologies.

## Project Purpose and Scope

This open source project serves as a **comparative study and reference implementation** for web API development across multiple programming languages and frameworks. The primary goals are:

### Purpose
- **Educational Resource**: Provide clear, idiomatic code examples for developers learning new programming languages or frameworks
- **Technology Comparison**: Enable objective comparison of different languages, frameworks, and API design patterns
- **Best Practices Demonstration**: Showcase proper API design, testing practices, and code organization across different technology stacks
- **Decision Support**: Help development teams evaluate technology choices for API development projects

### Scope
The project implements a deliberately simple domain (lamp control) to focus on:
- **API Design Patterns**: REST, GraphQL, and gRPC implementations
- **Database Integration**: Support for both SQL (MySQL/PostgreSQL) and NoSQL (MongoDB) databases
- **Testing Strategies**: Comprehensive test coverage across all implementations
- **Code Quality**: Consistent code style, documentation, and maintainability practices
- **Development Workflow**: Modern development practices following DORA metrics and principles

The simple lamp resource (with ID and on/off state) allows for complete CRUD operations while keeping the focus on implementation patterns rather than complex business logic.

## Languages Included

The project currently includes implementations in **8 programming languages**, each demonstrating language-specific best practices and patterns:

- **TypeScript** - Node.js with Express.js, comprehensive type safety
- **Python** - FastAPI with modern async/await patterns
- **Java** - Spring Boot with enterprise-grade patterns
- **C#** - ASP.NET Core with .NET 8 features
- **PHP** - Slim Framework with modern PHP 8.1+ features
- **Go** - Chi Router with goroutines and channels
- **Kotlin** - Ktor framework with coroutines
- **Ruby** - Rails API (in development)

Each implementation maintains functional consistency while showcasing language-specific idioms, frameworks, and best practices.

## Key Features

- Consistent implementations across all languages
- Support for both SQL (MySQL/PostgreSQL) and MongoDB
- Three API interfaces (REST/OpenAPI 3.0+, GraphQL, gRPC)
- Comprehensive test coverage
- Standardized documentation

## How to Use This Repository

### For Learning and Comparison
1. **Browse Implementations**: Explore the `src/` directory to see how the same API is implemented across different languages
2. **Study Patterns**: Compare how each language handles routing, data modeling, error handling, and testing
3. **Run Examples**: Follow the setup instructions in each language's README to run and test the implementations
4. **Review Documentation**: Check the `docs/` directory for detailed analysis and comparisons

### For Development Teams
1. **Technology Evaluation**: Use the comparison metrics and implementation analysis to evaluate technology choices
2. **Reference Implementation**: Use as a starting point for new API projects
3. **Best Practices**: Study the code organization, testing patterns, and documentation approaches

### Repository Structure
```
lamp-control-api-reference/
├── docs/                          # Project documentation
│   ├── PRD.md                    # Product Requirements Document
│   ├── COMPARISON.md             # Implementation comparison and metrics
│   └── IMPLEMENTATION_INVENTORY.md  # Detailed implementation status
├── src/                          # Language implementations
│   ├── typescript/               # Node.js + Express implementation
│   ├── python/                   # FastAPI implementation
│   ├── java/                     # Spring Boot implementation
│   ├── csharp/                   # ASP.NET Core implementation
│   ├── php/                      # Slim Framework implementation
│   ├── go/                       # Chi Router implementation
│   ├── kotlin/                   # Ktor implementation
│   └── ruby/                     # Rails API implementation
├── database/                     # Database schemas and setup scripts
├── .github/                      # GitHub Actions workflows and templates
└── CONTRIBUTING.md               # Detailed contribution guidelines
```

### Quick Start
1. **Choose a language** you want to explore from the `src/` directory
2. **Read the language-specific README** for setup and run instructions
3. **Review the API documentation** in `docs/api/openapi.yaml` for endpoint specifications
4. **Run the tests** to see comprehensive test coverage examples
5. **Compare implementations** using the metrics in `docs/COMPARISON.md`

## How Solutions Are Compared

### Comparison Methodology

This project uses a comprehensive approach to compare implementations across multiple dimensions:

#### Quantitative Metrics
- **Lines of Code**: Both total lines and lines excluding generated code
- **Test Coverage**: Percentage of code covered by automated tests
- **Test Density**: Ratio of test code to application code
- **Code Complexity**: Cyclomatic complexity and maintainability scores

#### Qualitative Analysis
- **Code Readability**: How easy is the code to read and understand
- **Idiomatic Usage**: How well the implementation follows language conventions
- **Framework Integration**: How effectively each implementation uses its chosen framework
- **Error Handling**: Consistency and robustness of error handling patterns

#### Performance Considerations
- **Development Speed**: How quickly can features be implemented
- **Runtime Performance**: Response times and resource usage
- **Developer Experience**: Ease of setup, testing, and maintenance

### Current Comparison Results

| Language   | App Lines | App Lines (No Generated) | Test Lines | Test/App Ratio | Coverage (%) |
|------------|-----------|--------------------------|------------|---------------|--------------|
| TypeScript | 365       | 183                      | 343        | 0.94          | 85.00        |
| Python     | 346       | 217                      | 215        | 0.62          | 86.40        |
| Java       | 691       | 555                      | 619        | 0.90          | 92.00        |
| C#         | 412       | 254                      | 435        | 1.06          | 98.59        |
| PHP        | 1419      | 774                      | 1158       | 0.82          | 88.89        |
| Go         | 715       | 197                      | 1307       | 1.83          | 98.60        |
| Kotlin     | 583       | 462                      | 795        | 1.36          | 79.19        |
| Ruby       | -         | -                        | -          | -             | -            |

*Detailed analysis and insights available in [docs/COMPARISON.md](docs/COMPARISON.md)*

### API Interface Comparison

All implementations support multiple API styles:

- **REST API**: OpenAPI 3.0 specification with standard HTTP methods
- **GraphQL**: Query and mutation operations with strong typing
- **gRPC**: High-performance RPC with Protocol Buffers

*Note: Current implementations primarily focus on REST APIs, with GraphQL and gRPC implementations planned for future releases.*

## Contributing

We welcome contributions from developers of all skill levels! This project follows the [DORA Core Model](https://dora.dev/) for development practices, emphasizing collaboration, learning, and high-quality code.

### Quick Contribution Guide

1. **Choose Your Focus**:
   - Add a new language implementation
   - Improve existing implementations
   - Enhance documentation
   - Add new API interfaces (GraphQL, gRPC)
   - Improve testing coverage

2. **Development Workflow**:
   - Fork the repository
   - Create a feature branch: `[language]-[feature]` (e.g., `python-graphql-api`)
   - Follow the existing code style and patterns
   - Write comprehensive tests
   - Update documentation as needed
   - Submit a pull request

3. **Code Standards**:
   - Follow language-specific best practices and conventions
   - Maintain or improve test coverage (aim for >85%)
   - Use conventional commit messages
   - Include appropriate documentation

4. **Getting Help**:
   - Check the [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines
   - Review existing implementations for patterns and examples
   - Open an issue for questions or discussions

### What We're Looking For

- **New Language Implementations**: Help us expand to more languages
- **API Interface Implementations**: GraphQL and gRPC implementations across languages
- **Database Connectors**: MySQL, PostgreSQL, and MongoDB integration
- **Performance Optimizations**: Benchmarking and optimization examples
- **Documentation Improvements**: Better examples, tutorials, and guides

*For comprehensive contribution guidelines, see [CONTRIBUTING.md](CONTRIBUTING.md)*

## License

This project is currently under development and does not have a formal license yet. Please check back for licensing information or contact the maintainers for questions about usage and distribution.

## Development Requirements

### Node.js/TypeScript Implementation
- **Node.js:** `>=20.x`
- **npm:** `>=10.x`

*For architectural decisions, see [Architecture Decision Records](docs/adr/)*

### Other Languages
Each language implementation has specific requirements documented in their respective README files:
- [Python Requirements](src/python/README.md)
- [Java Requirements](src/java/README.md)
- [C# Requirements](src/csharp/README.md)
- [PHP Requirements](src/php/README.md)
- [Go Requirements](src/go/README.md)
- [Kotlin Requirements](src/kotlin/README.md)

## Documentation and References

### Project Documentation
- **[Product Requirements Document](docs/PRD.md)** - Comprehensive project requirements and specifications
- **[Implementation Comparison](docs/COMPARISON.md)** - Detailed comparison metrics and analysis
- **[Implementation Inventory](docs/IMPLEMENTATION_INVENTORY.md)** - Current status of all implementations
- **[Contributing Guidelines](CONTRIBUTING.md)** - Detailed development guidelines based on DORA principles

### API Documentation
- **[REST API Specification](docs/api/openapi.yaml)** - OpenAPI 3.0 specifications
- **[GraphQL Schema](docs/api/graphql.graphql)** - GraphQL type definitions and operations
- **[gRPC Protocol](docs/api/lamp.proto)** - Protocol Buffer definitions and service contracts

### External References
- **[DORA Core Model](https://dora.dev/)** - DevOps Research and Assessment principles used in this project
- **[OpenAPI Specification](https://swagger.io/specification/)** - REST API documentation standard
- **[GraphQL](https://graphql.org/)** - Query language for APIs
- **[gRPC](https://grpc.io/)** - High-performance RPC framework
- **[Conventional Commits](https://www.conventionalcommits.org/)** - Commit message convention used in this project

### Learning Resources
- **Language-specific best practices** - Each implementation demonstrates idiomatic code for its respective language
- **API design patterns** - Examples of REST, GraphQL, and gRPC implementations
- **Testing strategies** - Comprehensive test coverage examples across all languages
- **Code organization** - Repository patterns, dependency injection, and modular architecture examples

---

**Happy coding!** 🚀 Whether you're learning a new language, evaluating technology choices, or contributing to open source, we hope this project serves as a valuable resource for your development journey.
