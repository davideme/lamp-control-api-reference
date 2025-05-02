import { FastifyInstance } from 'fastify';
import { LampService } from '../../domain/services/lamp.service';
import { LampCreate, LampUpdate } from '../../domain/models/lamp';

export function registerLampRoutes(app: FastifyInstance, service: LampService) {
    // List all lamps
    app.get('/lamps', async (request, reply) => {
        const { limit } = request.query as { limit?: string };
        const limitNumber = limit ? parseInt(limit) : undefined;
        const lamps = await service.listLamps(limitNumber);
        return reply.send(lamps);
    });

    // Get a specific lamp
    app.get('/lamps/:lampId', async (request, reply) => {
        const { lampId } = request.params as { lampId: string };
        try {
            const lamp = await service.getLamp(lampId);
            return reply.send(lamp);
        } catch (error) {
            return reply.code(404).send();
        }
    });

    // Create a new lamp
    app.post('/lamps', async (request, reply) => {
        const body = request.body as LampCreate;
        const lamp = await service.createLamp(body);
        return reply.code(201).send(lamp);
    });

    // Update a lamp
    app.put('/lamps/:lampId', async (request, reply) => {
        const { lampId } = request.params as { lampId: string };
        const body = request.body as LampUpdate;
        try {
            const lamp = await service.updateLamp(lampId, body);
            return reply.send(lamp);
        } catch (error) {
            return reply.code(404).send();
        }
    });

    // Delete a lamp
    app.delete('/lamps/:lampId', async (request, reply) => {
        const { lampId } = request.params as { lampId: string };
        try {
            await service.deleteLamp(lampId);
            return reply.code(204).send();
        } catch (error) {
            return reply.code(404).send();
        }
    });
} 