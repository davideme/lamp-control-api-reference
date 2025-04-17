// filepath: /Users/davide/Documents/GitHub/lamp/lamp-control-api-reference/src/typescript/src/infrastructure/repositories/__tests__/PostgreSQLLampRepository.test.ts
import { PostgreSQLLampRepository } from '../PostgreSQLLampRepository';
import { Lamp } from '../../../domain/models/Lamp';
import { PrismaClient } from '@prisma/client';
// Import jest explicitly to ensure proper detection
import '@jest/globals';

// Mock the Prisma Client
jest.mock('@prisma/client', () => {
  const mockPrismaClient = {
    lamp: {
      upsert: jest.fn(),
      findFirst: jest.fn(),
      findMany: jest.fn(),
      update: jest.fn(),
      deleteMany: jest.fn(),
    },
    $disconnect: jest.fn(),
  };
  
  return {
    PrismaClient: jest.fn(() => mockPrismaClient),
  };
});

/**
 * Test suite for PostgreSQLLampRepository
 * 
 * These tests use jest mocks to mock the Prisma Client
 * This allows us to test the repository without requiring an actual PostgreSQL connection
 */
describe('PostgreSQLLampRepository', () => {
  let repository: PostgreSQLLampRepository;
  let prismaClientMock: PrismaClient;

  // Set up mocks before tests
  beforeEach(() => {
    // Clear all mocks before each test
    jest.clearAllMocks();
    
    // Create a new repository instance
    repository = new PostgreSQLLampRepository();
    
    // Get reference to the mocked Prisma client
    prismaClientMock = (repository as any).prisma;
  });

  describe('save', () => {
    test('should save a new lamp', async () => {
      // Arrange
      const lamp = new Lamp('5224f654-4b02-4de5-9a0d-b8e6a12a32c6', 'Test Lamp', { isOn: false });
      prismaClientMock.lamp.upsert.mockResolvedValueOnce({
        id: lamp.id,
        name: lamp.name,
        isOn: lamp.isOn,
        createdAt: new Date(),
        updatedAt: new Date(),
        deletedAt: null
      });

      // Act
      await repository.save(lamp);

      // Assert
      expect(prismaClientMock.lamp.upsert).toHaveBeenCalledWith({
        where: { id: lamp.id },
        update: expect.objectContaining({
          id: lamp.id,
          name: lamp.name,
          isOn: lamp.isOn,
          deletedAt: null
        }),
        create: expect.objectContaining({
          id: lamp.id,
          name: lamp.name,
          isOn: lamp.isOn
        })
      });
    });

    test('should handle errors when saving a lamp', async () => {
      // Arrange
      const lamp = new Lamp('5224f654-4b02-4de5-9a0d-b8e6a12a32c6', 'Test Lamp', { isOn: false });
      prismaClientMock.lamp.upsert.mockRejectedValueOnce(new Error('Database error'));

      // Act & Assert
      await expect(repository.save(lamp)).rejects.toThrow('Database error');
    });
  });

  describe('findById', () => {
    test('should find a lamp by id', async () => {
      // Arrange
      const lampId = '5224f654-4b02-4de5-9a0d-b8e6a12a32c6';
      prismaClientMock.lamp.findFirst.mockResolvedValueOnce({
        id: lampId,
        name: 'Test Lamp',
        isOn: false,
        createdAt: new Date(),
        updatedAt: new Date(),
        deletedAt: null
      });

      // Act
      const result = await repository.findById(lampId);

      // Assert
      expect(prismaClientMock.lamp.findFirst).toHaveBeenCalledWith({
        where: {
          id: lampId,
          deletedAt: null
        }
      });
      expect(result).not.toBeNull();
      expect(result?.id).toBe(lampId);
      expect(result?.name).toBe('Test Lamp');
      expect(result?.isOn).toBe(false);
    });

    test('should return null when lamp not found', async () => {
      // Arrange
      const lampId = 'non-existent-id';
      prismaClientMock.lamp.findFirst.mockResolvedValueOnce(null);

      // Act
      const result = await repository.findById(lampId);

      // Assert
      expect(result).toBeNull();
    });

    test('should handle errors when finding a lamp', async () => {
      // Arrange
      const lampId = '5224f654-4b02-4de5-9a0d-b8e6a12a32c6';
      prismaClientMock.lamp.findFirst.mockRejectedValueOnce(new Error('Database error'));

      // Act & Assert
      await expect(repository.findById(lampId)).rejects.toThrow('Database error');
    });
  });

  describe('findAll', () => {
    test('should return all non-deleted lamps', async () => {
      // Arrange
      const lamps = [
        {
          id: '5224f654-4b02-4de5-9a0d-b8e6a12a32c6',
          name: 'Test Lamp 1',
          isOn: false,
          createdAt: new Date(),
          updatedAt: new Date(),
          deletedAt: null
        },
        {
          id: 'a1b2c3d4-e5f6-4a2b-8c7d-0e1f2a3b4c5d',
          name: 'Test Lamp 2',
          isOn: true,
          createdAt: new Date(),
          updatedAt: new Date(),
          deletedAt: null
        }
      ];
      prismaClientMock.lamp.findMany.mockResolvedValueOnce(lamps);

      // Act
      const result = await repository.findAll();

      // Assert
      expect(prismaClientMock.lamp.findMany).toHaveBeenCalledWith({
        where: {
          deletedAt: null
        }
      });
      expect(result).toHaveLength(2);
      expect(result[0].id).toBe('5224f654-4b02-4de5-9a0d-b8e6a12a32c6');
      expect(result[1].id).toBe('a1b2c3d4-e5f6-4a2b-8c7d-0e1f2a3b4c5d');
    });

    test('should return empty array when no lamps exist', async () => {
      // Arrange
      prismaClientMock.lamp.findMany.mockResolvedValueOnce([]);

      // Act
      const result = await repository.findAll();

      // Assert
      expect(result).toEqual([]);
    });

    test('should handle errors when finding all lamps', async () => {
      // Arrange
      prismaClientMock.lamp.findMany.mockRejectedValueOnce(new Error('Database error'));

      // Act & Assert
      await expect(repository.findAll()).rejects.toThrow('Database error');
    });
  });

  describe('delete', () => {
    test('should soft delete a lamp by id', async () => {
      // Arrange
      const lampId = '5224f654-4b02-4de5-9a0d-b8e6a12a32c6';
      prismaClientMock.lamp.update.mockResolvedValueOnce({
        id: lampId,
        deletedAt: new Date()
      });

      // Act
      await repository.delete(lampId);

      // Assert
      expect(prismaClientMock.lamp.update).toHaveBeenCalledWith({
        where: { id: lampId },
        data: { deletedAt: expect.any(Date) }
      });
    });

    test('should handle errors when deleting a lamp', async () => {
      // Arrange
      const lampId = '5224f654-4b02-4de5-9a0d-b8e6a12a32c6';
      prismaClientMock.lamp.update.mockRejectedValueOnce(new Error('Database error'));

      // Act & Assert
      await expect(repository.delete(lampId)).rejects.toThrow('Database error');
    });
  });

  describe('clear', () => {
    test('should delete all lamps', async () => {
      // Arrange
      prismaClientMock.lamp.deleteMany.mockResolvedValueOnce({ count: 2 });

      // Act
      await repository.clear();

      // Assert
      expect(prismaClientMock.lamp.deleteMany).toHaveBeenCalledWith({});
    });

    test('should handle errors when clearing lamps', async () => {
      // Arrange
      prismaClientMock.lamp.deleteMany.mockRejectedValueOnce(new Error('Database error'));

      // Act & Assert
      await expect(repository.clear()).rejects.toThrow('Database error');
    });
  });

  describe('disconnect', () => {
    test('should disconnect the Prisma client', async () => {
      // Arrange
      prismaClientMock.$disconnect.mockResolvedValueOnce(undefined);

      // Act
      await repository.disconnect();

      // Assert
      expect(prismaClientMock.$disconnect).toHaveBeenCalled();
    });

    test('should handle errors when disconnecting', async () => {
      // Arrange
      prismaClientMock.$disconnect.mockRejectedValueOnce(new Error('Disconnect error'));

      // Act & Assert
      await expect(repository.disconnect()).rejects.toThrow('Disconnect error');
    });
  });
});
