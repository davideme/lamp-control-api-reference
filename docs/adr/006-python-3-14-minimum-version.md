# ADR 006: Python 3.14 as Minimum Version

## Status

Accepted

## Context

The Python implementation previously declared `requires-python = ">=3.13"` in `pyproject.toml` but the CI pipeline was updated (see branch `python-314`) to test exclusively against Python 3.14. The tooling configuration for mypy already targeted `python_version = "3.14"`, while Black and Ruff were still set to `py312`, creating an inconsistency: the type checker understood 3.14 syntax, but the formatter and linter would flag valid 3.14 constructs or miss newly deprecated patterns.

Python 3.14 became the stable release in the project's target environment, and all CI runners were updated accordingly.

## Decision

Raise the minimum required Python version to **3.14** across all configuration:

- `pyproject.toml`: `requires-python = ">=3.14"` and `python = "^3.14"` (already in place)
- `[tool.mypy]`: `python_version = "3.14"` (already in place)
- `[tool.black]`: `target-version = ["py314"]`
- `[tool.ruff]`: `target-version = "py314"`
- `src/python/README.md`: prerequisites updated to reflect `Python >= 3.14`

## Consequences

- Contributors must use Python 3.14 or later; older interpreters are no longer supported for this implementation.
- Black, Ruff, and mypy all target the same language version, eliminating false positives/negatives caused by version skew between tools.
- Any Python 3.14-specific syntax (e.g., new type-parameter syntax, `__future__` annotations behaviour changes) can be used without workarounds.
- Developers still on 3.12/3.13 must upgrade their local environment or use the provided Docker/devcontainer setup.
