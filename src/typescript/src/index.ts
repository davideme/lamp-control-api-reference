import { createApp } from './infrastructure/server';
import { appLogger } from './utils/logger';
import { MongoDBLampRepository } from './infrastructure/repositories/MongoDBLampRepository';

const port = process.env['PORT'] || 3000;

async function startServer(): Promise<void> {
  try {
    // Determine which database to use
    const dbType = process.env['DB_TYPE'] || 'memory';
    appLogger.info(`Using database type: ${dbType}`);
    
    // Create the app with the appropriate repository
    const app = await createApp();

    app.listen(port, () => {
      appLogger.info(`Server is running on port ${port}`, {
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

    // Handle graceful shutdown
    process.on('SIGTERM', async () => {
      appLogger.info('SIGTERM received, shutting down gracefully');
      
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
