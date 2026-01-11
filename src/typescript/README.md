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
- **Database**:
  - In-memory (default, for development and testing)
  - PostgreSQL with Prisma ORM (optional, for production)
- **Testing**: Jest with Supertest, Testcontainers for integration tests

## Prerequisites

- Node.js >= 22
- npm >= 10
- Docker (optional, for PostgreSQL or integration tests)

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

3. (Optional) Set up environment variables:
   ```bash
   cp .env.example .env
   # Edit .env to configure database connection
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

## Database Configuration

The application supports two storage backends:

### In-Memory Storage (Default)

By default, the application uses an in-memory repository. This is suitable for development, testing, and demo purposes. No configuration required.

```bash
npm run dev
# or
USE_POSTGRES=false npm run dev
```

### PostgreSQL with Prisma ORM

For production deployments or when you need persistent storage:

1. **Start PostgreSQL using Docker Compose:**
   ```bash
   docker-compose up -d
   ```

2. **Run Prisma migrations:**
   ```bash
   npx prisma migrate dev
   # or for production
   npx prisma migrate deploy
   ```

3. **Generate Prisma Client:**
   ```bash
   npx prisma generate
   ```

4. **Run the application with PostgreSQL:**
   ```bash
   USE_POSTGRES=true npm run dev
   ```

5. **(Optional) Open Prisma Studio to view/edit data:**
   ```bash
   npx prisma studio
   # Opens at http://localhost:5555
   ```

### Environment Variables

Configure these in your `.env` file:

```bash
# Database
DATABASE_URL="postgresql://lampuser:lamppass@localhost:5432/lampcontrol?schema=public"

# Application
PORT=8080
NODE_ENV=development

# Storage Backend Selection
USE_POSTGRES=true  # Set to 'true' to use PostgreSQL, 'false' or unset for in-memory
```

### Prisma Commands

- `npm run prisma:generate` - Generate Prisma Client
- `npm run prisma:migrate` - Create and apply a new migration
- `npm run prisma:deploy` - Apply migrations in production
- `npm run prisma:studio` - Open Prisma Studio GUI

For more details, see [ADR 007: PostgreSQL Storage with Prisma](docs/adr/007-postgresql-storage.md).

## Testing

Run the test suite (unit tests with in-memory storage):
```bash
npm test
```

Run tests with coverage:
```bash
npm run test:coverage
```

Run integration tests with Testcontainers (requires Docker):
```bash
npm run test:integration
```

**Note:** Integration tests automatically spin up a PostgreSQL container using Testcontainers, run migrations, execute tests, and tear down the container. Docker must be running for integration tests to work.

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
