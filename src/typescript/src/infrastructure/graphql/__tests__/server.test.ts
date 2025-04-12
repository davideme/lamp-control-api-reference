import request from 'supertest';
import { createApp } from '../../server';
import { App } from 'supertest/types';

describe('GraphQL Server', () => {
  let app: App;

  beforeAll(async () => {
    app = await createApp();
  });

  it('should respond to a basic GraphQL query', async () => {
    const query = '{ __typename }';
    const response = await request(app).post('/graphql').send({ query });
    expect(response.status).toBe(200);
    expect(response.body).toHaveProperty('data');
  });

  // Add more integration tests for GraphQL queries and mutations
});
