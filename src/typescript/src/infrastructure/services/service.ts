import type { FastifyReply } from 'fastify';
import type { LampRepository } from '../../domain/repositories/LampRepository.ts';
import { toApiModel, toDomainEntityCreate, toDomainEntityUpdate } from '../mappers/LampMapper.ts';
import type {
  ListLampsRequest,
  ListLampsResponse,
  GetLampRequest,
  CreateLampRequest,
  UpdateLampRequest,
  DeleteLampRequest,
} from './types.ts';

// service.ts
export class Service {
  private readonly repository: LampRepository;

  constructor(repository: LampRepository) {
    this.repository = repository;
  }

  async listLamps(request: ListLampsRequest, reply: FastifyReply): Promise<void> {
    const { pageSize = 25 } = request.query;
    const lampEntities = await this.repository.findAll(pageSize);

    // Convert domain entities to API models
    const lamps = lampEntities.map(toApiModel);

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
    const lamp = toApiModel(lampEntity);
    return reply.code(200).send(lamp);
  }

  async createLamp(request: CreateLampRequest, reply: FastifyReply): Promise<void> {
    const body = request.body;

    // Convert API model to domain entity
    const lampEntityCreate = toDomainEntityCreate(body);
    const newLampEntity = await this.repository.create(lampEntityCreate);

    // Convert domain entity to API model
    const newLamp = toApiModel(newLampEntity);
    return reply.code(201).send(newLamp);
  }

  async updateLamp(request: UpdateLampRequest, reply: FastifyReply): Promise<void> {
    const { lampId } = request.params;
    const body = request.body;

    // Convert API model to domain entity
    const lampEntityUpdate = toDomainEntityUpdate(body);
    const updatedLampEntity = await this.repository.update(lampId, lampEntityUpdate);

    // Convert domain entity to API model
    const updatedLamp = toApiModel(updatedLampEntity);
    return reply.code(200).send(updatedLamp);
  }

  async deleteLamp(request: DeleteLampRequest, reply: FastifyReply): Promise<void> {
    const { lampId } = request.params;
    await this.repository.delete(lampId);
    return reply.code(204).send();
  }
}

export default Service;
