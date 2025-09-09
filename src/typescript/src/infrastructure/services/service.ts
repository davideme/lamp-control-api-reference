import type { FastifyRequest, FastifyReply } from 'fastify';
import { LampRepository } from '../../domain/repositories/LampRepository';
import { components, operations } from '../types/api';
import { LampNotFoundError } from '../../domain/errors/DomainError';
import { LampMapper } from '../mappers/LampMapper';

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
    const { pageSize = 25 } = request.query;
    const lampEntities = await this.repository.findAll(pageSize);

    // Convert domain entities to API models
    const lamps = lampEntities.map(LampMapper.toApiModel);

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
    const lampEntity = await this.repository.findById(lampId);

    if (!lampEntity) {
      return reply.code(404).send();
    }

    // Convert domain entity to API model
    const lamp = LampMapper.toApiModel(lampEntity);
    return reply.code(200).send(lamp);
  }

  async createLamp(request: CreateLampRequest, reply: FastifyReply): Promise<void> {
    const body = request.body;
    
    // Convert API model to domain entity
    const lampEntityCreate = LampMapper.toDomainEntityCreate(body);
    const newLampEntity = await this.repository.create(lampEntityCreate);
    
    // Convert domain entity to API model
    const newLamp = LampMapper.toApiModel(newLampEntity);
    return reply.code(201).send(newLamp);
  }

  async updateLamp(request: UpdateLampRequest, reply: FastifyReply): Promise<void> {
    const { lampId } = request.params;
    const body = request.body;
    
    try {
      // Convert API model to domain entity
      const lampEntityUpdate = LampMapper.toDomainEntityUpdate(body);
      const updatedLampEntity = await this.repository.update(lampId, lampEntityUpdate);
      
      // Convert domain entity to API model
      const updatedLamp = LampMapper.toApiModel(updatedLampEntity);
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
