import * as grpc from '@grpc/grpc-js';
import { LampRepository } from '../../domain/repositories/LampRepository';
import { GrpcLampService } from './service';
import { appLogger } from '../../utils/logger';
import { LampServiceService } from './generated/lamp';

export function createGrpcServer(lampRepository: LampRepository): grpc.Server {
  const serviceImplementation = new GrpcLampService(lampRepository);

  const server = new grpc.Server();
  server.addService(LampServiceService, {
    createLamp: serviceImplementation.createLamp.bind(serviceImplementation),
    getLamp: serviceImplementation.getLamp.bind(serviceImplementation),
    listLamps: serviceImplementation.listLamps.bind(serviceImplementation),
    updateLamp: serviceImplementation.updateLamp.bind(serviceImplementation),
    deleteLamp: serviceImplementation.deleteLamp.bind(serviceImplementation),
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
