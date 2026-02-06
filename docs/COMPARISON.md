# Implementation Comparison

This document provides a comparison of the different language implementations in this repository.

## Code Metrics

| Language   | App Lines | App Lines (No Generated) | Test Lines | Test/App Ratio | Coverage (%) | SLOC | ULOC | DRYness |
|------------|-----------|--------------------------|------------|---------------|---------------|------|------|---------|
| TypeScript | 713 | 467 | 1160 | 1.63 | 96.87 | 2470 | 1095 | 0.44 |
| Python | 1061 | 697 | 1184 | 1.12 | 96.79 | 3795 | 1840 | 0.48 |
| Java | 1653 | 1369 | 1497 | 0.91 | 57.10 | 4716 | 1928 | 0.41 |
| C# | 950 | 686 | 1356 | 1.43 | 79.89 | 3787 | 1400 | 0.37 |
| Go | 1499 | 691 | 2290 | 1.53 | 84.30 | 5050 | 2179 | 0.43 |
| Kotlin | 1082 | 945 | 1858 | 1.72 | 84.62 | 3962 | 1880 | 0.47 |
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
