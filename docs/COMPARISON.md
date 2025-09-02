# Implementation Comparison

This document provides a comparison of the different language implementations in this repository.

## Code Metrics

| Language   | App Lines | App Lines (No Generated) | Test Lines | Test/App Ratio | Coverage (%) |
|------------|-----------|--------------------------|------------|---------------|--------------|
| TypeScript | 436 | 190 | 409 | 0.94 | 85.48 |
| Python | 550 | 271 | 289 | 0.53 | 86.40 |
| Java | 1105 | 873 | 676 | 0.61 | 94.04 |
| C# | 413 | 255 | 445 | 1.08 | 98.59 |
| PHP | 1601 | 792 | 1285 | 0.80 | 88.89 |
| Go | 801 | 207 | 1319 | 1.65 | 98.50 |
| Kotlin | 583 | 462 | 795 | 1.36 | 79.19 |
| Ruby | null | null | 0 | N/A | N/A |
## API Interface Comparison

### REST API

The REST API follows OpenAPI 3.0 specifications with the following endpoints:

- `GET /v1/lamps` - List all lamps
- `GET /v1/lamps/{id}` - Get a specific lamp
- `POST /v1/lamps` - Create a new lamp
- `PUT /v1/lamps/{id}` - Update a lamp's status
- `DELETE /v1/lamps/{id}` - Delete a lamp

### GraphQL

The GraphQL API provides the following operations:

**Queries:**

- `getLamp(id: ID!): Lamp`
- `getLamps: [Lamp]`

**Mutations:**

- `createLamp(status: Boolean!): Lamp`
- `updateLamp(id: ID!, status: Boolean!): Lamp`
- `deleteLamp(id: ID!): Boolean`

### gRPC

The gRPC API defines the following services:

```protobuf
service LampService {
  rpc CreateLamp(CreateLampRequest) returns (Lamp);
  rpc GetLamp(GetLampRequest) returns (Lamp);
  rpc ListLamps(ListLampsRequest) returns (ListLampsResponse);
  rpc UpdateLamp(UpdateLampRequest) returns (Lamp);
  rpc DeleteLamp(DeleteLampRequest) returns (DeleteLampResponse);
}
```

## Language-Specific Observations

_This section will be populated as implementations progress_
