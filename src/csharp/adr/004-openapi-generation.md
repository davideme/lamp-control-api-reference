# ADR 004: Use NSwag in Debian Container for OpenAPI Stub Generation

## Status

Accepted

## Context

We need to generate ASP.NET Core controller stubs from OpenAPI definitions as part of our development workflow. The code generation should be reproducible, platform-agnostic, and suitable for CI/CD pipelines. Our build and automation environment is containerized and based on Debian Linux.

## Decision

We will use **NSwag.ConsoleCore** as a global .NET CLI tool inside a Debian-based container to generate server-side controller stubs for ASP.NET Core projects.

* **NSwag** is well-supported, actively maintained, and widely adopted in the .NET ecosystem.
* The tool is cross-platform and works on Linux (including Debian) via .NET SDK.
* Installing NSwag as a global .NET CLI tool in the container ensures reproducibility and simplifies CI integration.
* The workflow allows us to mount our OpenAPI definitions and retrieve generated code via Docker volumes.

**Example Dockerfile:**

```dockerfile
FROM mcr.microsoft.com/dotnet/sdk:8.0

RUN dotnet tool install --global NSwag.ConsoleCore
ENV PATH="${PATH}:/root/.dotnet/tools"
WORKDIR /src
```

**Example usage:**

```sh
docker run --rm -v $(pwd):/src nswag-demo \
  nswag openapi2cscontroller /input:/src/openapi.yaml /output:/src/Controllers.cs
```

## Consequences

* **Benefits:**

  * Platform-agnostic and CI-friendly code generation process.
  * Consistency across development and build environments.
  * Easy to upgrade or modify the code generation toolchain via container updates.
* **Drawbacks:**

  * Requires .NET SDK in the container, slightly increasing image size.
  * Developers unfamiliar with NSwag or .NET CLI tools may require initial onboarding.

## Alternatives Considered

* **OpenAPI Generator**: Also supports ASP.NET Core but is Java-based and less tightly integrated with the .NET ecosystem.
* **Manual stub writing**: Not maintainable for large or frequently changing APIs.
* **Swashbuckle**: Generates documentation from code, not code from OpenAPI definitions.

## Decision Outcome

Adopt **NSwag.ConsoleCore** in a Debian-based container as the standard tool for OpenAPI-driven ASP.NET Core stub generation.
