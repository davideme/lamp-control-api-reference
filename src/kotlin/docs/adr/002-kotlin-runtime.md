# ADR 002: Select Runtime for Kotlin Project

## Status

Accepted

## Date

2025-05-26

## Context

This project will be implemented in Kotlin. Kotlin supports multiple runtimes, each with specific trade-offs, ecosystem maturity, and target use cases. The primary deployment target is the JVM, but alternatives exist for native, web, and multiplatform scenarios.

## Decision

We will use **OpenJDK 21 (LTS)** as the default runtime for this Kotlin project.

## Rationale

* **Compatibility:** Kotlin’s JVM target is the most mature and widely supported, compatible with the largest set of libraries and tools.
* **LTS Support:** OpenJDK 21 is the current long-term support (LTS) release, with security and performance updates until at least 2029.
* **Ecosystem:** Major Kotlin frameworks (e.g., Spring Boot, Ktor, Android) have adopted Java 21 as the standard baseline.
* **Performance and Security:** Java 21 provides the latest security patches, performance improvements, and advanced language features (records, virtual threads).

## Alternatives Considered

| Runtime              | Description                                  | Use Cases                              | Stability (2025)               | Notes                                      |
| -------------------- | -------------------------------------------- | -------------------------------------- | ------------------------------ | ------------------------------------------ |
| **OpenJDK 21 (LTS)** | JVM-based, default for Kotlin                | Backend, Android, server, MPP          | Very stable, industry standard | Best overall compatibility                 |
| **Kotlin/Native**    | Compiles to native binaries, no JVM required | iOS, macOS, CLI tools, embedded        | Mature for iOS/macOS           | No JVM dependency; native interoperability |
| **Kotlin/JS**        | Compiles to JavaScript                       | Web frontends, shared MPP code         | Stable                         | Works with React/JS ecosystems             |
| **Kotlin/Wasm**      | Compiles to WebAssembly                      | High-performance web, emerging targets | Experimental/Stable for basic  | Evolving rapidly, some production usage    |
| **GraalVM Native**   | Compiles JVM bytecode to native images       | CLI tools, microservices, cloud        | Stable (limitations apply)     | Fast startup, but limited dynamic features |

## Consequences

* **Upgrades:** Regularly monitor and upgrade to newer Java LTS versions as ecosystem adoption progresses.
* **Dependencies:** Ensure all dependencies and CI/CD tooling are compatible with Java 21.
* **Multiplatform:** If targeting iOS, embedded, or pure web clients, consider secondary modules using Kotlin/Native, Kotlin/JS, or Kotlin/Wasm.
* **Native Images:** For fast-startup microservices or CLI tools, evaluate GraalVM Native Image, with the understanding that some JVM features may require additional configuration.

## Alternatives Not Chosen

* **Older JVM versions (Java 17 or earlier):** Phased out of mainstream support and ecosystem adoption.
* **Java non-LTS (Java 22+):** Not recommended for production due to short support cycles.
* **Kotlin/Native, Kotlin/JS, Kotlin/Wasm as primary:** Chosen only if specific, non-JVM targets are required for the project.
