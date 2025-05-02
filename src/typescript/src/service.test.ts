import { Service } from './service';
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
    let mockReply: MockFastifyReply;

    beforeEach(() => {
        service = new Service();
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
            service['lamps'].set('1', lamp1);
            service['lamps'].set('2', lamp2);

            // Act
            const result = await service.listLamps(mockRequest as any, mockReply as any);

            // Assert
            expect(result).toEqual([lamp1, lamp2]);
        });

        it('should return limited number of lamps when limit is specified', async () => {
            // Arrange
            const mockRequest: MockFastifyRequest<{ query: { limit?: string } }> = { query: { limit: '1' } };
            const lamp1: Lamp = { id: '1', status: true };
            const lamp2: Lamp = { id: '2', status: false };
            service['lamps'].set('1', lamp1);
            service['lamps'].set('2', lamp2);

            // Act
            const result = await service.listLamps(mockRequest as any, mockReply as any);

            // Assert
            expect(result).toEqual([lamp1]);
        });
    });

    describe('getLamp', () => {
        it('should return lamp when it exists', async () => {
            // Arrange
            const mockRequest: MockFastifyRequest<{ params: { lampId: string } }> = { params: { lampId: '1' } };
            const lamp: Lamp = { id: '1', status: true };
            service['lamps'].set('1', lamp);

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
            expect(service['lamps'].get(sentLamp!.id)).toEqual(sentLamp);
        });
    });

    describe('updateLamp', () => {
        it('should update existing lamp', async () => {
            // Arrange
            const mockRequest: MockFastifyRequest<{ params: { lampId: string }; body: { status: boolean } }> = {
                params: { lampId: '1' },
                body: { status: false }
            };
            const existingLamp: Lamp = { id: '1', status: true };
            service['lamps'].set('1', existingLamp);

            // Act
            const result = await service.updateLamp(mockRequest as any, mockReply as any);

            // Assert
            expect(result).toEqual({ id: '1', status: false });
            expect(service['lamps'].get('1')).toEqual({ id: '1', status: false });
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
            const mockRequest: MockFastifyRequest<{ params: { lampId: string } }> = { params: { lampId: '1' } };
            const lamp: Lamp = { id: '1', status: true };
            service['lamps'].set('1', lamp);

            // Act
            await service.deleteLamp(mockRequest as any, mockReply as any);

            // Assert
            expect(service['lamps'].has('1')).toBe(false);
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