import type { components } from '../../types/api';

type Lamp = components['schemas']['Lamp'];

export interface LampRepository {
    findAll(limit?: number): Lamp[];
    findById(id: string): Lamp | undefined;
    create(lamp: Omit<Lamp, 'id'>): Lamp;
    update(id: string, lamp: Partial<Lamp>): Lamp;
    delete(id: string): void;
}