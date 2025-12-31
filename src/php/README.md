# PHP Implementation

This directory contains the PHP implementation of the Lamp Control API using Slim 4 framework.

## Features

- REST API with OpenAPI 3.0 specification
- PHP 8.1+ with Slim 4 framework
- PSR-7 HTTP message interface
- PHP-DI dependency injection container
- Database support for MySQL, PostgreSQL, and MongoDB

## Getting Started

### Local Development

```bash
cd lamp-control-api
composer install
composer start
```

The API will be available at `http://localhost:8080`

### Deployment to Google Cloud Run with Buildpacks

The PHP application is configured to deploy to Google Cloud Run using buildpacks. The necessary files in `lamp-control-api/`:

- `Procfile`: Specifies how to start the web server with nginx
- `nginx.conf`: Nginx configuration for routing
- `composer.json`: Project dependencies and configuration
- `.gcloudignore`: Files to exclude from deployment

Deploy with Cloud Build (from repo root):

```bash
# Cloud Build will use --path=src/php/lamp-control-api
gcloud builds submit
```

Or deploy directly:

```bash
# From the lamp-control-api directory
cd lamp-control-api
gcloud run deploy php-lamp-control-api \
  --source . \
  --region europe-west1 \
  --allow-unauthenticated
```

The buildpack will:
1. Detect the PHP project via composer.json
2. Install dependencies with `composer install --no-dev`
3. Configure nginx with the custom config
4. Serve the application from public/

## Directory Structure

```
php/
├── src/            # Source code
├── tests/          # Test files
├── config/         # Configuration files
└── README.md       # This file
```

## Implementation Details

*Coming soon*

## Testing

*Coming soon*
