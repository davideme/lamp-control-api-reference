import { LampNotFoundError } from '../../domain/errors/DomainError.js';
import { LampMapper } from '../mappers/LampMapper.js';
// service.ts
export class Service {
    repository;
    constructor(repository) {
        this.repository = repository;
    }
    async listLamps(request, reply) {
        const { pageSize = 25 } = request.query;
        const lampEntities = await this.repository.findAll(pageSize);
        // Convert domain entities to API models
        const lamps = lampEntities.map(LampMapper.toApiModel);
        // Simple implementation without actual cursor-based pagination
        // In a real implementation, you'd use the cursor to fetch from a specific position
        const response = {
            data: lamps,
            hasMore: false, // Simple implementation assumes no more pages
            nextCursor: null,
        };
        return reply.code(200).send(response);
    }
    async getLamp(request, reply) {
        const { lampId } = request.params;
        const lampEntity = await this.repository.findById(lampId);
        if (!lampEntity) {
            return reply.code(404).send();
        }
        // Convert domain entity to API model
        const lamp = LampMapper.toApiModel(lampEntity);
        return reply.code(200).send(lamp);
    }
    async createLamp(request, reply) {
        const body = request.body;
        // Convert API model to domain entity
        const lampEntityCreate = LampMapper.toDomainEntityCreate(body);
        const newLampEntity = await this.repository.create(lampEntityCreate);
        // Convert domain entity to API model
        const newLamp = LampMapper.toApiModel(newLampEntity);
        return reply.code(201).send(newLamp);
    }
    async updateLamp(request, reply) {
        const { lampId } = request.params;
        const body = request.body;
        try {
            // Convert API model to domain entity
            const lampEntityUpdate = LampMapper.toDomainEntityUpdate(body);
            const updatedLampEntity = await this.repository.update(lampId, lampEntityUpdate);
            // Convert domain entity to API model
            const updatedLamp = LampMapper.toApiModel(updatedLampEntity);
            return reply.code(200).send(updatedLamp);
        }
        catch (error) {
            if (error instanceof LampNotFoundError) {
                return reply.code(404).send();
            }
            throw error;
        }
    }
    async deleteLamp(request, reply) {
        const { lampId } = request.params;
        try {
            await this.repository.delete(lampId);
            return reply.code(204).send();
        }
        catch (error) {
            if (error instanceof LampNotFoundError) {
                return reply.code(404).send();
            }
            throw error;
        }
    }
}
export default Service;
