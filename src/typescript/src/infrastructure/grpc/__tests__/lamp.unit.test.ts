import {
  Lamp,
  CreateLampRequest,
  GetLampRequest,
  ListLampsRequest,
  ListLampsResponse,
  UpdateLampRequest,
  DeleteLampRequest,
  DeleteLampResponse} from '../generated/lamp';

describe('Generated Lamp Protocol Buffer', () => {
  describe('Lamp message', () => {
    it('should create default lamp with empty values', () => {
      const lamp = Lamp.create();
      expect(lamp.id).toBe('');
      expect(lamp.status).toBe(false);
      expect(lamp.name).toBe('');
    });

    it('should create lamp with provided values', () => {
      const lamp = Lamp.create({
        id: '123',
        status: true,
        name: 'Test Lamp'
      });
      expect(lamp.id).toBe('123');
      expect(lamp.status).toBe(true);
      expect(lamp.name).toBe('Test Lamp');
    });

    it('should encode and decode lamp message', () => {
      const originalLamp = Lamp.create({
        id: '123',
        status: true,
        name: 'Test Lamp'
      });

      // Encode to binary format
      const encoded = Lamp.encode(originalLamp).finish();
      
      // Decode back to object
      const decodedLamp = Lamp.decode(encoded);
      
      expect(decodedLamp).toEqual(originalLamp);
    });
    
    it('should create from partial data', () => {
      const lamp = Lamp.fromPartial({
        id: '123',
        // Omitting status and name
      });
      
      expect(lamp.id).toBe('123');
      expect(lamp.status).toBe(false); // Default value
      expect(lamp.name).toBe(''); // Default value
    });

    it('should convert to and from JSON', () => {
      const original = Lamp.create({
        id: '123',
        status: true,
        name: 'Test Lamp'
      });
      
      const json = Lamp.toJSON(original);
      const fromJson = Lamp.fromJSON(json);
      
      expect(fromJson).toEqual(original);
    });
  });

  describe('CreateLampRequest message', () => {
    it('should create default request with empty values', () => {
      const request = CreateLampRequest.create();
      expect(request.status).toBe(false);
      expect(request.name).toBe('');
    });

    it('should create request with provided values', () => {
      const request = CreateLampRequest.create({
        status: true,
        name: 'New Lamp'
      });
      expect(request.status).toBe(true);
      expect(request.name).toBe('New Lamp');
    });

    it('should encode and decode request', () => {
      const originalRequest = CreateLampRequest.create({
        status: true,
        name: 'New Lamp'
      });
      
      const encoded = CreateLampRequest.encode(originalRequest).finish();
      const decoded = CreateLampRequest.decode(encoded);
      
      expect(decoded).toEqual(originalRequest);
    });
  });

  describe('GetLampRequest message', () => {
    it('should create request with id', () => {
      const request = GetLampRequest.create({ id: '123' });
      expect(request.id).toBe('123');
    });
    
    it('should encode and decode request', () => {
      const original = GetLampRequest.create({ id: '123' });
      const encoded = GetLampRequest.encode(original).finish();
      const decoded = GetLampRequest.decode(encoded);
      
      expect(decoded).toEqual(original);
    });
  });

  describe('ListLampsRequest message', () => {
    it('should create empty request', () => {
      const request = ListLampsRequest.create();
      expect(request).toEqual({});
    });
    
    it('should encode and decode request', () => {
      const original = ListLampsRequest.create();
      const encoded = ListLampsRequest.encode(original).finish();
      const decoded = ListLampsRequest.decode(encoded);
      
      expect(decoded).toEqual(original);
    });
  });

  describe('ListLampsResponse message', () => {
    it('should create response with empty lamps array', () => {
      const response = ListLampsResponse.create();
      expect(response.lamps).toEqual([]);
    });
    
    it('should create response with provided lamps', () => {
      const lamp1 = Lamp.create({ id: '1', status: true, name: 'Lamp 1' });
      const lamp2 = Lamp.create({ id: '2', status: false, name: 'Lamp 2' });
      
      const response = ListLampsResponse.create({
        lamps: [lamp1, lamp2]
      });
      
      expect(response.lamps).toHaveLength(2);
      expect(response.lamps[0]).toEqual(lamp1);
      expect(response.lamps[1]).toEqual(lamp2);
    });
    
    it('should encode and decode response', () => {
      const lamp1 = Lamp.create({ id: '1', status: true, name: 'Lamp 1' });
      const lamp2 = Lamp.create({ id: '2', status: false, name: 'Lamp 2' });
      
      const original = ListLampsResponse.create({
        lamps: [lamp1, lamp2]
      });
      
      const encoded = ListLampsResponse.encode(original).finish();
      const decoded = ListLampsResponse.decode(encoded);
      
      expect(decoded).toEqual(original);
    });
  });

  describe('UpdateLampRequest message', () => {
    it('should create default request with id and undefined optional fields', () => {
      const request = UpdateLampRequest.create({ id: '123' });
      expect(request.id).toBe('123');
      expect(request.status).toBeUndefined();
      expect(request.name).toBeUndefined();
    });
    
    it('should create request with all fields', () => {
      const request = UpdateLampRequest.create({
        id: '123',
        status: true,
        name: 'Updated Lamp'
      });
      
      expect(request.id).toBe('123');
      expect(request.status).toBe(true);
      expect(request.name).toBe('Updated Lamp');
    });
    
    it('should encode and decode request', () => {
      const original = UpdateLampRequest.create({
        id: '123',
        status: true,
        name: 'Updated Lamp'
      });
      
      const encoded = UpdateLampRequest.encode(original).finish();
      const decoded = UpdateLampRequest.decode(encoded);
      
      expect(decoded).toEqual(original);
    });
  });

  describe('DeleteLampRequest message', () => {
    it('should create request with id', () => {
      const request = DeleteLampRequest.create({ id: '123' });
      expect(request.id).toBe('123');
    });
    
    it('should encode and decode request', () => {
      const original = DeleteLampRequest.create({ id: '123' });
      const encoded = DeleteLampRequest.encode(original).finish();
      const decoded = DeleteLampRequest.decode(encoded);
      
      expect(decoded).toEqual(original);
    });
  });

  describe('DeleteLampResponse message', () => {
    it('should create default response with success false', () => {
      const response = DeleteLampResponse.create();
      expect(response.success).toBe(false);
    });
    
    it('should create response with provided success value', () => {
      const response = DeleteLampResponse.create({ success: true });
      expect(response.success).toBe(true);
    });
    
    it('should encode and decode response', () => {
      const original = DeleteLampResponse.create({ success: true });
      const encoded = DeleteLampResponse.encode(original).finish();
      const decoded = DeleteLampResponse.decode(encoded);
      
      expect(decoded).toEqual(original);
    });
  });

  describe('isSet utility', () => {
    it('should correctly determine if values are set', () => {
      // This is testing the internal isSet function used by the generated code
      // Since isSet is not directly exported, we can test it indirectly through fromJSON

      const withValues = Lamp.fromJSON({
        id: '123', 
        status: true,
        name: 'Test'
      });
      expect(withValues.id).toBe('123');
      expect(withValues.status).toBe(true);
      expect(withValues.name).toBe('Test');

      const withNull = Lamp.fromJSON({
        id: null,
        status: null,
        name: null
      });
      expect(withNull.id).toBe('');
      expect(withNull.status).toBe(false);
      expect(withNull.name).toBe('');

      const withUndefined = Lamp.fromJSON({});
      expect(withUndefined.id).toBe('');
      expect(withUndefined.status).toBe(false);
      expect(withUndefined.name).toBe('');
    });
  });
});
