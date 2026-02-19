/**
 * @jest-environment node
 */
import { jest, describe, it, expect, beforeEach } from '@jest/globals';

// Mock @prisma/client before importing client module
const mockDisconnect = jest.fn<() => Promise<void>>();
const MockPrismaClient = jest.fn().mockImplementation(() => ({
  $disconnect: mockDisconnect,
}));

jest.unstable_mockModule('@prisma/client', () => ({
  PrismaClient: MockPrismaClient,
}));

describe('Prisma Client', () => {
  const originalEnv = process.env;

  beforeEach(() => {
    jest.clearAllMocks();
    process.env = { ...originalEnv };
    // Reset module registry to get a fresh singleton each test
    jest.resetModules();
  });

  afterAll(() => {
    process.env = originalEnv;
  });

  describe('getPrismaClient', () => {
    it('should throw error when DATABASE_URL is not set', async () => {
      delete process.env.DATABASE_URL;

      const { getPrismaClient } = await import('./client.ts');

      expect(() => getPrismaClient()).toThrow(
        'DATABASE_URL environment variable is not set. Cannot initialize Prisma Client.',
      );
    });

    it('should create PrismaClient when DATABASE_URL is set', async () => {
      process.env.DATABASE_URL = 'postgresql://localhost:5432/test';

      const { getPrismaClient } = await import('./client.ts');

      const client = getPrismaClient();
      expect(client).toBeDefined();
      expect(MockPrismaClient).toHaveBeenCalledTimes(1);
    });

    it('should return same instance on subsequent calls (singleton)', async () => {
      process.env.DATABASE_URL = 'postgresql://localhost:5432/test';

      const { getPrismaClient } = await import('./client.ts');

      const first = getPrismaClient();
      const second = getPrismaClient();
      expect(first).toBe(second);
      expect(MockPrismaClient).toHaveBeenCalledTimes(1);
    });

    it('should configure development logging when NODE_ENV is development', async () => {
      process.env.DATABASE_URL = 'postgresql://localhost:5432/test';
      process.env.NODE_ENV = 'development';

      const { getPrismaClient } = await import('./client.ts');

      getPrismaClient();
      expect(MockPrismaClient).toHaveBeenCalledWith(
        expect.objectContaining({
          log: ['query', 'info', 'warn', 'error'],
        }),
      );
    });

    it('should configure error-only logging when NODE_ENV is not development', async () => {
      process.env.DATABASE_URL = 'postgresql://localhost:5432/test';
      process.env.NODE_ENV = 'production';

      const { getPrismaClient } = await import('./client.ts');

      getPrismaClient();
      expect(MockPrismaClient).toHaveBeenCalledWith(
        expect.objectContaining({
          log: ['error'],
        }),
      );
    });

    it('should normalize empty host URL when host query parameter is set', async () => {
      process.env.DATABASE_URL =
        'postgresql://postgres:pass@/lamp-control?host=/cloudsql/project:region:instance&connect_timeout=5';

      const { getPrismaClient } = await import('./client.ts');

      getPrismaClient();
      expect(MockPrismaClient).toHaveBeenCalledWith(
        expect.objectContaining({
          datasources: {
            db: {
              url: 'postgresql://postgres:pass@localhost/lamp-control?host=/cloudsql/project:region:instance&connect_timeout=5',
            },
          },
        }),
      );
    });

    it('should keep regular URL unchanged', async () => {
      process.env.DATABASE_URL = 'postgresql://localhost:5432/test';

      const { getPrismaClient } = await import('./client.ts');

      getPrismaClient();
      expect(MockPrismaClient).toHaveBeenCalledWith(
        expect.objectContaining({
          datasources: {
            db: {
              url: 'postgresql://localhost:5432/test',
            },
          },
        }),
      );
    });

    it('should normalize cloud sql socket URL with encoded password characters', async () => {
      process.env.DATABASE_URL =
        'postgresql://postgres:REDACTED%7BPASSWORD%7D@/lamp-control?host=/cloudsql/example-project:europe-west1:lamp-control-db&connect_timeout=5';

      const { getPrismaClient } = await import('./client.ts');

      getPrismaClient();
      expect(MockPrismaClient).toHaveBeenCalledWith(
        expect.objectContaining({
          datasources: {
            db: {
              url: 'postgresql://postgres:REDACTED%7BPASSWORD%7D@localhost/lamp-control?host=/cloudsql/example-project:europe-west1:lamp-control-db&connect_timeout=5',
            },
          },
        }),
      );
    });
  });

  describe('closePrismaClient', () => {
    it('should disconnect and clear client when client exists', async () => {
      process.env.DATABASE_URL = 'postgresql://localhost:5432/test';

      const { getPrismaClient, closePrismaClient } = await import('./client.ts');

      // Create the client first
      getPrismaClient();
      expect(MockPrismaClient).toHaveBeenCalledTimes(1);

      // Close it
      await closePrismaClient();
      expect(mockDisconnect).toHaveBeenCalledTimes(1);

      // Next getPrismaClient should create a new instance
      getPrismaClient();
      expect(MockPrismaClient).toHaveBeenCalledTimes(2);
    });

    it('should do nothing when no client exists', async () => {
      const { closePrismaClient } = await import('./client.ts');

      await closePrismaClient();
      expect(mockDisconnect).not.toHaveBeenCalled();
    });
  });

  describe('prismaClient proxy', () => {
    it('should lazily initialize client on property access', async () => {
      process.env.DATABASE_URL = 'postgresql://localhost:5432/test';

      const { prismaClient } = await import('./client.ts');

      expect(MockPrismaClient).not.toHaveBeenCalled();

      // Access a property to trigger proxy
      // eslint-disable-next-line @typescript-eslint/no-unused-expressions
      prismaClient.$disconnect;
      expect(MockPrismaClient).toHaveBeenCalledTimes(1);
    });

    it('should throw when accessing proxy without DATABASE_URL', async () => {
      delete process.env.DATABASE_URL;

      const { prismaClient } = await import('./client.ts');

      expect(() => prismaClient.$disconnect).toThrow(
        'DATABASE_URL environment variable is not set',
      );
    });
  });
});
