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
const mockExecSync = jest.fn();

jest.unstable_mockModule('./infrastructure/app.ts', () => ({
  buildApp: mockBuildApp,
}));

jest.unstable_mockModule('child_process', () => ({
  execSync: mockExecSync,
}));

describe('CLI', () => {
  const originalEnv = process.env;
  const originalArgv = process.argv;
  const originalExit = process.exit;
  const originalConsoleWarn = console.warn;
  const originalConsoleError = console.error;

  beforeEach(() => {
    jest.clearAllMocks();
    jest.resetModules();
    process.env = { ...originalEnv };
    process.argv = ['node', 'cli.ts'];
    process.exit = jest.fn() as unknown as typeof process.exit;
    console.warn = jest.fn();
    console.error = jest.fn();
    mockBuildApp.mockResolvedValue(mockServer);
    mockListen.mockImplementation((_opts, cb) => {
      cb(null, 'http://0.0.0.0:8080');
    });
  });

  afterAll(() => {
    process.env = originalEnv;
    process.argv = originalArgv;
    process.exit = originalExit;
    console.warn = originalConsoleWarn;
    console.error = originalConsoleError;
  });

  describe('mode parsing', () => {
    it('should default to serve-only mode when no mode specified', async () => {
      process.argv = ['node', 'cli.ts'];

      await import('./cli.ts');
      // Wait for main() to execute (it's called at module level)
      await new Promise((resolve) => setTimeout(resolve, 50));

      expect(mockBuildApp).toHaveBeenCalled();
      expect(console.warn).toHaveBeenCalledWith('Starting server without running migrations...');
    });

    it('should handle serve mode with migrations', async () => {
      process.argv = ['node', 'cli.ts', '--mode=serve'];
      process.env.DATABASE_URL = 'postgresql://localhost:5432/test';

      await import('./cli.ts');
      await new Promise((resolve) => setTimeout(resolve, 50));

      expect(console.warn).toHaveBeenCalledWith('Starting server with automatic migrations...');
      expect(mockExecSync).toHaveBeenCalledWith('npx prisma migrate deploy', expect.any(Object));
      expect(mockBuildApp).toHaveBeenCalled();
    });

    it('should handle serve mode without DATABASE_URL (skip migrations)', async () => {
      process.argv = ['node', 'cli.ts', '--mode=serve'];
      delete process.env.DATABASE_URL;

      await import('./cli.ts');
      await new Promise((resolve) => setTimeout(resolve, 50));

      expect(console.warn).toHaveBeenCalledWith('Starting server with automatic migrations...');
      expect(mockExecSync).not.toHaveBeenCalled();
      expect(mockBuildApp).toHaveBeenCalled();
    });

    it('should handle migrate mode', async () => {
      process.argv = ['node', 'cli.ts', '--mode=migrate'];
      process.env.DATABASE_URL = 'postgresql://localhost:5432/test';

      await import('./cli.ts');
      await new Promise((resolve) => setTimeout(resolve, 50));

      expect(console.warn).toHaveBeenCalledWith('Running migrations only...');
      expect(mockExecSync).toHaveBeenCalledWith('npx prisma migrate deploy', expect.any(Object));
      expect(mockBuildApp).not.toHaveBeenCalled();
    });

    it('should skip migrations in migrate mode when DATABASE_URL is not set', async () => {
      process.argv = ['node', 'cli.ts', '--mode=migrate'];
      delete process.env.DATABASE_URL;

      await import('./cli.ts');
      await new Promise((resolve) => setTimeout(resolve, 50));

      expect(console.warn).toHaveBeenCalledWith(
        'No PostgreSQL configuration found (DATABASE_URL not set), nothing to migrate',
      );
      expect(mockExecSync).not.toHaveBeenCalled();
    });

    it('should exit with error for invalid mode', async () => {
      process.argv = ['node', 'cli.ts', '--mode=invalid'];

      await import('./cli.ts');
      await new Promise((resolve) => setTimeout(resolve, 50));

      expect(console.error).toHaveBeenCalledWith(
        'Invalid mode: invalid. Valid modes are: serve, migrate, serve-only',
      );
      expect(process.exit).toHaveBeenCalledWith(1);
    });
  });

  describe('migration error handling', () => {
    it('should exit with code 1 when migration fails in migrate mode', async () => {
      process.argv = ['node', 'cli.ts', '--mode=migrate'];
      process.env.DATABASE_URL = 'postgresql://localhost:5432/test';
      mockExecSync.mockImplementation(() => {
        throw new Error('Migration failed');
      });

      await import('./cli.ts');
      await new Promise((resolve) => setTimeout(resolve, 50));

      expect(console.error).toHaveBeenCalledWith('Migration failed:', expect.any(Error));
      expect(process.exit).toHaveBeenCalledWith(1);
    });

    it('should exit with code 1 when migration fails in serve mode', async () => {
      process.argv = ['node', 'cli.ts', '--mode=serve'];
      process.env.DATABASE_URL = 'postgresql://localhost:5432/test';
      mockExecSync.mockImplementation(() => {
        throw new Error('Migration failed');
      });

      await import('./cli.ts');
      await new Promise((resolve) => setTimeout(resolve, 50));

      expect(console.error).toHaveBeenCalledWith('Migration failed:', expect.any(Error));
      expect(process.exit).toHaveBeenCalledWith(1);
    });
  });

  describe('server startup', () => {
    it('should use custom PORT and HOST from environment', async () => {
      process.env.PORT = '3000';
      process.env.HOST = '127.0.0.1';
      process.argv = ['node', 'cli.ts', '--mode=serve-only'];

      await import('./cli.ts');
      await new Promise((resolve) => setTimeout(resolve, 50));

      expect(mockListen).toHaveBeenCalledWith(
        { port: 3000, host: '127.0.0.1' },
        expect.any(Function),
      );
    });

    it('should exit with code 1 when server listen fails', async () => {
      process.argv = ['node', 'cli.ts', '--mode=serve-only'];
      mockListen.mockImplementation((_opts, cb) => {
        cb(new Error('Address in use'), '');
      });

      await import('./cli.ts');
      await new Promise((resolve) => setTimeout(resolve, 50));

      expect(mockLog.error).toHaveBeenCalledWith(expect.any(Error));
      expect(process.exit).toHaveBeenCalledWith(1);
    });
  });
});
