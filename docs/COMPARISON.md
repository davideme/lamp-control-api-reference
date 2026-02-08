# Implementation Comparison

This document provides a comparison of the different language implementations in this repository.

## Code Metrics

| Language   | App Lines | App Lines (No Generated) | Test Lines | Test/App Ratio | Coverage (%) | SLOC | ULOC | DRYness |
|------------|-----------|--------------------------|------------|---------------|---------------|------|------|---------|
| TypeScript | 702 | 456 | 1161 | 1.65 | 96.77 | 2450 | 1104 | 0.45 |
| Python | 1068 | 704 | 1203 | 1.13 | 97.56 | 3837 | 1852 | 0.48 |
| Java | 1578 | 1294 | 1522 | 0.96 | 92.61 | 4674 | 1922 | 0.41 |
| C# | 989 | 725 | 1397 | 1.41 | 100.00 | 3943 | 1447 | 0.37 |
| Go | 1505 | 697 | 2315 | 1.54 | 83.20 | 5078 | 2179 | 0.43 |
| Kotlin | 1072 | 969 | 1943 | 1.81 | 84.58 | 4092 | 1937 | 0.47 |
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
