FROM python:3.12-slim AS build-env

WORKDIR /app
COPY . /app

# Install dependencies directly using pip 
# Skip SSL certificate verification for this build environment
RUN pip install --no-cache-dir --upgrade pip --trusted-host pypi.org --trusted-host pypi.python.org --trusted-host files.pythonhosted.org
RUN pip install --no-cache-dir fastapi[standard] uvicorn --trusted-host pypi.org --trusted-host pypi.python.org --trusted-host files.pythonhosted.org

FROM python:3.12-slim

WORKDIR /app
COPY --from=build-env /app /app
COPY --from=build-env /usr/local/lib/python3.12/site-packages /usr/local/lib/python3.12/site-packages
COPY --from=build-env /usr/local/bin /usr/local/bin

# Use Python launcher script to handle PORT env var for Cloud Run compatibility
CMD ["python", "launcher.py"]
