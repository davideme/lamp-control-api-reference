/**
 * @jest-environment node
 */
import { jest } from '@jest/globals';
import { LampNotFoundError } from '../../domain/errors/lamp-not-found.error';
import { LampRepository } from '../../domain/repositories/lamp.repository';
import { Lamp, LampCreate, LampUpdate } from '../../domain/models/lamp';
import { Service } from './service';
import type { FastifyRequest, FastifyReply } from 'fastify';

// Mock Fastify types
type MockFastifyRequest<T = unknown> = Partial<FastifyRequest> & T;
type MockFastifyReply = Partial<FastifyReply> & {
  code: jest.Mock;
  send: jest.Mock;
};

// Mock repository
const mockRepository = {
  findAll: jest.fn<(limit?: number) => Lamp[]>(),
  findById: jest.fn<(id: string) => Lamp | undefined>(),
  create: jest.fn<(data: LampCreate) => Lamp>(),
  update: jest.fn<(id: string, data: LampUpdate) => Lamp>(),
  delete: jest.fn<(id: string) => void>(),
} as jest.Mocked<LampRepository>;

describe('Service', () => {
  let service: Service;
  let mockReply: MockFastifyReply;

  beforeEach(() => {
    service = new Service(mockRepository);
    mockReply = {
      code: jest.fn().mockReturnThis() as any,
      send: jest.fn().mockReturnThis() as any,
    };
    jest.clearAllMocks();
  });

  describe('listLamps', () => {
    it('should return empty array when no lamps exist', async () => {
      // Arrange
      const mockRequest: MockFastifyRequest<{ query: { limit?: string } }> = {
        query: {},
      };
      mockRepository.findAll.mockReturnValue([]);

      // Act
      const result = await service.listLamps(mockRequest as any, mockReply as any);

      // Assert
      expect(result).toEqual([]);
      expect(mockRepository.findAll).toHaveBeenCalledWith(undefined);
    });

    it('should return all lamps when no limit is specified', async () => {
      // Arrange
      const mockRequest: MockFastifyRequest<{ query: { limit?: string } }> = {
        query: {},
      };
      const lamps: Lamp[] = [
        { id: '1', status: true },
        { id: '2', status: false },
      ];
      mockRepository.findAll.mockReturnValue(lamps);

      // Act
      const result = await service.listLamps(mockRequest as any, mockReply as any);

      // Assert
      expect(result).toEqual(lamps);
      expect(mockRepository.findAll).toHaveBeenCalledWith(undefined);
    });

    it('should return limited number of lamps when limit is specified', async () => {
      // Arrange
      const mockRequest: MockFastifyRequest<{ query: { limit?: string } }> = {
        query: { limit: '1' },
      };
      const lamps: Lamp[] = [
        { id: '1', status: true },
        { id: '2', status: false },
      ];
      mockRepository.findAll.mockReturnValue([lamps[0]]);

      // Act
      const result = await service.listLamps(mockRequest as any, mockReply as any);

      // Assert
      expect(result).toEqual([lamps[0]]);
      expect(mockRepository.findAll).toHaveBeenCalledWith(1);
    });
  });

  describe('getLamp', () => {
    it('should return lamp when it exists', async () => {
      // Arrange
      const lamp: Lamp = { id: '1', status: true };
      const mockRequest: MockFastifyRequest<{ params: { lampId: string } }> = {
        params: { lampId: lamp.id },
      };
      mockRepository.findById.mockReturnValue(lamp);

      // Act
      const result = await service.getLamp(mockRequest as any, mockReply as any);

      // Assert
      expect(result).toEqual(lamp);
      expect(mockRepository.findById).toHaveBeenCalledWith(lamp.id);
    });

    it('should return 404 when lamp does not exist', async () => {
      // Arrange
      const mockRequest: MockFastifyRequest<{ params: { lampId: string } }> = {
        params: { lampId: 'nonexistent' },
      };
      mockRepository.findById.mockReturnValue(undefined);

      // Act
      await service.getLamp(mockRequest as any, mockReply as any);

      // Assert
      expect(mockRepository.findById).toHaveBeenCalledWith('nonexistent');
      expect(mockReply.code).toHaveBeenCalledWith(404);
      expect(mockReply.send).toHaveBeenCalled();
    });
  });

  describe('createLamp', () => {
    it('should create a new lamp', async () => {
      // Arrange
      const newLamp: Lamp = { id: '1', status: true };
      const mockRequest: MockFastifyRequest<{ body: { status: boolean } }> = {
        body: { status: true },
      };
      mockRepository.create.mockReturnValue(newLamp);

      // Act
      const result = await service.createLamp(mockRequest as any, mockReply as any);

      // Assert
      expect(result).toEqual(newLamp);
      expect(mockRepository.create).toHaveBeenCalledWith({ status: true });
      expect(mockReply.code).toHaveBeenCalledWith(201);
      expect(mockReply.send).toHaveBeenCalledWith(newLamp);
    });
  });

  describe('updateLamp', () => {
    it('should update existing lamp', async () => {
      // Arrange
      const updatedLamp: Lamp = { id: '1', status: false };
      const mockRequest: MockFastifyRequest<{
        params: { lampId: string };
        body: { status: boolean };
      }> = {
        params: { lampId: '1' },
        body: { status: false },
      };
      mockRepository.update.mockReturnValue(updatedLamp);

      // Act
      const result = await service.updateLamp(mockRequest as any, mockReply as any);

      // Assert
      expect(result).toEqual(updatedLamp);
      expect(mockRepository.update).toHaveBeenCalledWith('1', {
        status: false,
      });
    });

    it('should return 404 when lamp does not exist', async () => {
      // Arrange
      const mockRequest: MockFastifyRequest<{
        params: { lampId: string };
        body: { status: boolean };
      }> = {
        params: { lampId: 'nonexistent' },
        body: { status: false },
      };
      mockRepository.update.mockImplementation(() => {
        throw new LampNotFoundError('Lamp not found');
      });

      // Act
      await service.updateLamp(mockRequest as any, mockReply as any);

      // Assert
      expect(mockRepository.update).toHaveBeenCalledWith('nonexistent', {
        status: false,
      });
      expect(mockReply.code).toHaveBeenCalledWith(404);
      expect(mockReply.send).toHaveBeenCalled();
    });
  });

  describe('deleteLamp', () => {
    it('should delete existing lamp', async () => {
      // Arrange
      const mockRequest: MockFastifyRequest<{ params: { lampId: string } }> = {
        params: { lampId: '1' },
      };
      mockRepository.delete.mockImplementation(() => {});

      // Act
      await service.deleteLamp(mockRequest as any, mockReply as any);

      // Assert
      expect(mockRepository.delete).toHaveBeenCalledWith('1');
      expect(mockReply.code).toHaveBeenCalledWith(204);
      expect(mockReply.send).toHaveBeenCalled();
    });

    it('should return 404 when lamp does not exist', async () => {
      // Arrange
      const mockRequest: MockFastifyRequest<{ params: { lampId: string } }> = {
        params: { lampId: 'nonexistent' },
      };
      mockRepository.delete.mockImplementation(() => {
        throw new LampNotFoundError('Lamp not found');
      });

      // Act
      await service.deleteLamp(mockRequest as any, mockReply as any);

      // Assert
      expect(mockRepository.delete).toHaveBeenCalledWith('nonexistent');
      expect(mockReply.code).toHaveBeenCalledWith(404);
      expect(mockReply.send).toHaveBeenCalled();
    });
  });
});
