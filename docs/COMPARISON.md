# Implementation Comparison

This document provides a comparison of the different language implementations in this repository.

## Code Metrics

| Language   | App Lines | Test Lines | Test/App Ratio |
|------------|-----------|------------|---------------|
| TypeScript | 365 | 343 | 0.94 |
| Python | 346 | 215 | 0.62 |
| Java | null | 0 | N/A |
| PHP | null | 0 | N/A |
| Ruby | null | 0 | N/A |
| Go | null | 0 | N/A |
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

*This section will be populated as implementations progress*
