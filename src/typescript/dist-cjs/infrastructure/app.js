"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.buildApp = buildApp;
const url_1 = require("url");
const path_1 = require("path");
const fastify_1 = require("fastify");
const fastify_openapi_glue_1 = require("fastify-openapi-glue");
const security_js_1 = require("./security.js");
const InMemoryLampRepository_js_1 = require("./repositories/InMemoryLampRepository.js");
const service_js_1 = require("./services/service.js");
const __filename = (0, url_1.fileURLToPath)(import.meta.url);
const currentDir = (0, path_1.dirname)(__filename);
// Use a path relative to the project root, which works for both dev and production
const projectRoot = process.cwd();
const specPath = (0, path_1.join)(projectRoot, '../../docs/api/openapi.yaml');
const options = {
    specification: specPath,
    service: new service_js_1.default(new InMemoryLampRepository_js_1.InMemoryLampRepository()),
    securityHandlers: new security_js_1.default(),
    prefix: 'v1',
};
async function buildApp() {
    const server = (0, fastify_1.default)({
        logger: true,
    });
    // Health endpoint - infrastructure concern, separate from business API
    server.get('/health', async (_request, _reply) => {
        return { status: 'ok' };
    });
    server.register(fastify_openapi_glue_1.default, options);
    return server;
}
