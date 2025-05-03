import { Lamp } from '../models/Lamp';

export interface LampRepository {
  findAll(limit?: number): Promise<Lamp[]>;
  findById(id: string): Promise<Lamp | undefined>;
  create(lamp: Omit<Lamp, 'id'>): Promise<Lamp>;
  update(id: string, lamp: Partial<Lamp>): Promise<Lamp>;
  delete(id: string): Promise<void>;
}
