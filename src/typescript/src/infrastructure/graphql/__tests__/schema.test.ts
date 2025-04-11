import { schema } from '../schema';
import { printSchema } from 'graphql';

describe('GraphQL Schema', () => {
  it('should be a valid GraphQL schema', () => {
    const schemaString = printSchema(schema);
    expect(schemaString).toContain('type Query');
    expect(schemaString).toContain('type Mutation');
  });

  // Add more tests for specific schema definitions
});
