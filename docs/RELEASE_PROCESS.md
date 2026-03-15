# Release Process

This document describes the release process for the Lamp Control API Reference project.

## Versioning Strategy

This project follows [Semantic Versioning](https://semver.org/):
- **MAJOR** version (e.g., 2.0.0) - Incompatible API changes
- **MINOR** version (e.g., 1.1.0) - New functionality in a backward-compatible manner
- **PATCH** version (e.g., 1.0.1) - Backward-compatible bug fixes

## Branching Strategy

- **`main`** - Active development branch for the next minor/major version
- **`release-X.Y.x`** - Long-lived branches for maintaining released versions with bugfixes

## Release Process

### Creating a New Major or Minor Release

When releasing a new major or minor version (e.g., v1.0.0, v1.1.0, v2.0.0):

1. **Ensure all changes are merged to `main`**
   ```bash
   git checkout main
   git pull origin main
   ```

2. **Update version numbers** in all relevant files:
   - Update package.json, pyproject.toml, pom.xml, etc.
   - Update version references in documentation

3. **Update `CHANGELOG.md`** — major and minor releases require a full changelog entry:

   a. **Promote `[Unreleased]` to the new version**:
      ```markdown
      ## [vX.Y.0] — YYYY-MM-DD
      ```

   b. **Write a release goal** — one paragraph describing the intent of the milestone.
      Answer: *What problem does this release solve? What is the user-visible outcome?*
      Example: "Consolidate to six languages by dropping PHP, add PostgreSQL storage, and
      introduce three operation modes for zero-downtime deployments."

   c. **Add an ADR section** for every Architecture Decision Record introduced or finalised
      in this release cycle. Group by scope: cross-language first, then per language:
      ```markdown
      ### Architecture Decision Records

      #### Cross-language

      | ADR | Title | Decision |
      |-----|-------|----------|
      | [ADR-NNN](docs/adr/NNN-title.md) | Short title | One-sentence decision summary. |

      #### <Language>

      | ADR | Title | Decision |
      |-----|-------|----------|
      | [ADR-NNN](src/<lang>/adr/NNN-title.md) | Short title | One-sentence decision summary. |
      ```
      Only include ADRs whose **status changed to Accepted** during this release cycle.
      ADRs that remain Proposed or were already listed in a previous release are omitted.

   d. **Add a fresh `[Unreleased]` section** above the new versioned block, ready for
      the next cycle:
      ```markdown
      ## [Unreleased]

      ### Features
      ### Bug Fixes
      ```

   e. **Update the comparison links** at the bottom of the file:
      ```markdown
      [Unreleased]: https://github.com/…/compare/vX.Y.0...HEAD
      [vX.Y.0]: https://github.com/…/compare/vX.(Y-1).0...vX.Y.0
      ```

4. **Commit version updates**
   ```bash
   git add .
   git commit -m "chore: bump version to X.Y.0"
   git push origin main
   ```

5. **Create an annotated tag**
   ```bash
   git tag -a vX.Y.0 -m "Release version X.Y.0"
   git push origin vX.Y.0
   ```

6. **Create a release branch for bugfixes**
   ```bash
   git checkout -b release-X.Y.x
   git push origin release-X.Y.x
   ```

7. **Return to main for continued development**
   ```bash
   git checkout main
   ```

8. **(Optional) Bump to next development version on main**
   ```bash
   # Update version files to X.(Y+1).0-dev or similar
   git add .
   git commit -m "chore: start development for vX.(Y+1).0"
   git push origin main
   ```

### Creating a Patch Release (Bugfix)

When releasing a bugfix for an existing version (e.g., v1.0.1):

1. **Switch to the appropriate release branch**
   ```bash
   git checkout release-X.Y.x
   git pull origin release-X.Y.x
   ```

2. **Apply bugfixes**
   - Cherry-pick commits from main: `git cherry-pick <commit-hash>`
   - Or create bugfix commits directly on the release branch

3. **Update `CHANGELOG.md`** — add a patch entry under the relevant `[vX.Y.x]` section
   (or create it if this is the first patch). Patch entries do **not** need a goal
   paragraph or ADR table; a flat list of bug fixes and changes is sufficient:
   ```markdown
   ## [vX.Y.Z] — YYYY-MM-DD

   ### Bug Fixes
   - fix(lang): description (#PR)
   ```
   Then update the comparison link at the bottom of the file.

4. **Update version numbers** for the patch release
   ```bash
   # Update version to X.Y.Z in all relevant files
   git add .
   git commit -m "chore: bump version to X.Y.Z"
   ```

5. **Create an annotated tag**
   ```bash
   git tag -a vX.Y.Z -m "Release version X.Y.Z"
   git push origin vX.Y.Z
   git push origin release-X.Y.x
   ```

6. **Merge bugfixes back to main** (if applicable)
   ```bash
   git checkout main
   git merge release-X.Y.x
   # Or cherry-pick specific commits
   git push origin main
   ```

## Example: Version 1.0.0 Release

The following commands were used to create the initial 1.0.0 release:

```bash
# Create version 1.0.0 tag
git tag -a v1.0.0 -m "Release version 1.0.0"

# Create release branch for 1.0.x bugfixes
git checkout -b release-1.0.x
git push origin release-1.0.x

# Return to main for v1.1+ development
git checkout main
```

## Publishing Releases

After creating a tag, additional steps may include:

1. **GitHub Release**: Create a release on GitHub from the tag with release notes
2. **Docker Images**: Build and push Docker images for each language implementation
3. **Package Registries**: Publish packages to npm, PyPI, Maven Central, etc. (if applicable)
4. **Documentation**: Deploy updated documentation for the release

## Hotfix Workflow

For critical bugs requiring immediate release:

1. Create a hotfix branch from the release branch
2. Fix the issue
3. Follow the patch release process above
4. Ensure the fix is merged back to main

## Version History

- **v1.0.0** (2026-01-01) - Initial release
  - Branch: `release-1.0.x` for bugfixes
  - Main branch continues with v1.1+ development
