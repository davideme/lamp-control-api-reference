import { LampMapper } from './LampMapper.ts';
import type { LampEntity } from '../../domain/entities/LampEntity.ts';

describe('LampMapper', () => {
  describe('toApiModel', () => {
    it('should convert domain entity to API model', () => {
      const entity: LampEntity = {
        id: '123',
        status: true,
        createdAt: '2025-01-01T00:00:00.000Z',
        updatedAt: '2025-01-01T00:00:00.000Z',
      };

      const apiModel = LampMapper.toApiModel(entity);

      expect(apiModel).toEqual({
        id: '123',
        status: true,
        createdAt: '2025-01-01T00:00:00.000Z',
        updatedAt: '2025-01-01T00:00:00.000Z',
      });
    });
  });

  describe('toDomainEntity', () => {
    it('should convert API model to domain entity', () => {
      const apiModel = {
        id: '456',
        status: false,
        createdAt: '2025-01-02T00:00:00.000Z',
        updatedAt: '2025-01-02T00:00:00.000Z',
      };

      const entity = LampMapper.toDomainEntity(apiModel);

      expect(entity).toEqual({
        id: '456',
        status: false,
        createdAt: '2025-01-02T00:00:00.000Z',
        updatedAt: '2025-01-02T00:00:00.000Z',
      });
    });
  });

  describe('toDomainEntityCreate', () => {
    it('should convert API create model to domain create model', () => {
      const apiModel = { status: true };

      const domainModel = LampMapper.toDomainEntityCreate(apiModel);

      expect(domainModel).toEqual({ status: true });
    });
  });

  describe('toDomainEntityUpdate', () => {
    it('should convert API update model to domain update model', () => {
      const apiModel = { status: false };

      const domainModel = LampMapper.toDomainEntityUpdate(apiModel);

      expect(domainModel).toEqual({ status: false });
    });
  });
});
