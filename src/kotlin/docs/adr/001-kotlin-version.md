# ADR 001: Select Kotlin Version for New Project

## Status

Accepted

## Date

2025-05-26

## Context

Kotlin is the primary language for this project, targeting the JVM with potential multiplatform capabilities. The Kotlin language and ecosystem evolve rapidly, with regular releases but **no official LTS (Long-Term Support) versions**. As of May 2025, JetBrains supports only the latest stable release, which receives ongoing feature updates, bug fixes, and security patches.

## Decision

We will use **Kotlin 2.1.21** (the latest stable version as of May 2025) for all new development.

## Rationale

* **Up-to-date Features**: Kotlin 2.1.x offers the latest language improvements, multiplatform enhancements, and performance optimizations.
* **Ecosystem Support**: Most Kotlin-compatible frameworks (including Android, Ktor, and Spring) support Kotlin 2.1.x.
* **Bug Fixes & Security**: Only the most recent release receives active bug fixes and security updates from JetBrains.
* **No LTS Policy**: There is no LTS release for Kotlin; staying current with the latest version is industry best practice.
* **Community Standard**: The Kotlin community and most major organizations recommend adopting the latest stable release to avoid compatibility and support issues.

## Consequences

* **Maintenance**: We must regularly monitor and upgrade to new Kotlin versions to benefit from fixes and improvements.
* **Compatibility**: All project dependencies and CI tooling must be compatible with Kotlin 2.1.x. Coordination with the team is necessary to ensure alignment.
* **No Backwards Guarantees**: Upgrades between minor versions may occasionally require code adjustments due to evolving language features and deprecations.

## Alternatives Considered

* **Using an older version (e.g., 2.0.x or 1.9.x):**
  Rejected, as these versions do not receive updates and may introduce compatibility or security risks.
* **Waiting for an LTS release:**
  Not applicable, as JetBrains does not provide LTS support for Kotlin.
