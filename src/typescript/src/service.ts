import type { components, operations } from './types/api';
import type { FastifyRequest, FastifyReply } from 'fastify';
import { LampRepository } from './repository';

type Lamp = components['schemas']['Lamp'];
type LampCreate = components['schemas']['LampCreate'];
type LampUpdate = components['schemas']['LampUpdate'];

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

    async listLamps(
        request: ListLampsRequest,
        reply: ListLampsReply
    ): Promise<Lamp[]> {
        const { limit } = request.query;
        const limitNumber = limit ? parseInt(limit) : undefined;
        return this.repository.findAll(limitNumber);
    }

    async getLamp(
        request: GetLampRequest,
        reply: GetLampReply
    ): Promise<Lamp> {
        const { lampId } = request.params;
        const lamp = this.repository.findById(lampId);
        
        if (!lamp) {
            throw { statusCode: 404, message: 'Lamp not found' };
        }
        
        return lamp;
    }

    async createLamp(
        request: CreateLampRequest,
        reply: CreateLampReply
    ): Promise<Lamp> {
        const body = request.body;
        const newLamp = this.repository.create({ status: body.status });
        return reply.code(201).send(newLamp);
    }

    async updateLamp(
        request: UpdateLampRequest,
        reply: UpdateLampReply
    ): Promise<Lamp> {
        const { lampId } = request.params;
        const body = request.body;
        return this.repository.update(lampId, { status: body.status });
    }

    async deleteLamp(
        request: DeleteLampRequest,
        reply: DeleteLampReply
    ): Promise<void> {
        const { lampId } = request.params;
        this.repository.delete(lampId);
        reply.code(204).send();
    }
}

export default Service;