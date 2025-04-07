import { Request, Response, NextFunction } from 'express';
import { register, Histogram } from 'prom-client';

// Create a histogram to measure request duration
const httpRequestDurationMs = new Histogram({
  name: 'http_request_duration_ms',
  help: 'Duration of HTTP requests in ms',
  labelNames: ['method', 'route', 'status_code'],
  buckets: [5, 10, 25, 50, 100, 250, 500, 1000, 2500, 5000],
});

// Initialize metrics collection
register.setDefaultLabels({
  app: 'lamp-control-api',
});

// Middleware to collect metrics
export const metricsMiddleware = (req: Request, res: Response, next: NextFunction) => {
  const start = Date.now();

  // Record response metrics after request is finished
  res.on('finish', () => {
    const duration = Date.now() - start;
    httpRequestDurationMs
      .labels(req.method, req.route?.path || req.path, res.statusCode.toString())
      .observe(duration);
  });

  next();
};

// Endpoint to expose metrics
export const metricsEndpoint = async (_req: Request, res: Response) => {
  try {
    res.set('Content-Type', register.contentType);
    res.end(await register.metrics());
  } catch (error) {
    res.status(500).end(error);
  }
}; 