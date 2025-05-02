export class DomainError extends Error {
    constructor(message: string) {
      super(message);
      this.name = this.constructor.name;
      Error.captureStackTrace(this, this.constructor);
    }
  }
  
  export class LampNotFoundError extends DomainError {
    constructor(id: string) {
      super(`Lamp with ID ${id} not found`);
    }
  }
  