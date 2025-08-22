# Implementation Comparison

This document provides a comparison of the different language implementations in this repository.

## Code Metrics

| Language   | App Lines | Test Lines | Test/App Ratio | Coverage (%) |
|------------|-----------|------------|---------------|--------------|
| TypeScript | 365 | 343 | 0.94 | 85.00 |
| Python | 346 | 215 | 0.62 | 86.40 |
| Java | 691 | 619 | 0.90 | 92.00 |
| C# | 412 | 435 | 1.06 | 98.59 |
| PHP | 1419 | 1158 | 0.82 | 88.89 |
| Go | 197 | 1307 | 6.63 | 98.60 |
| Kotlin | 583 | 795 | 1.36 | 79.19 |
| Ruby | null | 0 | N/A | N/A |
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
