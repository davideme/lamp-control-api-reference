import { LampService } from '../../domain/services/LampService';
import { Lamp } from '../../domain/models/Lamp';
import { logger } from '../../utils/logger';

export interface ResolverContext {
  lampService: LampService;
}

export const resolvers = {
  Query: {
    getLamp: async (
      _: unknown,
      { id }: { id: string },
      { lampService }: ResolverContext,
    ): Promise<Lamp | null> => {
      try {
        return await lampService.getLamp(id);
      } catch (error) {
        logger.error('Error fetching lamp by ID', { error, id });
        throw error;
      }
    },
    getLamps: async (
      _: unknown,
      __: unknown,
      { lampService }: ResolverContext,
    ): Promise<Lamp[]> => {
      try {
        return await lampService.getAllLamps();
      } catch (error) {
        logger.error('Error fetching all lamps', { error });
        throw error;
      }
    },
  },
  Mutation: {
    createLamp: async (
      _: unknown,
      { status }: { status: boolean },
      { lampService }: ResolverContext,
    ): Promise<Lamp> => {
      try {
        return await lampService.createLamp(status);
      } catch (error) {
        logger.error('Error creating lamp', { error, status });
        throw error;
      }
    },
    updateLamp: async (
      _: unknown,
      { id, status }: { id: string; status: boolean },
      { lampService }: ResolverContext,
    ): Promise<Lamp | null> => {
      try {
        return await lampService.updateLamp(id, status);
      } catch (error) {
        logger.error('Error updating lamp', { error, id, status });
        throw error;
      }
    },
    deleteLamp: async (
      _: unknown,
      { id }: { id: string },
      { lampService }: ResolverContext,
    ): Promise<boolean> => {
      try {
        return await lampService.deleteLamp(id);
      } catch (error) {
        logger.error('Error deleting lamp', { error, id });
        throw error;
      }
    },
  },
};
