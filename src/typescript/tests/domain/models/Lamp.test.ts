import { v4 as uuidv4 } from 'uuid';
import { Lamp } from '../../../src/domain/models/Lamp';
import { ValidationError } from '../../../src/domain/errors/DomainError';

describe('Lamp', () => {
  const validId = uuidv4();
  const validName = 'Test Lamp';
  const validOptions = {
    brightness: 75,
    color: '#FF0000'
  };

  describe('constructor', () => {
    it('should create a valid lamp with default values', () => {
      const lamp = new Lamp(validId, validName);
      expect(lamp.id).toBe(validId);
      expect(lamp.name).toBe(validName);
      expect(lamp.isOn).toBe(false);
      expect(lamp.brightness).toBe(100);
      expect(lamp.color).toBe('#FFFFFF');
      expect(lamp.createdAt).toBeInstanceOf(Date);
      expect(lamp.updatedAt).toBeInstanceOf(Date);
    });

    it('should create a valid lamp with custom values', () => {
      const lamp = new Lamp(validId, validName, validOptions);
      expect(lamp.id).toBe(validId);
      expect(lamp.name).toBe(validName);
      expect(lamp.isOn).toBe(false);
      expect(lamp.brightness).toBe(validOptions.brightness);
      expect(lamp.color).toBe(validOptions.color);
    });

    it('should throw ValidationError for invalid brightness', () => {
      expect(() => {
        new Lamp(validId, validName, { brightness: 101 });
      }).toThrow(ValidationError);

      expect(() => {
        new Lamp(validId, validName, { brightness: -1 });
      }).toThrow(ValidationError);
    });

    it('should throw ValidationError for invalid color', () => {
      expect(() => {
        new Lamp(validId, validName, { color: 'invalid' });
      }).toThrow(ValidationError);

      expect(() => {
        new Lamp(validId, validName, { color: '#GGGGGG' });
      }).toThrow(ValidationError);
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

    it('should update brightness', () => {
      const newBrightness = 50;
      lamp.setBrightness(newBrightness);
      expect(lamp.brightness).toBe(newBrightness);
    });

    it('should update color', () => {
      const newColor = '#00FF00';
      lamp.setColor(newColor);
      expect(lamp.color).toBe(newColor);
    });

    it('should serialize to JSON correctly', () => {
      const json = lamp.toJSON();
      expect(json).toEqual({
        id: lamp.id,
        name: lamp.name,
        isOn: lamp.isOn,
        brightness: lamp.brightness,
        color: lamp.color,
        createdAt: lamp.createdAt,
        updatedAt: lamp.updatedAt
      });
    });
  });
}); 