import type { PrismaClient, Lamp as PrismaLamp } from '@prisma/client';
import { LampNotFoundError } from '../../domain/errors/DomainError.ts';
import type {
  LampEntity,
  LampEntityCreate,
  LampEntityUpdate,
} from '../../domain/entities/LampEntity.ts';
import type { LampRepository } from '../../domain/repositories/LampRepository.ts';
import { prismaClient } from '../database/client.ts';

export class PrismaLampRepository implements LampRepository {
  private prisma: PrismaClient;

  constructor(prisma: PrismaClient = prismaClient) {
    this.prisma = prisma;
  }

  async findAll(limit?: number): Promise<LampEntity[]> {
    const lamps = await this.prisma.lamp.findMany({
      where: {
        deletedAt: null,
      },
      orderBy: {
        createdAt: 'asc',
      },
      take: limit,
    });
    return lamps.map(this.toEntity);
  }

  async findById(id: string): Promise<LampEntity | undefined> {
    const lamp = await this.prisma.lamp.findUnique({
      where: {
        id,
        deletedAt: null,
      },
    });
    return lamp ? this.toEntity(lamp) : undefined;
  }

  async create(lampCreate: LampEntityCreate): Promise<LampEntity> {
    const lamp = await this.prisma.lamp.create({
      data: {
        isOn: lampCreate.status,
      },
    });
    return this.toEntity(lamp);
  }

  async update(id: string, lampUpdate: LampEntityUpdate): Promise<LampEntity> {
    try {
      const lamp = await this.prisma.lamp.update({
        where: {
          id,
          deletedAt: null,
        },
        data: {
          isOn: lampUpdate.status,

        },
      });
      return this.toEntity(lamp);
    } catch (error) {
      // Prisma throws PrismaClientKnownRequestError with code P2025 if record not found
      throw new LampNotFoundError(id);
    }
  }

  async delete(id: string): Promise<void> {
    try {
      await this.prisma.lamp.update({
        where: {
          id,
          deletedAt: null,
        },
        data: {
          deletedAt: new Date(),
        },
      });
    } catch (error) {
      throw new LampNotFoundError(id);
    }
  }

  private toEntity(prismaLamp: PrismaLamp): LampEntity {
    return {
      id: prismaLamp.id,
      status: prismaLamp.isOn,
      createdAt: prismaLamp.createdAt.toISOString(),
      updatedAt: prismaLamp.updatedAt.toISOString(),
    };
  }
}
