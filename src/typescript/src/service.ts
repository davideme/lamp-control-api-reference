import type { components } from './types/api';
import type { FastifyRequest, FastifyReply } from 'fastify';

type Lamp = components['schemas']['Lamp'];
type LampCreate = components['schemas']['LampCreate'];
type LampUpdate = components['schemas']['LampUpdate'];

// service.ts
export class Service {
    private lamps: Map<string, Lamp> = new Map();

    async listLamps(
        request: FastifyRequest<{ Querystring: { limit?: string } }>,
        reply: FastifyReply
    ): Promise<Lamp[]> {
        const { limit } = request.query;
        let result = Array.from(this.lamps.values());
        
        if (limit && !isNaN(parseInt(limit))) {
            result = result.slice(0, parseInt(limit));
        }
        
        return result;
    }

    async getLamp(
        request: FastifyRequest<{ Params: { lampId: string } }>,
        reply: FastifyReply
    ): Promise<Lamp | undefined> {
        const { lampId } = request.params;
        const lamp = this.lamps.get(lampId);
        
        if (!lamp) {
            return reply.code(404).send({ error: 'Lamp not found' });
        }
        
        return lamp;
    }

    async createLamp(
        request: FastifyRequest<{ Body: LampCreate }>,
        reply: FastifyReply
    ): Promise<Lamp> {
        const body = request.body as LampCreate;
        const newLamp: Lamp = {
            id: crypto.randomUUID(),
            status: body.status
        };
        
        this.lamps.set(newLamp.id, newLamp);
        return reply.code(201).send(newLamp);
    }

    async updateLamp(
        request: FastifyRequest<{ 
            Params: { lampId: string };
            Body: LampUpdate;
        }>,
        reply: FastifyReply
    ): Promise<Lamp | undefined> {
        const { lampId } = request.params;
        const body = request.body as LampUpdate;
        const lamp = this.lamps.get(lampId);
        
        if (!lamp) {
            return reply.code(404).send({ error: 'Lamp not found' });
        }
        
        const updatedLamp: Lamp = {
            ...lamp,
            status: body.status
        };
        
        this.lamps.set(lampId, updatedLamp);
        return updatedLamp;
    }

    async deleteLamp(
        request: FastifyRequest<{ Params: { lampId: string } }>,
        reply: FastifyReply
    ): Promise<void> {
        const { lampId } = request.params;
        
        if (!this.lamps.has(lampId)) {
            return reply.code(404).send({ error: 'Lamp not found' });
        }
        
        this.lamps.delete(lampId);
        return reply.code(204).send();
    }
}

export default Service;