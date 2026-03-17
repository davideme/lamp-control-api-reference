# Implementation Comparison

This document provides a comparison of the different language implementations in this repository.

## Code Metrics

| Language   | App Lines | App Lines (No Generated) | Test Lines | Test/App Ratio | Coverage (%) | SLOC | ULOC | DRYness |
|------------|-----------|--------------------------|------------|---------------|---------------|------|------|---------|
| TypeScript | 723 | 477 | 1265 | 1.75 | 97.02 | 2616 | 1156 | 0.44 |
| Python | 1163 | 815 | 1449 | 1.25 | 95.91 | 4369 | 2169 | 0.50 |
| Java | 1888 | 1604 | 1788 | 0.95 | 90.84 | 5430 | 2262 | 0.42 |
| C# | 1472 | 1150 | 1805 | 1.23 | 98.61 | 5111 | 1937 | 0.38 |
| Go | 1598 | 796 | 2721 | 1.70 | 83.60 | 5644 | 2472 | 0.44 |
| Kotlin | 1274 | 1176 | 2036 | 1.60 | 83.87 | 4433 | 2152 | 0.49 |
## ULOC Metrics

| Language   | App ULOC | App ULOC (No Generated) | Test ULOC | Test/App Ratio | Coverage (%) |
|------------|----------|-------------------------|-----------|---------------|--------------|
| TypeScript | 494 | 150 | 237 | 0.92 | 85.48 |
| Python | 603 | 236 | 214 | 0.55 | 86.40 |
| Java | 995 | 517 | 365 | 0.58 | 94.04 |
| C# | 615 | 225 | 281 | 0.84 | 98.59 |
| Go | 1168 | 179 | 630 | 1.17 | 98.50 |
| Kotlin | 881 | 382 | 390 | 0.79 | 79.19 |

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
