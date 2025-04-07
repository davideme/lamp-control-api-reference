import { Lamp } from '../../domain/models/Lamp';
import { LampRepository } from '../../domain/repositories/LampRepository';

export class InMemoryLampRepository implements LampRepository {
  private lamps: Map<string, Lamp>;

  constructor() {
    this.lamps = new Map<string, Lamp>();
  }

  async save(lamp: Lamp): Promise<void> {
    this.lamps.set(lamp.id, lamp);
  }

  async findById(id: string): Promise<Lamp | null> {
    return this.lamps.get(id) || null;
  }

  async findAll(): Promise<Lamp[]> {
    return Array.from(this.lamps.values());
  }

  async delete(id: string): Promise<void> {
    this.lamps.delete(id);
  }

  async clear(): Promise<void> {
    this.lamps.clear();
  }
} 