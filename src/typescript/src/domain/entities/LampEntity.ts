/**
 * Domain entity representing a Lamp in the internal model.
 * This is separate from the HTTP API model to allow independent evolution
 * of the internal domain logic and external API contract.
 */
export interface LampEntity {
  id: string;
  status: boolean;
  createdAt: string;
  updatedAt: string;
}

export type LampEntityCreate = Omit<LampEntity, 'id' | 'createdAt' | 'updatedAt'>;

export type LampEntityUpdate = Partial<Pick<LampEntity, 'status'>>;
