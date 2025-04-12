import { MongoDBLampRepository } from '../MongoDBLampRepository';
import { MongoMemoryServer } from 'mongodb-memory-server';
import { Lamp } from '../../../domain/models/Lamp';
import mongoose from 'mongoose';
// Import jest explicitly to ensure proper detection
import '@jest/globals';

/**
 * Test suite for MongoDBLampRepository
 * 
 * These tests use mongodb-memory-server to run an in-memory MongoDB instance
 * This allows us to test the repository without requiring an actual MongoDB connection
 */
describe('MongoDBLampRepository', () => {
  let mongoServer: MongoMemoryServer;
  let repository: MongoDBLampRepository;
  let mongoUri: string;

  // Set up the in-memory MongoDB server before tests
  beforeAll(async () => {
    // Create a new MongoDB Memory Server
    mongoServer = await MongoMemoryServer.create();
    mongoUri = mongoServer.getUri();
    
    // Silence mongoose warnings during tests
    mongoose.set('strictQuery', false);
  });

  // Close the server after all tests
  afterAll(async () => {
    // Disconnect mongoose
    await MongoDBLampRepository.disconnect();
    
    // Stop the memory server
    await mongoServer.stop();
  });

  // Create a fresh repository before each test
  beforeEach(async () => {
    // Connect to the in-memory database
    await MongoDBLampRepository.connect(mongoUri);
    
    // Create a new repository instance
    repository = new MongoDBLampRepository();
    
    // Clear all data before each test for isolation
    await repository.clear();
  });

  describe('save', () => {
    test('should save a new lamp', async () => {
      // Arrange
      const lamp = new Lamp('5224f654-4b02-4de5-9a0d-b8e6a12a32c6', 'Test Lamp', { isOn: false });
      
      // Act
      await repository.save(lamp);
      
      // Assert
      const retrieved = await repository.findById('5224f654-4b02-4de5-9a0d-b8e6a12a32c6');
      expect(retrieved).not.toBeNull();
      expect(retrieved?.id).toBe('5224f654-4b02-4de5-9a0d-b8e6a12a32c6');
      expect(retrieved?.name).toBe('Test Lamp');
      expect(retrieved?.isOn).toBe(false);
    });

    test('should update an existing lamp', async () => {
      // Arrange
      const lamp = new Lamp('5224f654-4b02-4de5-9a0d-b8e6a12a32c6', 'Test Lamp', { isOn: false });
      await repository.save(lamp);
      
      // Update the lamp
      lamp.turnOn();
      lamp.setName('Updated Lamp');
      
      // Act
      await repository.save(lamp);
      
      // Assert
      const updated = await repository.findById('5224f654-4b02-4de5-9a0d-b8e6a12a32c6');
      expect(updated).not.toBeNull();
      expect(updated?.name).toBe('Updated Lamp');
      expect(updated?.isOn).toBe(true);
    });

    test('should handle errors when saving a lamp', async () => {
      // Arrange
      const lamp = new Lamp('5224f654-4b02-4de5-9a0d-b8e6a12a32c6', 'Test Lamp', { isOn: false });
      
      // Mock the connect method to simulate a connection error
      const spy = jest.spyOn(MongoDBLampRepository, 'connect').mockRejectedValueOnce(new Error('Connection error'));
      
      // Act & Assert
      await expect(repository.save(lamp)).rejects.toThrow('Connection error');
      
      // Clean up
      spy.mockRestore();
    });
  });

  describe('findById', () => {
    test('should return null for non-existent lamp', async () => {
      // Act
      const result = await repository.findById('non-existent-id');
      
      // Assert
      expect(result).toBeNull();
    });

    test('should return a lamp by id', async () => {
      // Arrange
      const lamp = new Lamp('5224f654-4b02-4de5-9a0d-b8e6a12a32c6', 'Test Lamp', { isOn: true });
      await repository.save(lamp);
      
      // Act
      const result = await repository.findById('5224f654-4b02-4de5-9a0d-b8e6a12a32c6');
      
      // Assert
      expect(result).not.toBeNull();
      expect(result?.id).toBe('5224f654-4b02-4de5-9a0d-b8e6a12a32c6');
      expect(result?.name).toBe('Test Lamp');
      expect(result?.isOn).toBe(true);
    });

    test('should not return soft-deleted lamps', async () => {
      // Arrange
      const lamp = new Lamp('5224f654-4b02-4de5-9a0d-b8e6a12a32c6', 'Test Lamp', { isOn: true });
      await repository.save(lamp);
      await repository.delete('5224f654-4b02-4de5-9a0d-b8e6a12a32c6');
      
      // Act
      const result = await repository.findById('5224f654-4b02-4de5-9a0d-b8e6a12a32c6');
      
      // Assert
      expect(result).toBeNull();
    });

    test('should handle errors when finding a lamp', async () => {
      // Mock the connect method to simulate a connection error
      const spy = jest.spyOn(MongoDBLampRepository, 'connect').mockRejectedValueOnce(new Error('Connection error'));
      
      // Act & Assert
      await expect(repository.findById('5224f654-4b02-4de5-9a0d-b8e6a12a32c6')).rejects.toThrow('Connection error');
      
      // Clean up
      spy.mockRestore();
    });
  });

  describe('findAll', () => {
    test('should return empty array when no lamps exist', async () => {
      // Act
      const lamps = await repository.findAll();
      
      // Assert
      expect(lamps).toEqual([]);
    });

    test('should return all lamps', async () => {
      // Arrange
      const lamp1 = new Lamp('d78960d2-4c23-4eda-ac06-5304afad85ed', 'Lamp 1', { isOn: false });
      const lamp2 = new Lamp('f4d42e8d-292e-4275-b6a1-1b644414d0de', 'Lamp 2', { isOn: true });
      await repository.save(lamp1);
      await repository.save(lamp2);
      
      // Act
      const lamps = await repository.findAll();
      
      // Assert
      expect(lamps.length).toBe(2);
      expect(lamps.some(lamp => lamp.id === 'd78960d2-4c23-4eda-ac06-5304afad85ed')).toBe(true);
      expect(lamps.some(lamp => lamp.id === 'f4d42e8d-292e-4275-b6a1-1b644414d0de')).toBe(true);
    });

    test('should not return soft-deleted lamps', async () => {
      // Arrange
      const lamp1 = new Lamp('d78960d2-4c23-4eda-ac06-5304afad85ed', 'Lamp 1', { isOn: false });
      const lamp2 = new Lamp('f4d42e8d-292e-4275-b6a1-1b644414d0de', 'Lamp 2', { isOn: true });
      await repository.save(lamp1);
      await repository.save(lamp2);
      await repository.delete('d78960d2-4c23-4eda-ac06-5304afad85ed');
      
      // Act
      const lamps = await repository.findAll();
      
      // Assert
      expect(lamps.length).toBe(1);
      expect(lamps[0].id).toBe('f4d42e8d-292e-4275-b6a1-1b644414d0de');
    });

    test('should handle errors when finding all lamps', async () => {
      // Mock the connect method to simulate a connection error
      const spy = jest.spyOn(MongoDBLampRepository, 'connect').mockRejectedValueOnce(new Error('Connection error'));
      
      // Act & Assert
      await expect(repository.findAll()).rejects.toThrow('Connection error');
      
      // Clean up
      spy.mockRestore();
    });
  });

  describe('delete', () => {
    test('should soft-delete a lamp', async () => {
      // Arrange
      const lamp = new Lamp('5224f654-4b02-4de5-9a0d-b8e6a12a32c6', 'Test Lamp', { isOn: true });
      await repository.save(lamp);
      
      // Act
      await repository.delete('5224f654-4b02-4de5-9a0d-b8e6a12a32c6');
      
      // Assert
      const deleted = await repository.findById('5224f654-4b02-4de5-9a0d-b8e6a12a32c6');
      expect(deleted).toBeNull();
    });

    test('should handle deleting non-existent lamp', async () => {
      // Act & Assert
      // This should not throw an error
      await expect(repository.delete('non-existent-id')).resolves.not.toThrow();
    });

    test('should handle errors when deleting a lamp', async () => {
      // Mock the connect method to simulate a connection error
      const spy = jest.spyOn(MongoDBLampRepository, 'connect').mockRejectedValueOnce(new Error('Connection error'));
      
      // Act & Assert
      await expect(repository.delete('5224f654-4b02-4de5-9a0d-b8e6a12a32c6')).rejects.toThrow('Connection error');
      
      // Clean up
      spy.mockRestore();
    });
  });

  describe('clear', () => {
    test('should remove all lamps', async () => {
      // Arrange
      const lamp1 = new Lamp('d78960d2-4c23-4eda-ac06-5304afad85ed', 'Lamp 1', { isOn: false });
      const lamp2 = new Lamp('f4d42e8d-292e-4275-b6a1-1b644414d0de', 'Lamp 2', { isOn: true });
      await repository.save(lamp1);
      await repository.save(lamp2);
      
      // Act
      await repository.clear();
      
      // Assert
      const lamps = await repository.findAll();
      expect(lamps.length).toBe(0);
    });

    test('should handle errors when clearing lamps', async () => {
      // Mock the connect method to simulate a connection error
      const spy = jest.spyOn(MongoDBLampRepository, 'connect').mockRejectedValueOnce(new Error('Connection error'));
      
      // Act & Assert
      await expect(repository.clear()).rejects.toThrow('Connection error');
      
      // Clean up
      spy.mockRestore();
    });
  });

  describe('documentToDomain', () => {
    test('should handle null document', async () => {
      // Arrange - Create a repository and get access to the private method
      const repo = new MongoDBLampRepository();
      const documentToDomain = (repo as any).documentToDomain.bind(repo);
      
      // Act
      const result = documentToDomain(null);
      
      // Assert
      expect(result).toBeNull();
    });
  });

  describe('static connection management', () => {
    test('should reuse existing connection', async () => {
      // Arrange
      // Reset the connection promise to ensure we're testing from a clean state
      (MongoDBLampRepository as any).connectionPromise = null;
      await MongoDBLampRepository.disconnect();
      
      const connectSpy = jest.spyOn(mongoose, 'connect');
      
      // Act
      // First connection should call mongoose.connect
      await MongoDBLampRepository.connect(mongoUri);
      // Second connection should reuse the existing connection
      await MongoDBLampRepository.connect(mongoUri);
      
      // Assert
      // Mongoose.connect should only be called once
      expect(connectSpy).toHaveBeenCalledTimes(1);
      
      // Clean up
      connectSpy.mockRestore();
    });

    test('should disconnect properly', async () => {
      // Arrange
      const disconnectSpy = jest.spyOn(mongoose, 'disconnect');
      
      // Act
      await MongoDBLampRepository.connect(mongoUri);
      await MongoDBLampRepository.disconnect();
      
      // Assert
      expect(disconnectSpy).toHaveBeenCalledTimes(1);
      
      // Clean up
      disconnectSpy.mockRestore();
    });
  });
});
