import * as grpc from '@grpc/grpc-js';
import * as protoLoader from '@grpc/proto-loader';
import { InMemoryLampRepository } from '../../repositories/InMemoryLampRepository';
import { startGrpcServer, stopGrpcServer } from '../server';
import path from 'path';
import { LampService } from '../../../domain/services/LampService';
import { v4 as uuidv4 } from 'uuid';
import { LampServiceClient } from '../generated/lamp';

const PROTO_PATH = path.resolve(__dirname, '../../../../../../docs/api/lamp.proto');

describe('gRPC Server', () => {
  let server: grpc.Server;
  let client: LampServiceClient;
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
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
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
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      client.listLamps({}, (error: Error | null, response: any) => {
        if (error) reject(error);
        else resolve(response);
      });
    });

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
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
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        (error: Error | null, response: any) => {
          if (error) reject(error);
          else resolve(response);
        },
      );
    });

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const lamp: any = await createPromise;
    expect(lamp).toBeDefined();
    expect(lamp.id).toBeDefined();
    expect(lamp.name).toBe('New Lamp');
    expect(lamp.status).toBe(false);

    // Verify it was actually added to the repository
    const allLamps = await new Promise((resolve, reject) => {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      client.listLamps({}, (error: Error | null, response: any) => {
        if (error) reject(error);
        else resolve(response);
      });
    });

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    expect((allLamps as any).lamps.length).toBe(2);
  });

  it('should get a specific lamp', async () => {
    // First get all lamps to get a valid ID
    const listPromise = new Promise((resolve, reject) => {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      client.listLamps({}, (error: Error | null, response: any) => {
        if (error) reject(error);
        else resolve(response);
      });
    });

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const listResponse: any = await listPromise;
    const lampId = listResponse.lamps[0].id;

    const getPromise = new Promise((resolve, reject) => {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      client.getLamp({ id: lampId }, (error: Error | null, response: any) => {
        if (error) reject(error);
        else resolve(response);
      });
    });

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const lamp: any = await getPromise;
    expect(lamp).toBeDefined();
    expect(lamp.id).toBe(lampId);
  });

  it('should update a lamp status', async () => {
    // First get all lamps to get a valid ID
    const listPromise = new Promise((resolve, reject) => {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      client.listLamps({}, (error: Error | null, response: any) => {
        if (error) reject(error);
        else resolve(response);
      });
    });

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const listResponse: any = await listPromise;
    const lampId = listResponse.lamps[0].id;
    const initialStatus = listResponse.lamps[0].status;

    const updatePromise = new Promise((resolve, reject) => {
      client.updateLamp(
        { id: lampId, status: !initialStatus },
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        (error: Error | null, response: any) => {
          if (error) reject(error);
          else resolve(response);
        },
      );
    });

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const updatedLamp: any = await updatePromise;
    expect(updatedLamp).toBeDefined();
    expect(updatedLamp.id).toBe(lampId);
    expect(updatedLamp.status).toBe(!initialStatus);
  });

  it('should update a lamp name', async () => {
    // First get all lamps to get a valid ID
    const listPromise = new Promise((resolve, reject) => {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      client.listLamps({}, (error: Error | null, response: any) => {
        if (error) reject(error);
        else resolve(response);
      });
    });

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const listResponse: any = await listPromise;
    const lampId = listResponse.lamps[0].id;
    const newName = 'Updated Lamp Name';

    const updatePromise = new Promise((resolve, reject) => {
      client.updateLamp(
        { id: lampId, name: newName },
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        (error: Error | null, response: any) => {
          if (error) reject(error);
          else resolve(response);
        },
      );
    });

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const updatedLamp: any = await updatePromise;
    expect(updatedLamp).toBeDefined();
    expect(updatedLamp.id).toBe(lampId);
    expect(updatedLamp.name).toBe(newName);
  });

  it('should update both lamp name and status', async () => {
    // First create a new lamp to update
    const createPromise = new Promise((resolve, reject) => {
      client.createLamp(
        { name: 'Lamp For Full Update', status: false },
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        (error: Error | null, response: any) => {
          if (error) reject(error);
          else resolve(response);
        },
      );
    });

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const newLamp: any = await createPromise;
    const lampId = newLamp.id;
    const newName = 'Completely Updated Lamp';
    const newStatus = true;

    const updatePromise = new Promise((resolve, reject) => {
      client.updateLamp(
        { id: lampId, name: newName, status: newStatus },
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        (error: Error | null, response: any) => {
          if (error) reject(error);
          else resolve(response);
        },
      );
    });

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const updatedLamp: any = await updatePromise;
    expect(updatedLamp).toBeDefined();
    expect(updatedLamp.id).toBe(lampId);
    expect(updatedLamp.name).toBe(newName);
    expect(updatedLamp.status).toBe(newStatus);
  });

  it('should handle lamp update with invalid ID', async () => {
    const nonExistentId = uuidv4();

    const updatePromise = new Promise((resolve, reject) => {
      client.updateLamp(
        { id: nonExistentId, name: 'Should Fail', status: true },
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        (error: Error | null, _response: any) => {
          if (error) resolve(error);
          else reject(new Error('Expected an error but got a successful response'));
        },
      );
    });

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const error: any = await updatePromise;
    expect(error).toBeDefined();
    expect(error.code).toBe(grpc.status.NOT_FOUND);
  });

  it('should delete a lamp', async () => {
    // First create a lamp to delete
    const createPromise = new Promise((resolve, reject) => {
      client.createLamp(
        { name: 'Lamp to Delete', status: true },
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        (error: Error | null, response: any) => {
          if (error) reject(error);
          else resolve(response);
        },
      );
    });

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const newLamp: any = await createPromise;
    const lampId = newLamp.id;

    // Now delete it
    const deletePromise = new Promise((resolve, reject) => {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      client.deleteLamp({ id: lampId }, (error: Error | null, response: any) => {
        if (error) reject(error);
        else resolve(response);
      });
    });

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const deleteResponse: any = await deletePromise;
    expect(deleteResponse).toBeDefined();
    expect(deleteResponse.success).toBe(true);

    // Try to get the deleted lamp - should fail
    const getPromise = new Promise((resolve, reject) => {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      client.getLamp({ id: lampId }, (error: Error | null, _response: any) => {
        if (error) resolve(error);
        else reject(new Error('Expected an error but got a successful response'));
      });
    });

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const error: any = await getPromise;
    expect(error).toBeDefined();
    expect(error.code).toBe(grpc.status.NOT_FOUND);
  });

  it('should handle deleteLamp with invalid ID', async () => {
    const nonExistentId = uuidv4();

    const deletePromise = new Promise((resolve, reject) => {
      client.deleteLamp(
        { id: nonExistentId },
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        (error: Error | null, _response: any) => {
          if (error) resolve(error);
          else reject(new Error('Expected an error but got a successful response'));
        },
      );
    });

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const error: any = await deletePromise;
    expect(error).toBeDefined();
    expect(error.code).toBe(grpc.status.NOT_FOUND);
  });

  it('should validate the success response format from deleteLamp', async () => {
    // First create a lamp to delete
    const createPromise = new Promise((resolve, reject) => {
      client.createLamp(
        { name: 'Lamp for Deletion Test', status: true },
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        (error: Error | null, response: any) => {
          if (error) reject(error);
          else resolve(response);
        },
      );
    });

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const newLamp: any = await createPromise;
    const lampId = newLamp.id;

    // Delete it
    const deletePromise = new Promise((resolve, reject) => {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      client.deleteLamp({ id: lampId }, (error: Error | null, response: any) => {
        if (error) reject(error);
        else resolve(response);
      });
    });

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const deleteResponse: any = await deletePromise;
    expect(deleteResponse).toBeDefined();
    expect(deleteResponse).toHaveProperty('success');
    expect(typeof deleteResponse.success).toBe('boolean');
    expect(deleteResponse.success).toBe(true);
  });

  it('should confirm lamp is removed from the repository after deletion', async () => {
    // First create a lamp to delete
    const createPromise = new Promise((resolve, reject) => {
      client.createLamp(
        { name: 'Repository Check Lamp', status: false },
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        (error: Error | null, response: any) => {
          if (error) reject(error);
          else resolve(response);
        },
      );
    });

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const newLamp: any = await createPromise;
    const lampId = newLamp.id;

    // Get all lamps before deletion
    const beforeListPromise = new Promise((resolve, reject) => {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      client.listLamps({}, (error: Error | null, response: any) => {
        if (error) reject(error);
        else resolve(response);
      });
    });

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const beforeList: any = await beforeListPromise;
    const initialCount = beforeList.lamps.length;

    // Delete the lamp
    const deletePromise = new Promise((resolve, reject) => {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      client.deleteLamp({ id: lampId }, (error: Error | null, response: any) => {
        if (error) reject(error);
        else resolve(response);
      });
    });

    await deletePromise;

    // Get all lamps after deletion
    const afterListPromise = new Promise((resolve, reject) => {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      client.listLamps({}, (error: Error | null, response: any) => {
        if (error) reject(error);
        else resolve(response);
      });
    });

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const afterList: any = await afterListPromise;
    const afterCount = afterList.lamps.length;

    expect(afterCount).toBe(initialCount - 1);

    // Make sure the deleted lamp isn't in the list
    const foundDeletedLamp = afterList.lamps.some((lamp: { id: string }) => lamp.id === lampId);
    expect(foundDeletedLamp).toBe(false);
  });

  it('should handle not found errors correctly', async () => {
    const nonExistentId = uuidv4();

    const getPromise = new Promise((resolve, reject) => {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      client.getLamp({ id: nonExistentId }, (error: Error | null, _response: any) => {
        if (error) resolve(error);
        else reject(new Error('Expected an error but got a successful response'));
      });
    });

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const error: any = await getPromise;
    expect(error).toBeDefined();
    expect(error.code).toBe(grpc.status.NOT_FOUND);
  });
});
