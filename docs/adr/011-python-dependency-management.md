# ADR 011: Python Dependency Management Tool Selection 

## Status

Accepted

## Context

For the Python implementation of the Lamp Control API, we need to choose a dependency management tool that will handle:
- Virtual environment management
- Package installation and version locking
- Development vs production dependencies
- Reproducible builds
- Easy integration with CI/CD pipelines

Current popular options include:
1. pip + venv + requirements.txt
2. Poetry
3. Pipenv
4. PDM (Python Development Master)
5. Hatch

## Decision

We will use **Poetry** as our dependency management tool for this project.

## Rationale

### Why Poetry?

1. **Modern Package Management**
   - Deterministic builds through poetry.lock file
   - Semantic versioning support
   - Handles dependencies and sub-dependencies effectively
   - Built-in virtual environment management

2. **Project Structure**
   - Uses standardized `pyproject.toml` (PEP 517/518)
   - Clear separation of dev and production dependencies
   - Built-in build system
   - Package publishing capabilities

3. **Developer Experience**
   - Intuitive CLI commands
   - Fast dependency resolution
   - Excellent documentation
   - Active community and maintenance
   - Growing industry adoption

4. **Integration Support**
   - Works well with modern CI/CD pipelines
   - Good IDE integration (VS Code, PyCharm)
   - Compatible with pre-commit hooks
   - Supports multiple Python versions

### Comparison with Alternatives

#### pip + venv + requirements.txt
- ✗ Manual virtual environment management
- ✗ No built-in dependency resolution
- ✗ Requires multiple files for dev/prod dependencies
- ✓ Universal support
- ✓ Simple to understand

#### Pipenv
- ✗ Slower dependency resolution
- ✗ Less active development
- ✗ Some historical stability issues
- ✓ Similar feature set to Poetry
- ✓ Pipfile is human-readable

#### PDM
- ✓ Modern PEP 582 support
- ✓ Fast dependency resolution
- ✗ Smaller community
- ✗ Less mature ecosystem
- ✗ Fewer integrations available

#### Hatch
- ✓ All-in-one project management
- ✓ Built-in testing/publishing
- ✗ Newer tool with smaller adoption
- ✗ Learning curve for additional features
- ✗ May be overkill for our needs

## Implementation Plan

1. Remove existing pip/venv setup from documentation
2. Initialize project with Poetry:
   ```bash
   poetry init
   ```

3. Create `pyproject.toml` with:
   ```toml
   [tool.poetry]
   name = "lamp-control-api"
   version = "0.1.0"
   description = "Lamp Control API implementation in Python"
   authors = ["Your Name <your.email@example.com>"]

   [tool.poetry.dependencies]
   python = "^3.12.9"
   fastapi = "^0.109.0"
   uvicorn = "^0.27.0"
   sqlalchemy = "^2.0.25"
   strawberry-graphql = "^0.219.0"
   grpcio = "^1.60.0"
   structlog = "^24.1.0"
   pydantic = "^2.6.0"

   [tool.poetry.group.dev.dependencies]
   pytest = "^8.0.0"
   black = "^24.1.0"
   ruff = "^0.2.0"
   mypy = "^1.8.0"
   pre-commit = "^3.6.0"
   ```

4. Update CI/CD pipelines to use Poetry
5. Update documentation with new setup instructions

## Consequences

### Positive
- More reliable dependency management
- Better developer experience
- Standardized project structure
- Easier CI/CD integration
- Future-proof tooling choice

### Negative
- Learning curve for developers new to Poetry
- Slightly more complex than basic pip+requirements.txt
- Need to update existing documentation and scripts

### Neutral
- Need to maintain `pyproject.toml` and `poetry.lock` files
- Different workflow from traditional pip-based projects

## References

- [Poetry Documentation](https://python-poetry.org/)
- [PEP 517 -- Build System Support](https://www.python.org/dev/peps/pep-0517/)
- [PEP 518 -- Build System Requirements](https://www.python.org/dev/peps/pep-0518/) 