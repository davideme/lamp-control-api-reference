import { LampEntity, LampEntityCreate, LampEntityUpdate } from '../../domain/entities/LampEntity.js';
import { components } from '../types/api.js';

type LampApiModel = components['schemas']['Lamp'];
type LampCreateApiModel = components['schemas']['LampCreate'];
type LampUpdateApiModel = components['schemas']['LampUpdate'];

/**
 * Mapper to convert between domain entities and API models.
 * This separation allows the internal domain model to evolve independently
 * from the external API contract.
 */
export class LampMapper {
  /**
   * Convert from domain entity to API model
   */
  static toApiModel(entity: LampEntity): LampApiModel {
    return {
      id: entity.id,
      status: entity.status,
      createdAt: entity.createdAt,
      updatedAt: entity.updatedAt,
    };
  }

  /**
   * Convert from API model to domain entity
   */
  static toDomainEntity(apiModel: LampApiModel): LampEntity {
    return {
      id: apiModel.id,
      status: apiModel.status,
      createdAt: apiModel.createdAt,
      updatedAt: apiModel.updatedAt,
    };
  }

  /**
   * Convert from API create model to domain create model
   */
  static toDomainEntityCreate(apiModel: LampCreateApiModel): LampEntityCreate {
    return {
      status: apiModel.status,
    };
  }

  /**
   * Convert from API update model to domain update model
   */
  static toDomainEntityUpdate(apiModel: LampUpdateApiModel): LampEntityUpdate {
    return {
      status: apiModel.status,
    };
  }
}
