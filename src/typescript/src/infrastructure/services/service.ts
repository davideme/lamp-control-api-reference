import type { FastifyRequest, FastifyReply } from 'fastify';
import { LampRepository } from '../../domain/repositories/LampRepository';
import { components, operations } from '../types/api';
import { LampNotFoundError } from '../../domain/errors/DomainError';

type Lamp = components['schemas']['Lamp'];

type ListLampsRequest = FastifyRequest<{
  Querystring: { cursor?: string | null; pageSize?: number };
}>;

type ListLampsResponse = {
  data: Lamp[];
  nextCursor?: string | null;
  hasMore: boolean;
};

type GetLampRequest = FastifyRequest<{
  Params: operations['getLamp']['parameters']['path'];
}>;

type CreateLampRequest = FastifyRequest<{
  Body: operations['createLamp']['requestBody']['content']['application/json'];
}>;

type UpdateLampRequest = FastifyRequest<{
  Params: operations['updateLamp']['parameters']['path'];
  Body: operations['updateLamp']['requestBody']['content']['application/json'];
}>;

type DeleteLampRequest = FastifyRequest<{
  Params: operations['deleteLamp']['parameters']['path'];
}>;

// service.ts
export class Service {
  constructor(private readonly repository: LampRepository) {}

  async listLamps(request: ListLampsRequest, reply: FastifyReply): Promise<void> {
    const { cursor, pageSize = 25 } = request.query;
    const lamps = await this.repository.findAll(pageSize);

    // Simple implementation without actual cursor-based pagination
    // In a real implementation, you'd use the cursor to fetch from a specific position
    const response: ListLampsResponse = {
      data: lamps,
      hasMore: false, // Simple implementation assumes no more pages
      nextCursor: null,
    };

    return reply.code(200).send(response);
  }

  async getLamp(request: GetLampRequest, reply: FastifyReply): Promise<void> {
    const { lampId } = request.params;
    const lamp = await this.repository.findById(lampId);

    if (!lamp) {
      return reply.code(404).send();
    }

    return reply.code(200).send(lamp);
  }

  async createLamp(request: CreateLampRequest, reply: FastifyReply): Promise<void> {
    const body = request.body;
    const newLamp = await this.repository.create({ status: body.status });
    return reply.code(201).send(newLamp);
  }

  async updateLamp(request: UpdateLampRequest, reply: FastifyReply): Promise<void> {
    const { lampId } = request.params;
    const body = request.body;
    try {
      const updatedLamp = await this.repository.update(lampId, { status: body.status });
      return reply.code(200).send(updatedLamp);
    } catch (error) {
      if (error instanceof LampNotFoundError) {
        return reply.code(404).send();
      }
      throw error;
    }
  }

  async deleteLamp(request: DeleteLampRequest, reply: FastifyReply): Promise<void> {
    const { lampId } = request.params;
    try {
      await this.repository.delete(lampId);
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
