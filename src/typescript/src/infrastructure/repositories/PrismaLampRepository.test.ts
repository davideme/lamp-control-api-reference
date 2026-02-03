import { describe, it, expect, jest, beforeEach } from '@jest/globals';
import { PrismaClient, type Lamp as PrismaLamp } from '@prisma/client';
import { PrismaClientKnownRequestError } from '@prisma/client/runtime/library';
import { PrismaLampRepository } from './PrismaLampRepository.js';
import { LampNotFoundError } from '../../domain/errors/DomainError.js';

describe('PrismaLampRepository', () => {
  let mockPrisma: jest.Mocked<PrismaClient>;
  let repository: PrismaLampRepository;

  beforeEach(() => {
    // Create a mock Prisma client
    mockPrisma = {
      lamp: {
        findMany: jest.fn(),
        findUnique: jest.fn(),
        create: jest.fn(),
        update: jest.fn(),
      },
    } as unknown as jest.Mocked<PrismaClient>;

    repository = new PrismaLampRepository(mockPrisma);
  });

  describe('findAll', () => {
    it('should return all lamps', async () => {
      const mockLamps: PrismaLamp[] = [
        {
          id: '1',
          isOn: true,
          createdAt: new Date('2024-01-01'),
          updatedAt: new Date('2024-01-01'),
          deletedAt: null,
        },
        {
          id: '2',
          isOn: false,
          createdAt: new Date('2024-01-02'),
          updatedAt: new Date('2024-01-02'),
          deletedAt: null,
        },
      ];

      mockPrisma.lamp.findMany.mockResolvedValue(mockLamps);

      const result = await repository.findAll();

      expect(result).toHaveLength(2);
      expect(result[0].id).toBe('1');
      expect(result[0].status).toBe(true);
      expect(result[1].id).toBe('2');
      expect(result[1].status).toBe(false);
      expect(mockPrisma.lamp.findMany).toHaveBeenCalledWith({
        where: { deletedAt: null },
        orderBy: { createdAt: 'asc' },
        take: undefined,
      });
    });

    it('should return lamps with limit', async () => {
      const mockLamps: PrismaLamp[] = [
        {
          id: '1',
          isOn: true,
          createdAt: new Date('2024-01-01'),
          updatedAt: new Date('2024-01-01'),
          deletedAt: null,
        },
      ];

      mockPrisma.lamp.findMany.mockResolvedValue(mockLamps);

      const result = await repository.findAll(5);

      expect(result).toHaveLength(1);
      expect(mockPrisma.lamp.findMany).toHaveBeenCalledWith({
        where: { deletedAt: null },
        orderBy: { createdAt: 'asc' },
        take: 5,
      });
    });
  });

  describe('findById', () => {
    it('should return lamp when found', async () => {
      const mockLamp: PrismaLamp = {
        id: '1',
        isOn: true,
        createdAt: new Date('2024-01-01'),
        updatedAt: new Date('2024-01-01'),
        deletedAt: null,
      };

      mockPrisma.lamp.findUnique.mockResolvedValue(mockLamp);

      const result = await repository.findById('1');

      expect(result).toBeDefined();
      expect(result?.id).toBe('1');
      expect(result?.status).toBe(true);
      expect(mockPrisma.lamp.findUnique).toHaveBeenCalledWith({
        where: { id: '1', deletedAt: null },
      });
    });

    it('should return undefined when lamp not found', async () => {
      mockPrisma.lamp.findUnique.mockResolvedValue(null);

      const result = await repository.findById('nonexistent');

      expect(result).toBeUndefined();
    });
  });

  describe('create', () => {
    it('should create a new lamp', async () => {
      const mockLamp: PrismaLamp = {
        id: '1',
        isOn: true,
        createdAt: new Date('2024-01-01'),
        updatedAt: new Date('2024-01-01'),
        deletedAt: null,
      };

      mockPrisma.lamp.create.mockResolvedValue(mockLamp);

      const result = await repository.create({ status: true });

      expect(result.id).toBe('1');
      expect(result.status).toBe(true);
      expect(mockPrisma.lamp.create).toHaveBeenCalledWith({
        data: { isOn: true },
      });
    });
  });

  describe('update', () => {
    it('should update lamp status', async () => {
      const mockLamp: PrismaLamp = {
        id: '1',
        isOn: false,
        createdAt: new Date('2024-01-01'),
        updatedAt: new Date('2024-01-02'),
        deletedAt: null,
      };

      mockPrisma.lamp.update.mockResolvedValue(mockLamp);

      const result = await repository.update('1', { status: false });

      expect(result.status).toBe(false);
      expect(mockPrisma.lamp.update).toHaveBeenCalledWith({
        where: { id: '1', deletedAt: null },
        data: { isOn: false },
      });
    });

    it('should throw LampNotFoundError when lamp not found', async () => {
      const error = new PrismaClientKnownRequestError('Record not found', {
        code: 'P2025',
        clientVersion: '5.0.0',
      });

      mockPrisma.lamp.update.mockRejectedValue(error);

      await expect(repository.update('nonexistent', { status: true })).rejects.toThrow(
        LampNotFoundError,
      );
    });

    it('should rethrow other errors', async () => {
      const error = new Error('Database connection error');
      mockPrisma.lamp.update.mockRejectedValue(error);

      await expect(repository.update('1', { status: true })).rejects.toThrow(
        'Database connection error',
      );
    });
  });

  describe('delete', () => {
    it('should soft delete lamp', async () => {
      const mockLamp: PrismaLamp = {
        id: '1',
        isOn: true,
        createdAt: new Date('2024-01-01'),
        updatedAt: new Date('2024-01-02'),
        deletedAt: new Date('2024-01-02'),
      };

      mockPrisma.lamp.update.mockResolvedValue(mockLamp);

      await repository.delete('1');

      expect(mockPrisma.lamp.update).toHaveBeenCalledWith({
        where: { id: '1', deletedAt: null },
        data: { deletedAt: expect.any(Date) },
      });
    });

    it('should throw LampNotFoundError when lamp not found', async () => {
      const error = new PrismaClientKnownRequestError('Record not found', {
        code: 'P2025',
        clientVersion: '5.0.0',
      });

      mockPrisma.lamp.update.mockRejectedValue(error);

      await expect(repository.delete('nonexistent')).rejects.toThrow(LampNotFoundError);
    });

    it('should rethrow other errors', async () => {
      const error = new Error('Database connection error');
      mockPrisma.lamp.update.mockRejectedValue(error);

      await expect(repository.delete('1')).rejects.toThrow('Database connection error');
    });
  });
});
