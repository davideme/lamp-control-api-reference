# Lamp Control API

A comprehensive API for controlling smart lamps, built with TypeScript and implementing multiple interface protocols (REST, GraphQL, gRPC).

## Features

- Multiple API interfaces:
  - RESTful API with Express and OpenAPI 3.0
  - GraphQL API with Apollo Server
  - gRPC API with Protocol Buffers
- CRUD operations for managing lamps
- Toggle lamp on/off functionality
- Input validation using Zod
- OpenAPI/Swagger documentation
- Rate limiting and request throttling
- Performance monitoring with Prometheus metrics
- Structured logging with Winston
- Multiple database support (MySQL, PostgreSQL, MongoDB)
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
│   ├── routes/       # API routes
│   ├── middleware/   # Express middleware
│   └── repositories/ # Repository implementations
└── utils/           # Shared utilities
```

## Technology Stack

- **Language**: TypeScript 5.x
- **API Frameworks**:
  - REST: Express.js with OpenAPI 3.0
  - GraphQL: Apollo Server
  - gRPC: gRPC-js with Protocol Buffers
- **Database ORMs/ODMs**:
  - Relational (MySQL/PostgreSQL): Prisma ORM
  - NoSQL (MongoDB): Mongoose ODM
- **Validation**: Zod
- **Logging**: Winston
- **Metrics**: Prometheus
- **Security**: Helmet, Express Rate Limit
- **Testing**: Jest with Supertest

## Prerequisites

- Node.js >= 18
- npm >= 9

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

3. Set up database:
   ```bash
   npx prisma generate  # Generate Prisma client
   ```

## Database Configuration

The application supports multiple database types. Configuration can be set in environment variables or in `src/config/database.ts`.

### Supported Databases

#### PostgreSQL
```env
DATABASE_TYPE=postgres
DATABASE_URL=postgresql://user:password@localhost:5432/lamp_control
```

#### MySQL
```env
DATABASE_TYPE=mysql
DATABASE_URL=mysql://user:password@localhost:3306/lamp_control
```

#### MongoDB
```env
DATABASE_TYPE=mongodb
DATABASE_URL=mongodb://user:password@localhost:27017/lamp_control
```

### Database Migrations

For relational databases (PostgreSQL, MySQL), you can use Prisma migrations:

```bash
# Create a migration
npx prisma migrate dev --name init

# Apply migrations
npx prisma migrate deploy
```

### Database Schema

The database schema is defined in `prisma/schema.prisma` for relational databases and `src/infrastructure/repositories/MongoDBLampRepository.ts` for MongoDB.

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

## Monitoring

### Metrics

Prometheus metrics are exposed at `/metrics`. Available metrics include:
- HTTP request duration
- Request counts by endpoint
- Response status codes

### Logging

The application uses structured logging with the following levels:
- ERROR: For errors that require immediate attention
- WARN: For potentially harmful situations
- INFO: For general operational events
- DEBUG: For detailed debugging information

## Security

The API implements several security measures:
- Helmet.js for secure HTTP headers
- CORS protection
- Rate limiting (100 requests per 15 minutes per IP)
- Input validation for all endpoints

## Implementation Details

### API Interfaces

#### REST API
- Uses Express.js with versioned routes (`/api/v1/lamps`)
- Implements OpenAPI 3.0 specifications with Swagger UI
- Endpoints follow RESTful principles with proper HTTP methods and status codes
- Input validation with Zod schemas

#### GraphQL API
- Uses Apollo Server integrated with Express
- Provides a single endpoint (`/graphql`) for all lamp operations
- Supports queries (get lamps) and mutations (create/update/delete/toggle lamps)
- Implements proper error handling with GraphQL error types

#### gRPC API
- Implements Protocol Buffers defined in `docs/api/lamp.proto`
- Supports bidirectional streaming for real-time lamp status updates
- Includes strongly typed request/response models
- Auto-generated TypeScript interfaces from .proto files using ts-proto

### Database Implementations

- **Prisma ORM**: Used for SQL databases (PostgreSQL, MySQL)
  - Type-safe database client
  - Schema-driven development with migrations
  - Automatic query building

- **Mongoose ODM**: Used for MongoDB
  - Schema validation
  - Middleware support
  - Rich querying capabilities

### Code Metrics

| Component           | Lines of Code | Files |
|---------------------|---------------|-------|
| Domain              | 248           | 8     |
| Infrastructure      | 892           | 27    |
| Utils               | 102           | 4     |
| Tests               | 475           | 15    |
| **Total**           | **1,717**     | **54**|

Test coverage: 86.4%

## Contributing

1. Create a feature branch:
   ```bash
   git checkout -b feature-name
   ```

2. Make your changes following the project guidelines

3. Run tests and ensure they pass:
   ```bash
   npm test
   ```

4. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
