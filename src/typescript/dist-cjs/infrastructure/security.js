"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
class Security {
    async apiKeyAuth(_request, _reply, _key) {
        // For development, we'll accept any API key
        return true;
    }
}
exports.default = Security;
