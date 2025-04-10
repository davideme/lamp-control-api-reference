import express, { Request, Response, NextFunction } from 'express';
import cors from 'cors';
import helmet from 'helmet';
import swaggerUi from 'swagger-ui-express';
import { appLogger } from '../utils/logger';
import { createLampRouter } from './routes/lampRoutes';
import { ValidationError, LampNotFoundError } from '../domain/errors/DomainError';
import { InMemoryLampRepository } from './repositories/InMemoryLampRepository';
import { LampService } from '../domain/services/LampService';
import { openApiDocument } from './openapi';
import { metricsMiddleware, metricsEndpoint } from './middleware/metrics';
import { rateLimiter } from './middleware/rateLimiter';
import { LampRepository } from '../domain/repositories/LampRepository';

export function createApp(repository: LampRepository = new InMemoryLampRepository()) {
  const app = express();
  const lampService = new LampService(repository);

  // Middleware
  app.use(express.json());
  app.use(cors());
  app.use(helmet());

  // Apply rate limiting to all routes
  app.use(rateLimiter);

  // Apply metrics middleware
  app.use(metricsMiddleware);

  // Request logging middleware
  app.use((req: Request, _res: Response, next: NextFunction) => {
    appLogger.info('Incoming request', {
      method: req.method,
      path: req.path,
      query: req.query,
      body: req.body,
    });
    next();
  });

  // Routes
  app.use('/api/lamps', createLampRouter(lampService));

  // Serve OpenAPI documentation
  app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(openApiDocument));

  // Expose metrics endpoint
  app.get('/metrics', metricsEndpoint);

  // 404 handler
  app.use((_req: Request, res: Response) => {
    res.status(404).json({
      error: 'NotFoundError',
      message: 'Resource not found',
    });
  });

  // Error handler
  app.use((err: Error, _req: Request, res: Response, _next: NextFunction) => {
    appLogger.error('Server error', { error: err });

    if (err instanceof ValidationError || err instanceof LampNotFoundError) {
      return res.status(err instanceof ValidationError ? 400 : 404).json({
        error: err.name,
        message: err.message,
      });
    }

    return res.status(500).json({
      error: 'InternalServerError',
      message: 'An unexpected error occurred',
    });
  });

  return app;
}

export const app = createApp();
