import { buildApp } from './infrastructure/app.ts';
import type { FastifyInstance } from 'fastify';

buildApp().then((server: FastifyInstance) => {
  server.listen({ port: 8080, host: '0.0.0.0' }, (err: Error | null, address: string) => {
    if (err) {
      server.log.error(err);
      process.exit(1);
    }
    server.log.info(`Server listening at ${address}`);
  });
});
