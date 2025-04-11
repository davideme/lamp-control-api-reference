import { createApp } from './infrastructure/server';
import { appLogger } from './utils/logger';

const port = process.env['PORT'] || 3000;

async function startServer(): Promise<void> {
  try {
    const app = await createApp();

    app.listen(port, () => {
      appLogger.info(`Server is running on port ${port}`, {
        port,
        env: process.env['NODE_ENV'] || 'development',
        services: ['REST API', 'GraphQL API'],
      });

      appLogger.info('API endpoints available:', {
        rest: '/api/lamps',
        graphql: '/graphql',
        docs: '/api-docs',
        metrics: '/metrics',
      });
    });
  } catch (error) {
    appLogger.error('Failed to start server', { error });
    process.exit(1);
  }
}

startServer();
