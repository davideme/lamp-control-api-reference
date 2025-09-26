FROM python:3.12 AS builder

WORKDIR /usr/src/app

# Copy pyproject.toml to install the app via pip (using the build-backend)
COPY pyproject.toml poetry.lock ./
COPY . .

# Install the application using pip (which will use poetry-core as build backend)
RUN pip install --trusted-host pypi.org --trusted-host files.pythonhosted.org --no-cache-dir .


FROM python:3.12 AS test_runner
WORKDIR /tmp
COPY --from=builder /usr/local/lib/python3.12/site-packages /usr/local/lib/python3.12/site-packages
COPY --from=builder /usr/local/bin /usr/local/bin
COPY --from=builder /usr/src/app/tests tests

# install test dependencies
RUN pip install --trusted-host pypi.org --trusted-host files.pythonhosted.org pytest pytest-asyncio pytest-cov

# run tests
RUN pytest tests


FROM python:3.12 AS service
WORKDIR /root/app/site-packages
COPY --from=test_runner /usr/local/lib/python3.12/site-packages /usr/local/lib/python3.12/site-packages
COPY --from=test_runner /usr/local/bin /usr/local/bin
