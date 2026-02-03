import { describe, it, expect, beforeAll, afterAll, beforeEach } from '@jest/globals';
import { PostgreSqlContainer, StartedPostgreSqlContainer } from '@testcontainers/postgresql';
import { PrismaClient } from '@prisma/client';
import { readFileSync } from 'fs';
import { join } from 'path';
import { fileURLToPath } from 'url';
import { dirname } from 'path';
import { PrismaLampRepository } from '../../src/infrastructure/repositories/PrismaLampRepository.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

describe.skip('PrismaLampRepository Integration Tests', () => {
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

    // Create Prisma client
    prisma = new PrismaClient({
      datasources: {
        db: { url: connectionString },
      },
    });

    // Apply database schema directly from the SQL file
    // Split and execute individual statements since Prisma cannot execute multiple commands at once
    const schemaPath = join(__dirname, '../../../../database/sql/postgresql/schema.sql');
    const schema = readFileSync(schemaPath, 'utf-8');

    // Split the SQL file into individual statements
    // Handle multi-line statements and $$ delimiters for functions
    const statements: string[] = [];
    let currentStatement = '';
    let insideDollarQuote = false;

    const lines = schema.split('\n');
    for (const line of lines) {
      // Skip comments
      if (line.trim().startsWith('--')) {
        continue;
      }

      // Track $$ delimiters for function definitions
      if (line.includes('$$')) {
        insideDollarQuote = !insideDollarQuote;
      }

      currentStatement += line + '\n';

      // If we hit a semicolon outside of a $$ block, it's the end of a statement
      if (line.includes(';') && !insideDollarQuote) {
        const trimmed = currentStatement.trim();
        if (trimmed.length > 0) {
          statements.push(trimmed);
        }
        currentStatement = '';
      }
    }

    // Add any remaining statement
    if (currentStatement.trim().length > 0) {
      statements.push(currentStatement.trim());
    }

    // Execute each statement individually
    for (const statement of statements) {
      await prisma.$executeRawUnsafe(statement);
    }

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
    // Wait a small amount to ensure timestamps differ
    await new Promise((resolve) => setTimeout(resolve, 10));
    const updated = await repository.update(created.id, { status: true });

    expect(updated.status).toBe(true);
    expect(updated.updatedAt).not.toBe(created.updatedAt);
  });

  it('should throw LampNotFoundError when updating non-existent lamp', async () => {
    await expect(
      repository.update('00000000-0000-0000-0000-000000000000', { status: true }),
    ).rejects.toThrow('Lamp with ID 00000000-0000-0000-0000-000000000000 not found');
  });

  it('should soft delete lamp', async () => {
    const created = await repository.create({ status: true });
    await repository.delete(created.id);

    const found = await repository.findById(created.id);
    expect(found).toBeUndefined();
  });

  it('should throw LampNotFoundError when deleting non-existent lamp', async () => {
    await expect(repository.delete('00000000-0000-0000-0000-000000000000')).rejects.toThrow(
      'Lamp with ID 00000000-0000-0000-0000-000000000000 not found',
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
