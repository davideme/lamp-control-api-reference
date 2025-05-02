export interface Lamp {
    id: string;
    status: boolean;
}

export type LampCreate = Omit<Lamp, 'id'>;

export type LampUpdate = Partial<Lamp>;