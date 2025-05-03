import { Lamp, LampCreate, LampUpdate } from '../models/Lamp';

export interface LampRepository {
  findAll(limit?: number): Promise<Lamp[]>;
  findById(id: string): Promise<Lamp | undefined>;
  create(lamp: LampCreate): Promise<Lamp>;
  update(id: string, lamp: LampUpdate): Promise<Lamp>;
  delete(id: string): Promise<void>;
}
