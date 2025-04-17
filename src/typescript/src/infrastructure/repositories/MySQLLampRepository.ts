import { PrismaClient } from '@prisma/client';
import { Lamp } from '../../domain/models/Lamp';
import { LampRepository } from '../../domain/repositories/LampRepository';
import { appLogger } from '../../utils/logger';
import dbConfig from '../../config/database';

interface PrismaLampModel {
  id: string;
  name: string;
  isOn: boolean;
  createdAt: Date;
  updatedAt: Date;
  deletedAt: Date | null;
}

export class MySQLLampRepository implements LampRepository {
  private prisma: PrismaClient;

  constructor(prismaClient?: PrismaClient) {
    this.prisma =
      prismaClient ||
      new PrismaClient({
        datasources: {
          mysql: {
            url: dbConfig.mysql.connectionString,
          },
        },
      });
    appLogger.info('MySQLLampRepository initialized');
  }

  // Domain entity to database model conversion
  private domainToModel(lamp: Lamp): PrismaLampModel {
    return {
      id: lamp.id,
      name: lamp.name,
      isOn: lamp.isOn,
      updatedAt: lamp.updatedAt || new Date(),
      createdAt: lamp.createdAt || new Date(),
      deletedAt: null,
    };
  }

  // Database model to domain entity conversion
  private modelToDomain(model: PrismaLampModel): Lamp | null {
    if (!model) return null;

    return new Lamp(model.id, model.name, {
      isOn: model.isOn,
    });
  }

  async save(lamp: Lamp): Promise<void> {
    try {
      await this.prisma.lamp.upsert({
        where: { id: lamp.id },
        update: this.domainToModel(lamp),
        create: {
          id: lamp.id,
          name: lamp.name,
          isOn: lamp.isOn,
          createdAt: new Date(),
          updatedAt: new Date(),
        },
      });
      appLogger.debug('Lamp saved to MySQL', { lampId: lamp.id });
    } catch (error) {
      appLogger.error('Error saving lamp to MySQL', { error, lampId: lamp.id });
      throw error;
    }
  }

  async findById(id: string): Promise<Lamp | null> {
    try {
      const lamp = await this.prisma.lamp.findFirst({
        where: {
          id: id,
          deletedAt: null,
        },
      });
      appLogger.debug('Lamp retrieved from MySQL', { lampId: id, found: !!lamp });
      return this.modelToDomain(lamp);
    } catch (error) {
      appLogger.error('Error finding lamp in MySQL', { error, lampId: id });
      throw error;
    }
  }

  async findAll(): Promise<Lamp[]> {
    try {
      const lamps = await this.prisma.lamp.findMany({
        where: {
          deletedAt: null,
        },
      });
      appLogger.debug('All lamps retrieved from MySQL', { count: lamps.length });
      return lamps.map((lamp: PrismaLampModel) => this.modelToDomain(lamp)!);
    } catch (error) {
      appLogger.error('Error finding all lamps in MySQL', { error });
      throw error;
    }
  }

  async delete(id: string): Promise<void> {
    try {
      // Soft delete - just mark as deleted with timestamp
      await this.prisma.lamp.update({
        where: { id },
        data: { deletedAt: new Date() },
      });
      appLogger.debug('Lamp soft deleted in MySQL', { lampId: id });
    } catch (error) {
      appLogger.error('Error deleting lamp in MySQL', { error, lampId: id });
      throw error;
    }
  }

  async clear(): Promise<void> {
    try {
      // Only for testing purposes - hard delete everything
      await this.prisma.lamp.deleteMany({});
      appLogger.debug('All lamps cleared from MySQL');
    } catch (error) {
      appLogger.error('Error clearing lamps in MySQL', { error });
      throw error;
    }
  }

  // Disconnect the client (useful for tests)
  async disconnect(): Promise<void> {
    try {
      await this.prisma.$disconnect();
      appLogger.info('MySQL connection closed');
    } catch (error) {
      appLogger.error('Error disconnecting from MySQL', { error });
      throw error;
    }
  }
}
