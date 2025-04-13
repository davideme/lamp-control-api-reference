import {
  CreateLampRequest,
  DeleteLampRequest,
  DeleteLampResponse,
  GetLampRequest,
  Lamp,
  ListLampsResponse,
  UpdateLampRequest,
} from '@/infrastructure/grpc/generated/lamp';
import { createLampClient, grpcPromise } from '../infrastructure/grpc/client';
import { appLogger } from '../utils/logger';

/**
 * Example of using the gRPC client to interact with the Lamp Control API
 */
async function main(): Promise<void> {
  try {
    // Create a gRPC client
    const client = createLampClient('localhost', 50051);

    appLogger.info('Creating a new lamp...');
    const newLamp = await grpcPromise<CreateLampRequest, Lamp>(client, 'createLamp', {
      name: 'Living Room Lamp',
      status: true,
    });
    appLogger.info('Lamp created:', { lamp: newLamp });

    appLogger.info('\nListing all lamps...');
    const { lamps } = await grpcPromise<Record<string, never>, ListLampsResponse>(
      client,
      'listLamps',
      {},
    );
    appLogger.info(`Found ${lamps.length} lamps`);
    lamps.forEach((lamp: Lamp) => {
      appLogger.info(`- ${lamp.id}: ${lamp.name} (${lamp.status ? 'ON' : 'OFF'})`);
    });

    appLogger.info('\nUpdating lamp status...');
    const updatedLamp = await grpcPromise<UpdateLampRequest, Lamp>(client, 'updateLamp', {
      id: newLamp.id,
      status: false,
    });
    appLogger.info('Lamp updated:', { lamp: updatedLamp });

    appLogger.info('\nGetting lamp by ID...');
    const lamp = await grpcPromise<GetLampRequest, Lamp>(client, 'getLamp', { id: newLamp.id });
    appLogger.info('Lamp details:', { lamp });

    appLogger.info('\nDeleting lamp...');
    const deleteResult = await grpcPromise<DeleteLampRequest, DeleteLampResponse>(
      client,
      'deleteLamp',
      { id: newLamp.id },
    );
    appLogger.info('Lamp deleted:', { result: deleteResult });

    process.exit(0);
  } catch (error) {
    appLogger.error('Error:', { error });
    process.exit(1);
  }
}

// Run the example if this file is executed directly
if (require.main === module) {
  main();
}
