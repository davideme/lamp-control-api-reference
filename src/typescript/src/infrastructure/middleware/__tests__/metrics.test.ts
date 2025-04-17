import { Request, Response } from 'express';
import { register } from 'prom-client';
import { metricsEndpoint } from '../metrics';

// Mock prom-client
jest.mock('prom-client', () => {
  const originalModule = jest.requireActual('prom-client');

  return {
    ...originalModule,
    register: {
      contentType: 'text/plain; version=0.0.4; charset=utf-8',
      metrics: jest.fn().mockResolvedValue('metrics data'),
      setDefaultLabels: jest.fn(),
    },
    Histogram: jest.fn().mockImplementation(() => ({
      labels: jest.fn().mockReturnThis(),
      observe: jest.fn(),
    })),
  };
});

describe('Metrics Middleware', () => {
  let mockRequest: Partial<Request>;
  let mockResponse: Partial<Response>;

  beforeEach(() => {
    // Reset mocks
    jest.clearAllMocks();

    // Setup mock request object
    mockRequest = {
      method: 'GET',
      path: '/test-path',
      route: { path: '/test-path' },
    };

    // Setup mock response object
    mockResponse = {
      statusCode: 200,
      set: jest.fn(),
      end: jest.fn(),
      status: jest.fn().mockReturnThis(),
    };

    // Reset Date.now mock to return incremental values
    let currentTime = 1000;
    jest.spyOn(Date, 'now').mockImplementation(() => {
      currentTime += 100;
      return currentTime;
    });
  });

  afterAll(() => {
    jest.restoreAllMocks();
  });

  describe('metricsEndpoint', () => {
    it('should return metrics with proper content type', async () => {
      await metricsEndpoint(mockRequest as Request, mockResponse as Response);

      expect(mockResponse.set).toHaveBeenCalledWith(
        'Content-Type',
        'text/plain; version=0.0.4; charset=utf-8',
      );
      expect(register.metrics).toHaveBeenCalled();
      expect(mockResponse.end).toHaveBeenCalledWith('metrics data');
    });

    it('should handle errors and return 500 status', async () => {
      // Mock metrics to throw an error
      (register.metrics as jest.Mock).mockRejectedValueOnce(new Error('Metrics error'));

      await metricsEndpoint(mockRequest as Request, mockResponse as Response);

      expect(mockResponse.status).toHaveBeenCalledWith(500);
      expect(mockResponse.end).toHaveBeenCalledWith(new Error('Metrics error'));
    });
  });
});
