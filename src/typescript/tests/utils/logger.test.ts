import { appLogger, LogContext } from '../../src/utils/logger';

// Mock Winston logger
jest.mock('winston', () => {
  const mockLogger = {
    debug: jest.fn(),
    info: jest.fn(),
    warn: jest.fn(),
    error: jest.fn(),
  };
  return {
    createLogger: jest.fn(() => mockLogger),
    format: {
      combine: jest.fn(),
      timestamp: jest.fn(),
      errors: jest.fn(),
      json: jest.fn(),
      colorize: jest.fn(),
      printf: jest.fn(() => jest.fn()),
    },
    transports: {
      Console: jest.fn(),
    },
  };
});

describe('appLogger', () => {
  let infoSpy: jest.SpyInstance;
  let errorSpy: jest.SpyInstance;
  let debugSpy: jest.SpyInstance;
  let warnSpy: jest.SpyInstance;
  let mockWinstonLogger: any;

  beforeEach(() => {
    mockWinstonLogger = require('winston').createLogger();
    infoSpy = jest.spyOn(appLogger, 'info');
    errorSpy = jest.spyOn(appLogger, 'error');
    debugSpy = jest.spyOn(appLogger, 'debug');
    warnSpy = jest.spyOn(appLogger, 'warn');

    // Clear context before each test
    appLogger.clearContext();
  });

  afterEach(() => {
    infoSpy.mockRestore();
    errorSpy.mockRestore();
    debugSpy.mockRestore();
    warnSpy.mockRestore();
    jest.clearAllMocks();
  });

  describe('Log methods', () => {
    it('should log info messages with context', () => {
      const context = { userId: '123', action: 'test' };
      appLogger.info('Info message', context);
      expect(infoSpy).toHaveBeenCalledWith('Info message', context);
      expect(mockWinstonLogger.info).toHaveBeenCalled();
    });

    it('should log debug messages with context', () => {
      const context = { userId: '123', action: 'test' };
      appLogger.debug('Debug message', context);
      expect(debugSpy).toHaveBeenCalledWith('Debug message', context);
      expect(mockWinstonLogger.debug).toHaveBeenCalled();
    });

    it('should log warn messages with context', () => {
      const context = { userId: '123', action: 'test' };
      appLogger.warn('Warning message', context);
      expect(warnSpy).toHaveBeenCalledWith('Warning message', context);
      expect(mockWinstonLogger.warn).toHaveBeenCalled();
    });

    it('should log error messages with error and context', () => {
      const error = new Error('Test error');
      const context = { additionalInfo: 'test' };
      appLogger.error('Error occurred', { error, ...context });
      expect(errorSpy).toHaveBeenCalledWith('Error occurred', { error, ...context });
      expect(mockWinstonLogger.error).toHaveBeenCalled();
    });

    it('should format error objects correctly', () => {
      const error = new Error('Test error');
      appLogger.error('Error occurred', error);
      expect(mockWinstonLogger.error).toHaveBeenCalledWith(
        expect.objectContaining({
          message: 'Error occurred',
          name: 'Error',
          stack: expect.any(String),
        }),
      );
    });
  });

  describe('Context management', () => {
    it('should set and include context in log messages', () => {
      const logContext: LogContext = { requestId: 'req-123', userId: 'user-456' };
      appLogger.setContext(logContext);

      appLogger.info('Test message');
      expect(mockWinstonLogger.info).toHaveBeenCalledWith(
        expect.objectContaining({
          requestId: 'req-123',
          userId: 'user-456',
          message: 'Test message',
        }),
      );
    });

    it('should merge multiple context objects', () => {
      appLogger.setContext({ requestId: 'req-123' });
      appLogger.setContext({ userId: 'user-456' });

      appLogger.info('Test with merged context');
      expect(mockWinstonLogger.info).toHaveBeenCalledWith(
        expect.objectContaining({
          requestId: 'req-123',
          userId: 'user-456',
          message: 'Test with merged context',
        }),
      );
    });

    it('should clear context when requested', () => {
      appLogger.setContext({ requestId: 'req-123', userId: 'user-456' });
      appLogger.clearContext();

      appLogger.info('Test after clearing context');
      expect(mockWinstonLogger.info).toHaveBeenCalledWith(
        expect.objectContaining({
          message: 'Test after clearing context',
        }),
      );
      expect(mockWinstonLogger.info).not.toHaveBeenCalledWith(
        expect.objectContaining({
          requestId: 'req-123',
          userId: 'user-456',
        }),
      );
    });

    it('should merge context with message metadata', () => {
      appLogger.setContext({ requestId: 'req-123' });

      appLogger.info('Test message', { userId: 'user-456', action: 'test' });
      expect(mockWinstonLogger.info).toHaveBeenCalledWith(
        expect.objectContaining({
          requestId: 'req-123',
          userId: 'user-456',
          action: 'test',
          message: 'Test message',
        }),
      );
    });
  });

  describe('Error handling', () => {
    it('should properly format error objects', () => {
      const testError = new Error('Test error');
      testError.name = 'CustomError';

      appLogger.error('An error occurred', testError);

      expect(mockWinstonLogger.error).toHaveBeenCalledWith(
        expect.objectContaining({
          message: 'An error occurred',
          name: 'CustomError',
          stack: expect.any(String),
        }),
      );
    });
  });
});
