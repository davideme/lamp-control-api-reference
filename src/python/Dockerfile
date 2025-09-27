FROM python:3.11 AS build-env

WORKDIR /app

RUN pip install poetry

COPY pyproject.toml poetry.lock ./

RUN poetry config virtualenvs.create false && \
    poetry install --no-root --only=main

COPY . .

FROM gcr.io/distroless/python3-debian12:nonroot

WORKDIR /app

# Ensure Python can locate third-party packages installed under /usr/local
ENV PYTHONPATH="/usr/local/lib/python3.11/site-packages"
ENV PATH="/usr/local/bin:${PATH}"

# Copy Python packages from build stage
COPY --from=build-env /usr/local/lib/python3.11/site-packages /usr/local/lib/python3.11/site-packages

# Copy the entire /usr/local/bin for any installed scripts
COPY --from=build-env /usr/local/bin /usr/local/bin

# Copy application code
COPY --from=build-env /app .

# Use Python launcher script to handle PORT env var for Cloud Run compatibility
CMD ["launcher.py"]
