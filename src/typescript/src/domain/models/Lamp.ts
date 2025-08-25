export interface Lamp {
  id: string;
  status: boolean;
  createdAt: string;
  updatedAt: string;
}

export type LampCreate = Omit<Lamp, 'id' | 'createdAt' | 'updatedAt'>;

export type LampUpdate = Partial<Pick<Lamp, 'status'>>;
