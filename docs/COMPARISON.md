# Implementation Comparison

This document provides a comparison of the different language implementations in this repository.

## Code Metrics

| Language   | App Lines | App Lines (No Generated) | Test Lines | Test/App Ratio | Coverage (%) | SLOC | ULOC | DRYness |
|------------|-----------|--------------------------|------------|---------------|---------------|------|------|---------|
| TypeScript | 484 | 238 | 409 | 0.85 | 85.48 | 1193 | 539 | 0.45 |
| Python | 609 | 330 | 285 | 0.47 | 86.40 | 1612 | 695 | 0.43 |
| Java | 1156 | 924 | 743 | 0.64 | 89.88 | 2737 | 1048 | 0.38 |
| C# | 519 | 361 | 622 | 1.20 | 98.59 | 1967 | 798 | 0.41 |
| Go | 889 | 295 | 1490 | 1.68 | 98.60 | 3220 | 1337 | 0.42 |
| Kotlin | 692 | 571 | 914 | 1.32 | 82.34 | 2253 | 1030 | 0.46 |
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
