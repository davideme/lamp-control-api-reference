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

Start the development server with hot reloading:
```bash
npm run dev
```

The server will be available at `http://localhost:3000`.

## Testing

Run the test suite:
```bash
npm test
```

Run tests with coverage:
```bash
npm run test:coverage
```

## API Documentation

The API documentation is available at `http://localhost:3000/api-docs` when the server is running.

### Endpoints

- `GET /api/lamps` - List all lamps
- `POST /api/lamps` - Create a new lamp
- `GET /api/lamps/:id` - Get a lamp by ID
- `PATCH /api/lamps/:id` - Update a lamp
- `DELETE /api/lamps/:id` - Delete a lamp
- `POST /api/lamps/:id/toggle` - Toggle a lamp on/off
