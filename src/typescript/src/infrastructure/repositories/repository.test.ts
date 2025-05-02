import { InMemoryLampRepository } from './in-memory-lamp.repository';

describe('InMemoryLampRepository', () => {
  let repository: InMemoryLampRepository;

  beforeEach(() => {
    repository = new InMemoryLampRepository();
  });

  describe('findAll', () => {
    it('should return empty array when no lamps exist', () => {
      // Act
      const result = repository.findAll();

      // Assert
      expect(result).toEqual([]);
    });

    it('should return all lamps when no limit is specified', () => {
      // Arrange
      const lamp1 = repository.create({ status: true });
      const lamp2 = repository.create({ status: false });

      // Act
      const result = repository.findAll();

      // Assert
      expect(result).toHaveLength(2);
      expect(result).toContainEqual(lamp1);
      expect(result).toContainEqual(lamp2);
    });

    it('should return limited number of lamps when limit is specified', () => {
      // Arrange
      repository.create({ status: true });
      repository.create({ status: false });

      // Act
      const result = repository.findAll(1);

      // Assert
      expect(result).toHaveLength(1);
    });
  });

  describe('findById', () => {
    it('should return lamp when it exists', () => {
      // Arrange
      const lamp = repository.create({ status: true });

      // Act
      const result = repository.findById(lamp.id);

      // Assert
      expect(result).toEqual(lamp);
    });

    it('should return undefined when lamp does not exist', () => {
      // Act
      const result = repository.findById('nonexistent');

      // Assert
      expect(result).toBeUndefined();
    });
  });

  describe('create', () => {
    it('should create a new lamp with generated UUID', () => {
      // Arrange
      const lampData = { status: true };

      // Act
      const result = repository.create(lampData);

      // Assert
      expect(result.id).toBeDefined();
      expect(result.status).toBe(true);
      expect(repository.findById(result.id)).toEqual(result);
    });

    it('should create multiple lamps with different UUIDs', () => {
      // Arrange
      const lampData = { status: true };

      // Act
      const lamp1 = repository.create(lampData);
      const lamp2 = repository.create(lampData);

      // Assert
      expect(lamp1.id).not.toBe(lamp2.id);
      expect(repository.findAll()).toHaveLength(2);
    });
  });

  describe('update', () => {
    it('should update existing lamp', () => {
      // Arrange
      const lamp = repository.create({ status: true });
      const updateData = { status: false };

      // Act
      const result = repository.update(lamp.id, updateData);

      // Assert
      expect(result).toEqual({ id: lamp.id, status: false });
      expect(repository.findById(lamp.id)).toEqual({
        id: lamp.id,
        status: false,
      });
    });

    it('should throw LampNotFoundError when lamp does not exist', () => {
      // Act & Assert
      expect(() => repository.update('nonexistent', { status: false })).toThrow(
        expect.objectContaining({
          name: 'LampNotFoundError',
          message: 'Lamp with ID nonexistent not found',
        }),
      );
    });

    it('should only update specified fields', () => {
      // Arrange
      const lamp = repository.create({ status: true });
      const updateData = { status: false };

      // Act
      const result = repository.update(lamp.id, updateData);

      // Assert
      expect(result.id).toBe(lamp.id); // ID should not change
      expect(result.status).toBe(false); // Only status should be updated
    });
  });

  describe('delete', () => {
    it('should delete existing lamp', () => {
      // Arrange
      const lamp = repository.create({ status: true });

      // Act
      repository.delete(lamp.id);

      // Assert
      expect(repository.findById(lamp.id)).toBeUndefined();
      expect(repository.findAll()).toHaveLength(0);
    });

    it('should throw LampNotFoundError when lamp does not exist', () => {
      // Act & Assert
      expect(() => repository.delete('nonexistent')).toThrow(
        expect.objectContaining({
          name: 'LampNotFoundError',
          message: 'Lamp with ID nonexistent not found',
        }),
      );
    });

    it('should not affect other lamps when deleting one', () => {
      // Arrange
      const lamp1 = repository.create({ status: true });
      const lamp2 = repository.create({ status: false });

      // Act
      repository.delete(lamp1.id);

      // Assert
      expect(repository.findById(lamp1.id)).toBeUndefined();
      expect(repository.findById(lamp2.id)).toEqual(lamp2);
      expect(repository.findAll()).toHaveLength(1);
    });
  });
});
