import { v4 as uuidv4 } from 'uuid';
import { Lamp } from '../models/Lamp';
import { LampRepository } from '../repositories/LampRepository';
import { ValidationError, LampNotFoundError } from '../errors/DomainError';
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
  constructor(private repository: LampRepository) {}

  async createLamp(data: CreateLampData): Promise<Lamp> {
    try {
      const lamp = new Lamp(uuidv4(), data.name, {
        brightness: data.brightness,
        color: data.color,
      });

      await this.repository.save(lamp);
      appLogger.info('Lamp created successfully', {
        lampId: lamp.id,
        name: lamp.name,
      });

      return lamp;
    } catch (error) {
      appLogger.error('Failed to create lamp', { error: error as Error, data });
      throw new ValidationError('Invalid lamp state');
    }
  }

  async getLamp(id: string): Promise<Lamp> {
    const lamp = await this.repository.findById(id);
    if (!lamp) {
      appLogger.warn('Lamp not found', { lampId: id });
      throw new LampNotFoundError(id);
    }
    return lamp;
  }

  async getAllLamps(): Promise<Lamp[]> {
    return this.repository.findAll();
  }

  async updateLamp(id: string, data: UpdateLampData): Promise<Lamp> {
    try {
      const lamp = await this.getLamp(id);

      if (data.name !== undefined) {
        lamp.setName(data.name);
      }

      if (data.brightness !== undefined) {
        lamp.setBrightness(data.brightness);
      }

      if (data.color !== undefined) {
        lamp.setColor(data.color);
      }

      await this.repository.save(lamp);
      return lamp;
    } catch (error) {
      appLogger.error('Failed to update lamp', { error: error as Error, lampId: id, data });
      if (error instanceof LampNotFoundError) {
        throw error;
      }
      throw new ValidationError('Invalid lamp state');
    }
  }

  async deleteLamp(id: string): Promise<void> {
    await this.getLamp(id); // Verify lamp exists
    await this.repository.delete(id);
  }

  async toggleLamp(id: string): Promise<Lamp> {
    const lamp = await this.getLamp(id);
    lamp.toggle();
    await this.repository.save(lamp);
    return lamp;
  }
}
