import { fileURLToPath } from 'url';
import { dirname, join } from 'path';
import { existsSync } from 'fs';
import fastify from 'fastify';
import fastifyOpenapiGlue from 'fastify-openapi-glue';
import Security from './security.ts';
import { InMemoryLampRepository } from './repositories/InMemoryLampRepository.ts';
import { PrismaLampRepository } from './repositories/PrismaLampRepository.ts';
import { closePrismaClient } from './database/client.ts';
import { LampNotFoundError } from '../domain/errors/DomainError.ts';
import Service from './services/service.ts';

const __filename = fileURLToPath(import.meta.url);
const currentDir = dirname(__filename);

// Determine OpenAPI spec path - works for compiled (dist), source (tests), and deployment contexts
const isCompiledContext = currentDir.includes('/dist/');

// Try multiple paths in order of preference
let openapiPath: string;
if (isCompiledContext) {
  // In production/compiled context, check for bundled spec first
  const bundledPath = join(currentDir, '../../../openapi/openapi.yaml');
  const repoPath = join(currentDir, '../../../../../docs/api/openapi.yaml');
  openapiPath = existsSync(bundledPath) ? bundledPath : repoPath;
} else {
  // In test/development context
  openapiPath = join(currentDir, '../../../../docs/api/openapi.yaml');
}

// Select repository based on environment variable
const usePostgres = !!process.env.DATABASE_URL;
const repository = usePostgres ? new PrismaLampRepository() : new InMemoryLampRepository();

const options = {
  specification: openapiPath,
  service: new Service(repository),
  securityHandlers: new Security(),
  prefix: 'v1',
};

export async function buildApp(): Promise<import('fastify').FastifyInstance> {
  const server = fastify({
    logger: true,
  });

  // Centralized error handler — maps domain errors to HTTP responses
  server.setErrorHandler(async (error, _request, reply) => {
    if (error instanceof LampNotFoundError) {
      return reply.code(404).send();
    }
    throw error;
  });

  // Health endpoint - infrastructure concern, separate from business API
  server.get('/health', async (_request, _reply) => {
    return { status: 'ok' };
  });

  server.register(fastifyOpenapiGlue, options);

  // Graceful shutdown - close Prisma connection if using PostgreSQL
  server.addHook('onClose', async () => {
    if (usePostgres) {
      await closePrismaClient();
    }
  });

  return server;
}
