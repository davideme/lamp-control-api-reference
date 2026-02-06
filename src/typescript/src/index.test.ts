/**
 * @jest-environment node
 */
import { jest, describe, it, expect, beforeEach, afterAll } from '@jest/globals';

const mockListen =
  jest.fn<(opts: unknown, cb: (err: Error | null, address: string) => void) => void>();
const mockLog = { error: jest.fn(), info: jest.fn() };
const mockServer = {
  listen: mockListen,
  log: mockLog,
};
const mockBuildApp = jest.fn<() => Promise<typeof mockServer>>();

jest.unstable_mockModule('./infrastructure/app.ts', () => ({
  buildApp: mockBuildApp,
}));

describe('index', () => {
  const originalEnv = process.env;
  const originalExit = process.exit;

  beforeEach(() => {
    jest.clearAllMocks();
    jest.resetModules();
    process.env = { ...originalEnv };
    process.exit = jest.fn() as unknown as typeof process.exit;
    mockBuildApp.mockResolvedValue(mockServer);
    mockListen.mockImplementation((_opts, cb) => {
      cb(null, 'http://0.0.0.0:8080');
    });
  });

  afterAll(() => {
    process.env = originalEnv;
    process.exit = originalExit;
  });

  it('should build app and start listening on default port', async () => {
    await import('./index.ts');
    await new Promise((resolve) => setTimeout(resolve, 50));

    expect(mockBuildApp).toHaveBeenCalled();
    expect(mockListen).toHaveBeenCalledWith({ port: 8080, host: '0.0.0.0' }, expect.any(Function));
  });

  it('should use custom PORT and HOST from environment', async () => {
    process.env.PORT = '3000';
    process.env.HOST = '127.0.0.1';

    await import('./index.ts');
    await new Promise((resolve) => setTimeout(resolve, 50));

    expect(mockListen).toHaveBeenCalledWith(
      { port: 3000, host: '127.0.0.1' },
      expect.any(Function),
    );
  });

  it('should exit with code 1 when listen fails', async () => {
    mockListen.mockImplementation((_opts, cb) => {
      cb(new Error('Address in use'), '');
    });

    await import('./index.ts');
    await new Promise((resolve) => setTimeout(resolve, 50));

    expect(mockLog.error).toHaveBeenCalledWith(expect.any(Error));
    expect(process.exit).toHaveBeenCalledWith(1);
  });
});
