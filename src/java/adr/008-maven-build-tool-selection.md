# ADR 008: Maven as Build Tool and Dependency Manager

**Status:** Accepted  
**Date:** 2025-06-01

## Context

The Java Spring Boot Lamp Control API project requires a robust build tool and dependency management system to handle:

- Project compilation and packaging
- Dependency resolution and management
- Build lifecycle management
- Plugin ecosystem integration
- CI/CD pipeline compatibility
- Code quality tool integration
- Multi-module project support (if needed)

The primary build tool options for Java projects include:

1. **Apache Maven** - Convention-over-configuration, XML-based build tool
2. **Gradle** - Flexible, Groovy/Kotlin DSL-based build tool
3. **Apache Ant** - Legacy XML-based build tool
4. **sbt** - Scala-focused build tool with Java support

## Decision

We will use **Apache Maven** as our build tool and dependency management system for this Java project.

## Rationale

### Why Maven?

1. **Industry Standard**
   - Widely adopted across the Java ecosystem
   - De facto standard for Spring Boot projects
   - Extensive documentation and community support
   - Familiar to most Java developers

2. **Convention Over Configuration**
   - Standardized project structure (`src/main/java`, `src/test/java`)
   - Predefined build lifecycle phases
   - Minimal configuration for standard projects
   - Clear separation of concerns

3. **Dependency Management**
   - Central repository system (Maven Central)
   - Transitive dependency resolution
   - Version conflict resolution
   - Scope-based dependency management (compile, test, runtime, provided)

4. **Plugin Ecosystem**
   - Rich ecosystem of plugins for code quality, testing, packaging
   - Spring Boot Maven plugin integration
   - Excellent support for code quality tools (SpotBugs, PMD, Checkstyle, Spotless)
   - OpenAPI Generator Maven plugin compatibility

5. **CI/CD Integration**
   - Excellent support in all major CI/CD platforms
   - Standardized commands (`mvn clean install`)
   - Artifact publishing capabilities
   - Integration with artifact repositories

6. **Project Lifecycle Management**
   - Well-defined build phases (validate, compile, test, package, install, deploy)
   - Consistent build reproducibility
   - Multi-module project support
   - Profile-based configuration for different environments

### Integration with Existing Tools

- **Spring Boot**: Seamless integration with Spring Boot starter dependencies
- **Code Quality**: Native support for Spotless, SpotBugs, PMD, Checkstyle plugins
- **Testing**: JUnit, Mockito, Testcontainers Maven plugin integration
- **OpenAPI**: OpenAPI Generator Maven plugin for code generation
- **Docker**: Jib Maven plugin for containerization

## Alternatives Considered

### Gradle
**Pros:**
- More flexible and powerful build scripts
- Better performance with build caching and parallel execution
- Modern Groovy/Kotlin DSL syntax
- Excellent incremental builds

**Cons:**
- Steeper learning curve
- More complex configuration for simple projects
- Less standardized project structures
- Debugging build scripts can be challenging

### Apache Ant
**Pros:**
- Maximum flexibility and control
- XML-based configuration

**Cons:**
- No dependency management (requires Ivy)
- No standard project structure
- Verbose configuration
- Legacy tool with declining adoption

### sbt (Simple Build Tool)
**Pros:**
- Powerful for Scala projects
- Incremental compilation

**Cons:**
- Scala-focused, less optimal for pure Java
- Steeper learning curve
- Smaller ecosystem for Java-specific tools

## Implementation Details

### Project Structure
```
src/
├── main/
│   ├── java/           # Main source code
│   └── resources/      # Configuration files
└── test/
    ├── java/           # Test source code
    └── resources/      # Test resources
target/                 # Build output directory
pom.xml                # Maven configuration
```

### Key Maven Configuration

```xml
<properties>
    <java.version>21</java.version>
    <maven.compiler.source>${java.version}</maven.compiler.source>
    <maven.compiler.target>${java.version}</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>
```

### Essential Plugins
- **spring-boot-maven-plugin**: Spring Boot application packaging
- **maven-compiler-plugin**: Java compilation
- **maven-surefire-plugin**: Unit test execution
- **spotless-maven-plugin**: Code formatting
- **spotbugs-maven-plugin**: Static analysis
- **jacoco-maven-plugin**: Code coverage

### Build Profiles
- **default**: Standard build with unit tests
- **integration-tests**: Include integration tests with Testcontainers
- **code-quality**: Enhanced static analysis and formatting checks

## Consequences

### Positive
- **Standardization**: Consistent build process across development and CI/CD
- **Ecosystem**: Access to extensive Maven plugin ecosystem
- **Maintainability**: Well-understood build configuration for team members
- **Reliability**: Proven, stable build tool with predictable behavior
- **Integration**: Seamless integration with Spring Boot and Java ecosystem

### Negative
- **Verbosity**: XML configuration can be verbose for complex builds
- **Performance**: Slower than Gradle for large projects (mitigated by our project size)
- **Flexibility**: Less flexible than Gradle for complex build scenarios

### Neutral
- **Learning Curve**: Most Java developers are already familiar with Maven
- **Migration**: Standard Maven project structure aligns with existing setup

## Implementation Plan

1. ✅ Configure `pom.xml` with project dependencies and plugins
2. ✅ Set up code quality plugins (Spotless, SpotBugs, PMD, Checkstyle)
3. ✅ Configure test execution profiles
4. ✅ Integrate with CI/CD pipeline
5. ✅ Document build commands in Makefile
6. ✅ Set up artifact generation and packaging

## Build Commands

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package application
mvn package

# Full build with tests
mvn clean install

# Code formatting
mvn spotless:apply

# Static analysis
mvn spotbugs:check pmd:check checkstyle:check

# Generate reports
mvn site
```

## References

- [Apache Maven Documentation](https://maven.apache.org/guides/)
- [Maven Central Repository](https://search.maven.org/)
- [Spring Boot Maven Plugin](https://docs.spring.io/spring-boot/docs/current/maven-plugin/reference/htmlsingle/)
- [Maven Standard Directory Layout](https://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html)
- [Maven Build Lifecycle](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html)
