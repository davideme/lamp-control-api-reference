import { createApp } from './infrastructure/server';
import { appLogger } from './utils/logger';
import { MongoDBLampRepository } from './infrastructure/repositories/MongoDBLampRepository';
import { InMemoryLampRepository } from './infrastructure/repositories/InMemoryLampRepository';
import { startGrpcServer } from './infrastructure/grpc/server';
import { LampRepository } from './domain/repositories/LampRepository';

const port = process.env['PORT'] || 3000;
const grpcPort = process.env['GRPC_PORT'] || 50051;

async function startServer(): Promise<void> {
  try {
    // Determine which database to use
    const dbType = process.env['DB_TYPE'] || 'memory';
    appLogger.info(`Using database type: ${dbType}`);

    // Initialize the appropriate repository
    let repository: LampRepository;

    switch (dbType) {
      case 'mongodb':
        appLogger.info('Using MongoDB repository');
        // Connect to MongoDB
        await MongoDBLampRepository.connect();
        repository = new MongoDBLampRepository();
        break;

      case 'memory':
      default:
        appLogger.info('Using in-memory repository');
        repository = new InMemoryLampRepository();
        break;
    }

    // Create the Express app with the repository
    const app = await createApp(repository);

    // Start the Express server (REST + GraphQL)
    const httpServer = app.listen(port, () => {
      appLogger.info(`HTTP server is running on port ${port}`, {
        port,
        env: process.env['NODE_ENV'] || 'development',
        dbType,
        services: ['REST API', 'GraphQL API'],
      });

      appLogger.info('API endpoints available:', {
        rest: '/api/lamps',
        graphql: '/graphql',
        docs: '/api-docs',
        metrics: '/metrics',
      });
    });

    // Start the gRPC server
    const grpcServer = await startGrpcServer(Number(grpcPort), repository);
    appLogger.info(`gRPC server is running on port ${grpcPort}`, {
      port: grpcPort,
      env: process.env['NODE_ENV'] || 'development',
      dbType,
      service: 'gRPC API',
    });

    // Handle graceful shutdown
    process.on('SIGTERM', async () => {
      appLogger.info('SIGTERM received, shutting down gracefully');

      // Close HTTP server
      httpServer.close(() => {
        appLogger.info('HTTP server closed');
      });

      // Close gRPC server
      grpcServer.tryShutdown(() => {
        appLogger.info('gRPC server closed');
      });

      // Close MongoDB connection if using MongoDB
      if (dbType === 'mongodb') {
        await MongoDBLampRepository.disconnect();
      }

      process.exit(0);
    });
  } catch (error) {
    appLogger.error('Failed to start server', { error });
    process.exit(1);
  }
}

startServer();
