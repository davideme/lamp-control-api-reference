import { createLampClient, grpcPromise } from '../infrastructure/grpc/client';

// Define TypeScript interfaces matching our gRPC message types
interface Lamp {
  id: string;
  name: string;
  status: boolean;
}

interface ListLampsResponse {
  lamps: Lamp[];
}

interface CreateLampRequest {
  name: string;
  status: boolean;
}

interface UpdateLampRequest {
  id: string;
  name?: string;
  status?: boolean;
}

interface GetLampRequest {
  id: string;
}

interface DeleteLampRequest {
  id: string;
}

interface DeleteLampResponse {
  success: boolean;
}

/**
 * Example of using the gRPC client to interact with the Lamp Control API
 */
async function main() {
  try {
    // Create a gRPC client
    const client = createLampClient('localhost', 50051);

    console.log('Creating a new lamp...');
    const newLamp = await grpcPromise<CreateLampRequest, Lamp>(client, 'createLamp', {
      name: 'Living Room Lamp',
      status: true,
    });
    console.log('Lamp created:', newLamp);

    console.log('\nListing all lamps...');
    const { lamps } = await grpcPromise<{}, ListLampsResponse>(client, 'listLamps', {});
    console.log('Found', lamps.length, 'lamps:');
    lamps.forEach((lamp: Lamp) => {
      console.log(`- ${lamp.id}: ${lamp.name} (${lamp.status ? 'ON' : 'OFF'})`);
    });

    console.log('\nUpdating lamp status...');
    const updatedLamp = await grpcPromise<UpdateLampRequest, Lamp>(client, 'updateLamp', {
      id: newLamp.id,
      status: false,
    });
    console.log('Lamp updated:', updatedLamp);

    console.log('\nGetting lamp by ID...');
    const lamp = await grpcPromise<GetLampRequest, Lamp>(client, 'getLamp', { id: newLamp.id });
    console.log('Lamp details:', lamp);

    console.log('\nDeleting lamp...');
    const deleteResult = await grpcPromise<DeleteLampRequest, DeleteLampResponse>(client, 'deleteLamp', { id: newLamp.id });
    console.log('Lamp deleted:', deleteResult);

    process.exit(0);
  } catch (error) {
    console.error('Error:', error);
    process.exit(1);
  }
}

// Run the example if this file is executed directly
if (require.main === module) {
  main();
}
