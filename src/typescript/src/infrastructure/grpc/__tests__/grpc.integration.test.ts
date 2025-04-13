import * as grpc from '@grpc/grpc-js';
import * as protoLoader from '@grpc/proto-loader';
import { InMemoryLampRepository } from '../../repositories/InMemoryLampRepository';
import { startGrpcServer, stopGrpcServer } from '../server';
import path from 'path';
import { LampService } from '../../../domain/services/LampService';
import { v4 as uuidv4 } from 'uuid';

const PROTO_PATH = path.resolve(__dirname, '../../../../../../docs/api/lamp.proto');

describe('gRPC Server', () => {
  let server: grpc.Server;
  let client: any;
  let lampRepository: InMemoryLampRepository;
  const port = 50052; // Use a different port for testing

  beforeAll(async () => {
    // Create repository and initialize with test data
    lampRepository = new InMemoryLampRepository();
    const lampService = new LampService(lampRepository);

    // Add a test lamp
    await lampService.createLamp({
      name: 'Test Lamp',
      isOn: true,
    });

    // Start gRPC server
    server = await startGrpcServer(port, lampRepository);

    // Create gRPC client for testing
    const packageDefinition = protoLoader.loadSync(PROTO_PATH, {
      keepCase: true,
      longs: String,
      enums: String,
      defaults: true,
      oneofs: true,
    });

    const proto = grpc.loadPackageDefinition(packageDefinition);
    client = new (proto.lamp as any).LampService(
      `localhost:${port}`,
      grpc.credentials.createInsecure(),
    );
  });

  afterAll(async () => {
    // Clean up
    await stopGrpcServer(server);
  });

  it('should list lamps', async () => {
    const listPromise = new Promise((resolve, reject) => {
      client.listLamps({}, (error: Error | null, response: any) => {
        if (error) reject(error);
        else resolve(response);
      });
    });

    const response: any = await listPromise;
    expect(response).toBeDefined();
    expect(response.lamps).toBeInstanceOf(Array);
    expect(response.lamps.length).toBeGreaterThan(0);

    const lamp = response.lamps[0];
    expect(lamp.id).toBeDefined();
    expect(lamp.name).toBe('Test Lamp');
    expect(lamp.status).toBe(true);
  });

  it('should create a lamp', async () => {
    const createPromise = new Promise((resolve, reject) => {
      client.createLamp(
        { name: 'New Lamp', status: false },
        (error: Error | null, response: any) => {
          if (error) reject(error);
          else resolve(response);
        },
      );
    });

    const lamp: any = await createPromise;
    expect(lamp).toBeDefined();
    expect(lamp.id).toBeDefined();
    expect(lamp.name).toBe('New Lamp');
    expect(lamp.status).toBe(false);

    // Verify it was actually added to the repository
    const allLamps = await new Promise((resolve, reject) => {
      client.listLamps({}, (error: Error | null, response: any) => {
        if (error) reject(error);
        else resolve(response);
      });
    });

    expect((allLamps as any).lamps.length).toBe(2);
  });

  it('should get a specific lamp', async () => {
    // First get all lamps to get a valid ID
    const listPromise = new Promise((resolve, reject) => {
      client.listLamps({}, (error: Error | null, response: any) => {
        if (error) reject(error);
        else resolve(response);
      });
    });

    const listResponse: any = await listPromise;
    const lampId = listResponse.lamps[0].id;

    const getPromise = new Promise((resolve, reject) => {
      client.getLamp({ id: lampId }, (error: Error | null, response: any) => {
        if (error) reject(error);
        else resolve(response);
      });
    });

    const lamp: any = await getPromise;
    expect(lamp).toBeDefined();
    expect(lamp.id).toBe(lampId);
  });

  it('should update a lamp', async () => {
    // First get all lamps to get a valid ID
    const listPromise = new Promise((resolve, reject) => {
      client.listLamps({}, (error: Error | null, response: any) => {
        if (error) reject(error);
        else resolve(response);
      });
    });

    const listResponse: any = await listPromise;
    const lampId = listResponse.lamps[0].id;
    const initialStatus = listResponse.lamps[0].status;

    const updatePromise = new Promise((resolve, reject) => {
      client.updateLamp(
        { id: lampId, status: !initialStatus },
        (error: Error | null, response: any) => {
          if (error) reject(error);
          else resolve(response);
        },
      );
    });

    const updatedLamp: any = await updatePromise;
    expect(updatedLamp).toBeDefined();
    expect(updatedLamp.id).toBe(lampId);
    expect(updatedLamp.status).toBe(!initialStatus);
  });

  it('should delete a lamp', async () => {
    // First create a lamp to delete
    const createPromise = new Promise((resolve, reject) => {
      client.createLamp(
        { name: 'Lamp to Delete', status: true },
        (error: Error | null, response: any) => {
          if (error) reject(error);
          else resolve(response);
        },
      );
    });

    const newLamp: any = await createPromise;
    const lampId = newLamp.id;

    // Now delete it
    const deletePromise = new Promise((resolve, reject) => {
      client.deleteLamp({ id: lampId }, (error: Error | null, response: any) => {
        if (error) reject(error);
        else resolve(response);
      });
    });

    const deleteResponse: any = await deletePromise;
    expect(deleteResponse).toBeDefined();
    expect(deleteResponse.success).toBe(true);

    // Try to get the deleted lamp - should fail
    const getPromise = new Promise((resolve, reject) => {
      client.getLamp({ id: lampId }, (error: Error | null, _response: any) => {
        if (error) resolve(error);
        else reject(new Error('Expected an error but got a successful response'));
      });
    });

    const error: any = await getPromise;
    expect(error).toBeDefined();
    expect(error.code).toBe(grpc.status.NOT_FOUND);
  });

  it('should handle not found errors correctly', async () => {
    const nonExistentId = uuidv4();

    const getPromise = new Promise((resolve, reject) => {
      client.getLamp({ id: nonExistentId }, (error: Error | null, _response: any) => {
        if (error) resolve(error);
        else reject(new Error('Expected an error but got a successful response'));
      });
    });

    const error: any = await getPromise;
    expect(error).toBeDefined();
    expect(error.code).toBe(grpc.status.NOT_FOUND);
  });
});
