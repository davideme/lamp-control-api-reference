# ADR 001: Project Setup and Language Selection

## Status
Accepted

## Context
We need to establish the foundation for the lamp control API project. This includes selecting the primary programming language and setting up the initial project structure.

## Decision
We have chosen TypeScript as our primary programming language for the following reasons:

1. **Type Safety**: TypeScript provides static typing, which helps catch errors at compile time and improves code quality.
2. **Modern JavaScript Features**: TypeScript supports modern JavaScript features while maintaining backward compatibility.
3. **Tooling Support**: Excellent IDE support, debugging tools, and build tools are available for TypeScript.
4. **Community and Ecosystem**: Strong community support and a rich ecosystem of libraries and frameworks.
5. **Documentation**: TypeScript's type system serves as documentation and helps with code maintainability.

The project will follow a clean architecture approach with the following structure:
```
src/
├── domain/         # Business logic and entities
├── application/    # Use cases and application services
├── infrastructure/ # External interfaces and implementations
```

## Consequences
### Positive
- Improved code quality through static typing
- Better developer experience with modern tooling
- Clear separation of concerns through clean architecture
- Easier maintenance and refactoring
- Better documentation through types

### Negative
- Additional build step required
- Learning curve for developers unfamiliar with TypeScript
- Slightly more complex project setup

## References
- [TypeScript Documentation](https://www.typescriptlang.org/docs/)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html) 