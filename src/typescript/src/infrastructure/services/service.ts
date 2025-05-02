import type { FastifyRequest, FastifyReply } from 'fastify';
import { LampRepository } from '../../domain/repositories/LampRepository';
import { components, operations } from '../types/api';
import { LampNotFoundError } from '../../domain/errors/DomainError';

type Lamp = components['schemas']['Lamp'];

type ListLampsRequest = FastifyRequest<{
  Querystring: { limit?: string };
}>;
type ListLampsReply = FastifyReply<{
  Reply: operations['listLamps']['responses'][200]['content']['application/json'];
}>;

type GetLampRequest = FastifyRequest<{
  Params: operations['getLamp']['parameters']['path'];
}>;
type GetLampReply = FastifyReply<{
  Reply: operations['getLamp']['responses'][200]['content']['application/json'];
}>;

type CreateLampRequest = FastifyRequest<{
  Body: operations['createLamp']['requestBody']['content']['application/json'];
}>;
type CreateLampReply = FastifyReply<{
  Reply: operations['createLamp']['responses'][201]['content']['application/json'];
}>;

type UpdateLampRequest = FastifyRequest<{
  Params: operations['updateLamp']['parameters']['path'];
  Body: operations['updateLamp']['requestBody']['content']['application/json'];
}>;
type UpdateLampReply = FastifyReply<{
  Reply: operations['updateLamp']['responses'][200]['content']['application/json'];
}>;

type DeleteLampRequest = FastifyRequest<{
  Params: operations['deleteLamp']['parameters']['path'];
}>;
type DeleteLampReply = FastifyReply<{
  Reply: void;
}>;

// service.ts
export class Service {
  constructor(private readonly repository: LampRepository) {}

  async listLamps(request: ListLampsRequest, _reply: ListLampsReply): Promise<Lamp[]> {
    const { limit } = request.query;
    const limitNumber = limit ? parseInt(limit) : undefined;
    return this.repository.findAll(limitNumber);
  }

  async getLamp(request: GetLampRequest, reply: GetLampReply): Promise<Lamp> {
    const { lampId } = request.params;
    const lamp = this.repository.findById(lampId);

    if (!lamp) {
      return reply.code(404).send();
    }

    return lamp;
  }

  async createLamp(request: CreateLampRequest, reply: CreateLampReply): Promise<Lamp> {
    const body = request.body;
    const newLamp = this.repository.create({ status: body.status });
    reply.code(201).send(newLamp);
    return newLamp;
  }

  async updateLamp(request: UpdateLampRequest, reply: UpdateLampReply): Promise<Lamp> {
    const { lampId } = request.params;
    const body = request.body;
    try {
      return this.repository.update(lampId, { status: body.status });
    } catch (error) {
      if (error instanceof LampNotFoundError) {
        return reply.code(404).send();
      }
      throw error;
    }
  }

  async deleteLamp(request: DeleteLampRequest, reply: DeleteLampReply): Promise<void> {
    const { lampId } = request.params;
    try {
      this.repository.delete(lampId);
      return reply.code(204).send();
    } catch (error) {
      if (error instanceof LampNotFoundError) {
        return reply.code(404).send();
      }
      throw error;
    }
  }
}

export default Service;
