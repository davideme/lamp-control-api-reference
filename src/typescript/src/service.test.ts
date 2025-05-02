import { Service } from './service';
import { InMemoryLampRepository } from './repository';
import type { components } from './types/api';
import type { FastifyRequest, FastifyReply } from 'fastify';

type Lamp = components['schemas']['Lamp'];

// Mock Fastify types
type MockFastifyRequest<T = unknown> = Partial<FastifyRequest> & T;
type MockFastifyReply = Partial<FastifyReply> & {
    code: jest.Mock;
    send: jest.Mock;
};

describe('Service', () => {
    let service: Service;
    let repository: InMemoryLampRepository;
    let mockReply: MockFastifyReply;

    beforeEach(() => {
        repository = new InMemoryLampRepository();
        service = new Service(repository);
        mockReply = {
            code: jest.fn().mockReturnThis(),
            send: jest.fn().mockReturnThis(),
        };
    });

    describe('listLamps', () => {
        it('should return empty array when no lamps exist', async () => {
            // Arrange
            const mockRequest: MockFastifyRequest<{ query: { limit?: string } }> = { query: {} };

            // Act
            const result = await service.listLamps(mockRequest as any, mockReply as any);

            // Assert
            expect(result).toEqual([]);
        });

        it('should return all lamps when no limit is specified', async () => {
            // Arrange
            const mockRequest: MockFastifyRequest<{ query: { limit?: string } }> = { query: {} };
            const lamp1: Lamp = { id: '1', status: true };
            const lamp2: Lamp = { id: '2', status: false };
            repository.create({ status: true });
            repository.create({ status: false });

            // Act
            const result = await service.listLamps(mockRequest as any, mockReply as any);

            // Assert
            expect(result).toHaveLength(2);
            expect(result[0].status).toBe(true);
            expect(result[1].status).toBe(false);
        });

        it('should return limited number of lamps when limit is specified', async () => {
            // Arrange
            const mockRequest: MockFastifyRequest<{ query: { limit?: string } }> = { query: { limit: '1' } };
            repository.create({ status: true });
            repository.create({ status: false });

            // Act
            const result = await service.listLamps(mockRequest as any, mockReply as any);

            // Assert
            expect(result).toHaveLength(1);
        });
    });

    describe('getLamp', () => {
        it('should return lamp when it exists', async () => {
            // Arrange
            const lamp = repository.create({ status: true });
            const mockRequest: MockFastifyRequest<{ params: { lampId: string } }> = { 
                params: { lampId: lamp.id } 
            };

            // Act
            const result = await service.getLamp(mockRequest as any, mockReply as any);

            // Assert
            expect(result).toEqual(lamp);
        });

        it('should throw 404 error when lamp does not exist', async () => {
            // Arrange
            const mockRequest: MockFastifyRequest<{ params: { lampId: string } }> = { params: { lampId: 'nonexistent' } };

            // Act & Assert
            await expect(service.getLamp(mockRequest as any, mockReply as any)).rejects.toEqual({
                statusCode: 404,
                message: 'Lamp not found'
            });
        });
    });

    describe('createLamp', () => {
        it('should create a new lamp with generated UUID', async () => {
            // Arrange
            const mockRequest: MockFastifyRequest<{ body: { status: boolean } }> = { body: { status: true } };
            let sentLamp: Lamp | undefined;

            // Capture the lamp that was sent
            mockReply.send.mockImplementation((lamp) => {
                sentLamp = lamp as Lamp;
                return mockReply;
            });

            // Act
            await service.createLamp(mockRequest as any, mockReply as any);

            // Assert
            expect(sentLamp).toBeDefined();
            expect(sentLamp!.id).toBeDefined();
            expect(sentLamp!.status).toBe(true);
            expect(mockReply.code).toHaveBeenCalledWith(201);
            expect(mockReply.send).toHaveBeenCalledWith(sentLamp);
            expect(repository.findById(sentLamp!.id)).toEqual(sentLamp);
        });
    });

    describe('updateLamp', () => {
        it('should update existing lamp', async () => {
            // Arrange
            const lamp = repository.create({ status: true });
            const mockRequest: MockFastifyRequest<{ params: { lampId: string }; body: { status: boolean } }> = {
                params: { lampId: lamp.id },
                body: { status: false }
            };

            // Act
            const result = await service.updateLamp(mockRequest as any, mockReply as any);

            // Assert
            expect(result).toEqual({ id: lamp.id, status: false });
            expect(repository.findById(lamp.id)).toEqual({ id: lamp.id, status: false });
        });

        it('should throw 404 error when lamp does not exist', async () => {
            // Arrange
            const mockRequest: MockFastifyRequest<{ params: { lampId: string }; body: { status: boolean } }> = {
                params: { lampId: 'nonexistent' },
                body: { status: false }
            };

            // Act & Assert
            await expect(service.updateLamp(mockRequest as any, mockReply as any)).rejects.toEqual({
                statusCode: 404,
                message: 'Lamp not found'
            });
        });
    });

    describe('deleteLamp', () => {
        it('should delete existing lamp', async () => {
            // Arrange
            const lamp = repository.create({ status: true });
            const mockRequest: MockFastifyRequest<{ params: { lampId: string } }> = { params: { lampId: lamp.id } };

            // Act
            await service.deleteLamp(mockRequest as any, mockReply as any);

            // Assert
            expect(repository.findById(lamp.id)).toBeUndefined();
            expect(mockReply.code).toHaveBeenCalledWith(204);
            expect(mockReply.send).toHaveBeenCalled();
        });

        it('should throw 404 error when lamp does not exist', async () => {
            // Arrange
            const mockRequest: MockFastifyRequest<{ params: { lampId: string } }> = { params: { lampId: 'nonexistent' } };

            // Act & Assert
            await expect(service.deleteLamp(mockRequest as any, mockReply as any)).rejects.toEqual({
                statusCode: 404,
                message: 'Lamp not found'
            });
        });
    });
}); 