import { describe, it, expect, beforeAll, afterAll, beforeEach } from '@jest/globals';
import { PostgreSqlContainer, StartedPostgreSqlContainer } from '@testcontainers/postgresql';
import { PrismaClient } from '@prisma/client';
import { execSync } from 'child_process';
import { PrismaLampRepository } from '../../src/infrastructure/repositories/PrismaLampRepository.js';

describe('PrismaLampRepository Integration Tests', () => {
  let container: StartedPostgreSqlContainer;
  let prisma: PrismaClient;
  let repository: PrismaLampRepository;

  beforeAll(async () => {
    // Start PostgreSQL container
    container = await new PostgreSqlContainer('postgres:16-alpine')
      .withDatabase('lampcontrol_test')
      .withUsername('test')
      .withPassword('test')
      .start();

    const connectionString = container.getConnectionUri();
    process.env.DATABASE_URL = connectionString;

    // Run migrations
    execSync('npx prisma migrate deploy', {
      env: { ...process.env, DATABASE_URL: connectionString },
      stdio: 'inherit',
    });

    // Create Prisma client
    prisma = new PrismaClient({
      datasources: {
        db: { url: connectionString },
      },
    });

    repository = new PrismaLampRepository(prisma);
  }, 60000);

  afterAll(async () => {
    await prisma.$disconnect();
    await container.stop();
  });

  beforeEach(async () => {
    // Clean up database between tests
    await prisma.lamp.deleteMany();
  });

  it('should create a lamp', async () => {
    const lamp = await repository.create({ status: true });

    expect(lamp.id).toBeDefined();
    expect(lamp.status).toBe(true);
    expect(lamp.createdAt).toBeDefined();
    expect(lamp.updatedAt).toBeDefined();
  });

  it('should find lamp by ID', async () => {
    const created = await repository.create({ status: false });
    const found = await repository.findById(created.id);

    expect(found).toBeDefined();
    expect(found?.id).toBe(created.id);
    expect(found?.status).toBe(false);
  });

  it('should return undefined for non-existent lamp', async () => {
    const found = await repository.findById('00000000-0000-0000-0000-000000000000');
    expect(found).toBeUndefined();
  });

  it('should update lamp status', async () => {
    const created = await repository.create({ status: false });
    const updated = await repository.update(created.id, { status: true });

    expect(updated.status).toBe(true);
    expect(updated.updatedAt).not.toBe(created.updatedAt);
  });

  it('should throw LampNotFoundError when updating non-existent lamp', async () => {
    await expect(
      repository.update('00000000-0000-0000-0000-000000000000', { status: true }),
    ).rejects.toThrow('Lamp not found');
  });

  it('should soft delete lamp', async () => {
    const created = await repository.create({ status: true });
    await repository.delete(created.id);

    const found = await repository.findById(created.id);
    expect(found).toBeUndefined();
  });

  it('should throw LampNotFoundError when deleting non-existent lamp', async () => {
    await expect(repository.delete('00000000-0000-0000-0000-000000000000')).rejects.toThrow(
      'Lamp not found',
    );
  });

  it('should find all lamps with pagination', async () => {
    await Promise.all([
      repository.create({ status: true }),
      repository.create({ status: false }),
      repository.create({ status: true }),
      repository.create({ status: false }),
      repository.create({ status: true }),
    ]);

    const lamps = await repository.findAll(3);
    expect(lamps).toHaveLength(3);
  });

  it('should exclude soft-deleted lamps from findAll', async () => {
    const lamp1 = await repository.create({ status: true });
    const lamp2 = await repository.create({ status: false });

    await repository.delete(lamp1.id);

    const lamps = await repository.findAll();
    expect(lamps).toHaveLength(1);
    expect(lamps[0].id).toBe(lamp2.id);
  });
});
