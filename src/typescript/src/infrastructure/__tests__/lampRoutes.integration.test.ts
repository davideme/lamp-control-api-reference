import request from 'supertest';
import { v4 as uuidv4 } from 'uuid';
import { createApp } from '../server';
import { InMemoryLampRepository } from '../repositories/InMemoryLampRepository';
import { Lamp } from '../../domain/models/Lamp';

describe('Lamp Routes Integration Tests', () => {
  let repository: InMemoryLampRepository;
  let testLamp: Lamp;
  let app: ReturnType<typeof createApp>;

  beforeEach(async () => {
    repository = new InMemoryLampRepository();
    app = createApp(repository);
    await repository.clear();

    testLamp = new Lamp(uuidv4(), 'Test Lamp', {
      brightness: 100,
      color: '#FFFFFF',
    });
    await repository.save(testLamp);
  });

  describe('GET /api/lamps', () => {
    it('should return all lamps', async () => {
      const response = await request(app).get('/api/lamps').expect(200);

      expect(response.body).toHaveLength(1);
      expect(response.body[0].id).toBe(testLamp.id);
      expect(response.body[0].name).toBe(testLamp.name);
    });
  });

  describe('GET /api/lamps/:id', () => {
    it('should return a lamp by id', async () => {
      const response = await request(app).get(`/api/lamps/${testLamp.id}`).expect(200);

      expect(response.body.id).toBe(testLamp.id);
      expect(response.body.name).toBe(testLamp.name);
    });

    it('should return 404 for non-existent lamp', async () => {
      await request(app).get('/api/lamps/non-existent-id').expect(404);
    });
  });

  describe('POST /api/lamps', () => {
    it('should create a new lamp', async () => {
      const newLamp = {
        name: 'New Lamp',
        brightness: 75,
        color: '#FF0000',
      };

      const response = await request(app).post('/api/lamps').send(newLamp).expect(201);

      expect(response.body.name).toBe(newLamp.name);
      expect(response.body.brightness).toBe(newLamp.brightness);
      expect(response.body.color).toBe(newLamp.color);
      expect(response.body.id).toBeDefined();
    });

    it('should return 400 for invalid lamp data', async () => {
      const invalidLamp = {
        name: 'Invalid Lamp',
        brightness: 150, // Invalid brightness
        color: 'invalid-color',
      };

      await request(app).post('/api/lamps').send(invalidLamp).expect(400);
    });
  });

  describe('PATCH /api/lamps/:id', () => {
    it('should update an existing lamp', async () => {
      const updates = {
        name: 'Updated Lamp',
        brightness: 50,
        color: '#00FF00',
      };

      const response = await request(app)
        .patch(`/api/lamps/${testLamp.id}`)
        .send(updates)
        .expect(200);

      expect(response.body.name).toBe(updates.name);
      expect(response.body.brightness).toBe(updates.brightness);
      expect(response.body.color).toBe(updates.color);
    });

    it('should return 404 for non-existent lamp', async () => {
      await request(app).patch('/api/lamps/non-existent-id').send({ name: 'Updated' }).expect(404);
    });
  });

  describe('DELETE /api/lamps/:id', () => {
    it('should delete an existing lamp', async () => {
      await request(app).delete(`/api/lamps/${testLamp.id}`).expect(204);

      await request(app).get(`/api/lamps/${testLamp.id}`).expect(404);
    });

    it('should return 404 for non-existent lamp', async () => {
      await request(app).delete('/api/lamps/non-existent-id').expect(404);
    });
  });

  describe('POST /api/lamps/:id/toggle', () => {
    it('should toggle lamp state', async () => {
      const response = await request(app).post(`/api/lamps/${testLamp.id}/toggle`).expect(200);

      expect(response.body.isOn).toBe(true);

      const secondResponse = await request(app)
        .post(`/api/lamps/${testLamp.id}/toggle`)
        .expect(200);

      expect(secondResponse.body.isOn).toBe(false);
    });

    it('should return 404 for non-existent lamp', async () => {
      await request(app).post('/api/lamps/non-existent-id/toggle').expect(404);
    });
  });
});
