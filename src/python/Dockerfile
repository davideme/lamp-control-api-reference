FROM python:3.12 AS builder

WORKDIR /usr/src/app

# Install Poetry using the official installer
RUN pip install --upgrade pip
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

# Copy source code
COPY . .


FROM python:3.12 AS test_runner
WORKDIR /tmp

# Install Poetry
RUN pip install --upgrade pip
RUN curl -sSL https://install.python-poetry.org | python3 -

# Set PATH and configure Poetry
ENV PATH="/root/.local/bin:$PATH" \
    POETRY_NO_INTERACTION=1 \
    POETRY_VENV_IN_PROJECT=1 \
    POETRY_CACHE_DIR=/tmp/poetry_cache

# Copy project from builder stage
COPY --from=builder /usr/src/app /usr/src/app
WORKDIR /usr/src/app

# Install all dependencies (including dev dependencies for testing)
RUN poetry install && rm -rf $POETRY_CACHE_DIR

# Run tests
RUN poetry run pytest tests


FROM python:3.12 AS service
WORKDIR /root/app

# Copy the virtual environment and source code from test stage
COPY --from=test_runner /usr/src/app/.venv /root/app/.venv
COPY --from=test_runner /usr/src/app /root/app

# Set environment to use the virtual environment
ENV PATH="/root/app/.venv/bin:$PATH"
