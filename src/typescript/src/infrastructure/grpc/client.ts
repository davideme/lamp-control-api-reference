import * as grpc from '@grpc/grpc-js';
import * as protoLoader from '@grpc/proto-loader';
import path from 'path';
import { appLogger } from '../../utils/logger';

const PROTO_PATH = path.resolve(__dirname, '../../../../../../docs/api/lamp.proto');

/**
 * Creates a gRPC client for the Lamp Service
 * @param host The host address of the gRPC server
 * @param port The port of the gRPC server
 * @returns A gRPC client for the Lamp Service
 */
export function createLampClient(host = 'localhost', port = 50051): any {
  const address = `${host}:${port}`;

  appLogger.info(`Creating gRPC client for ${address}`);

  const packageDefinition = protoLoader.loadSync(PROTO_PATH, {
    keepCase: true,
    longs: String,
    enums: String,
    defaults: true,
    oneofs: true,
  });

  const proto = grpc.loadPackageDefinition(packageDefinition);

  // Cast to any because TypeScript doesn't know the structure of the loaded proto
  const LampService = (proto.lamp as any).LampService;

  if (!LampService) {
    throw new Error('Failed to load LampService from proto file');
  }

  return new LampService(address, grpc.credentials.createInsecure());
}

/**
 * Promisifies a gRPC call
 * @param client The gRPC client
 * @param method The method name
 * @param request The request object
 * @returns A promise that resolves to the response
 */
export function grpcPromise<TRequest, TResponse>(
  client: any,
  method: string,
  request: TRequest,
): Promise<TResponse> {
  return new Promise((resolve, reject) => {
    client[method](request, (error: Error | null, response: TResponse) => {
      if (error) {
        reject(error);
      } else {
        resolve(response);
      }
    });
  });
}
