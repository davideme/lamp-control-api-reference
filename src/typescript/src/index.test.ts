import { buildApp } from './infrastructure/app';

buildApp().then((server) => {
  server.listen({ port: 8080 }, (err, address) => {
    if (err) {
      server.log.error(err);
      process.exit(1);
    }
    server.log.info(`Server listening at ${address}`);
  });
});