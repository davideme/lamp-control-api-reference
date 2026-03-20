# ADR 006: C# / .NET package management and lockfile policy

## Status

Accepted

## Context

The C# portion of this repository is an application (LampControlApi) that restores NuGet packages during CI and local workflows. Reproducible restores and vulnerability scanning are important for reliable CI and developer experience. The repository already uses `dotnet restore --locked-mode` in some workflows which requires a lockfile to be present and up-to-date.

NuGet supports a lockfile (`packages.lock.json`) which captures resolved transitive package versions. For applications, committing the lockfile improves determinism. For libraries, lockfiles are optional and often omitted to avoid locking downstream consumers.

## Decision

We will adopt the following C#/.NET package management rules for this repository (application projects):

- Commit `packages.lock.json` for application projects that use PackageReference and generate a lockfile.
- Enforce lockfile usage in CI: use `dotnet restore --locked-mode` so the build fails if the lockfile is missing or out of date.
- Regenerate lockfiles only via the package manager (`dotnet restore --use-lock-file` or project-based `RestorePackagesWithLockFile` setting) and submit dependency updates via dedicated PRs with tests and review.
- Do not edit `packages.lock.json` by hand.

For library projects in this mono-repo, prefer NOT committing lockfiles unless an application within the same repository depends on the library and determinism is required. If a library does commit a lockfile, document the reason in the PR.

## Implementation details

- Enabling lockfile generation (recommended for this repo's app projects):
  - Option A (project file): add the following property to the project that should produce a lockfile:

    <PropertyGroup>
      <RestorePackagesWithLockFile>true</RestorePackagesWithLockFile>
    </PropertyGroup>

    After this, run `dotnet restore --use-lock-file` once to produce `packages.lock.json` and commit it.

  - Option B (explicit restore): run `dotnet restore --use-lock-file` in the project directory. This also creates `packages.lock.json` when the project opts-in via the property above.

- CI enforcement:
  - Use `dotnet restore --locked-mode` (already used in some workflows) to ensure the lockfile is authoritative; the command fails if `packages.lock.json` must be updated.
  - Example CI snippet (shell):

    dotnet restore --locked-mode LampControlApi/LampControlApi.csproj

  - Fail the build if restore returns a non-zero exit code and present guidance to the contributor on how to regenerate the lockfile.

- Updating dependencies:
  - Create a dependency PR which updates the package reference(s) and regenerates `packages.lock.json` using `dotnet restore --use-lock-file` (or by changing the project property and running restore).
  - Include test runs and a short rationale for the update. If the update fixes a security issue, call it out in the PR description.

## Rationale

- Deterministic restores in CI and local dev.
- Reliable vulnerability scanning and SBOM generation.
- Prevents accidental transitive upgrades from sneaking into CI.

## Alternatives considered

- Do not commit `packages.lock.json`: simpler but leads to non-deterministic restores and possible CI drift.
- Commit lockfiles for libraries: gives determinism but risks locking downstream consumers—rejected for libraries unless justified.

## Consequences

- Positive: reproducible builds, clearer dependency updates, safer CVE remediation.
- Negative: extra files to review on dependency updates, small contributor friction when regenerating locks.

## Notes

- Add a short section to `src/csharp/README.md` or `CONTRIBUTING.md` with the exact commands to regenerate `packages.lock.json` for contributors (example commands above).
- Consider enabling Dependabot/Renovate for NuGet and require reviewers to include test runs on dependency PRs.
