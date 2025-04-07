import express, { Request, Response, NextFunction } from 'express';
import cors from 'cors';
import helmet from 'helmet';
import { appLogger } from '../utils/logger';
import { DomainError, ValidationError, LampNotFoundError } from '../domain/errors/DomainError';
import { createLampRouter } from './routes/lampRoutes';
import { InMemoryLampRepository } from './repositories/InMemoryLampRepository';
import { LampService } from '../domain/services/LampService';

const app = express();

// Middleware
app.use(helmet());
app.use(cors());
app.use(express.json());

// Request logging middleware
app.use((req: Request, _res: Response, next: NextFunction) => {
  appLogger.setContext({
    requestId: req.headers['x-request-id'] as string,
    method: req.method,
    path: req.path,
  });
  appLogger.info('Incoming request', {
    query: req.query,
    params: req.params,
  });
  next();
});

// Setup repositories and services
const lampRepository = new InMemoryLampRepository();
const lampService = new LampService(lampRepository);

// Routes
app.use('/api/lamps', createLampRouter(lampService));

// 404 handler
app.use((_req: Request, res: Response) => {
  res.status(404).json({
    error: 'Not Found',
    message: 'The requested resource was not found',
  });
});

// Error handling middleware
app.use((err: Error, _req: Request, res: Response, _next: NextFunction) => {
  if (err instanceof ValidationError) {
    appLogger.warn('Validation error', err);
    return res.status(400).json({
      error: 'Validation Error',
      message: err.message,
    });
  }

  if (err instanceof LampNotFoundError) {
    appLogger.warn('Resource not found', err);
    return res.status(404).json({
      error: 'Not Found',
      message: err.message,
    });
  }

  if (err instanceof DomainError) {
    appLogger.error('Domain error', err);
    return res.status(400).json({
      error: 'Domain Error',
      message: err.message,
    });
  }

  appLogger.error('Unexpected error', err);
  return res.status(500).json({
    error: 'Internal Server Error',
    message: 'An unexpected error occurred',
  });
});

export { app }; 