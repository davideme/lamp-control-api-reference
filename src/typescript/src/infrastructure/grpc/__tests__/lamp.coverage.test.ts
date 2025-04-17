import {
  Lamp,
  CreateLampRequest,
  ListLampsRequest,
  ListLampsResponse,
  UpdateLampRequest,
  LampServiceService} from '../generated/lamp';

// The isSet function might not be directly importable, we'll test it indirectly

describe('Generated Protobuf Tests', () => {
  describe('Message Creation', () => {
    it('should create Lamp with default values', () => {
      const lamp = Lamp.create();
      expect(lamp.id).toBe('');
      expect(lamp.status).toBe(false);
      expect(lamp.name).toBe('');
    });
    
    it('should create Lamp with custom values', () => {
      const lamp = Lamp.create({
        id: '123',
        status: true,
        name: 'Test Lamp'
      });
      expect(lamp.id).toBe('123');
      expect(lamp.status).toBe(true);
      expect(lamp.name).toBe('Test Lamp');
    });
    
    it('should create UpdateLampRequest with optional fields', () => {
      const request = UpdateLampRequest.create({
        id: '123',
        status: true,
        // name is undefined
      });
      expect(request.id).toBe('123');
      expect(request.status).toBe(true);
      expect(request.name).toBeUndefined();
    });
  });
  
  describe('Encoding and Decoding', () => {
    it('should encode and decode Lamp correctly', () => {
      const original = Lamp.create({
        id: '123',
        status: true,
        name: 'Test Lamp'
      });
      
      const encoded = Lamp.encode(original).finish();
      const decoded = Lamp.decode(encoded);
      
      expect(decoded).toEqual(original);
    });
    
    it('should encode and decode empty ListLampsRequest', () => {
      const request = ListLampsRequest.create();
      const encoded = ListLampsRequest.encode(request).finish();
      const decoded = ListLampsRequest.decode(encoded);
      
      expect(decoded).toEqual({});
    });
    
    it('should encode and decode ListLampsResponse with multiple lamps', () => {
      const lamp1 = Lamp.create({ id: '1', status: true, name: 'Lamp 1' });
      const lamp2 = Lamp.create({ id: '2', status: false, name: 'Lamp 2' });
      
      const response = ListLampsResponse.create({ lamps: [lamp1, lamp2] });
      const encoded = ListLampsResponse.encode(response).finish();
      const decoded = ListLampsResponse.decode(encoded);
      
      expect(decoded.lamps).toHaveLength(2);
      expect(decoded.lamps[0]).toEqual(lamp1);
      expect(decoded.lamps[1]).toEqual(lamp2);
    });
    
    it('should handle partial encoding of UpdateLampRequest', () => {
      const request = UpdateLampRequest.create({
        id: '123',
        // Only status is defined, name is undefined
        status: true
      });
      
      const encoded = UpdateLampRequest.encode(request).finish();
      const decoded = UpdateLampRequest.decode(encoded);
      
      expect(decoded.id).toBe('123');
      expect(decoded.status).toBe(true);
      expect(decoded.name).toBeUndefined();
    });
  });
  
  describe('JSON Conversion', () => {
    it('should convert Lamp to and from JSON', () => {
      const original = Lamp.create({
        id: '123',
        status: true,
        name: 'Test Lamp'
      });
      
      const json = Lamp.toJSON(original);
      const fromJson = Lamp.fromJSON(json);
      
      expect(fromJson).toEqual(original);
    });
    
    it('should convert UpdateLampRequest with undefined fields to JSON', () => {
      const original = UpdateLampRequest.create({
        id: '123',
        status: true
        // name is undefined
      });
      
      const json = UpdateLampRequest.toJSON(original);
      expect(json).toHaveProperty('id');
      expect(json).toHaveProperty('status');
      
      const fromJson = UpdateLampRequest.fromJSON(json);
      expect(fromJson.id).toBe('123');
      expect(fromJson.status).toBe(true);
    });
  });
  
  describe('Partial Creation', () => {
    it('should create from partial objects', () => {
      // Using fromPartial
      const lamp = Lamp.fromPartial({
        id: '123',
        // status and name are not provided
      });
      
      expect(lamp.id).toBe('123');
      expect(lamp.status).toBe(false); // Default
      expect(lamp.name).toBe(''); // Default
    });
    
    it('should handle nested object structures', () => {
      const response = ListLampsResponse.fromPartial({
        lamps: [
          {
            id: '1',
            status: true
            // name missing
          },
          {
            id: '2',
            // status missing
            name: 'Lamp 2'
          }
        ]
      });
      
      expect(response.lamps).toHaveLength(2);
      expect(response.lamps[0].id).toBe('1');
      expect(response.lamps[0].status).toBe(true);
      expect(response.lamps[0].name).toBe(''); // Default value
      
      expect(response.lamps[1].id).toBe('2');
      expect(response.lamps[1].status).toBe(false); // Default value
      expect(response.lamps[1].name).toBe('Lamp 2');
    });
  });

  describe('Service Definition', () => {
    it('should define all required service methods with correct paths', () => {
      expect(LampServiceService.createLamp.path).toBe('/lamp.LampService/CreateLamp');
      expect(LampServiceService.getLamp.path).toBe('/lamp.LampService/GetLamp');
      expect(LampServiceService.listLamps.path).toBe('/lamp.LampService/ListLamps');
      expect(LampServiceService.updateLamp.path).toBe('/lamp.LampService/UpdateLamp');
      expect(LampServiceService.deleteLamp.path).toBe('/lamp.LampService/DeleteLamp');
    });

    it('should define proper serialization methods', () => {
      // Test request serialization
      const request = CreateLampRequest.create({ status: true, name: 'Test' });
      const serialized = LampServiceService.createLamp.requestSerialize(request);
      expect(serialized).toBeInstanceOf(Buffer);
      
      // Test request deserialization
      const deserialized = LampServiceService.createLamp.requestDeserialize(serialized);
      expect(deserialized).toEqual(request);
      
      // Test response serialization/deserialization
      const lamp = Lamp.create({ id: '123', status: true, name: 'Test' });
      const serializedResponse = LampServiceService.createLamp.responseSerialize(lamp);
      const deserializedResponse = LampServiceService.createLamp.responseDeserialize(serializedResponse);
      expect(deserializedResponse).toEqual(lamp);
    });
  });

  describe('Edge Cases', () => {
    it('should handle empty inputs in decode', () => {
      // Empty buffer
      const emptyBuffer = new Uint8Array(0);
      const lamp = Lamp.decode(emptyBuffer);
      expect(lamp).toEqual(Lamp.create());
    });
  });
  
  // Test the behavior of the isSet function indirectly
  describe('isSet behavior (tested through fromJSON)', () => {
    it('should correctly distinguish between defined and undefined values', () => {
      // JSON with explicitly defined values
      const lamp1 = Lamp.fromJSON({
        id: '',         // Empty string is "set"
        status: false,  // False boolean is "set"
        name: ''        // Empty string is "set"
      });
      
      // Even though these are falsy values, they should be recognized as set
      expect(lamp1.id).toBe('');
      expect(lamp1.status).toBe(false);
      expect(lamp1.name).toBe('');
      
      // JSON with explicitly null values
      const lamp2 = Lamp.fromJSON({
        id: null,
        status: null,
        name: null
      });
      
      // Null values should be treated as "not set" and use defaults
      expect(lamp2.id).toBe('');
      expect(lamp2.status).toBe(false);
      expect(lamp2.name).toBe('');
      
      // JSON with undefined/missing values
      const lamp3 = Lamp.fromJSON({});
      expect(lamp3.id).toBe('');
      expect(lamp3.status).toBe(false);
      expect(lamp3.name).toBe('');
    });
  });
});
