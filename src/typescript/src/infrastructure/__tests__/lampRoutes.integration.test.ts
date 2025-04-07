import request from 'supertest';
import { app } from '../server';
import { InMemoryLampRepository } from '../repositories/InMemoryLampRepository';
import { LampService } from '../../domain/services/LampService';
import { Lamp } from '../../domain/models/Lamp';

describe('Lamp API Integration Tests', () => {
  let repository: InMemoryLampRepository;
  let service: LampService;
  let testLamp: Lamp;

  beforeEach(async () => {
    repository = new InMemoryLampRepository();
    service = new LampService(repository);
    testLamp = await service.createLamp({
      name: 'Test Lamp',
      brightness: 50,
      color: '#FFFFFF',
    });
  });

  describe('GET /api/lamps', () => {
    it('should return all lamps', async () => {
      const response = await request(app).get('/api/lamps');
      expect(response.status).toBe(200);
      expect(response.body).toHaveLength(1);
      expect(response.body[0].id).toBe(testLamp.id);
    });
  });

  describe('POST /api/lamps', () => {
    it('should create a new lamp', async () => {
      const lampData = {
        name: 'New Lamp',
        brightness: 75,
        color: '#FF0000',
      };

      const response = await request(app)
        .post('/api/lamps')
        .send(lampData);

      expect(response.status).toBe(201);
      expect(response.body.name).toBe(lampData.name);
      expect(response.body.brightness).toBe(lampData.brightness);
      expect(response.body.color).toBe(lampData.color);
    });

    it('should return 400 for invalid lamp data', async () => {
      const invalidData = {
        name: '',
        brightness: 150,
        color: 'invalid',
      };

      const response = await request(app)
        .post('/api/lamps')
        .send(invalidData);

      expect(response.status).toBe(400);
      expect(response.body.error).toBe('ValidationError');
    });
  });

  describe('GET /api/lamps/:id', () => {
    it('should return a lamp by ID', async () => {
      const response = await request(app).get(`/api/lamps/${testLamp.id}`);
      expect(response.status).toBe(200);
      expect(response.body.id).toBe(testLamp.id);
    });

    it('should return 404 for non-existent lamp', async () => {
      const response = await request(app).get('/api/lamps/non-existent-id');
      expect(response.status).toBe(404);
      expect(response.body.error).toBe('LampNotFoundError');
    });
  });

  describe('PATCH /api/lamps/:id', () => {
    it('should update a lamp', async () => {
      const updateData = {
        name: 'Updated Lamp',
        brightness: 25,
        color: '#00FF00',
      };

      const response = await request(app)
        .patch(`/api/lamps/${testLamp.id}`)
        .send(updateData);

      expect(response.status).toBe(200);
      expect(response.body.name).toBe(updateData.name);
      expect(response.body.brightness).toBe(updateData.brightness);
      expect(response.body.color).toBe(updateData.color);
    });

    it('should return 404 for non-existent lamp', async () => {
      const response = await request(app)
        .patch('/api/lamps/non-existent-id')
        .send({ name: 'Updated' });

      expect(response.status).toBe(404);
      expect(response.body.error).toBe('LampNotFoundError');
    });
  });

  describe('DELETE /api/lamps/:id', () => {
    it('should delete a lamp', async () => {
      const response = await request(app).delete(`/api/lamps/${testLamp.id}`);
      expect(response.status).toBe(204);

      const getResponse = await request(app).get(`/api/lamps/${testLamp.id}`);
      expect(getResponse.status).toBe(404);
    });

    it('should return 404 for non-existent lamp', async () => {
      const response = await request(app).delete('/api/lamps/non-existent-id');
      expect(response.status).toBe(404);
      expect(response.body.error).toBe('LampNotFoundError');
    });
  });

  describe('POST /api/lamps/:id/toggle', () => {
    it('should toggle a lamp on/off', async () => {
      const initialState = testLamp.isOn;
      const response = await request(app).post(`/api/lamps/${testLamp.id}/toggle`);

      expect(response.status).toBe(200);
      expect(response.body.isOn).toBe(!initialState);

      const secondResponse = await request(app).post(`/api/lamps/${testLamp.id}/toggle`);
      expect(secondResponse.status).toBe(200);
      expect(secondResponse.body.isOn).toBe(initialState);
    });

    it('should return 404 for non-existent lamp', async () => {
      const response = await request(app).post('/api/lamps/non-existent-id/toggle');
      expect(response.status).toBe(404);
      expect(response.body.error).toBe('LampNotFoundError');
    });
  });
}); 