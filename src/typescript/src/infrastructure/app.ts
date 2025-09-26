import { fileURLToPath } from 'url';
import { dirname } from 'path';
import fastify from 'fastify';
import fastifyOpenapiGlue from 'fastify-openapi-glue';
import Security from './security.ts';
import { InMemoryLampRepository } from './repositories/InMemoryLampRepository.ts';
import Service from './services/service.ts';

const __filename = fileURLToPath(import.meta.url);
const currentDir = dirname(__filename);

const options = {
  specification: `${currentDir}/../../../../docs/api/openapi.yaml`,
  service: new Service(new InMemoryLampRepository()),
  securityHandlers: new Security(),
  prefix: 'v1',
};

export async function buildApp(): Promise<import('fastify').FastifyInstance> {
  const server = fastify({
    logger: true,
  });

  // Health endpoint - infrastructure concern, separate from business API
  server.get('/health', async (_request, _reply) => {
    return { status: 'ok' };
  });

  server.register(fastifyOpenapiGlue, options);

  return server;
}
