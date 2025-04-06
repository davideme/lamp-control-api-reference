import { appLogger, LogContext } from '../../src/utils/logger';

describe('Logger', () => {
  beforeEach(() => {
    appLogger.clearContext();
  });

  it('should log messages with different levels', () => {
    // These calls should not throw errors
    expect(() => {
      appLogger.debug('Debug message');
      appLogger.info('Info message');
      appLogger.warn('Warning message');
      appLogger.error('Error message');
    }).not.toThrow();
  });

  it('should handle context data', () => {
    const context: LogContext = {
      requestId: '123',
      userId: 'user-456',
    };

    appLogger.setContext(context);

    // These calls should not throw errors and should include context
    expect(() => {
      appLogger.info('Message with context');
    }).not.toThrow();
  });

  it('should handle error objects in error logs', () => {
    const error = new Error('Test error');
    
    expect(() => {
      appLogger.error('Error occurred', error, { additionalInfo: 'test' });
    }).not.toThrow();
  });

  it('should handle metadata in logs', () => {
    const metadata = {
      operation: 'test',
      duration: 100,
    };

    expect(() => {
      appLogger.info('Operation completed', metadata);
    }).not.toThrow();
  });

  it('should clear context', () => {
    const context: LogContext = {
      requestId: '123',
      userId: 'user-456',
    };

    appLogger.setContext(context);
    appLogger.clearContext();

    // Should log without the previous context
    expect(() => {
      appLogger.info('Message without context');
    }).not.toThrow();
  });
}); 