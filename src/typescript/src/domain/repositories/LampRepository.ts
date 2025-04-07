import { Lamp } from '../models/Lamp';

export interface LampRepository {
  // Save a lamp
  save(lamp: Lamp): Promise<Lamp>;

  // Find a lamp by its ID
  findById(id: string): Promise<Lamp | null>;

  // Find all lamps, with optional pagination
  findAll(): Promise<Lamp[]>;

  // Delete a lamp by its ID
  delete(id: string): Promise<void>;
} 