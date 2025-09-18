import { fileURLToPath } from 'url';
import { dirname, join } from 'path';
import fastify from 'fastify';
import fastifyOpenapiGlue from 'fastify-openapi-glue';
import Security from './security.js';
import { InMemoryLampRepository } from './repositories/InMemoryLampRepository.js';
import Service from './services/service.js';
const __filename = fileURLToPath(import.meta.url);
const currentDir = dirname(__filename);
// Use a path relative to the project root, which works for both dev and production
const projectRoot = process.cwd();
const specPath = join(projectRoot, '../../docs/api/openapi.yaml');
const options = {
    specification: specPath,
    service: new Service(new InMemoryLampRepository()),
    securityHandlers: new Security(),
    prefix: 'v1',
};
export async function buildApp() {
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
