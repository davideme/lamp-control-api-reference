import { ApolloServer } from '@apollo/server';
import { expressMiddleware } from '@apollo/server/express4';
import { LampService } from '../../domain/services/LampService';
import { typeDefs } from './schema';
import { resolvers, ResolverContext } from './resolvers';
import { Express } from 'express';
import cors from 'cors';
import { json } from 'express';
import { appLogger } from '../../utils/logger';

export const setupGraphQLServer = async (app: Express, lampService: LampService): Promise<void> => {
  // Create the Apollo Server instance
  const server = new ApolloServer<ResolverContext>({
    typeDefs,
    resolvers,
    includeStacktraceInErrorResponses: process.env.NODE_ENV !== 'production',
    formatError: (formattedError, _) => {
      appLogger.error('GraphQL error', { error: formattedError });
      // For production, hide implementation details
      if (process.env.NODE_ENV === 'production') {
        return {
          message: formattedError.message,
          path: formattedError.path,
        };
      }
      return formattedError;
    },
  });

  // Start the Apollo Server
  await server.start();

  // Apply the Apollo GraphQL middleware to the Express app
  app.use(
    '/graphql',
    cors<cors.CorsRequest>(),
    json(),
    expressMiddleware(server, {
      context: async (): Promise<ResolverContext> => ({ lampService }),
    }),
  );

  appLogger.info('GraphQL API ready at /graphql');
};
