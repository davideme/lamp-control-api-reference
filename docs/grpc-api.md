# gRPC API Documentation

This document provides information on how to use the Lamp Control gRPC API.

## Overview

The Lamp Control gRPC API provides a high-performance, strongly-typed interface for controlling lamps. It supports all the core operations available in the REST and GraphQL APIs:

- Creating lamps
- Retrieving lamp information
- Updating lamp status
- Deleting lamps
- Listing all lamps

## Prerequisites

To use the gRPC API, you'll need:

- A gRPC client library for your programming language
- The Protocol Buffers definition file (`lamp.proto`)
- Connection details for the gRPC server

## Protocol Buffer Definition

The API is defined using Protocol Buffers (proto3). Here's the definition:

```proto
syntax = "proto3";

package lamp;

service LampService {
  // Create a new lamp with the given status
  rpc CreateLamp(CreateLampRequest) returns (Lamp);
  
  // Get a lamp by its ID
  rpc GetLamp(GetLampRequest) returns (Lamp);
  
  // List all lamps
  rpc ListLamps(ListLampsRequest) returns (ListLampsResponse);
  
  // Update a lamp's status
  rpc UpdateLamp(UpdateLampRequest) returns (Lamp);
  
  // Delete a lamp
  rpc DeleteLamp(DeleteLampRequest) returns (DeleteLampResponse);
}

// A lamp with an ID and on/off status
message Lamp {
  string id = 1;
  string name = 2;
  bool status = 3;
  string createdAt = 4;
  string updatedAt = 5;
}

message CreateLampRequest {
  string name = 1;
  bool status = 2;
}

message GetLampRequest {
  string id = 1;
}

message ListLampsRequest {
  // Empty request
}

message ListLampsResponse {
  repeated Lamp lamps = 1;
}

message UpdateLampRequest {
  string id = 1;
  string name = 2;
  bool status = 3;
}

message DeleteLampRequest {
  string id = 1;
}

message DeleteLampResponse {
  bool success = 1;
}
```

## Using the API

### Connection

By default, the gRPC server runs on port 50051. You can configure this using the `GRPC_PORT` environment variable.

### TypeScript/JavaScript Example

Here's a complete example of using the gRPC API with Node.js:

```typescript
import * as grpc from '@grpc/grpc-js';
import * as protoLoader from '@grpc/proto-loader';
import path from 'path';

// Load proto file
const PROTO_PATH = path.resolve(__dirname, './lamp.proto');
const packageDefinition = protoLoader.loadSync(PROTO_PATH, {
  keepCase: true,
  longs: String,
  enums: String,
  defaults: true,
  oneofs: true,
});

const proto = grpc.loadPackageDefinition(packageDefinition);
const client = new (proto.lamp as any).LampService(
  'localhost:50051',
  grpc.credentials.createInsecure()
);

// Helper function to promisify gRPC calls
function grpcPromise(method: string, request: any) {
  return new Promise((resolve, reject) => {
    client[method](request, (error: Error | null, response: any) => {
      if (error) reject(error);
      else resolve(response);
    });
  });
}

async function main() {
  try {
    // Create a lamp
    const newLamp = await grpcPromise('createLamp', { 
      name: 'Kitchen Lamp', 
      status: true 
    });
    console.log('Created lamp:', newLamp);

    // Get all lamps
    const { lamps } = await grpcPromise('listLamps', {});
    console.log('All lamps:', lamps);

    // Update lamp status
    const updatedLamp = await grpcPromise('updateLamp', {
      id: newLamp.id,
      status: false
    });
    console.log('Updated lamp:', updatedLamp);

    // Delete lamp
    const result = await grpcPromise('deleteLamp', { id: newLamp.id });
    console.log('Delete result:', result);
  } catch (error) {
    console.error('Error:', error);
  }
}

main();
```

### Error Handling

The gRPC API uses standard gRPC status codes:

- `NOT_FOUND` (5): When a requested lamp doesn't exist
- `INVALID_ARGUMENT` (3): When the provided data is invalid
- `INTERNAL` (13): For server-side errors

## Environment Variables

- `GRPC_PORT`: The port for the gRPC server (default: 50051)

## Performance Considerations

The gRPC API provides several advantages over REST:

- Efficient binary serialization
- Streaming capabilities
- Strong typing
- Reduced network overhead

For high-throughput scenarios or when working with systems that benefit from stronger typing, the gRPC API is recommended over REST.
