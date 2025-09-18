"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.LampNotFoundError = exports.DomainError = void 0;
class DomainError extends Error {
    constructor(message) {
        super(message);
        this.name = this.constructor.name;
        Error.captureStackTrace(this, this.constructor);
    }
}
exports.DomainError = DomainError;
class LampNotFoundError extends DomainError {
    constructor(id) {
        super(`Lamp with ID ${id} not found`);
    }
}
exports.LampNotFoundError = LampNotFoundError;
