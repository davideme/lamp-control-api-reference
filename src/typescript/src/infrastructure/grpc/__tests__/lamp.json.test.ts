import {
  Lamp,
  CreateLampRequest,
  GetLampRequest,
  ListLampsRequest,
  ListLampsResponse,
  UpdateLampRequest,
  DeleteLampRequest,
  DeleteLampResponse,
} from '../generated/lamp';

describe('Protocol Buffer JSON Conversion Tests', () => {
  describe('Lamp fromJSON/toJSON', () => {
    it('should convert Lamp to JSON', () => {
      const lamp = Lamp.create({
        id: 'lamp-123',
        status: true,
        name: 'Living Room Lamp',
      });

      const json = Lamp.toJSON(lamp);
      expect(json).toEqual({
        id: 'lamp-123',
        status: true,
        name: 'Living Room Lamp',
      });
    });

    it('should convert Lamp to JSON with default values omitted', () => {
      const lamp = Lamp.create({
        id: '',
        status: false,
        name: '',
      });

      const json = Lamp.toJSON(lamp);
      expect(json).toEqual({});
    });

    it('should convert JSON to Lamp', () => {
      const json = {
        id: 'lamp-123',
        status: true,
        name: 'Living Room Lamp',
      };

      const lamp = Lamp.fromJSON(json);
      expect(lamp).toEqual({
        id: 'lamp-123',
        status: true,
        name: 'Living Room Lamp',
      });
    });

    it('should handle empty JSON and set default values for Lamp', () => {
      const lamp = Lamp.fromJSON({});
      expect(lamp).toEqual({
        id: '',
        status: false,
        name: '',
      });
    });

    it('should handle null values in JSON for Lamp', () => {
      const lamp = Lamp.fromJSON({
        id: null,
        status: null,
        name: null,
      });

      expect(lamp).toEqual({
        id: '',
        status: false,
        name: '',
      });
    });
  });

  describe('CreateLampRequest fromJSON/toJSON', () => {
    it('should convert CreateLampRequest to JSON', () => {
      const request = CreateLampRequest.create({
        status: true,
        name: 'New Lamp',
      });

      const json = CreateLampRequest.toJSON(request);
      expect(json).toEqual({
        status: true,
        name: 'New Lamp',
      });
    });

    it('should convert CreateLampRequest to JSON with default values omitted', () => {
      const request = CreateLampRequest.create({
        status: false,
        name: '',
      });

      const json = CreateLampRequest.toJSON(request);
      expect(json).toEqual({});
    });

    it('should convert JSON to CreateLampRequest', () => {
      const json = {
        status: true,
        name: 'New Lamp',
      };

      const request = CreateLampRequest.fromJSON(json);
      expect(request).toEqual({
        status: true,
        name: 'New Lamp',
      });
    });

    it('should handle empty JSON and set default values for CreateLampRequest', () => {
      const request = CreateLampRequest.fromJSON({});
      expect(request).toEqual({
        status: false,
        name: '',
      });
    });
  });

  describe('GetLampRequest fromJSON/toJSON', () => {
    it('should convert GetLampRequest to JSON', () => {
      const request = GetLampRequest.create({
        id: 'lamp-123',
      });

      const json = GetLampRequest.toJSON(request);
      expect(json).toEqual({
        id: 'lamp-123',
      });
    });

    it('should convert GetLampRequest to JSON with default values omitted', () => {
      const request = GetLampRequest.create({
        id: '',
      });

      const json = GetLampRequest.toJSON(request);
      expect(json).toEqual({});
    });

    it('should convert JSON to GetLampRequest', () => {
      const json = {
        id: 'lamp-123',
      };

      const request = GetLampRequest.fromJSON(json);
      expect(request).toEqual({
        id: 'lamp-123',
      });
    });

    it('should handle empty JSON and set default values for GetLampRequest', () => {
      const request = GetLampRequest.fromJSON({});
      expect(request).toEqual({
        id: '',
      });
    });
  });

  describe('ListLampsRequest fromJSON/toJSON', () => {
    it('should convert ListLampsRequest to JSON', () => {
      const request = ListLampsRequest.create();

      const json = ListLampsRequest.toJSON(request);
      expect(json).toEqual({});
    });

    it('should convert JSON to ListLampsRequest', () => {
      const json = {};

      const request = ListLampsRequest.fromJSON(json);
      expect(request).toEqual({});
    });

    it('should handle additional properties in JSON for ListLampsRequest', () => {
      const request = ListLampsRequest.fromJSON({
        extraProp: 'ignored',
      });

      expect(request).toEqual({});
    });
  });

  describe('ListLampsResponse fromJSON/toJSON', () => {
    it('should convert ListLampsResponse with empty array to JSON', () => {
      const response = ListLampsResponse.create({
        lamps: [],
      });

      const json = ListLampsResponse.toJSON(response);
      expect(json).toEqual({});
    });

    it('should convert ListLampsResponse with lamps to JSON', () => {
      const response = ListLampsResponse.create({
        lamps: [
          Lamp.create({ id: 'lamp-1', status: true, name: 'Lamp 1' }),
          Lamp.create({ id: 'lamp-2', status: false, name: 'Lamp 2' }),
        ],
      });

      const json = ListLampsResponse.toJSON(response);
      expect(json).toEqual({
        lamps: [
          { id: 'lamp-1', status: true, name: 'Lamp 1' },
          { id: 'lamp-2', name: 'Lamp 2' },
        ],
      });
    });

    it('should convert JSON to ListLampsResponse', () => {
      const json = {
        lamps: [
          { id: 'lamp-1', status: true, name: 'Lamp 1' },
          { id: 'lamp-2', status: false, name: 'Lamp 2' },
        ],
      };

      const response = ListLampsResponse.fromJSON(json);
      expect(response.lamps.length).toBe(2);
      expect(response.lamps[0].id).toBe('lamp-1');
      expect(response.lamps[0].status).toBe(true);
      expect(response.lamps[0].name).toBe('Lamp 1');
      expect(response.lamps[1].id).toBe('lamp-2');
      expect(response.lamps[1].status).toBe(false);
      expect(response.lamps[1].name).toBe('Lamp 2');
    });

    it('should handle empty JSON and set default values for ListLampsResponse', () => {
      const response = ListLampsResponse.fromJSON({});
      expect(response).toEqual({
        lamps: [],
      });
    });

    it('should handle non-array lamps in JSON for ListLampsResponse', () => {
      const response = ListLampsResponse.fromJSON({
        lamps: 'not-an-array',
      });

      expect(response).toEqual({
        lamps: [],
      });
    });
  });

  describe('UpdateLampRequest fromJSON/toJSON', () => {
    it('should convert UpdateLampRequest with all fields to JSON', () => {
      const request = UpdateLampRequest.create({
        id: 'lamp-123',
        status: true,
        name: 'Updated Lamp',
      });

      const json = UpdateLampRequest.toJSON(request);
      expect(json).toEqual({
        id: 'lamp-123',
        status: true,
        name: 'Updated Lamp',
      });
    });

    it('should convert UpdateLampRequest with only required fields to JSON', () => {
      const request = UpdateLampRequest.create({
        id: 'lamp-123',
      });

      const json = UpdateLampRequest.toJSON(request);
      expect(json).toEqual({
        id: 'lamp-123',
      });
    });

    it('should convert UpdateLampRequest with only status field to JSON', () => {
      const request = UpdateLampRequest.create({
        id: 'lamp-123',
        status: false,
      });

      const json = UpdateLampRequest.toJSON(request);
      expect(json).toEqual({
        id: 'lamp-123',
        status: false,
      });
    });

    it('should convert UpdateLampRequest with only name field to JSON', () => {
      const request = UpdateLampRequest.create({
        id: 'lamp-123',
        name: 'Updated Lamp',
      });

      const json = UpdateLampRequest.toJSON(request);
      expect(json).toEqual({
        id: 'lamp-123',
        name: 'Updated Lamp',
      });
    });

    it('should convert JSON to UpdateLampRequest', () => {
      const json = {
        id: 'lamp-123',
        status: true,
        name: 'Updated Lamp',
      };

      const request = UpdateLampRequest.fromJSON(json);
      expect(request).toEqual({
        id: 'lamp-123',
        status: true,
        name: 'Updated Lamp',
      });
    });

    it('should convert partial JSON to UpdateLampRequest with only status field', () => {
      const json = {
        id: 'lamp-123',
        status: true,
      };

      const request = UpdateLampRequest.fromJSON(json);
      expect(request).toEqual({
        id: 'lamp-123',
        status: true,
        name: undefined,
      });
    });

    it('should convert partial JSON to UpdateLampRequest with only name field', () => {
      const json = {
        id: 'lamp-123',
        name: 'Updated Lamp',
      };

      const request = UpdateLampRequest.fromJSON(json);
      expect(request).toEqual({
        id: 'lamp-123',
        status: undefined,
        name: 'Updated Lamp',
      });
    });

    it('should handle empty JSON and set default values for UpdateLampRequest', () => {
      const request = UpdateLampRequest.fromJSON({});
      expect(request).toEqual({
        id: '',
        status: undefined,
        name: undefined,
      });
    });
  });

  describe('DeleteLampRequest fromJSON/toJSON', () => {
    it('should convert DeleteLampRequest to JSON', () => {
      const request = DeleteLampRequest.create({
        id: 'lamp-123',
      });

      const json = DeleteLampRequest.toJSON(request);
      expect(json).toEqual({
        id: 'lamp-123',
      });
    });

    it('should convert DeleteLampRequest to JSON with default values omitted', () => {
      const request = DeleteLampRequest.create({
        id: '',
      });

      const json = DeleteLampRequest.toJSON(request);
      expect(json).toEqual({});
    });

    it('should convert JSON to DeleteLampRequest', () => {
      const json = {
        id: 'lamp-123',
      };

      const request = DeleteLampRequest.fromJSON(json);
      expect(request).toEqual({
        id: 'lamp-123',
      });
    });

    it('should handle empty JSON and set default values for DeleteLampRequest', () => {
      const request = DeleteLampRequest.fromJSON({});
      expect(request).toEqual({
        id: '',
      });
    });
  });

  describe('DeleteLampResponse fromJSON/toJSON', () => {
    it('should convert DeleteLampResponse with success=true to JSON', () => {
      const response = DeleteLampResponse.create({
        success: true,
      });

      const json = DeleteLampResponse.toJSON(response);
      expect(json).toEqual({
        success: true,
      });
    });

    it('should convert DeleteLampResponse with default values to JSON (empty object)', () => {
      const response = DeleteLampResponse.create({
        success: false,
      });

      const json = DeleteLampResponse.toJSON(response);
      expect(json).toEqual({});
    });

    it('should convert JSON to DeleteLampResponse', () => {
      const json = {
        success: true,
      };

      const response = DeleteLampResponse.fromJSON(json);
      expect(response).toEqual({
        success: true,
      });
    });

    it('should handle empty JSON and set default values for DeleteLampResponse', () => {
      const response = DeleteLampResponse.fromJSON({});
      expect(response).toEqual({
        success: false,
      });
    });

    it('should handle non-boolean success in JSON for DeleteLampResponse', () => {
      const response = DeleteLampResponse.fromJSON({
        success: 'not-a-boolean',
      });

      expect(response).toEqual({
        success: true,
      });
    });
  });
});
