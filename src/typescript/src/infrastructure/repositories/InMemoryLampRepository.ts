import { Lamp } from '../../domain/models/Lamp';
import { LampRepository } from '../../domain/repositories/LampRepository';

export class InMemoryLampRepository implements LampRepository {
  private lamps: Map<string, Lamp> = new Map();

  async save(lamp: Lamp): Promise<Lamp> {
    this.lamps.set(lamp.id, lamp);
    return lamp;
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
} 