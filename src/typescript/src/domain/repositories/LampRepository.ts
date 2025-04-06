import { Lamp } from '../models/Lamp';

export interface LampRepository {
  // Save a lamp
  save(lamp: Lamp): Promise<Lamp>;

  // Find a lamp by its ID
  findById(id: string): Promise<Lamp | null>;

  // Find all lamps, with optional pagination
  findAll(options?: { skip?: number; take?: number }): Promise<Lamp[]>;

  // Delete a lamp by its ID
  delete(id: string): Promise<void>;

  // Count total number of lamps
  count(): Promise<number>;
} 