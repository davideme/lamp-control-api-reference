# Lamp Control API

A RESTful API for controlling smart lamps, built with TypeScript and Express.

## Features

- CRUD operations for managing lamps
- Toggle lamp on/off functionality
- Input validation using Zod
- OpenAPI/Swagger documentation
- Rate limiting
- Performance monitoring with Prometheus metrics
- Structured logging with Winston
- Integration tests with Jest and Supertest

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
