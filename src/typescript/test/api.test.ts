import { FastifyInstance } from 'fastify';
import { buildApp } from '../src/infrastructure/app';
import supertest from 'supertest';

describe('Lamp API Endpoints', () => {
    let app: FastifyInstance;
    let request: supertest.SuperTest<supertest.Test>;

    beforeAll(async () => {
        app = await buildApp();
        await app.ready()
        request = supertest(app.server);
    });

    afterAll(async () => {
        await app.close();
    });

    describe('GET /v1/lamps', () => {
        it('should return a list of lamps', async () => {
            const response = await request
                .get('/v1/lamps')
                .expect('Content-Type', /json/)
                .expect(200);

            expect(Array.isArray(response.body)).toBe(true);
        });
    });

    describe('GET /v1/lamps/:lampId', () => {
        it('should return a specific lamp', async () => {
            // First create a lamp to test with
            const createResponse = await request
                .post('/v1/lamps')
                .send({ status: true })
                .expect(201);

            const lampId = createResponse.body.id;

            const response = await request
                .get(`/v1/lamps/${lampId}`)
                .expect('Content-Type', /json/)
                .expect(200);

            expect(response.body).toHaveProperty('id', lampId);
            expect(response.body).toHaveProperty('status', true);
        });

        it('should return 404 for non-existent lamp', async () => {
            await request
                .get('/v1/lamps/non-existent-id')
                .expect(404);
        });
    });

    describe('POST /v1/lamps', () => {
        it('should create a new lamp', async () => {
            const response = await request
                .post('/v1/lamps')
                .send({ status: true })
                .expect('Content-Type', /json/)
                .expect(201);

            expect(response.body).toHaveProperty('id');
            expect(response.body).toHaveProperty('status', true);
        });

        it('should validate request body', async () => {
            await request
                .post('/v1/lamps')
                .send({ invalid: 'data' })
                .expect(400);
        });
    });

    describe('PUT /v1/lamps/:lampId', () => {
        it('should update an existing lamp', async () => {
            // First create a lamp to test with
            const createResponse = await request
                .post('/v1/lamps')
                .send({ status: true })
                .expect(201);

            const lampId = createResponse.body.id;

            const response = await request
                .put(`/v1/lamps/${lampId}`)
                .send({ status: false })
                .expect('Content-Type', /json/)
                .expect(200);

            expect(response.body).toHaveProperty('id', lampId);
            expect(response.body).toHaveProperty('status', false);
        });

        it('should return 404 for non-existent lamp', async () => {
            await request
                .put('/v1/lamps/non-existent-id')
                .send({ status: false })
                .expect(404);
        });

        it('should validate request body', async () => {
            // First create a lamp to test with
            const createResponse = await request
                .post('/v1/lamps')
                .send({ status: true })
                .expect(201);

            const lampId = createResponse.body.id;

            await request
                .put(`/v1/lamps/${lampId}`)
                .send({ invalid: 'data' })
                .expect(400);
        });
    });

    describe('DELETE /v1/lamps/:lampId', () => {
        it('should delete an existing lamp', async () => {
            // First create a lamp to test with
            const createResponse = await request
                .post('/v1/lamps')
                .send({ status: true })
                .expect(201);

            const lampId = createResponse.body.id;

            await request
                .delete(`/v1/lamps/${lampId}`)
                .expect(204);

            // Verify the lamp is deleted
            await request
                .get(`/v1/lamps/${lampId}`)
                .expect(404);
        });

        it('should return 404 for non-existent lamp', async () => {
            await request
                .delete('/v1/lamps/non-existent-id')
                .expect(404);
        });
    });
}); 