import { LampNotFoundError } from '../../domain/errors/DomainError';
import { Lamp, LampCreate, LampUpdate } from '../../domain/models/Lamp';
import { LampRepository } from '../../domain/repositories/LampRepository';

export class InMemoryLampRepository implements LampRepository {
  private lamps: Map<string, Lamp> = new Map();

  async findAll(limit?: number): Promise<Lamp[]> {
    const lamps = Array.from(this.lamps.values());
    return limit ? lamps.slice(0, limit) : lamps;
  }

  async findById(id: string): Promise<Lamp | undefined> {
    return this.lamps.get(id);
  }

  async create(lamp: LampCreate): Promise<Lamp> {
    const now = new Date().toISOString();
    const newLamp: Lamp = {
      id: crypto.randomUUID(),
      createdAt: now,
      updatedAt: now,
      ...lamp,
    };
    this.lamps.set(newLamp.id, newLamp);
    return newLamp;
  }

  async update(id: string, lamp: LampUpdate): Promise<Lamp> {
    const existingLamp = this.lamps.get(id);
    if (!existingLamp) {
      throw new LampNotFoundError(id);
    }
    const updatedLamp = {
      ...existingLamp,
      ...lamp,
      updatedAt: new Date().toISOString(),
    };
    this.lamps.set(id, updatedLamp);
    return updatedLamp;
  }

  async delete(id: string): Promise<void> {
    if (!this.lamps.has(id)) {
      throw new LampNotFoundError(id);
    }
    this.lamps.delete(id);
  }
}
