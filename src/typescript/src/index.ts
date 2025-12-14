import { buildApp } from './infrastructure/app.ts';
import type { FastifyInstance } from 'fastify';

const PORT = parseInt(process.env.PORT || '8080', 10);
const HOST = process.env.HOST || '0.0.0.0';

buildApp().then((server: FastifyInstance) => {
  server.listen({ port: PORT, host: HOST }, (err: Error | null, address: string) => {
    if (err) {
      server.log.error(err);
      process.exit(1);
    }
    server.log.info(`Server listening at ${address}`);
  });
});
