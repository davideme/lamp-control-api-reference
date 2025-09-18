"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const app_1 = require("./infrastructure/app");
(0, app_1.buildApp)().then((server) => {
    server.listen({ port: 8080 }, (err, address) => {
        if (err) {
            server.log.error(err);
            process.exit(1);
        }
        server.log.info(`Server listening at ${address}`);
    });
});
