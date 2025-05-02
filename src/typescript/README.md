# Lamp Control API

A TypeScript implementation of a RESTful API for controlling lamps, demonstrating CRUD operations and following best practices.

## Features

- RESTful API endpoints for lamp management
- OpenAPI 3.0 specification
- TypeScript implementation with strong typing
- In-memory repository for data storage
- Comprehensive test coverage
- Fastify web framework
- Jest and Supertest for testing

## API Endpoints

### List Lamps
- **GET** `/v1/lamps`
- Returns a list of all lamps
- Optional query parameter: `limit` (number)

### Get Lamp
- **GET** `/v1/lamps/{lampId}`
- Returns details of a specific lamp
- Returns 404 if lamp not found

### Create Lamp
- **POST** `/v1/lamps`
- Creates a new lamp
- Request body: `{ "status": boolean }`
- Returns 201 with created lamp details

### Update Lamp
- **PUT** `/v1/lamps/{lampId}`
- Updates a lamp's status
- Request body: `{ "status": boolean }`
- Returns 200 with updated lamp details
- Returns 404 if lamp not found

### Delete Lamp
- **DELETE** `/v1/lamps/{lampId}`
- Deletes a lamp
- Returns 204 on success
- Returns 404 if lamp not found

## Prerequisites

- Node.js 22 or later
- npm 9 or later

## Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/lamp-control-api.git
cd lamp-control-api
```

2. Install dependencies:
```bash
npm install
```

3. Generate TypeScript types from OpenAPI specification:
```bash
npm run generate-types
```

## Development

1. Start the development server:
```bash
npm run dev
```

The server will start on port 8080.

## Testing

Run the test suite:
```bash
npm test
```

Run tests in watch mode:
```bash
npm run test:watch
```

Generate coverage report:
```bash
npm run test:coverage
```

## Project Structure

```
.
├── src/
│   ├── app.ts           # Fastify application setup
│   ├── index.ts         # Application entry point
│   ├── repository.ts    # Data access layer
│   ├── service.ts       # Business logic
│   ├── security.ts      # Security handlers
│   └── types/           # TypeScript types
├── test/
│   └── api.test.ts      # API endpoint tests
├── docs/
│   └── api/
│       └── openapi.yaml # OpenAPI specification
├── jest.config.js       # Jest configuration
├── tsconfig.json        # TypeScript configuration
└── package.json         # Project configuration
```

## TypeScript Configuration

The project uses TypeScript with the following key configurations:
- ES modules support
- Strict type checking
- Node.js 22 configuration
- Path aliases for cleaner imports

## Testing Strategy

- Unit tests for repository and service layers
- Integration tests for API endpoints
- Test coverage threshold of 80%
- Jest and Supertest for testing
- Mocked repository for isolated testing

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'feat: add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Fastify for the web framework
- OpenAPI for API specification
- Jest and Supertest for testing
- TypeScript for type safety 