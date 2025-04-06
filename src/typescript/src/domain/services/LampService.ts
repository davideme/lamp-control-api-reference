import { v4 as uuidv4 } from 'uuid';
import { Lamp } from '../models/Lamp';
import { LampRepository } from '../repositories/LampRepository';
import { LampNotFoundError, ValidationError } from '../errors/DomainError';
import { appLogger } from '../../utils/logger';

export interface CreateLampData {
  name: string;
  brightness?: number;
  color?: string;
}

export interface UpdateLampData {
  name?: string;
  brightness?: number;
  color?: string;
}

export class LampService {
  constructor(private readonly repository: LampRepository) {}

  async createLamp(data: CreateLampData): Promise<Lamp> {
    try {
      const lamp = new Lamp(uuidv4(), data.name, {
        brightness: data.brightness,
        color: data.color,
      });

      await this.repository.save(lamp);
      appLogger.info('Lamp created successfully', { lampId: lamp.id, name: lamp.name });
      return lamp;
    } catch (error) {
      appLogger.error('Failed to create lamp', error as Error, { data });
      if (error instanceof Error) {
        throw new ValidationError(error.message);
      }
      throw error;
    }
  }

  async getLamp(id: string): Promise<Lamp> {
    const lamp = await this.repository.findById(id);
    if (!lamp) {
      appLogger.warn('Lamp not found', { lampId: id });
      throw new LampNotFoundError(id);
    }
    appLogger.debug('Lamp retrieved', { lampId: id });
    return lamp;
  }

  async getAllLamps(): Promise<Lamp[]> {
    const lamps = await this.repository.findAll();
    appLogger.debug('Retrieved all lamps', { count: lamps.length });
    return lamps;
  }

  async updateLamp(id: string, data: UpdateLampData): Promise<Lamp> {
    try {
      const lamp = await this.getLamp(id);


      await this.repository.save(lamp);
      appLogger.info('Lamp updated successfully', { lampId: id, updates: data });
      return lamp;
    } catch (error) {
      appLogger.error('Failed to update lamp', error as Error, { lampId: id, data });
      if (error instanceof ValidationError || error instanceof LampNotFoundError) {
        throw error;
      }
      throw new ValidationError('Failed to update lamp');
    }
  }

  async deleteLamp(id: string): Promise<void> {
    const lamp = await this.getLamp(id);
    await this.repository.delete(lamp.id);
    appLogger.info('Lamp deleted successfully', { lampId: id });
  }

  async toggleLamp(id: string): Promise<Lamp> {
    const lamp = await this.getLamp(id);
    lamp.isOn ? lamp.turnOff() : lamp.turnOn();
    await this.repository.save(lamp);
    appLogger.info('Lamp toggled', { lampId: id, isOn: lamp.isOn });
    return lamp;
  }
} 