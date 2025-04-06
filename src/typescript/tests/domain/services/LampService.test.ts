import { Lamp, LampState } from '../../../src/domain/models/Lamp';
import { LampRepository } from '../../../src/domain/repositories/LampRepository';
import { LampService } from '../../../src/domain/services/LampService';
import { v4 as uuidv4 } from 'uuid';

describe('LampService', () => {
  let mockRepository: jest.Mocked<LampRepository>;
  let service: LampService;
  let testLamp: Lamp;

  beforeEach(() => {
    // Create a mock repository
    mockRepository = {
      create: jest.fn(),
      findById: jest.fn(),
      findAll: jest.fn(),
      update: jest.fn(),
      delete: jest.fn(),
      count: jest.fn(),
    };

    service = new LampService(mockRepository);

    // Create a test lamp
    testLamp = new Lamp({
      id: uuidv4(),
      name: 'Test Lamp',
      isOn: false,
      brightness: 50,
      color: '#FFFFFF',
    });
  });

  describe('createLamp', () => {
    it('should create a new lamp', async () => {
      const lampData = {
        name: 'New Lamp',
        isOn: false,
        brightness: 100,
        color: '#FF0000',
      };

      mockRepository.create.mockResolvedValue(testLamp);

      const result = await service.createLamp(lampData);
      expect(mockRepository.create).toHaveBeenCalled();
      expect(result).toBe(testLamp);
    });
  });

  describe('getLamp', () => {
    it('should return a lamp when found', async () => {
      mockRepository.findById.mockResolvedValue(testLamp);

      const result = await service.getLamp(testLamp.id);
      expect(result).toBe(testLamp);
      expect(mockRepository.findById).toHaveBeenCalledWith(testLamp.id);
    });

    it('should throw error when lamp not found', async () => {
      mockRepository.findById.mockResolvedValue(null);

      await expect(service.getLamp('non-existent')).rejects.toThrow('not found');
    });
  });

  describe('getAllLamps', () => {
    it('should return paginated lamps with total count', async () => {
      const lamps = [testLamp];
      const total = 1;

      mockRepository.findAll.mockResolvedValue(lamps);
      mockRepository.count.mockResolvedValue(total);

      const result = await service.getAllLamps(1, 10);
      expect(result).toEqual({ lamps, total });
      expect(mockRepository.findAll).toHaveBeenCalledWith({ skip: 0, take: 10 });
    });
  });

  describe('updateLamp', () => {
    it('should update lamp properties', async () => {
      mockRepository.findById.mockResolvedValue(testLamp);
      mockRepository.update.mockResolvedValue(testLamp);

      const updates = {
        name: 'Updated Lamp',
        brightness: 75,
        color: '#00FF00',
      };

      await service.updateLamp(testLamp.id, updates);
      expect(mockRepository.update).toHaveBeenCalled();
    });
  });

  describe('deleteLamp', () => {
    it('should delete an existing lamp', async () => {
      mockRepository.findById.mockResolvedValue(testLamp);

      await service.deleteLamp(testLamp.id);
      expect(mockRepository.delete).toHaveBeenCalledWith(testLamp.id);
    });

    it('should throw error when trying to delete non-existent lamp', async () => {
      mockRepository.findById.mockResolvedValue(null);

      await expect(service.deleteLamp('non-existent')).rejects.toThrow('not found');
    });
  });

  describe('toggleLamp', () => {
    it('should toggle lamp state', async () => {
      mockRepository.findById.mockResolvedValue(testLamp);
      mockRepository.update.mockResolvedValue(testLamp);

      const initialState = testLamp.isOn;
      await service.toggleLamp(testLamp.id);
      expect(testLamp.isOn).toBe(!initialState);
      expect(mockRepository.update).toHaveBeenCalled();
    });
  });
}); 