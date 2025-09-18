export class DomainError extends Error {
    constructor(message) {
        super(message);
        this.name = this.constructor.name;
        Error.captureStackTrace(this, this.constructor);
    }
}
export class LampNotFoundError extends DomainError {
    constructor(id) {
        super(`Lamp with ID ${id} not found`);
    }
}
