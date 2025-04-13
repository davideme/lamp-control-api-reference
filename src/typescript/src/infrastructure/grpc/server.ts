import * as grpc from '@grpc/grpc-js';
import * as protoLoader from '@grpc/proto-loader';
import * as path from 'path';
import { LampRepository } from '../../domain/repositories/LampRepository';
import { GrpcLampService } from './service';
import { appLogger } from '../../utils/logger';

// Update the Proto path to the correct location
// The current path that's failing is: '../../../docs/api/lamp.proto'
// Let's update it to a more appropriate location within the typescript src directory
const PROTO_PATH = path.resolve(__dirname, '../../../../../docs/api/lamp.proto');

export function createGrpcServer(lampRepository: LampRepository): grpc.Server {
  const packageDefinition = protoLoader.loadSync(PROTO_PATH, {
    keepCase: true,
    longs: String,
    enums: String,
    defaults: true,
    oneofs: true,
  });

  // Load the proto file
  const protoDescriptor = grpc.loadPackageDefinition(packageDefinition);

  // Get the service definition
  // Using type assertion to help TypeScript understand the structure
  const lampProto = protoDescriptor.lamp as any;
  const lampService = lampProto?.LampService?.service;

  if (!lampService) {
    throw new Error('Failed to load LampService from proto file');
  }

  const server = new grpc.Server();
  const serviceImplementation = new GrpcLampService(lampRepository);

  server.addService(lampService, {
    createLamp: serviceImplementation.CreateLamp.bind(serviceImplementation),
    getLamp: serviceImplementation.GetLamp.bind(serviceImplementation),
    listLamps: serviceImplementation.ListLamps.bind(serviceImplementation),
    updateLamp: serviceImplementation.UpdateLamp.bind(serviceImplementation),
    deleteLamp: serviceImplementation.DeleteLamp.bind(serviceImplementation),
  });

  return server;
}

export async function startGrpcServer(
  port: number,
  lampRepository: LampRepository,
): Promise<grpc.Server> {
  const server = createGrpcServer(lampRepository);

  return new Promise((resolve, reject) => {
    server.bindAsync(
      `0.0.0.0:${port}`,
      grpc.ServerCredentials.createInsecure(),
      (error, boundPort) => {
        if (error) {
          appLogger.error('Failed to bind gRPC server', { error });
          return reject(error);
        }

        server.start();
        appLogger.info(`gRPC server started on port ${boundPort}`);
        resolve(server);
      },
    );
  });
}

export function stopGrpcServer(server: grpc.Server): Promise<void> {
  return new Promise((resolve) => {
    server.tryShutdown(() => {
      appLogger.info('gRPC server stopped');
      resolve();
    });
  });
}
