/**
 * @jest-environment node
 */
import { jest } from '@jest/globals';
import { LampRepository } from '../../domain/repositories/LampRepository';
import { LampEntity, LampEntityCreate, LampEntityUpdate } from '../../domain/entities/LampEntity';
import { Service } from './service';
import type { FastifyRequest, FastifyReply } from 'fastify';
import { LampNotFoundError } from '../../domain/errors/DomainError';

// Mock Fastify types
type MockFastifyRequest<T = unknown> = Partial<FastifyRequest> & T;
type MockFastifyReply = Partial<FastifyReply>;

// Mock repository
const mockRepository = {
  findAll: jest.fn<(limit?: number) => Promise<LampEntity[]>>(),
  findById: jest.fn<(id: string) => Promise<LampEntity | undefined>>(),
  create: jest.fn<(data: LampEntityCreate) => Promise<LampEntity>>(),
  update: jest.fn<(id: string, data: LampEntityUpdate) => Promise<LampEntity>>(),
  delete: jest.fn<(id: string) => Promise<void>>(),
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
      const mockRequest: MockFastifyRequest<{
        query: { cursor?: string | null; pageSize?: number };
      }> = {
        query: {},
      };
      mockRepository.findAll.mockResolvedValue([]);

      // Act
      await service.listLamps(mockRequest as any, mockReply as any);

      // Assert
      expect(mockReply.code).toHaveBeenCalledWith(200);
      expect(mockReply.send).toHaveBeenCalledWith({
        data: [],
        hasMore: false,
        nextCursor: null,
      });
      expect(mockRepository.findAll).toHaveBeenCalledWith(25);
    });

    it('should return all lamps when no limit is specified', async () => {
      // Arrange
      const mockRequest: MockFastifyRequest<{
        query: { cursor?: string | null; pageSize?: number };
      }> = {
        query: {},
      };
      const lampEntities: LampEntity[] = [
        {
          id: '1',
          status: true,
          createdAt: '2023-01-01T00:00:00.000Z',
          updatedAt: '2023-01-01T00:00:00.000Z',
        },
        {
          id: '2',
          status: false,
          createdAt: '2023-01-01T00:00:00.000Z',
          updatedAt: '2023-01-01T00:00:00.000Z',
        },
      ];
      mockRepository.findAll.mockResolvedValue(lampEntities);

      // Act
      await service.listLamps(mockRequest as any, mockReply as any);

      // Assert
      expect(mockReply.code).toHaveBeenCalledWith(200);
      expect(mockReply.send).toHaveBeenCalledWith({
        data: lampEntities,
        hasMore: false,
        nextCursor: null,
      });
      expect(mockRepository.findAll).toHaveBeenCalledWith(25);
    });

    it('should return limited number of lamps when pageSize is specified', async () => {
      // Arrange
      const mockRequest: MockFastifyRequest<{
        query: { cursor?: string | null; pageSize?: number };
      }> = {
        query: { pageSize: 1 },
      };
      const lampEntities: LampEntity[] = [
        {
          id: '1',
          status: true,
          createdAt: '2023-01-01T00:00:00.000Z',
          updatedAt: '2023-01-01T00:00:00.000Z',
        },
      ];
      mockRepository.findAll.mockResolvedValue(lampEntities);

      // Act
      await service.listLamps(mockRequest as any, mockReply as any);

      // Assert
      expect(mockReply.code).toHaveBeenCalledWith(200);
      expect(mockReply.send).toHaveBeenCalledWith({
        data: lampEntities,
        hasMore: false,
        nextCursor: null,
      });
      expect(mockRepository.findAll).toHaveBeenCalledWith(1);
    });
  });

  describe('getLamp', () => {
    it('should return lamp when it exists', async () => {
      // Arrange
      const lampEntity: LampEntity = {
        id: '1',
        status: true,
        createdAt: '2023-01-01T00:00:00.000Z',
        updatedAt: '2023-01-01T00:00:00.000Z',
      };
      const mockRequest: MockFastifyRequest<{ params: { lampId: string } }> = {
        params: { lampId: lampEntity.id },
      };
      mockRepository.findById.mockResolvedValue(lampEntity);

      // Act
      await service.getLamp(mockRequest as any, mockReply as any);

      // Assert
      expect(mockRepository.findById).toHaveBeenCalledWith(lampEntity.id);
      expect(mockReply.code).toHaveBeenCalledWith(200);
      expect(mockReply.send).toHaveBeenCalledWith(lampEntity);
    });

    it('should return 404 when lamp does not exist', async () => {
      // Arrange
      const mockRequest: MockFastifyRequest<{ params: { lampId: string } }> = {
        params: { lampId: 'nonexistent' },
      };
      mockRepository.findById.mockResolvedValue(undefined);

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
      const newLampEntity: LampEntity = {
        id: '1',
        status: true,
        createdAt: '2023-01-01T00:00:00.000Z',
        updatedAt: '2023-01-01T00:00:00.000Z',
      };
      const mockRequest: MockFastifyRequest<{ body: { status: boolean } }> = {
        body: { status: true },
      };
      mockRepository.create.mockResolvedValue(newLampEntity);

      // Act
      await service.createLamp(mockRequest as any, mockReply as any);

      // Assert
      expect(mockRepository.create).toHaveBeenCalledWith({ status: true });
      expect(mockReply.code).toHaveBeenCalledWith(201);
      expect(mockReply.send).toHaveBeenCalledWith(newLampEntity);
    });
  });

  describe('updateLamp', () => {
    it('should update existing lamp', async () => {
      // Arrange
      const updatedLampEntity: LampEntity = {
        id: '1',
        status: false,
        createdAt: '2023-01-01T00:00:00.000Z',
        updatedAt: '2023-01-01T00:01:00.000Z',
      };
      const mockRequest: MockFastifyRequest<{
        params: { lampId: string };
        body: { status: boolean };
      }> = {
        params: { lampId: '1' },
        body: { status: false },
      };
      mockRepository.update.mockResolvedValue(updatedLampEntity);

      // Act
      await service.updateLamp(mockRequest as any, mockReply as any);

      // Assert
      expect(mockRepository.update).toHaveBeenCalledWith('1', {
        status: false,
      });
      expect(mockReply.code).toHaveBeenCalledWith(200);
      expect(mockReply.send).toHaveBeenCalledWith(updatedLampEntity);
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
      mockRepository.update.mockRejectedValue(new LampNotFoundError('nonexistent'));

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
      mockRepository.delete.mockResolvedValue(undefined);

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
      mockRepository.delete.mockRejectedValue(new LampNotFoundError('nonexistent'));

      // Act
      await service.deleteLamp(mockRequest as any, mockReply as any);

      // Assert
      expect(mockRepository.delete).toHaveBeenCalledWith('nonexistent');
      expect(mockReply.code).toHaveBeenCalledWith(404);
      expect(mockReply.send).toHaveBeenCalled();
    });
  });
});
