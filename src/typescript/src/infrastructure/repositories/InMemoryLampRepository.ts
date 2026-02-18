import { LampNotFoundError } from '../../domain/errors/DomainError.ts';
import type {
  LampEntity,
  LampEntityCreate,
  LampEntityUpdate,
} from '../../domain/entities/LampEntity.ts';
import type { LampRepository } from '../../domain/repositories/LampRepository.ts';

export class InMemoryLampRepository implements LampRepository {
  private lamps: Map<string, LampEntity> = new Map();

  async findAll(limit?: number): Promise<LampEntity[]> {
    const lamps = Array.from(this.lamps.values());
    return limit ? lamps.slice(0, limit) : lamps;
  }

  async findById(id: string): Promise<LampEntity | undefined> {
    return this.lamps.get(id);
  }

  async create(lamp: LampEntityCreate): Promise<LampEntity> {
    const now = new Date().toISOString();
    const newLamp: LampEntity = {
      id: crypto.randomUUID(),
      createdAt: now,
      updatedAt: now,
      ...lamp,
    };
    this.lamps.set(newLamp.id, newLamp);
    return newLamp;
  }

  async update(id: string, lamp: LampEntityUpdate): Promise<LampEntity> {
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
