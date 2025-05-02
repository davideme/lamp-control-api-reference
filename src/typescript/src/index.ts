import { fileURLToPath } from 'url';
import { dirname } from 'path';
import fastify from 'fastify'
import fastifyOpenapiGlue from 'fastify-openapi-glue';
import Service from './service';
import Security from './security';

const __filename = fileURLToPath(import.meta.url);
const currentDir = dirname(__filename);

const options = {
    specification: `${currentDir}/../../../docs/api/openapi.yaml`,
    service: new Service(),
    securityHandlers: new Security(),
    prefix: "v1",
  };
  

const server = fastify({
    logger: true,
})

server.register(fastifyOpenapiGlue, options)

server.listen({ port: 8080 }, (err, address) => {
  if (err) {
    console.error(err)
    process.exit(1)
  }
  console.log(`Server listening at ${address}`)
}) 