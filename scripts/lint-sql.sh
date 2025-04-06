#!/bin/bash
set -e

# Build the SQL linting image if needed
docker-compose build sql-lint

# Run the linter with the specified command (default: lint)
docker-compose run --rm sql-lint ${1:-lint} 