// filepath: /workspaces/lamp-control-api-reference/src/csharp/LampControlApi/README.md
# Lamp Control API - C# Implementation

This is a C# ASP.NET Core Web API implementation of the Lamp Control API using an in-memory repository.

## Features

- **Full CRUD Operations**: Create, Read, Update, and Delete lamps
- **In-Memory Storage**: Uses `ConcurrentDictionary` for thread-safe operations
- **Error Handling**: Proper HTTP status codes and error responses
- **OpenAPI/Swagger**: Automatically generated API documentation
- **Dependency Injection**: Clean separation of concerns with proper DI

## Architecture

### Components

1. **Controllers/Controllers.cs**: Auto-generated controller interface and implementation
2. **Services/ILampRepository.cs**: Repository interface for data operations
3. **Services/InMemoryLampRepository.cs**: In-memory implementation with seeded data
4. **Services/LampControllerImplementation.cs**: Business logic implementation
5. **Middleware/ExceptionHandlingMiddleware.cs**: Global exception handling

### Data Models

- **Lamp**: Main entity with ID (Guid) and Status (bool)
- **LampCreate**: DTO for creating new lamps
- **LampUpdate**: DTO for updating existing lamps

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/v1/lamps` | List all lamps |
| POST | `/v1/lamps` | Create a new lamp |
| GET | `/v1/lamps/{lampId}` | Get a specific lamp |
| PUT | `/v1/lamps/{lampId}` | Update a lamp's status |
| DELETE | `/v1/lamps/{lampId}` | Delete a lamp |

## Running the Application

```bash
dotnet build
dotnet run --urls "http://localhost:5170"
```

Access Swagger UI at: `http://localhost:5170/swagger`

## Sample Data

The application is seeded with three lamps:
- `123e4567-e89b-12d3-a456-426614174000` (ON)
- `123e4567-e89b-12d3-a456-426614174001` (OFF)
- `123e4567-e89b-12d3-a456-426614174002` (ON)

## Error Handling

The API returns appropriate HTTP status codes:
- `200 OK`: Successful operations
- `400 Bad Request`: Invalid input (malformed GUID, null parameters)
- `404 Not Found`: Lamp doesn't exist
- `500 Internal Server Error`: Unexpected errors

## Example Usage

### Get all lamps
```bash
curl -X GET "http://localhost:5170/v1/lamps"
```

### Create a new lamp
```bash
curl -X POST "http://localhost:5170/v1/lamps" \
  -H "Content-Type: application/json" \
  -d '{"status": true}'
```

### Update a lamp
```bash
curl -X PUT "http://localhost:5170/v1/lamps/{lampId}" \
  -H "Content-Type: application/json" \
  -d '{"status": false}'
```

### Delete a lamp
```bash
curl -X DELETE "http://localhost:5170/v1/lamps/{lampId}"
```
