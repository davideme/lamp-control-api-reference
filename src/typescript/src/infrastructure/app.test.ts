/**
 * @jest-environment node
 */
import { jest, describe, it, expect, beforeEach, afterAll } from '@jest/globals';

const mockClosePrismaClient = jest.fn<() => Promise<void>>();

jest.unstable_mockModule('./database/client.ts', () => ({
  closePrismaClient: mockClosePrismaClient,
  prismaClient: {},
}));

describe('App', () => {
  const originalEnv = process.env;

  beforeEach(() => {
    jest.clearAllMocks();
    process.env = { ...originalEnv };
    delete process.env.DATABASE_URL;
  });

  afterAll(() => {
    process.env = originalEnv;
  });

  describe('buildApp', () => {
    it('should create a fastify server with health endpoint', async () => {
      jest.resetModules();
      const { buildApp } = await import('./app.ts');
      const server = await buildApp();

      const response = await server.inject({
        method: 'GET',
        url: '/health',
      });

      expect(response.statusCode).toBe(200);
      expect(JSON.parse(response.payload)).toEqual({ status: 'ok' });

      await server.close();
    });

    it('should use InMemoryLampRepository when DATABASE_URL is not set', async () => {
      jest.resetModules();
      delete process.env.DATABASE_URL;

      const { buildApp } = await import('./app.ts');
      const server = await buildApp();

      // Verify the server works with in-memory repository by creating a lamp
      const createResponse = await server.inject({
        method: 'POST',
        url: '/v1/lamps',
        payload: { status: true },
      });

      expect(createResponse.statusCode).toBe(201);

      await server.close();
    });

    it('should not call closePrismaClient on shutdown when not using PostgreSQL', async () => {
      jest.resetModules();
      delete process.env.DATABASE_URL;

      const { buildApp } = await import('./app.ts');
      const server = await buildApp();

      await server.close();

      expect(mockClosePrismaClient).not.toHaveBeenCalled();
    });

    it('should call closePrismaClient on shutdown when using PostgreSQL', async () => {
      jest.resetModules();
      process.env.DATABASE_URL = 'postgresql://localhost:5432/test';

      const { buildApp } = await import('./app.ts');
      const server = await buildApp();

      await server.close();

      expect(mockClosePrismaClient).toHaveBeenCalledTimes(1);
    });
  });
});
