import { resolvers, ResolverContext } from '../resolvers';
import { Lamp } from '../../../domain/models/Lamp';
import { v4 as uuidv4 } from 'uuid';
import { LampService } from '../../../domain/services/LampService';

// Mock the logger to prevent test output pollution
jest.mock('../../../utils/logger', () => ({
  appLogger: {
    info: jest.fn(),
    error: jest.fn(),
    warn: jest.fn(),
  },
}));

jest.mock('../../../domain/services/LampService', () => ({
  LampService: jest.fn().mockImplementation(() => ({
    createLamp: jest.fn(),
    getLamp: jest.fn(),
    getAllLamps: jest.fn(),
    updateLamp: jest.fn(),
    deleteLamp: jest.fn(),
    toggleLamp: jest.fn(),
  })),
}));

jest.mock('../../../domain/services/LampService', () => ({
  LampService: jest.fn().mockImplementation(() => ({
    createLamp: jest.fn(),
    getLamp: jest.fn(),
    getAllLamps: jest.fn(),
    updateLamp: jest.fn(),
    deleteLamp: jest.fn(),
    toggleLamp: jest.fn(),
  })),
}));

describe('GraphQL Resolvers', () => {
  // Sample lamp for testing
  const sampleLamp = new Lamp(uuidv4(), 'Test Lamp', { isOn: true });
  // Cast to MockLampService to get TypeScript to recognize the mock methods
  const mockLampService = {
    getLamp: jest.fn(),
    getAllLamps: jest.fn(),
    createLamp: jest.fn(),
    updateLamp: jest.fn(),
    deleteLamp: jest.fn(),
  };

  // Mock context for resolvers
  const mockContext: ResolverContext = {
    lampService: mockLampService as unknown as LampService,
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Query Resolvers', () => {
    it('should have Query resolvers defined', () => {
      expect(resolvers.Query).toBeDefined();
      expect(resolvers.Query.getLamp).toBeDefined();
      expect(resolvers.Query.getLamps).toBeDefined();
    });

    describe('getLamp resolver', () => {
      it('should return a lamp by ID', async () => {
        mockLampService.getLamp.mockResolvedValueOnce(sampleLamp);

        const result = await resolvers.Query.getLamp(null, { id: sampleLamp.id }, mockContext);

        expect(result).toEqual(sampleLamp);
        expect(mockLampService.getLamp).toHaveBeenCalledWith(sampleLamp.id);
      });

      it('should handle errors when fetching a lamp', async () => {
        const error = new Error('Lamp not found');
        mockLampService.getLamp.mockRejectedValueOnce(error);
        await expect(
          resolvers.Query.getLamp(null, { id: 'invalid-id' }, mockContext),
        ).rejects.toThrow(error);
      });
    });

    describe('getLamps resolver', () => {
      it('should return all lamps', async () => {
        const lamps = [sampleLamp, new Lamp(uuidv4(), 'Another Lamp')];
        mockLampService.getAllLamps.mockResolvedValueOnce(lamps);
        const result = await resolvers.Query.getLamps(null, {}, mockContext);
        expect(result).toEqual(lamps);
        expect(mockLampService.getAllLamps).toHaveBeenCalled();
      });

      it('should handle errors when fetching all lamps', async () => {
        const error = new Error('Failed to fetch lamps');
        mockLampService.getAllLamps.mockRejectedValueOnce(error);

        await expect(resolvers.Query.getLamps(null, {}, mockContext)).rejects.toThrow(error);
      });
    });
  });

  describe('Mutation Resolvers', () => {
    it('should have Mutation resolvers defined', () => {
      expect(resolvers.Mutation).toBeDefined();
      expect(resolvers.Mutation.createLamp).toBeDefined();
      expect(resolvers.Mutation.updateLamp).toBeDefined();
      expect(resolvers.Mutation.deleteLamp).toBeDefined();
    });

    describe('createLamp resolver', () => {
      it('should create a new lamp', async () => {
        mockLampService.createLamp.mockResolvedValueOnce(sampleLamp);

        const result = await resolvers.Mutation.createLamp(null, { status: true }, mockContext);

        expect(result).toEqual(sampleLamp);
        expect(mockLampService.createLamp).toHaveBeenCalledWith(
          expect.objectContaining({
            name: expect.any(String),
            isOn: true,
          }),
        );
      });

      it('should handle errors when creating a lamp', async () => {
        const error = new Error('Failed to create lamp');
        mockLampService.createLamp.mockRejectedValueOnce(error);
        await expect(
          resolvers.Mutation.createLamp(null, { status: true }, mockContext),
        ).rejects.toThrow(error);
      });
    });

    describe('updateLamp resolver', () => {
      it('should update a lamp', async () => {
        const updatedLamp = new Lamp(sampleLamp.id, 'Updated Lamp', { isOn: false });
        mockLampService.updateLamp.mockResolvedValueOnce(updatedLamp);
        const result = await resolvers.Mutation.updateLamp(
          null,
          { id: sampleLamp.id, status: false },
          mockContext,
        );
        expect(result).toEqual(updatedLamp);
        expect(mockLampService.updateLamp).toHaveBeenCalledWith(
          sampleLamp.id,
          expect.objectContaining({ isOn: false }),
        );
      });

      it('should handle errors when updating a lamp', async () => {
        const error = new Error('Lamp not found');
        mockLampService.updateLamp.mockRejectedValueOnce(error);
        await expect(
          resolvers.Mutation.updateLamp(null, { id: 'invalid-id', status: false }, mockContext),
        ).rejects.toThrow(error);
      });
    });

    describe('deleteLamp resolver', () => {
      it('should delete a lamp and return true on success', async () => {
        mockLampService.deleteLamp.mockResolvedValueOnce(undefined);
        const result = await resolvers.Mutation.deleteLamp(
          null,
          { id: sampleLamp.id },
          mockContext,
        );
        expect(result).toBe(true);
        expect(mockLampService.deleteLamp).toHaveBeenCalledWith(sampleLamp.id);
      });

      it('should handle errors when deleting a lamp', async () => {
        const error = new Error('Failed to delete lamp');
        mockLampService.deleteLamp.mockRejectedValueOnce(error);
        await expect(
          resolvers.Mutation.deleteLamp(null, { id: 'invalid-id' }, mockContext),
        ).rejects.toThrow(error);
      });
    });
  });
});
