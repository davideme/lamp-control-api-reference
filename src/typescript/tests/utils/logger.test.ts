import { appLogger } from '../../src/utils/logger';

describe('appLogger', () => {
  let infoSpy: jest.SpyInstance;
  let errorSpy: jest.SpyInstance;
  let debugSpy: jest.SpyInstance;

  beforeEach(() => {
    infoSpy = jest.spyOn(appLogger, 'info');
    errorSpy = jest.spyOn(appLogger, 'error');
    debugSpy = jest.spyOn(appLogger, 'debug');
  });

  afterEach(() => {
    infoSpy.mockRestore();
    errorSpy.mockRestore();
    debugSpy.mockRestore();
  });

  it('should log info messages with context', () => {
    const context = { userId: '123', action: 'test' };
    appLogger.info('Info message', context);
    expect(infoSpy).toHaveBeenCalledWith('Info message', context);
  });

  it('should log debug messages with context', () => {
    const context = { userId: '123', action: 'test' };
    appLogger.debug('Debug message', context);
    expect(debugSpy).toHaveBeenCalledWith('Debug message', context);
  });

  it('should log error messages with error and context', () => {
    const error = new Error('Test error');
    const context = { additionalInfo: 'test' };
    appLogger.error('Error occurred', { error, ...context });
    expect(errorSpy).toHaveBeenCalledWith('Error occurred', { error, ...context });
  });
});
