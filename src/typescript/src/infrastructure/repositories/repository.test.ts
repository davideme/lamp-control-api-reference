import { InMemoryLampRepository } from './InMemoryLampRepository.ts';

describe('InMemoryLampRepository', () => {
  let repository: InMemoryLampRepository;

  beforeEach(() => {
    repository = new InMemoryLampRepository();
  });

  describe('findAll', () => {
    it('should return empty array when no lamps exist', async () => {
      // Act
      const result = await repository.findAll();

      // Assert
      expect(result).toEqual([]);
    });

    it('should return all lamps when no limit is specified', async () => {
      // Arrange
      const lamp1 = await repository.create({ status: true });
      const lamp2 = await repository.create({ status: false });

      // Act
      const result = await repository.findAll();

      // Assert
      expect(result).toHaveLength(2);
      expect(result).toContainEqual(lamp1);
      expect(result).toContainEqual(lamp2);
    });

    it('should return limited number of lamps when limit is specified', async () => {
      // Arrange
      await repository.create({ status: true });
      await repository.create({ status: false });

      // Act
      const result = await repository.findAll(1);

      // Assert
      expect(result).toHaveLength(1);
    });
  });

  describe('findById', () => {
    it('should return lamp when it exists', async () => {
      // Arrange
      const lamp = await repository.create({ status: true });

      // Act
      const result = await repository.findById(lamp.id);

      // Assert
      expect(result).toEqual(lamp);
    });

    it('should return undefined when lamp does not exist', async () => {
      // Act
      const result = await repository.findById('nonexistent');

      // Assert
      expect(result).toBeUndefined();
    });
  });

  describe('create', () => {
    it('should create a new lamp with generated UUID', async () => {
      // Arrange
      const lampData = { status: true };

      // Act
      const result = await repository.create(lampData);

      // Assert
      expect(result.id).toBeDefined();
      expect(result.status).toBe(true);
      expect(result.createdAt).toBeDefined();
      expect(result.updatedAt).toBeDefined();
      expect(await repository.findById(result.id)).toEqual(result);
    });

    it('should create multiple lamps with different UUIDs', async () => {
      // Arrange
      const lampData = { status: true };

      // Act
      const lamp1 = await repository.create(lampData);
      const lamp2 = await repository.create(lampData);

      // Assert
      expect(lamp1.id).not.toBe(lamp2.id);
      expect(await repository.findAll()).toHaveLength(2);
    });
  });

  describe('update', () => {
    it('should update existing lamp', async () => {
      // Arrange
      const lamp = await repository.create({ status: true });
      const updateData = { status: false };

      // Add a small delay to ensure different timestamps
      await new Promise((resolve) => setTimeout(resolve, 10));

      // Act
      const result = await repository.update(lamp.id, updateData);

      // Assert
      expect(result.id).toBe(lamp.id);
      expect(result.status).toBe(false);
      expect(result.createdAt).toBe(lamp.createdAt); // Should preserve creation time
      expect(result.updatedAt).not.toBe(lamp.updatedAt); // Should update modification time

      const refetchedLamp = await repository.findById(lamp.id);
      expect(refetchedLamp).toEqual(result);
    });

    it('should throw LampNotFoundError when lamp does not exist', async () => {
      // Act & Assert
      await expect(repository.update('nonexistent', { status: false })).rejects.toThrow(
        expect.objectContaining({
          name: 'LampNotFoundError',
          message: 'Lamp with ID nonexistent not found',
        }),
      );
    });

    it('should only update specified fields', async () => {
      // Arrange
      const lamp = await repository.create({ status: true });
      const updateData = { status: false };

      // Add a small delay to ensure different timestamps
      await new Promise((resolve) => setTimeout(resolve, 10));

      // Act
      const result = await repository.update(lamp.id, updateData);

      // Assert
      expect(result.id).toBe(lamp.id); // ID should not change
      expect(result.status).toBe(false); // Only status should be updated
      expect(result.createdAt).toBe(lamp.createdAt); // createdAt should not change
      expect(result.updatedAt).not.toBe(lamp.updatedAt); // updatedAt should change
    });
  });

  describe('delete', () => {
    it('should delete existing lamp', async () => {
      // Arrange
      const lamp = await repository.create({ status: true });

      // Act
      await repository.delete(lamp.id);

      // Assert
      expect(await repository.findById(lamp.id)).toBeUndefined();
      expect(await repository.findAll()).toHaveLength(0);
    });

    it('should throw LampNotFoundError when lamp does not exist', async () => {
      // Act & Assert
      await expect(repository.delete('nonexistent')).rejects.toThrow(
        expect.objectContaining({
          name: 'LampNotFoundError',
          message: 'Lamp with ID nonexistent not found',
        }),
      );
    });

    it('should not affect other lamps when deleting one', async () => {
      // Arrange
      const lamp1 = await repository.create({ status: true });
      const lamp2 = await repository.create({ status: false });

      // Act
      await repository.delete(lamp1.id);

      // Assert
      expect(await repository.findById(lamp1.id)).toBeUndefined();
      expect(await repository.findById(lamp2.id)).toEqual(lamp2);
      expect(await repository.findAll()).toHaveLength(1);
    });
  });
});
