import { fileURLToPath } from 'url';
import { dirname } from 'path';
import fastify from 'fastify';
import fastifyOpenapiGlue from 'fastify-openapi-glue';
import Security from './security';
import { InMemoryLampRepository } from './repositories/InMemoryLampRepository';
import Service from './services/service';

const __filename = fileURLToPath(import.meta.url);
const currentDir = dirname(__filename);

const options = {
  specification: `${currentDir}/../../../../docs/api/openapi.yaml`,
  service: new Service(new InMemoryLampRepository()),
  securityHandlers: new Security(),
  prefix: 'v1',
};

export async function buildApp() {
  const server = fastify({
    logger: true,
  });

  server.register(fastifyOpenapiGlue, options);

  return server;
}
