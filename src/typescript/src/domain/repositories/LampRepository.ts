import type { LampEntity, LampEntityCreate, LampEntityUpdate } from '../entities/LampEntity.ts';

export interface LampRepository {
  findAll(limit?: number): Promise<LampEntity[]>;
  findById(id: string): Promise<LampEntity | undefined>;
  create(lamp: LampEntityCreate): Promise<LampEntity>;
  update(id: string, lamp: LampEntityUpdate): Promise<LampEntity>;
  delete(id: string): Promise<void>;
}
