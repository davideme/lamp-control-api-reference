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

    it('should turn on the lamp', () => {
      expect(lamp.isOn).toBe(false);
      lamp.turnOn();
      expect(lamp.isOn).toBe(true);
      // Call again to test the branch where lamp is already on
      lamp.turnOn();
      expect(lamp.isOn).toBe(true);
    });

    it('should turn off the lamp', () => {
      lamp.turnOn();
      expect(lamp.isOn).toBe(true);
      lamp.turnOff();
      expect(lamp.isOn).toBe(false);
      // Call again to test the branch where lamp is already off
      lamp.turnOff();
      expect(lamp.isOn).toBe(false);
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

  describe('static methods', () => {
    it('should validate lamp data', () => {
      const validData = {
        id: 'ebabdb7d-3205-42f0-ac95-3d8d5eb1f774',
        name: 'Valid Lamp',
        isOn: true,
        createdAt: new Date(),
        updatedAt: new Date(),
      };
      const result = Lamp.validate(validData);
      expect(result).toEqual(validData);
    });

    it('should throw on invalid lamp data', () => {
      const invalidData = {
        name: 123, // name should be a string
        isOn: 'not-a-boolean', // isOn should be a boolean
      };
      expect(() => Lamp.validate(invalidData)).toThrow();
    });
  });
});
