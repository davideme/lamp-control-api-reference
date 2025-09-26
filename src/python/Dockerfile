FROM python:3.12-slim AS build-env
COPY . /app
WORKDIR /app

# Install curl for Poetry installation
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Install Poetry using the official installer
RUN pip install --no-cache-dir --upgrade pip
RUN curl -sSL https://install.python-poetry.org | python3 -

# Set PATH and configure Poetry
ENV PATH="/root/.local/bin:$PATH" \
    POETRY_NO_INTERACTION=1 \
    POETRY_VENV_IN_PROJECT=1 \
    POETRY_CACHE_DIR=/tmp/poetry_cache

# Copy dependency files first for better layer caching
COPY pyproject.toml poetry.lock ./

# Install production dependencies only
RUN poetry install --only=main && rm -rf $POETRY_CACHE_DIR


FROM gcr.io/distroless/python3-debian12
COPY --from=build-env /app /app
WORKDIR /app

# Set environment to use the virtual environment
ENV PATH="/app/.venv/bin:$PATH"

# Use Python launcher script to handle PORT env var for Cloud Run compatibility
CMD ["python", "launcher.py"]
