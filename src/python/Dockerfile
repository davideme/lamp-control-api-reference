FROM python:3.12-slim AS build-env

WORKDIR /app
COPY . /app

# Install dependencies using pip for reliable container builds
# Note: In production Cloud Run environment, SSL certificates work correctly
RUN pip install --no-cache-dir --upgrade pip
RUN pip install --no-cache-dir fastapi[standard] uvicorn

FROM python:3.12-slim

WORKDIR /app
COPY --from=build-env /app /app
COPY --from=build-env /usr/local/lib/python3.12/site-packages /usr/local/lib/python3.12/site-packages
COPY --from=build-env /usr/local/bin /usr/local/bin

# Use Python launcher script to handle PORT env var for Cloud Run compatibility
CMD ["python", "launcher.py"]
