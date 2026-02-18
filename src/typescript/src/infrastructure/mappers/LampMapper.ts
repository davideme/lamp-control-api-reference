import type {
  LampEntity,
  LampEntityCreate,
  LampEntityUpdate,
} from '../../domain/entities/LampEntity.ts';
import type { components } from '../types/api.ts';

type LampApiModel = components['schemas']['Lamp'];
type LampCreateApiModel = components['schemas']['LampCreate'];
type LampUpdateApiModel = components['schemas']['LampUpdate'];

/**
 * Convert from domain entity to API model
 */
export function toApiModel(entity: LampEntity): LampApiModel {
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
export function toDomainEntity(apiModel: LampApiModel): LampEntity {
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
export function toDomainEntityCreate(apiModel: LampCreateApiModel): LampEntityCreate {
  return {
    status: apiModel.status,
  };
}

/**
 * Convert from API update model to domain update model
 */
export function toDomainEntityUpdate(apiModel: LampUpdateApiModel): LampEntityUpdate {
  return {
    status: apiModel.status,
  };
}
