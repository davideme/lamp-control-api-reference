import { resolvers } from '../resolvers';

describe('GraphQL Resolvers', () => {
  it('should have a Query resolver', () => {
    expect(resolvers.Query).toBeDefined();
  });

  it('should have a Mutation resolver', () => {
    expect(resolvers.Mutation).toBeDefined();
  });

  // Add more specific tests for each resolver function
});
