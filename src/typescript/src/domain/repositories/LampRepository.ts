import { LampEntity, LampEntityCreate, LampEntityUpdate } from '../entities/LampEntity.js';

export interface LampRepository {
  findAll(limit?: number): Promise<LampEntity[]>;
  findById(id: string): Promise<LampEntity | undefined>;
  create(lamp: LampEntityCreate): Promise<LampEntity>;
  update(id: string, lamp: LampEntityUpdate): Promise<LampEntity>;
  delete(id: string): Promise<void>;
}
