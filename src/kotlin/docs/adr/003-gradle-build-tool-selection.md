# ADR 003: Gradle as Build Tool and Dependency Manager

## Status

Accepted

## Context

The Kotlin implementation of the Lamp Control API requires a robust build system and dependency management solution to handle:

- Project compilation and build orchestration
- Dependency resolution and version management
- Multi-module project support (if needed)
- Kotlin-specific build tasks and optimizations
- JVM target compatibility and configuration
- Integration with CI/CD pipelines
- Code quality and testing tool integration

Available build tool options for Kotlin projects include:

1. **Gradle** - Modern build automation system
2. **Maven** - Traditional XML-based build tool
3. **sbt** - Scala-focused build tool with Kotlin support
4. **Bazel** - Google's build system for large-scale projects
5. **IntelliJ IDEA Build System** - IDE-integrated build

## Decision

We will use **Gradle** with Kotlin DSL as our build tool and dependency manager for the Kotlin implementation.

## Rationale

### Why Gradle?

1. **Kotlin-First Design**
   - Native Kotlin support with excellent integration
   - Kotlin DSL for build scripts (build.gradle.kts)
   - Optimized for Kotlin compilation and tooling
   - Developed by JetBrains (creators of Kotlin)

2. **Modern Build System**
   - Incremental compilation and build caching
   - Parallel task execution for faster builds
   - Flexible and powerful build script DSL
   - Plugin ecosystem with excellent Kotlin support

3. **Dependency Management**
   - Advanced dependency resolution with conflict resolution
   - Support for multiple repositories (Maven Central, JCenter, etc.)
   - Version catalogs for centralized dependency management
   - Gradle Module Metadata for improved dependency handling

4. **Performance**
   - Build cache for faster incremental builds
   - Gradle daemon for reduced startup times
   - Parallel project builds
   - Incremental annotation processing

5. **Ecosystem Integration**
   - Excellent IDE integration (IntelliJ IDEA, Android Studio)
   - Comprehensive plugin ecosystem
   - Spring Boot Gradle plugin support
   - Kotlin multiplatform support

6. **Flexibility**
   - Custom task definitions and build logic
   - Multi-module project support
   - Environment-specific configurations
   - Custom repository and publication support

## Project Configuration

### build.gradle.kts Structure
```kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.1.21"
    kotlin("plugin.serialization") version "2.1.21"
    application
}

group = "com.lampcontrolapi"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    val ktor_version = "2.3.12"
    val kotlin_version = "2.1.21"
    val logback_version = "1.4.14"
    val exposed_version = "0.41.1"
    
    // Ktor server dependencies
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-config-yaml:$ktor_version")
    
    // Content negotiation and serialization
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
    
    // Request validation
    implementation("io.ktor:ktor-server-request-validation:$ktor_version")
    
    // Status pages and call logging
    implementation("io.ktor:ktor-server-status-pages:$ktor_version")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktor_version")
    
    // Kotlin coroutines and datetime
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
    
    // Database (Exposed ORM - more Kotlin idiomatic than JPA)
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposed_version")
    
    // Database drivers
    implementation("com.h2database:h2:2.2.224")
    implementation("org.postgresql:postgresql:42.7.2")
    
    // Logging
    implementation("ch.qos.logback:logback-classic:$logback_version")
    
    // Testing
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.testcontainers:junit-jupiter:1.19.1")
    testImplementation("org.testcontainers:postgresql:1.19.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "21"
        freeCompilerArgs += listOf(
            "-Xjsr305=strict",
            "-opt-in=kotlin.RequiresOptIn"
        )
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

application {
    mainClass.set("com.lampcontrolapi.ApplicationKt")
}
```

### gradle.properties
```properties
# Gradle configuration
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.daemon=true

# Kotlin configuration
kotlin.code.style=official
kotlin.incremental=true
kotlin.incremental.multiplatform=true
kotlin.parallel.tasks.in.project=true

# Spring Boot configuration
spring.jpa.defer-datasource-initialization=true
```

### settings.gradle.kts
```kotlin
rootProject.name = "lamp-control-api-kotlin"

// Enable Gradle version catalogs
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
```

## Build Tasks and Commands

### Basic Commands
```bash
# Build the project
./gradlew build

# Run the application
./gradlew run

# Run tests
./gradlew test

# Clean build artifacts
./gradlew clean

# Compile Kotlin code
./gradlew compileKotlin

# Generate dependency report
./gradlew dependencies

# Check for dependency updates
./gradlew dependencyUpdates

# Run specific test
./gradlew test --tests "LampControllerTest"

# Build without tests
./gradlew build -x test

# Continuous build (watch for changes)
./gradlew build --continuous
```

### Custom Tasks
```kotlin
// Custom tasks in build.gradle.kts
tasks.register<Exec>("lint") {
    group = "verification"
    description = "Run ktlint check"
    commandLine("ktlint", "src/**/*.kt")
}

tasks.register<Exec>("format") {
    group = "formatting"
    description = "Format Kotlin code with ktlint"
    commandLine("ktlint", "-F", "src/**/*.kt")
}

tasks.register("printVersion") {
    group = "help"
    description = "Print project version"
    doLast {
        println("Project version: $version")
    }
}
```

## Dependency Management

### Version Catalogs (gradle/libs.versions.toml)
```toml
[versions]
kotlin = "2.1.21"
ktor = "2.3.12"
coroutines = "1.7.3"
exposed = "0.41.1"
logback = "1.4.14"
junit = "5.10.0"
mockk = "1.13.8"
testcontainers = "1.19.1"

[libraries]
# Ktor server
ktor-server-core = { group = "io.ktor", name = "ktor-server-core-jvm", version.ref = "ktor" }
ktor-server-netty = { group = "io.ktor", name = "ktor-server-netty-jvm", version.ref = "ktor" }
ktor-server-config-yaml = { group = "io.ktor", name = "ktor-server-config-yaml", version.ref = "ktor" }
ktor-server-content-negotiation = { group = "io.ktor", name = "ktor-server-content-negotiation-jvm", version.ref = "ktor" }
ktor-serialization-json = { group = "io.ktor", name = "ktor-serialization-kotlinx-json-jvm", version.ref = "ktor" }
ktor-server-status-pages = { group = "io.ktor", name = "ktor-server-status-pages", version.ref = "ktor" }
ktor-server-call-logging = { group = "io.ktor", name = "ktor-server-call-logging-jvm", version.ref = "ktor" }

# Kotlin libraries
kotlinx-coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "coroutines" }
kotlinx-datetime = { group = "org.jetbrains.kotlinx", name = "kotlinx-datetime", version = "0.4.1" }

# Database
exposed-core = { group = "org.jetbrains.exposed", name = "exposed-core", version.ref = "exposed" }
exposed-dao = { group = "org.jetbrains.exposed", name = "exposed-dao", version.ref = "exposed" }
exposed-jdbc = { group = "org.jetbrains.exposed", name = "exposed-jdbc", version.ref = "exposed" }
exposed-kotlin-datetime = { group = "org.jetbrains.exposed", name = "exposed-kotlin-datetime", version.ref = "exposed" }

# Logging
logback-classic = { group = "ch.qos.logback", name = "logback-classic", version.ref = "logback" }

# Testing
ktor-server-tests = { group = "io.ktor", name = "ktor-server-tests-jvm", version.ref = "ktor" }
kotlin-test-junit = { group = "org.jetbrains.kotlin", name = "kotlin-test-junit", version.ref = "kotlin" }
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
junit-jupiter = { group = "org.junit.jupiter", name = "junit-jupiter", version.ref = "junit" }

[bundles]
ktor-server = [
    "ktor-server-core", 
    "ktor-server-netty", 
    "ktor-server-config-yaml",
    "ktor-server-content-negotiation", 
    "ktor-serialization-json",
    "ktor-server-status-pages",
    "ktor-server-call-logging"
]
kotlin-coroutines = ["kotlinx-coroutines-core", "kotlinx-datetime"]
exposed = ["exposed-core", "exposed-dao", "exposed-jdbc", "exposed-kotlin-datetime"]
testing = ["ktor-server-tests", "kotlin-test-junit", "mockk", "junit-jupiter"]

[plugins]
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

### Using Version Catalogs
```kotlin
// In build.gradle.kts
dependencies {
    implementation(libs.bundles.ktor.server)
    implementation(libs.bundles.kotlin.coroutines)
    implementation(libs.bundles.exposed)
    implementation(libs.logback.classic)
    testImplementation(libs.bundles.testing)
}
```

## Code Quality Integration

### Kotlin Linting (ktlint)
```kotlin
// build.gradle.kts
plugins {
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1"
}

ktlint {
    version.set("0.50.0")
    debug.set(true)
    verbose.set(true)
    android.set(false)
    outputToConsole.set(true)
    ignoreFailures.set(false)
    filter {
        exclude("**/generated/**")
    }
}
```

### Static Analysis (Detekt)
```kotlin
// build.gradle.kts
plugins {
    id("io.gitlab.arturbosch.detekt") version "1.23.3"
}

detekt {
    toolVersion = "1.23.3"
    config = files("config/detekt/detekt.yml")
    buildUponDefaultConfig = true
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.3")
}
```

## Alternatives Considered

### Maven
**Pros:**
- Mature and stable build tool
- Extensive documentation and community support
- Standardized project structure
- Good IDE integration

**Cons:**
- XML-based configuration (verbose)
- Less flexible than Gradle
- Slower build performance for large projects
- Limited plugin ecosystem compared to Gradle

### sbt (Simple Build Tool)
**Pros:**
- Powerful incremental compilation
- Excellent for Scala/Kotlin mixed projects
- Advanced dependency management

**Cons:**
- Steeper learning curve
- Scala-focused ecosystem
- Less mainstream adoption for pure Kotlin projects
- Complex configuration for simple projects

### Bazel
**Pros:**
- Excellent performance for large-scale projects
- Advanced caching and distribution
- Language-agnostic build system

**Cons:**
- Complex setup and configuration
- Overkill for single-module projects
- Limited Kotlin-specific tooling
- Steep learning curve

## Multi-Module Support

### Project Structure
```
lamp-control-api-kotlin/
├── build.gradle.kts           # Root build script
├── settings.gradle.kts        # Project settings
├── gradle.properties          # Gradle configuration
├── app/                       # Main application module
│   └── build.gradle.kts
├── domain/                    # Domain logic module
│   └── build.gradle.kts
└── infrastructure/            # Infrastructure module
    └── build.gradle.kts
```

### Root build.gradle.kts
```kotlin
plugins {
    kotlin("jvm") version "1.9.20" apply false
    kotlin("plugin.spring") version "1.9.20" apply false
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    
    repositories {
        mavenCentral()
    }
    
    dependencies {
        testImplementation("org.jetbrains.kotlin:kotlin-test")
    }
}
```

## CI/CD Integration

### GitHub Actions Configuration
```yaml
# .github/workflows/kotlin.yml
name: Kotlin CI
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - uses: actions/setup-java@v3
      with:
        java-version: '21'
        distribution: 'temurin'
    - uses: gradle/gradle-build-action@v2
      with:
        cache-read-only: ${{ github.ref != 'refs/heads/main' }}
    - run: ./gradlew build
    - run: ./gradlew test
    - run: ./gradlew ktlintCheck
    - run: ./gradlew detekt
```

### Docker Integration
```dockerfile
# Multi-stage build
FROM gradle:8.4-jdk21 AS builder
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY src/ src/
RUN gradle build --no-daemon

FROM openjdk:21-jre-slim
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
```

## Performance Optimization

### Build Performance
```properties
# gradle.properties
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=1g
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configureondemand=true
org.gradle.daemon=true

# Kotlin compiler optimizations
kotlin.incremental=true
kotlin.incremental.multiplatform=true
kotlin.parallel.tasks.in.project=true
kotlin.compiler.execution.strategy=in-process
```

### Build Cache Configuration
```kotlin
// settings.gradle.kts
buildCache {
    local {
        directory = File(rootDir, "build-cache")
        removeUnusedEntriesAfterDays = 30
    }
}
```

## Consequences

### Positive
- **Modern Build System**: Fast, flexible, and feature-rich build automation
- **Kotlin Integration**: Excellent native Kotlin support and optimization
- **Performance**: Incremental builds, caching, and parallel execution
- **Ecosystem**: Rich plugin ecosystem and active community
- **Flexibility**: Powerful DSL for custom build logic and tasks
- **IDE Integration**: Excellent support in IntelliJ IDEA and other IDEs

### Negative
- **Learning Curve**: More complex than Maven for simple projects
- **Build Script Complexity**: Powerful DSL can lead to complex build scripts
- **Memory Usage**: Can consume significant memory for large projects

### Neutral
- **Configuration**: Kotlin DSL provides type-safe configuration
- **Debugging**: Build script debugging can be challenging

## Future Considerations

1. **Kotlin Multiplatform**
   - Potential for sharing code across JVM, Android, and Native targets
   - Gradle provides excellent multiplatform support

2. **Composite Builds**
   - Consider composite builds for large multi-repository projects
   - Better dependency management across related projects

3. **Gradle Enterprise**
   - For larger teams, consider Gradle Enterprise for build insights
   - Advanced caching and performance analytics

4. **Native Compilation**
   - Explore GraalVM native image compilation with Gradle
   - Spring Native support for faster startup times

## References

- [Gradle Documentation](https://docs.gradle.org/)
- [Kotlin Gradle Plugin](https://kotlinlang.org/docs/gradle.html)
- [Gradle Kotlin DSL](https://docs.gradle.org/current/userguide/kotlin_dsl.html)
- [Spring Boot Gradle Plugin](https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/htmlsingle/)
- [Gradle Best Practices](https://docs.gradle.org/current/userguide/best_practices.html)
