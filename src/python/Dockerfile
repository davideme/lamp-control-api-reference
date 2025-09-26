FROM python:3.12-slim AS build-env
COPY . /app
WORKDIR /app

FROM python:3.12-slim
COPY --from=build-env /app /app
WORKDIR /app

# Install dependencies
RUN pip install --no-cache-dir --upgrade pip && \
    pip install --no-cache-dir "fastapi[standard]>=0.115.12"

# Use FastAPI run command with PORT env var support for Cloud Run
CMD ["sh", "-c", "fastapi run src/openapi_server/main.py --port ${PORT:-80} --host 0.0.0.0"]
