# ADR 001: Selection of Go Version for New Project

## Status

Accepted

## Date

2025-05-26

## Context

We are starting a new Go project and need to choose the appropriate Go version. The selected version should be stable, actively supported, and compatible with modern tooling and libraries.

## Decision

We will use **Go 1.24.3** as the required version for development and deployment.

## Alternatives Considered

* **Go 1.23.x:**

  * **Pros:** Previously stable, mature, and still supported for critical bug and security fixes.
  * **Cons:** Does not include the latest features, improvements, and performance enhancements introduced in 1.24.x. The community and most libraries quickly adopt the newest stable release, reducing the advantages of staying on 1.23.x.

* **Go 1.22.x and earlier:**

  * **Pros:** May be required for rare cases where a critical dependency has not been updated for newer Go versions.
  * **Cons:** Misses significant new features, security patches, and optimizations. Lower priority for community support. Not recommended unless strictly necessary.

* **Go Development (Unstable/Nightly) Builds:**

  * **Pros:** Early access to upcoming features.
  * **Cons:** Not intended for production use. APIs may change, stability is not guaranteed, and there is no official support.

## Rationale

Go 1.24.3 is the latest stable version, offers the strongest security posture, and provides access to important language improvements such as generic type aliases and enhanced WebAssembly support. It is widely supported by the ecosystem and is the recommended baseline for new projects.

### Consequences

* Developers and CI/CD infrastructure must standardize on Go 1.24.3.
* The project will periodically evaluate new Go releases for future upgrades.
