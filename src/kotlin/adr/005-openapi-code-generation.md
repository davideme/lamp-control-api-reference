# ADR 005: Use OpenAPI Generator for Ktor Server Code Generation

## Status

Accepted

## Date

2025-06-26

## Context

The Kotlin Ktor implementation of the Lamp Control API needs to be generated from the OpenAPI 3.0 specification to ensure contract-first development and consistency across all language implementations in this multi-language project. The generated code should:

- Maintain type safety with Kotlin's strong type system
- Integrate seamlessly with Ktor framework patterns
- Follow Kotlin idioms and best practices
- Support kotlinx.serialization for JSON handling
- Allow for easy regeneration when the OpenAPI spec changes

Available options for generating Ktor server code from OpenAPI specifications:

1. **OpenAPI Generator** - Multi-language code generator with `kotlin-server` template
2. **Ktor OpenAPI Plugin** - Native Ktor plugin for OpenAPI integration
3. **Swagger Codegen** - Predecessor to OpenAPI Generator (less maintained)
4. **Custom code generation** - Manual template-based approach
5. **Manual implementation** - Hand-written code following OpenAPI spec

## Decision

We will use **OpenAPI Generator with the `kotlin-server` template** configured for Ktor library generation.

## Rationale

### Consistency with Project Architecture

The project already uses OpenAPI Generator for multiple language implementations:
- Java Spring Boot (using `spring` generator)
- Go (using `go-server` generator with chi router)
- Python (using `python-fastapi` generator)
- C# (using NSwag for ASP.NET Core)
- PHP (using `php-slim4` generator)

Using OpenAPI Generator for Kotlin maintains consistency in tooling and workflow across all implementations.

### Technical Advantages

1. **Mature Ktor Support**
   ```bash
   # Generates Ktor-specific routing and handlers
   openapi-generator generate \
     -g kotlin-server \
     --additional-properties=library=ktor,serializationLibrary=kotlinx_serialization
   ```

2. **Type Safety and Kotlin Idioms**
   - Generates Kotlin data classes with proper nullability
   - Uses kotlinx.serialization for JSON handling
   - Provides type-safe route parameters and request/response models
   - Leverages Kotlin's sealed classes for API responses

3. **Integration with Existing Toolchain**
   - Docker-based generation matches existing Makefile patterns
   - Consistent versioning with other language generators
   - Same OpenAPI specification validation workflow

4. **Maintenance and Updates**
   - Actively maintained by the OpenAPI Generator community
   - Regular updates with new Ktor versions
   - Comprehensive documentation and examples

### Configuration Options

```bash
# Docker-based generation following project patterns
docker run --rm \
  -v ${PWD}/../..:/local \
  openapitools/openapi-generator-cli:v7.13.0 generate \
  -i /local/docs/api/openapi.yaml \
  -g kotlin-server \
  -o /local/src/kotlin/generated \
  --additional-properties=\
library=ktor,\
packageName=com.lampcontrol.api,\
serializationLibrary=kotlinx_serialization,\
enumPropertyNaming=UPPERCASE,\
dateLibrary=kotlinx-datetime
```

## Consequences

### Positive

- **Contract-First Development**: API implementation is guaranteed to match the OpenAPI specification
- **Reduced Boilerplate**: Automatic generation of data classes, routing stubs, and serialization code
- **Type Safety**: Compile-time validation of API contracts and request/response types
- **Consistency**: Generated code follows established patterns from other language implementations
- **Maintainability**: Changes to API require updating OpenAPI spec, keeping documentation synchronized
- **Testing Support**: Generated interfaces facilitate unit testing through mocking

### Negative

- **Build Complexity**: Code generation must be integrated into the build process
- **Tool Dependency**: Project depends on OpenAPI Generator tool and its Kotlin template
- **Generated Code Management**: Need to handle generated code in version control (typically ignored)
- **Customization Limitations**: Generated code may require post-processing for specific customizations
- **Learning Curve**: Team needs to understand OpenAPI Generator configuration and limitations

### Mitigation Strategies

- **Build Integration**: Use Makefile pattern consistent with other language implementations
- **Documentation**: Maintain clear documentation of generation process and customization approaches
- **Version Pinning**: Use specific OpenAPI Generator versions for reproducible builds
- **Generated Code Isolation**: Keep generated code separate from business logic implementation
- **Validation**: Include OpenAPI specification validation in CI/CD pipeline

## Implementation Plan

1. **Create Makefile** following existing project patterns:
   ```makefile
   OPENAPI_GENERATOR_VERSION := v7.13.0
   OPENAPI_SPEC := ../../docs/api/openapi.yaml
   OUTPUT_DIR := generated
   ```

2. **Project Structure**:
   ```
   src/kotlin/
   ├── adr/
   ├── generated/          # Generated code (gitignored)
   ├── src/main/kotlin/
   │   ├── Application.kt  # Main Ktor application
   │   ├── plugins/        # Ktor plugins configuration
   │   └── routes/         # Business logic implementations
   ├── build.gradle.kts
   └── Makefile
   ```

3. **Dependencies**:
   ```kotlin
   dependencies {
       implementation("io.ktor:ktor-server-core:$ktor_version")
       implementation("io.ktor:ktor-server-netty:$ktor_version")
       implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
       implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
       implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinx_datetime_version")
   }
   ```

4. **Build Process Integration**:
   - Add `make generate` to development workflow
   - Include generation step in CI/CD pipeline
   - Document regeneration process for API changes

## Alternatives Considered

### Ktor OpenAPI Plugin
- **Pros**: Native Ktor integration, runtime OpenAPI documentation
- **Cons**: Less mature than OpenAPI Generator, limited code generation capabilities, doesn't fit project's contract-first approach

### Swagger Codegen
- **Pros**: Predecessor to OpenAPI Generator with similar capabilities
- **Cons**: Less actively maintained, fewer Kotlin-specific improvements, project is moving toward OpenAPI Generator

### Manual Implementation
- **Pros**: Full control over implementation details, no tool dependencies
- **Cons**: High maintenance burden, prone to specification drift, inconsistent with project's automated approach

## References

- [OpenAPI Generator Documentation](https://openapi-generator.tech/)
- [OpenAPI Generator Kotlin Server Template](https://openapi-generator.tech/docs/generators/kotlin-server/)
- [Ktor Framework Documentation](https://ktor.io/)
- [kotlinx.serialization Documentation](https://kotlinlang.org/docs/serialization.html)
- [Project OpenAPI Specification](/docs/api/openapi.yaml)
