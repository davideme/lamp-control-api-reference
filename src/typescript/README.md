# Lamp Control API

A comprehensive API for controlling lamps, built with TypeScript.

## Features

- Multiple API interfaces:
  - RESTful API with Express and OpenAPI 3.0
  [ ] GraphQL API with Apollo Server
  [ ] gRPC API with Protocol Buffers
- CRUD operations for managing lamps
- Toggle lamp on/off functionality
- OpenAPI/Swagger documentation
[ ] Rate limiting and request throttling
[ ] Performance monitoring with Prometheus metrics
[ ] Structured logging with Winston
[ ] Multiple database support (MySQL, PostgreSQL, MongoDB)
- Comprehensive testing suite with >80% coverage

## Architecture

The project follows a clean architecture pattern with the following layers:

```
src/
├── domain/           # Business logic and domain models
│   ├── models/       # Domain entities
│   ├── services/     # Business logic services
│   ├── repositories/ # Repository interfaces
│   └── errors/       # Domain-specific errors
├── infrastructure/   # External interfaces and implementations
│   ├── services/     # API routes
│   └── repositories/ # Repository implementations
└── utils/           # Shared utilities
```

## Technology Stack

- **Language**: TypeScript 5.x
- **API Frameworks**:
  - REST: Fastify with OpenAPI 3.0
- **Testing**: Jest with Supertest

## Prerequisites

- Node.js >= 22
- npm >= 10

## Installation

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd lamp-control-api/src/typescript
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

## Development

### Traditional TypeScript Development
Start the development server with hot reloading using tsx:
```bash
npm run dev
```

### Node.js Type Stripping (Node.js 22.6.0+)
Run TypeScript files directly with Node.js native type stripping:
```bash
npm run dev:native
```

The server will be available at `http://localhost:8080`.

### Type Stripping Benefits
- **No compilation step**: Run TypeScript files directly
- **Faster startup**: Eliminates build overhead
- **Native Node.js support**: Leverages built-in TypeScript processing
- **Simplified debugging**: Direct execution without source maps

For more details, see [ADR 007: Node.js Type Stripping Compatibility](docs/adr/007-nodejs-type-stripping-compatibility.md).

## Testing

Run the test suite:
```bash
npm test
```

Run tests with coverage:
```bash
npm run test:coverage
```

## Production

### Traditional Build & Deploy
Build and run the compiled JavaScript:
```bash
npm run build
npm start
```

### Native Type Stripping (Node.js 22.6.0+)
Run TypeScript directly in production:
```bash
npm run start:native
```

This eliminates the build step entirely while maintaining production performance.

### Docker Deployment

Build and run with Docker using the multi-stage Dockerfile:

```bash
# Build the Docker image
docker build -t lamp-control-api-ts .

# Run the container
docker run --rm -p 8080:8080 lamp-control-api-ts
```

The Docker configuration uses:
- **Multi-stage build**: Optimized for production with separate build and runtime stages
- **Node.js 22**: Build stage for compilation and dependency installation
- **Distroless runtime**: Minimal, secure production runtime environment
- **Optimized layers**: Efficient Docker layer caching for faster builds

The container exposes the API on port 8080 and includes all necessary dependencies and the OpenAPI specification.

## API Documentation

The API documentation is available at `http://localhost:8080/api-docs` when the server is running.

### Endpoints

**Health Check:**
- `GET /health` - Health check endpoint returning service status

**Lamp Control API (v1):**
- `GET /v1/lamps` - List all lamps
- `POST /v1/lamps` - Create a new lamp
- `GET /v1/lamps/:id` - Get a lamp by ID
- `PUT /v1/lamps/:id` - Update a lamp
- `DELETE /v1/lamps/:id` - Delete a lamp
