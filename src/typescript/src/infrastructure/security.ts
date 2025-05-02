import { FastifyReply, FastifyRequest } from 'fastify';

export default class Security {
  async apiKeyAuth(_request: FastifyRequest, _reply: FastifyReply, _key: string) {
    // For development, we'll accept any API key
    return true;
  }
}
