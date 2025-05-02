import type { components } from './types/api';

type Lamp = components['schemas']['Lamp'];

export interface LampRepository {
    findAll(limit?: number): Lamp[];
    findById(id: string): Lamp | undefined;
    create(lamp: Omit<Lamp, 'id'>): Lamp;
    update(id: string, lamp: Partial<Lamp>): Lamp;
    delete(id: string): void;
}

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
            ...lamp
        };
        this.lamps.set(newLamp.id, newLamp);
        return newLamp;
    }

    update(id: string, lamp: Partial<Lamp>): Lamp {
        const existingLamp = this.lamps.get(id);
        if (!existingLamp) {
            throw { statusCode: 404, message: 'Lamp not found' };
        }
        const updatedLamp = { ...existingLamp, ...lamp };
        this.lamps.set(id, updatedLamp);
        return updatedLamp;
    }

    delete(id: string): void {
        if (!this.lamps.has(id)) {
            throw { statusCode: 404, message: 'Lamp not found' };
        }
        this.lamps.delete(id);
    }
} 