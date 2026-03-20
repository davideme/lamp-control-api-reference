# ADR 008: Java Test Tools Selection

**Status:** Accepted
**Date:** 2025-05-25

**Context:**
The Java Spring Boot Lamp Control API project requires a comprehensive testing strategy to ensure code quality, reliability, and maintainability. The project needs to support multiple testing levels including unit tests, integration tests, and performance tests, while providing adequate code coverage reporting and async testing capabilities.

Key requirements:
- Unit testing with mocking capabilities for isolated component testing
- Integration testing with external dependencies (databases, external APIs)
- Performance testing capabilities for load and stress testing
- Code coverage measurement and reporting
- Asynchronous testing support for Spring's async request handling
- Fluent assertion capabilities for readable test code
- Multiple test execution profiles for different scenarios

**Decision:**
The project will adopt the following comprehensive test tools stack:

**Core Testing Framework:**
- **JUnit 5 (Jupiter)** - Modern testing framework with improved annotations and assertions
- **Spring Boot Test Starter** - Comprehensive testing support for Spring Boot applications

**Mocking and Test Doubles:**
- **Mockito** - Industry-standard mocking framework (included in Spring Boot Test Starter)
- **Spring Boot Test Mock** - `@MockBean` annotations for Spring context integration

**Integration Testing:**
- **Testcontainers** (v1.19.3) - Lightweight, throwaway instances of common databases and services
- **MockWebServer** (OkHttp) - Mock HTTP server for external API testing

**Assertions and Test Utilities:**
- **AssertJ** - Fluent assertion library for more readable and expressive tests
- **Awaitility** - Testing library for asynchronous systems and eventual consistency

**Code Coverage:**
- **JaCoCo** (v0.8.10) - Code coverage analysis with 80% instruction and 75% branch coverage requirements

**Test Execution:**
- **Maven Surefire Plugin** - Test execution with multiple profiles for different test types

**Implementation Approach:**

### Test Execution Profiles:
1. **unit-tests** (default): Runs unit tests only, excludes integration and performance tests
2. **integration-tests**: Runs integration tests using Testcontainers
3. **performance-tests**: Runs performance tests with special system properties
4. **all-tests**: Runs all test types including performance tests

### Testing Strategy:
- **Unit Tests**: Use `@WebMvcTest`, `@MockBean`, and Mockito for isolated testing
- **Integration Tests**: Use Testcontainers for database integration and MockWebServer for external API simulation
- **Async Testing**: Leverage Awaitility for testing asynchronous operations and Spring's async request handling
- **Assertions**: Use AssertJ for fluent and readable test assertions

### Code Coverage Configuration:
- Minimum 80% instruction coverage required
- Minimum 75% branch coverage required
- Excludes generated OpenAPI code from coverage analysis
- Generates HTML and XML reports for CI/CD integration

### Maven Dependencies:
```xml
<!-- Core Testing -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- Integration Testing -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>

<!-- External API Testing -->
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>mockwebserver</artifactId>
    <scope>test</scope>
</dependency>

<!-- Enhanced Assertions -->
<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <scope>test</scope>
</dependency>

<!-- Async Testing -->
<dependency>
    <groupId>org.awaitility</groupId>
    <artifactId>awaitility</artifactId>
    <scope>test</scope>
</dependency>
```

**Consequences:**

**Positive:**
- **Comprehensive Testing**: Multiple testing levels ensure robust code quality
- **Modern Framework**: JUnit 5 provides improved testing features and annotations
- **Realistic Integration Tests**: Testcontainers enables testing with real database instances
- **Readable Tests**: AssertJ provides fluent, expressive assertions
- **Async Support**: Awaitility handles asynchronous testing scenarios effectively
- **Flexible Execution**: Multiple Maven profiles allow targeted test execution
- **Coverage Enforcement**: JaCoCo ensures adequate test coverage with configurable thresholds
- **CI/CD Ready**: Generated reports integrate well with build pipelines

**Negative:**
- **Resource Intensive**: Testcontainers require Docker and can slow down integration tests
- **Learning Curve**: Team needs familiarity with multiple testing tools and frameworks
- **Build Complexity**: Multiple test profiles and coverage requirements add build complexity
- **Dependency Management**: Managing versions and compatibility across multiple test libraries

**Mitigation Strategies:**
- Provide team training on testing tools and best practices
- Use separate CI stages for different test types to optimize build times
- Maintain comprehensive documentation for test setup and execution
- Regular dependency updates to maintain security and compatibility

**Alternatives Considered:**

1. **TestNG**: Rejected in favor of JUnit 5's modern features and better Spring Boot integration
2. **WireMock**: Considered but MockWebServer chosen for simpler setup and OkHttp ecosystem alignment
3. **Truth (Google)**: Considered but AssertJ chosen for broader community adoption and richer API
4. **Embedded Database (H2)**: Considered but Testcontainers chosen for more realistic testing scenarios

**Implementation Timeline:**
- Phase 1: Core unit testing setup with JUnit 5 and Mockito ✅
- Phase 2: Integration testing with Testcontainers ✅
- Phase 3: Enhanced assertions with AssertJ ✅
- Phase 4: Async testing capabilities with Awaitility ✅
- Phase 5: Code coverage enforcement with JaCoCo ✅
- Phase 6: Performance testing profile setup ✅
