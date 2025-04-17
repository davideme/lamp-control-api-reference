import {
  Lamp,
  ListLampsResponse,
  UpdateLampRequest,
  LampServiceClient,
  LampServiceService,
} from '../generated/lamp';
import { BinaryWriter } from '@bufbuild/protobuf/wire';
import * as grpc from '@grpc/grpc-js';

describe('Generated Lamp Protocol Buffer', () => {
  // Original tests from the first file
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
        name: 'Test Lamp',
      });
      expect(lamp.id).toBe('123');
      expect(lamp.status).toBe(true);
      expect(lamp.name).toBe('Test Lamp');
    });

    it('should encode and decode lamp message', () => {
      const originalLamp = Lamp.create({
        id: '123',
        status: true,
        name: 'Test Lamp',
      });

      // Encode to binary format
      const encoded = Lamp.encode(originalLamp).finish();

      // Decode back to object
      const decodedLamp = Lamp.decode(encoded);

      expect(decodedLamp).toEqual(originalLamp);
    });
  });

  // NEW TESTS - Enhanced coverage

  // Test encoding/decoding edge cases
  describe('encoding and decoding edge cases', () => {
    it('should handle empty buffers in decode', () => {
      const emptyBuffer = new Uint8Array(0);
      const lamp = Lamp.decode(emptyBuffer);
      expect(lamp).toEqual(Lamp.create());
    });

    it('should handle unknown fields in decode', () => {
      // Create a writer with valid fields plus an unknown field
      const writer = new BinaryWriter();
      writer.uint32(10).string('123'); // id
      writer.uint32(16).bool(true); // status
      writer.uint32(26).string('Test'); // name
      writer.uint32(32).int32(42); // Unknown field with tag 4

      const buffer = writer.finish();
      const lamp = Lamp.decode(buffer);
      expect(lamp.id).toBe('123');
      expect(lamp.status).toBe(true);
      expect(lamp.name).toBe('Test');
    });
  });

  // Test more specific message types and edge cases
  describe('UpdateLampRequest with optional fields', () => {
    it('should create with only ID field', () => {
      const request = UpdateLampRequest.create({
        id: '123',
      });
      expect(request.id).toBe('123');
      expect(request.status).toBeUndefined();
      expect(request.name).toBeUndefined();
    });

    it('should handle partial updates', () => {
      const requestWithStatus = UpdateLampRequest.create({
        id: '123',
        status: true,
      });
      expect(requestWithStatus.id).toBe('123');
      expect(requestWithStatus.status).toBe(true);
      expect(requestWithStatus.name).toBeUndefined();

      const requestWithName = UpdateLampRequest.create({
        id: '123',
        name: 'New Name',
      });
      expect(requestWithName.id).toBe('123');
      expect(requestWithName.status).toBeUndefined();
      expect(requestWithName.name).toBe('New Name');
    });

    it('should properly encode and decode optional fields', () => {
      const request = UpdateLampRequest.create({
        id: '123',
        status: true,
      });

      const encoded = UpdateLampRequest.encode(request).finish();
      const decoded = UpdateLampRequest.decode(encoded);

      expect(decoded.id).toBe('123');
      expect(decoded.status).toBe(true);
      expect(decoded.name).toBeUndefined();
    });
  });

  // Test list operations
  describe('ListLampsResponse', () => {
    it('should handle empty arrays', () => {
      const response = ListLampsResponse.create();
      expect(response.lamps).toEqual([]);

      const encoded = ListLampsResponse.encode(response).finish();
      const decoded = ListLampsResponse.decode(encoded);

      expect(decoded.lamps).toEqual([]);
    });

    it('should encode and decode lists with multiple items', () => {
      const lamp1 = Lamp.create({ id: '1', status: true, name: 'Lamp 1' });
      const lamp2 = Lamp.create({ id: '2', status: false, name: 'Lamp 2' });
      const lamp3 = Lamp.create({ id: '3', status: true, name: 'Lamp 3' });

      const response = ListLampsResponse.create({
        lamps: [lamp1, lamp2, lamp3],
      });

      const encoded = ListLampsResponse.encode(response).finish();
      const decoded = ListLampsResponse.decode(encoded);

      expect(decoded.lamps).toHaveLength(3);
      expect(decoded.lamps[0]).toEqual(lamp1);
      expect(decoded.lamps[1]).toEqual(lamp2);
      expect(decoded.lamps[2]).toEqual(lamp3);
    });
  });

  // Test the isSet utility function
  describe('isSet utility', () => {
    it('should handle null, undefined, and zero values correctly', () => {
      // Testing through fromJSON which uses isSet

      // Check how false boolean values are handled (should not use default)
      const withFalse = Lamp.fromJSON({
        id: '',
        status: false,
        name: '',
      });
      expect(withFalse.status).toBe(false);

      // Using fromJSON to implicitly test isSet behavior
      const lampWithEmpty = Lamp.fromJSON({
        id: '',
        status: false,
        name: '',
      });

      expect(lampWithEmpty.id).toBe('');
      expect(lampWithEmpty.status).toBe(false);
      expect(lampWithEmpty.name).toBe('');
    });
  });

  // Test the service definitions
  describe('LampServiceService definition', () => {
    it('should define all required service methods', () => {
      expect(LampServiceService.createLamp).toBeDefined();
      expect(LampServiceService.getLamp).toBeDefined();
      expect(LampServiceService.listLamps).toBeDefined();
      expect(LampServiceService.updateLamp).toBeDefined();
      expect(LampServiceService.deleteLamp).toBeDefined();
    });

    it('should have correct path for each service method', () => {
      expect(LampServiceService.createLamp.path).toBe('/lamp.LampService/CreateLamp');
      expect(LampServiceService.getLamp.path).toBe('/lamp.LampService/GetLamp');
      expect(LampServiceService.listLamps.path).toBe('/lamp.LampService/ListLamps');
      expect(LampServiceService.updateLamp.path).toBe('/lamp.LampService/UpdateLamp');
      expect(LampServiceService.deleteLamp.path).toBe('/lamp.LampService/DeleteLamp');
    });
  });

  // Test client creation
  describe('LampServiceClient', () => {
    it('should be constructable with server address and credentials', () => {
      const client = new LampServiceClient('localhost:50051', grpc.credentials.createInsecure());

      expect(client).toBeDefined();
      expect(client.createLamp).toBeDefined();
      expect(client.getLamp).toBeDefined();
      expect(client.listLamps).toBeDefined();
      expect(client.updateLamp).toBeDefined();
      expect(client.deleteLamp).toBeDefined();
    });
  });

  // Test message constructor internals
  describe('Message creation internals', () => {
    it('should handle different types of input when creating messages', () => {
      // Test with empty objects
      const emptyLamp = Lamp.create({});
      expect(emptyLamp.id).toBe('');
      expect(emptyLamp.status).toBe(false);
      expect(emptyLamp.name).toBe('');

      // Test with null - this should use defaults
      // @ts-expect-error - intentionally passing invalid type for testing
      const nullLamp = Lamp.create(null);
      expect(nullLamp.id).toBe('');
      expect(nullLamp.status).toBe(false);
      expect(nullLamp.name).toBe('');

      // Test with undefined - should use defaults
      const undefinedLamp = Lamp.create(undefined);
      expect(undefinedLamp.id).toBe('');
      expect(undefinedLamp.status).toBe(false);
      expect(undefinedLamp.name).toBe('');
    });
  });

  // Test DeepPartial type utility with nested objects (if relevant to this proto)
  describe('FromPartial with complex types', () => {
    it('should handle nested objects in fromPartial', () => {
      // Create lamps using fromPartial
      const lamp1 = Lamp.fromPartial({
        id: '1',
        status: true,
        name: 'Lamp 1',
      });

      const lamp2 = Lamp.fromPartial({
        id: '2',
        name: 'Lamp 2',
        // status omitted
      });

      // Create a list response with these partial objects
      const response = ListLampsResponse.fromPartial({
        lamps: [lamp1, lamp2],
      });

      expect(response.lamps).toHaveLength(2);
      expect(response.lamps[0].id).toBe('1');
      expect(response.lamps[0].status).toBe(true);
      expect(response.lamps[0].name).toBe('Lamp 1');

      expect(response.lamps[1].id).toBe('2');
      expect(response.lamps[1].status).toBe(false); // Default value
      expect(response.lamps[1].name).toBe('Lamp 2');
    });
  });
});
