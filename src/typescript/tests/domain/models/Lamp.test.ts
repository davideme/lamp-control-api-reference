import { Lamp, LampState } from '../../../src/domain/models/Lamp';
import { v4 as uuidv4 } from 'uuid';

describe('Lamp', () => {
  let validLampData: Omit<LampState, 'createdAt' | 'updatedAt'>;

  beforeEach(() => {
    validLampData = {
      id: uuidv4(),
      name: 'Test Lamp',
      isOn: false,
      brightness: 50,
      color: '#FFFFFF',
    };
  });

  describe('constructor', () => {
    it('should create a lamp with valid data', () => {
      const lamp = new Lamp(validLampData);
      expect(lamp.id).toBe(validLampData.id);
      expect(lamp.name).toBe(validLampData.name);
      expect(lamp.isOn).toBe(validLampData.isOn);
      expect(lamp.createdAt).toBeInstanceOf(Date);
      expect(lamp.updatedAt).toBeInstanceOf(Date);
    });
  });

  describe('state modifications', () => {
    let lamp: Lamp;

    beforeEach(() => {
      lamp = new Lamp(validLampData);
    });

    it('should turn the lamp on', () => {
      lamp.turnOn();
      expect(lamp.isOn).toBe(true);
    });

    it('should turn the lamp off', () => {
      lamp.turnOn();
      lamp.turnOff();
      expect(lamp.isOn).toBe(false);
    });

    it('should update the updatedAt timestamp on state changes', () => {
      const initialUpdatedAt = lamp.updatedAt;
      lamp.turnOn();
      expect(lamp.updatedAt.getTime()).toBeGreaterThan(initialUpdatedAt.getTime());
    });
  });

  describe('serialization', () => {
    it('should serialize to JSON correctly', () => {
      const lamp = new Lamp(validLampData);
      const json = lamp.toJSON();
      expect(json).toEqual({
        ...validLampData,
        createdAt: expect.any(Date),
        updatedAt: expect.any(Date),
      });
    });
  });

  describe('validation', () => {
    it('should validate correct lamp data', () => {
      const lamp = new Lamp(validLampData);
      const data = lamp.toJSON();
      expect(() => Lamp.validate(data)).not.toThrow();
    });

    it('should throw error for invalid lamp data', () => {
      const invalidData = {
        ...validLampData,
        brightness: 101,
      };
      expect(() => Lamp.validate(invalidData)).toThrow();
    });
  });
}); 