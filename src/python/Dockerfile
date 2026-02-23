FROM python:3.14 AS build-env

WORKDIR /app

RUN pip install poetry

COPY pyproject.toml poetry.lock ./

RUN poetry config virtualenvs.create false && \
    poetry install --no-root --only=main

# Copy installed site-packages to a version-agnostic directory for runtime image.
RUN PY_MM=$(python -c "import sys; print(f'{sys.version_info.major}.{sys.version_info.minor}')") && \
    mkdir -p /opt/python-deps && \
    cp -R "/usr/local/lib/python${PY_MM}/site-packages/." /opt/python-deps

COPY . .

FROM gcr.io/distroless/python3-debian12:nonroot

WORKDIR /app

# Ensure Python can locate third-party packages without hard-coding Python minor version
ENV PYTHONPATH="/opt/python-deps"
ENV PATH="/usr/local/bin:${PATH}"

# Copy Python packages from build stage
COPY --from=build-env /opt/python-deps /opt/python-deps

# Copy the entire /usr/local/bin for any installed scripts
COPY --from=build-env /usr/local/bin /usr/local/bin

# Copy application code
COPY --from=build-env /app .

# Use Python launcher script to handle PORT and WORKERS env vars for Cloud Run compatibility
CMD ["launcher.py"]
