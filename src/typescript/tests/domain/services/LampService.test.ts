import { v4 as uuidv4 } from 'uuid';
import { Lamp } from '../../../src/domain/models/Lamp';
import { LampService } from '../../../src/domain/services/LampService';
import { LampRepository } from '../../../src/domain/repositories/LampRepository';
import { LampNotFoundError } from '../../../src/domain/errors/DomainError';

jest.mock('../../../src/domain/repositories/LampRepository');

describe('LampService', () => {
  let service: LampService;
  let mockRepository: jest.Mocked<LampRepository>;
  let testLamp: Lamp;

  beforeEach(() => {
    mockRepository = {
      save: jest.fn(),
      findById: jest.fn(),
      findAll: jest.fn(),
      delete: jest.fn(),
      clear: jest.fn(),
    };

    service = new LampService(mockRepository);

    testLamp = new Lamp(uuidv4(), 'Test Lamp', {
      isOn: false,
    });
  });

  describe('createLamp', () => {
    it('should create a new lamp', async () => {
      mockRepository.save.mockResolvedValue();

      const data = {
        name: 'New Lamp',
        isOn: false,
      };

      const lamp = await service.createLamp(data);
      expect(mockRepository.save).toHaveBeenCalled();
      expect(lamp.name).toBe(data.name);
    });
  });

  describe('getLamp', () => {
    it('should return a lamp by id', async () => {
      mockRepository.findById.mockResolvedValue(testLamp);

      const result = await service.getLamp(testLamp.id);
      expect(result).toBe(testLamp);
      expect(mockRepository.findById).toHaveBeenCalledWith(testLamp.id);
    });

    it('should throw LampNotFoundError when lamp does not exist', async () => {
      mockRepository.findById.mockResolvedValue(null);

      await expect(service.getLamp('non-existent-id')).rejects.toThrow(LampNotFoundError);
    });
  });

  describe('getAllLamps', () => {
    it('should return all lamps', async () => {
      const lamps = [testLamp];
      mockRepository.findAll.mockResolvedValue(lamps);

      const result = await service.getAllLamps();
      expect(result).toBe(lamps);
      expect(mockRepository.findAll).toHaveBeenCalled();
    });
  });

  describe('updateLamp', () => {
    it('should update an existing lamp', async () => {
      mockRepository.findById.mockResolvedValue(testLamp);
      mockRepository.save.mockResolvedValue();

      const updateData = {
        name: 'Updated Lamp',
        brightness: 50,
        color: '#00FF00',
      };

      const result = await service.updateLamp(testLamp.id, updateData);
      expect(mockRepository.save).toHaveBeenCalled();
      expect(result.name).toBe(updateData.name);
    });

    it('should throw LampNotFoundError when lamp does not exist', async () => {
      mockRepository.findById.mockResolvedValue(null);

      await expect(service.updateLamp('non-existent-id', { name: 'Updated' })).rejects.toThrow(
        LampNotFoundError,
      );
    });
  });

  describe('deleteLamp', () => {
    it('should delete an existing lamp', async () => {
      mockRepository.findById.mockResolvedValue(testLamp);
      mockRepository.delete.mockResolvedValue();

      await service.deleteLamp(testLamp.id);
      expect(mockRepository.delete).toHaveBeenCalledWith(testLamp.id);
    });

    it('should throw LampNotFoundError when lamp does not exist', async () => {
      mockRepository.findById.mockResolvedValue(null);

      await expect(service.deleteLamp('non-existent-id')).rejects.toThrow(LampNotFoundError);
    });
  });

  describe('toggleLamp', () => {
    it('should toggle an existing lamp', async () => {
      mockRepository.findById.mockResolvedValue(testLamp);
      mockRepository.save.mockResolvedValue();

      const result = await service.toggleLamp(testLamp.id);
      expect(mockRepository.save).toHaveBeenCalled();
      expect(result.isOn).toBe(true);
    });

    it('should throw LampNotFoundError when lamp does not exist', async () => {
      mockRepository.findById.mockResolvedValue(null);

      await expect(service.toggleLamp('non-existent-id')).rejects.toThrow(LampNotFoundError);
    });
  });
});
