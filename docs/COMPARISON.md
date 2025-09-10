# Implementation Comparison

This document provides a comparison of the different language implementations in this repository.

## Code Metrics

| Language   | App Lines | App Lines (No Generated) | Test Lines | Test/App Ratio | Coverage (%) |
|------------|-----------|--------------------------|------------|---------------|--------------|
| TypeScript | 436 | 190 | 409 | 0.94 | 85.48 |
| Python | 550 | 271 | 289 | 0.53 | 86.40 |
| Java | 1105 | 873 | 676 | 0.61 | 94.04 |
| C# | 413 | 255 | 449 | 1.09 | 98.59 |
| PHP | 1090 | 235 | 1015 | 0.93 | N/A |
| Go | 801 | 207 | 1319 | 1.65 | 98.50 |
| Kotlin | 583 | 462 | 795 | 1.36 | 79.19 |
| Ruby | null | null | 0 | N/A | N/A |

## ULOC Metrics

| Language   | App Lines | App Lines (No Generated) | Test Lines | App ULOC | App ULOC (No Generated) | Test ULOC | App DRYness | App DRYness (No Generated) | Test DRYness |
|------------|-----------|--------------------------|------------|----------|-------------------------|-----------|-------------|----------------------------|--------------|
| TypeScript | 436 | 190 | 409 | 494 | 150 | 237 | 1.13 | 0.78 | 0.57 |
| Python | 550 | 271 | 289 | 603 | 236 | 214 | 1.09 | 0.87 | 0.74 |
| Java | 1105 | 873 | 676 | 995 | 517 | 365 | 0.90 | 0.59 | 0.54 |
| C# | 413 | 255 | 449 | 615 | 225 | 281 | 1.48 | 0.88 | 0.63 |
| PHP | 1090 | 235 | 1015 | 1360 | 198 | 571 | 1.25 | 0.84 | 0.56 |
| Go | 801 | 207 | 1319 | 1168 | 179 | 630 | 1.45 | 0.86 | 0.48 |
| Kotlin | 583 | 462 | 795 | 881 | 382 | 390 | 1.51 | 0.83 | 0.49 |
| Ruby | null | null | 0 | null | null | null | N/A | N/A | N/A |

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
