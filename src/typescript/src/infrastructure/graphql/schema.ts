// GraphQL schema definition
import { makeExecutableSchema } from '@graphql-tools/schema';
import { resolvers } from './resolvers';

export const typeDefs = `#graphql
  type Lamp {
    id: ID!
    status: Boolean!
  }

  type Query {
    getLamp(id: ID!): Lamp
    getLamps: [Lamp!]!
  }

  type Mutation {
    createLamp(status: Boolean!): Lamp!
    updateLamp(id: ID!, status: Boolean!): Lamp
    deleteLamp(id: ID!): Boolean!
  }
`;

// Create and export the executable schema
export const schema = makeExecutableSchema({
  typeDefs,
  resolvers,
});
