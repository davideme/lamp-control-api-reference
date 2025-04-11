import { LampService } from '../../domain/services/LampService';
import { Lamp } from '../../domain/models/Lamp';
import { appLogger } from '../../utils/logger';

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
        appLogger.error('Error fetching lamp by ID', { error, id });
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
        appLogger.error('Error fetching all lamps', { error });
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
        // Create a new lamp with default name and status property mapped to isOn
        return await lampService.createLamp({
          name: `Lamp ${new Date().toISOString()}`,
          isOn: status, // Map the status parameter to the isOn property
        });
      } catch (error) {
        appLogger.error('Error creating lamp', { error, status });
        throw error;
      }
    },
    updateLamp: async (
      _: unknown,
      { id, isOn }: { id: string; isOn: boolean },
      { lampService }: ResolverContext,
    ): Promise<Lamp | null> => {
      try {
        // Update lamp with appropriate properties matching your LampService
        return await lampService.updateLamp(id, {
          isOn, // Include the status parameter to update the lamp's status
        });
      } catch (error) {
        appLogger.error('Error updating lamp', { error, id, isOn });
        throw error;
      }
    },
    deleteLamp: async (
      _: unknown,
      { id }: { id: string },
      { lampService }: ResolverContext,
    ): Promise<boolean> => {
      try {
        // Call the deleteLamp method from LampService
        await lampService.deleteLamp(id);
        // Return true to indicate success since GraphQL schema expects a boolean
        return true;
      } catch (error) {
        appLogger.error('Error deleting lamp', { error, id });
        throw error;
      }
    },
  },
};
