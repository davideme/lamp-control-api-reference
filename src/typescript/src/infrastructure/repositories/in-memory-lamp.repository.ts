import { LampNotFoundError } from '../../domain/errors/DomainError';
import { Lamp } from '../../domain/models/Lamp';
import { LampRepository } from '../../domain/repositories/LampRepository';

export class InMemoryLampRepository implements LampRepository {
  private lamps: Map<string, Lamp> = new Map();

  findAll(limit?: number): Lamp[] {
    const lamps = Array.from(this.lamps.values());
    return limit ? lamps.slice(0, limit) : lamps;
  }

  findById(id: string): Lamp | undefined {
    return this.lamps.get(id);
  }

  create(lamp: Omit<Lamp, 'id'>): Lamp {
    const newLamp: Lamp = {
      id: crypto.randomUUID(),
      ...lamp,
    };
    this.lamps.set(newLamp.id, newLamp);
    return newLamp;
  }

  update(id: string, lamp: Partial<Lamp>): Lamp {
    const existingLamp = this.lamps.get(id);
    if (!existingLamp) {
      throw new LampNotFoundError(id);
    }
    const updatedLamp = { ...existingLamp, ...lamp };
    this.lamps.set(id, updatedLamp);
    return updatedLamp;
  }

  delete(id: string): void {
    if (!this.lamps.has(id)) {
      throw new LampNotFoundError(id);
    }
    this.lamps.delete(id);
  }
}
