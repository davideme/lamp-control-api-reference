import { v4 as uuidv4 } from 'uuid';
import { Lamp } from '../../../src/domain/models/Lamp';

describe('Lamp', () => {
  const validId = uuidv4();
  const validName = 'Test Lamp';
  const validOptions = {
    isOn: false,
  };

  describe('constructor', () => {
    it('should create a valid lamp with default values', () => {
      const lamp = new Lamp(validId, validName);
      expect(lamp.id).toBe(validId);
      expect(lamp.name).toBe(validName);
      expect(lamp.isOn).toBe(false);
      expect(lamp.createdAt).toBeInstanceOf(Date);
      expect(lamp.updatedAt).toBeInstanceOf(Date);
    });

    it('should create a valid lamp with custom values', () => {
      const lamp = new Lamp(validId, validName, validOptions);
      expect(lamp.id).toBe(validId);
      expect(lamp.name).toBe(validName);
      expect(lamp.isOn).toBe(false);
    });
  });

  describe('methods', () => {
    let lamp: Lamp;

    beforeEach(() => {
      lamp = new Lamp(validId, validName, validOptions);
    });

    it('should toggle lamp state', () => {
      expect(lamp.isOn).toBe(false);
      lamp.toggle();
      expect(lamp.isOn).toBe(true);
      lamp.toggle();
      expect(lamp.isOn).toBe(false);
    });

    it('should update name', () => {
      const newName = 'Updated Lamp';
      lamp.setName(newName);
      expect(lamp.name).toBe(newName);
    });

    it('should serialize to JSON correctly', () => {
      const json = lamp.toJSON();
      expect(json).toEqual({
        id: lamp.id,
        name: lamp.name,
        isOn: lamp.isOn,
        createdAt: lamp.createdAt,
        updatedAt: lamp.updatedAt,
      });
    });
  });
});
