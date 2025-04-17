import {
  Lamp,
  CreateLampRequest,
  GetLampRequest,
  ListLampsRequest,
  UpdateLampRequest,
  DeleteLampRequest,
  DeleteLampResponse,
} from '../generated/lamp';
import { BinaryReader, BinaryWriter } from '@bufbuild/protobuf/wire';

describe('Protocol Buffer Decode Function Coverage', () => {
  describe('Lamp message decode', () => {
    it('should decode message with all fields', () => {
      // Manually construct binary data for all fields
      const writer = new BinaryWriter();
      writer.uint32(10).string('lamp-123'); // id field (tag 1, string)
      writer.uint32(16).bool(true); // status field (tag 2, bool)
      writer.uint32(26).string('Living Room'); // name field (tag 3, string)

      const buffer = writer.finish();
      const decoded = Lamp.decode(buffer);

      expect(decoded.id).toBe('lamp-123');
      expect(decoded.status).toBe(true);
      expect(decoded.name).toBe('Living Room');
    });

    it('should decode message with fields in different order', () => {
      // Fields in different order from protobuf definition
      const writer = new BinaryWriter();
      writer.uint32(26).string('Kitchen'); // name field first (tag 3)
      writer.uint32(10).string('lamp-456'); // then id field (tag 1)
      writer.uint32(16).bool(false); // then status field (tag 2)

      const buffer = writer.finish();
      const decoded = Lamp.decode(buffer);

      expect(decoded.id).toBe('lamp-456');
      expect(decoded.status).toBe(false);
      expect(decoded.name).toBe('Kitchen');
    });

    it('should decode message with repeated field values (last one wins)', () => {
      // Repeat the same field multiple times
      const writer = new BinaryWriter();
      writer.uint32(10).string('old-id'); // First id value
      writer.uint32(10).string('new-id'); // Second id value (should override)
      writer.uint32(16).bool(false); // First status value
      writer.uint32(16).bool(true); // Second status value (should override)

      const buffer = writer.finish();
      const decoded = Lamp.decode(buffer);

      expect(decoded.id).toBe('new-id'); // Last value should win
      expect(decoded.status).toBe(true); // Last value should win
    });

    it('should handle skipping unknown wire types correctly', () => {
      const writer = new BinaryWriter();
      writer.uint32(10).string('lamp-id'); // id field (tag 1)

      // Unknown field with wire type 0 (varint)
      writer.uint32(40).int32(42); // Field 5, wire type 0

      // Unknown field with wire type 2 (length-delimited)
      writer.uint32(50).string('unknown data'); // Field 6, wire type 2

      // Unknown field with wire type 5 (32-bit)
      const tag7WireType5 = (7 << 3) | 5;
      writer.uint32(tag7WireType5).fixed32(1234);

      writer.uint32(16).bool(true); // status field (tag 2)

      const buffer = writer.finish();
      const decoded = Lamp.decode(buffer);

      // Should have skipped unknown fields and processed known ones
      expect(decoded.id).toBe('lamp-id');
      expect(decoded.status).toBe(true);
    });

    it('should handle early termination with end group marker', () => {
      const writer = new BinaryWriter();
      writer.uint32(10).string('lamp-id'); // id field (tag 1)

      // Add end group wire type (4) - this should terminate processing
      writer.uint32(4); // Wire type 4 is end group

      // This should not be processed due to early termination
      writer.uint32(16).bool(true);

      const buffer = writer.finish();
      const decoded = Lamp.decode(buffer);

      expect(decoded.id).toBe('lamp-id');
      expect(decoded.status).toBe(false); // Should be default, not true
    });

    it('should handle zero tag termination correctly', () => {
      const writer = new BinaryWriter();
      writer.uint32(10).string('lamp-id'); // id field (tag 1)

      // Add zero tag (this is used to terminate messages in some encodings)
      writer.uint32(0);

      // This should not be processed due to early termination
      writer.uint32(16).bool(true);

      const buffer = writer.finish();
      const decoded = Lamp.decode(buffer);

      expect(decoded.id).toBe('lamp-id');
      expect(decoded.status).toBe(false); // Should be default, not true
    });

    it('should handle id field (1) with wrong wire type', () => {
      const writer = new BinaryWriter();
      // Encode field 1 (id) as a varint (wire type 0) instead of string (wire type 2)
      writer.uint32(8).uint32(12345); // Field 1, wire type 0 (varint)
      writer.uint32(16).bool(true); // status field (tag 2, bool)
      writer.uint32(26).string('Test Lamp'); // name field (tag 3, string)

      const buffer = writer.finish();
      const decoded = Lamp.decode(buffer);

      // ID should be default value (empty string) since it couldn't be parsed properly
      expect(decoded.id).toBe('');
      expect(decoded.status).toBe(true);
      expect(decoded.name).toBe('Test Lamp');
    });

    it('should handle status field (2) with wrong wire type', () => {
      const writer = new BinaryWriter();
      writer.uint32(10).string('lamp-123'); // id field (tag 1, string)
      // Encode field 2 (status) as string (wire type 2) instead of boolean (wire type 0)
      writer.uint32(18).string('true'); // Field 2, wire type 2 (string)
      writer.uint32(26).string('Test Lamp'); // name field (tag 3, string)

      const buffer = writer.finish();
      const decoded = Lamp.decode(buffer);

      expect(decoded.id).toBe('lamp-123');
      // status should be default value (false) since it couldn't be parsed properly
      expect(decoded.status).toBe(false);
      expect(decoded.name).toBe('Test Lamp');
    });

    it('should handle name field (3) with wrong wire type', () => {
      const writer = new BinaryWriter();
      writer.uint32(10).string('lamp-123'); // id field (tag 1, string)
      writer.uint32(16).bool(true); // status field (tag 2, bool)
      // Encode field 3 (name) as varint (wire type 0) instead of string (wire type 2)
      writer.uint32(24).uint32(12345); // Field 3, wire type 0 (varint)

      const buffer = writer.finish();
      const decoded = Lamp.decode(buffer);

      expect(decoded.id).toBe('lamp-123');
      expect(decoded.status).toBe(true);
      // name should be default value (empty string) since it couldn't be parsed properly
      expect(decoded.name).toBe('');
    });
  });

  describe('CreateLampRequest decode', () => {
    it('should decode message with all fields', () => {
      const writer = new BinaryWriter();
      writer.uint32(8).bool(true); // status field (tag 1, bool)
      writer.uint32(18).string('New Lamp'); // name field (tag 2, string)

      const buffer = writer.finish();
      const decoded = CreateLampRequest.decode(buffer);

      expect(decoded.status).toBe(true);
      expect(decoded.name).toBe('New Lamp');
    });

    it('should handle status field (1) with wrong wire type', () => {
      const writer = new BinaryWriter();
      // Encode field 1 (status) as string (wire type 2) instead of boolean (wire type 0)
      writer.uint32(10).string('true'); // Field 1, wire type 2 (string)
      writer.uint32(18).string('New Lamp'); // name field (tag 2, string)

      const buffer = writer.finish();
      const decoded = CreateLampRequest.decode(buffer);

      // status should be default value (false) since it couldn't be parsed properly
      expect(decoded.status).toBe(false);
      expect(decoded.name).toBe('New Lamp');
    });

    it('should handle name field (2) with wrong wire type', () => {
      const writer = new BinaryWriter();
      writer.uint32(8).bool(true); // status field (tag 1, bool)
      // Encode field 2 (name) as varint (wire type 0) instead of string (wire type 2)
      writer.uint32(16).uint32(12345); // Field 2, wire type 0 (varint)

      const buffer = writer.finish();
      const decoded = CreateLampRequest.decode(buffer);

      expect(decoded.status).toBe(true);
      // name should be default value (empty string) since it couldn't be parsed properly
      expect(decoded.name).toBe('');
    });
  });

  describe('GetLampRequest decode', () => {
    it('should decode message with id field', () => {
      const writer = new BinaryWriter();
      writer.uint32(10).string('lamp-123'); // id field (tag 1, string)

      const buffer = writer.finish();
      const decoded = GetLampRequest.decode(buffer);

      expect(decoded.id).toBe('lamp-123');
    });

    it('should handle id field (1) with wrong wire type', () => {
      const writer = new BinaryWriter();
      // Encode field 1 (id) as a varint (wire type 0) instead of string (wire type 2)
      writer.uint32(8).uint32(12345); // Field 1, wire type 0 (varint)

      const buffer = writer.finish();
      const decoded = GetLampRequest.decode(buffer);

      // ID should be default value (empty string) since it couldn't be parsed properly
      expect(decoded.id).toBe('');
    });
  });

  describe('ListLampsRequest decode', () => {
    it('should decode empty message', () => {
      const buffer = new Uint8Array(0);
      const decoded = ListLampsRequest.decode(buffer);

      // ListLampsRequest is empty, so just check that it's an object
      expect(decoded).toBeDefined();
      expect(Object.keys(decoded).length).toBe(0);
    });

    it('should handle unknown field with wrong wire type', () => {
      // Even though ListLampsRequest has no fields, let's test it handles unknown fields properly
      const writer = new BinaryWriter();
      writer.uint32(8).uint32(123); // Field 1, wire type 0 (varint)

      const buffer = writer.finish();
      const decoded = ListLampsRequest.decode(buffer);

      // Should still be an empty object, ignoring unknown field
      expect(decoded).toBeDefined();
      expect(Object.keys(decoded).length).toBe(0);
    });
  });

  describe('UpdateLampRequest decode', () => {
    it('should decode with only required fields', () => {
      const writer = new BinaryWriter();
      writer.uint32(10).string('lamp-123'); // id field (tag 1, string)

      const buffer = writer.finish();
      const decoded = UpdateLampRequest.decode(buffer);

      expect(decoded.id).toBe('lamp-123');
      expect(decoded.status).toBeUndefined();
      expect(decoded.name).toBeUndefined();
    });

    it('should decode with all fields', () => {
      const writer = new BinaryWriter();
      writer.uint32(10).string('lamp-123'); // id field (tag 1, string)
      writer.uint32(16).bool(true); // status field (tag 2, bool)
      writer.uint32(26).string('Updated'); // name field (tag 3, string)

      const buffer = writer.finish();
      const decoded = UpdateLampRequest.decode(buffer);

      expect(decoded.id).toBe('lamp-123');
      expect(decoded.status).toBe(true);
      expect(decoded.name).toBe('Updated');
    });

    it('should handle id field (1) with wrong wire type', () => {
      const writer = new BinaryWriter();
      // Encode field 1 (id) as a varint (wire type 0) instead of string (wire type 2)
      writer.uint32(8).uint32(12345); // Field 1, wire type 0 (varint)
      writer.uint32(16).bool(true); // status field (tag 2, bool)
      writer.uint32(26).string('Updated'); // name field (tag 3, string)

      const buffer = writer.finish();
      const decoded = UpdateLampRequest.decode(buffer);

      // ID should be default value (empty string) since it couldn't be parsed properly
      expect(decoded.id).toBe('');
      expect(decoded.status).toBe(true);
      expect(decoded.name).toBe('Updated');
    });

    it('should handle status field (2) with wrong wire type', () => {
      const writer = new BinaryWriter();
      writer.uint32(10).string('lamp-123'); // id field (tag 1, string)
      // Encode field 2 (status) as string (wire type 2) instead of boolean (wire type 0)
      writer.uint32(18).string('true'); // Field 2, wire type 2 (string)
      writer.uint32(26).string('Updated'); // name field (tag 3, string)

      const buffer = writer.finish();
      const decoded = UpdateLampRequest.decode(buffer);

      expect(decoded.id).toBe('lamp-123');
      // status should be undefined since it couldn't be parsed properly and is optional
      expect(decoded.status).toBeUndefined();
      expect(decoded.name).toBe('Updated');
    });

    it('should handle name field (3) with wrong wire type', () => {
      const writer = new BinaryWriter();
      writer.uint32(10).string('lamp-123'); // id field (tag 1, string)
      writer.uint32(16).bool(true); // status field (tag 2, bool)
      // Encode field 3 (name) as varint (wire type 0) instead of string (wire type 2)
      writer.uint32(24).uint32(12345); // Field 3, wire type 0 (varint)

      const buffer = writer.finish();
      const decoded = UpdateLampRequest.decode(buffer);

      expect(decoded.id).toBe('lamp-123');
      expect(decoded.status).toBe(true);
      // name should be undefined since it couldn't be parsed properly and is optional
      expect(decoded.name).toBeUndefined();
    });
  });

  describe('DeleteLampRequest and Response decode', () => {
    it('should decode DeleteLampRequest', () => {
      const writer = new BinaryWriter();
      writer.uint32(10).string('lamp-123'); // id field (tag 1, string)

      const buffer = writer.finish();
      const decoded = DeleteLampRequest.decode(buffer);

      expect(decoded.id).toBe('lamp-123');
    });

    it('should decode DeleteLampResponse', () => {
      const writer = new BinaryWriter();
      writer.uint32(8).bool(true); // success field (tag 1, bool)

      const buffer = writer.finish();
      const decoded = DeleteLampResponse.decode(buffer);

      expect(decoded.success).toBe(true);
    });

    it('should handle DeleteLampRequest id field (1) with wrong wire type', () => {
      const writer = new BinaryWriter();
      // Encode field 1 (id) as a varint (wire type 0) instead of string (wire type 2)
      writer.uint32(8).uint32(12345); // Field 1, wire type 0 (varint)

      const buffer = writer.finish();
      const decoded = DeleteLampRequest.decode(buffer);

      // ID should be default value (empty string) since it couldn't be parsed properly
      expect(decoded.id).toBe('');
    });

    it('should handle DeleteLampResponse success field (1) with wrong wire type', () => {
      const writer = new BinaryWriter();
      // Encode field 1 (success) as string (wire type 2) instead of boolean (wire type 0)
      writer.uint32(10).string('true'); // Field 1, wire type 2 (string)

      const buffer = writer.finish();
      const decoded = DeleteLampResponse.decode(buffer);

      // success should be default value (false) since it couldn't be parsed properly
      expect(decoded.success).toBe(false);
    });
  });

  describe('BinaryReader constructor variants in decode', () => {
    it('should decode using BinaryReader object directly', () => {
      const writer = new BinaryWriter();
      writer.uint32(10).string('lamp-id');
      writer.uint32(16).bool(true);

      const buffer = writer.finish();
      const reader = new BinaryReader(buffer);

      const decoded = Lamp.decode(reader);

      expect(decoded.id).toBe('lamp-id');
      expect(decoded.status).toBe(true);
    });

    it('should decode with explicit length parameter', () => {
      const writer = new BinaryWriter();
      writer.uint32(10).string('lamp-id');
      writer.uint32(16).bool(true);

      const buffer = writer.finish();

      // Only decode part of the buffer (just the id field)
      const partialLength = buffer.findIndex((b) => b === 16); // Find where the status field starts
      const decoded = Lamp.decode(buffer, partialLength);

      expect(decoded.id).toBe('lamp-id');
      expect(decoded.status).toBe(false); // Should not have read the status field
    });
  });
});
