export class LampNotFoundError extends Error {
  constructor(message: string) {
    super(message);
    this.name = "LampNotFoundError";
  }
}
