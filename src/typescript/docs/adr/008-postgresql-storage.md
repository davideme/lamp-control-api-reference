# ADR 007: PostgreSQL Storage with Prisma

## Status

Accepted

## Context

The TypeScript implementation of the Lamp Control API currently uses an in-memory repository for data persistence. While suitable for development and testing, production deployments require durable, ACID-compliant data storage with type-safe queries, excellent developer experience, and seamless TypeScript integration.

### Current State

- **Framework**: Fastify 5.3.2
- **Architecture**: `LampRepository` interface with `InMemoryLampRepository` implementation
- **Storage**: In-memory Map with no persistence
- **Dependencies**: Fastify, TypeScript 5.8.3, Jest

### Requirements

1. **Type Safety**: Auto-generated TypeScript types from schema
2. **Developer Experience**: Intuitive API, excellent tooling
3. **Performance**: Efficient queries, connection pooling
4. **Migration Management**: Version-controlled schema changes
5. **Schema Compatibility**: Use existing PostgreSQL schema at `database/sql/postgresql/schema.sql`
6. **Testing**: Integration tests with real PostgreSQL instances

### Technology Landscape (2025-2026)

**Prisma**
- Most popular TypeScript ORM (37k+ stars, 500k+ weekly downloads)
- Schema-first design with auto-generated types
- Type-safe query builder (zero runtime overhead)
- Prisma Studio (database GUI)
- Built-in migrations (declarative)
- Excellent DX (Developer Experience)

**Drizzle ORM**
- Newest and fastest-growing (19k+ stars)
- SQL-like API, lightweight
- Best TypeScript inference
- Serverless-friendly
- Smaller bundle size

**TypeORM**
- Mature ORM (33k+ stars)
- Decorator-based entities
- Poor TypeScript inference
- Losing popularity

**Kysely**
- Type-safe SQL query builder
- Excellent TypeScript types
- No ORM features (no migrations)
- SQL-first approach

## Decision

We will implement **Prisma 5.x** as the PostgreSQL data access layer for the TypeScript Lamp Control API implementation.

### Architecture

```
Fastify Routes → LampService → PrismaClient → Connection Pool → PostgreSQL
```

### Core Components

#### 1. **Prisma Schema**

```prisma
// prisma/schema.prisma
generator client {
  provider = "prisma-client-js"
}

datasource db {
  provider = "postgresql"
  url      = env("DATABASE_URL")
}

model Lamp {
  id        String    @id @default(uuid()) @db.Uuid
  isOn      Boolean   @map("is_on") @db.Boolean
  createdAt DateTime  @default(now()) @map("created_at") @db.Timestamptz
  updatedAt DateTime  @updatedAt @map("updated_at") @db.Timestamptz
  deletedAt DateTime? @map("deleted_at") @db.Timestamptz

  @@index([isOn], name: "idx_lamps_is_on")
  @@index([createdAt], name: "idx_lamps_created_at")
  @@index([deletedAt], name: "idx_lamps_deleted_at")
  @@map("lamps")
}
```

#### 2. **Database Client**

```typescript
// src/database/client.ts
import { PrismaClient } from '@prisma/client';

// Singleton pattern for PrismaClient
let prisma: PrismaClient;

export function getPrismaClient(): PrismaClient {
  if (!prisma) {
    prisma = new PrismaClient({
      log: process.env.NODE_ENV === 'development' 
        ? ['query', 'info', 'warn', 'error']
        : ['error'],
      datasources: {
        db: {
          url: process.env.DATABASE_URL,
        },
      },
    });
  }
  return prisma;
}

export async function closePrismaClient(): Promise<void> {
  if (prisma) {
    await prisma.$disconnect();
  }
}

// Middleware to exclude soft-deleted records
export function excludeSoftDeleted<T>(model: any) {
  return model.$extends({
    query: {
      $allModels: {
        async findMany({ args, query }: any) {
          args.where = { ...args.where, deletedAt: null };
          return query(args);
        },
        async findUnique({ args, query }: any) {
          args.where = { ...args.where, deletedAt: null };
          return query(args);
        },
        async findFirst({ args, query }: any) {
          args.where = { ...args.where, deletedAt: null };
          return query(args);
        },
      },
    },
  });
}

export const prismaClient = getPrismaClient();
```

#### 3. **Repository Implementation**

```typescript
// src/repositories/LampRepository.ts
import { PrismaClient, Lamp } from '@prisma/client';
import { prismaClient } from '../database/client';

export interface CreateLampDto {
  isOn: boolean;
}

export interface UpdateLampDto {
  isOn: boolean;
}

export class LampRepository {
  private prisma: PrismaClient;

  constructor(prisma: PrismaClient = prismaClient) {
    this.prisma = prisma;
  }

  async create(data: CreateLampDto): Promise<Lamp> {
    return this.prisma.lamp.create({
      data: {
        isOn: data.isOn,
      },
    });
  }

  async findById(id: string): Promise<Lamp | null> {
    return this.prisma.lamp.findUnique({
      where: {
        id,
        deletedAt: null, // Exclude soft-deleted
      },
    });
  }

  async findAll(offset: number = 0, limit: number = 100): Promise<Lamp[]> {
    return this.prisma.lamp.findMany({
      where: {
        deletedAt: null,
      },
      orderBy: {
        createdAt: 'asc',
      },
      skip: offset,
      take: limit,
    });
  }

  async update(id: string, data: UpdateLampDto): Promise<Lamp | null> {
    try {
      return await this.prisma.lamp.update({
        where: {
          id,
          deletedAt: null,
        },
        data: {
          isOn: data.isOn,
          updatedAt: new Date(),
        },
      });
    } catch (error) {
      // Prisma throws if record not found
      return null;
    }
  }

  async delete(id: string): Promise<boolean> {
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
      return true;
    } catch (error) {
      return false;
    }
  }

  async count(): Promise<number> {
    return this.prisma.lamp.count({
      where: {
        deletedAt: null,
      },
    });
  }
}
```

#### 4. **Domain Types**

```typescript
// src/types/lamp.types.ts
import { Lamp as PrismaLamp } from '@prisma/client';

// Re-export Prisma-generated type
export type Lamp = PrismaLamp;

// Request/Response DTOs
export interface CreateLampRequest {
  isOn: boolean;
}

export interface UpdateLampRequest {
  isOn: boolean;
}

export interface LampResponse {
  id: string;
  isOn: boolean;
  createdAt: Date;
  updatedAt: Date;
  deletedAt: Date | null;
}

// Transform Prisma model to API response
export function toLampResponse(lamp: Lamp): LampResponse {
  return {
    id: lamp.id,
    isOn: lamp.isOn,
    createdAt: lamp.createdAt,
    updatedAt: lamp.updatedAt,
    deletedAt: lamp.deletedAt,
  };
}
```

#### 5. **Fastify Routes**

```typescript
// src/routes/lamps.ts
import { FastifyInstance, FastifyRequest, FastifyReply } from 'fastify';
import { LampRepository } from '../repositories/LampRepository';
import {
  CreateLampRequest,
  UpdateLampRequest,
  toLampResponse,
} from '../types/lamp.types';

interface LampParams {
  id: string;
}

interface LampQuery {
  offset?: number;
  limit?: number;
}

export async function lampRoutes(fastify: FastifyInstance) {
  const repository = new LampRepository();

  // Create lamp
  fastify.post<{ Body: CreateLampRequest }>(
    '/lamps',
    async (request: FastifyRequest<{ Body: CreateLampRequest }>, reply: FastifyReply) => {
      const lamp = await repository.create(request.body);
      reply.code(201).send(toLampResponse(lamp));
    }
  );

  // Get all lamps
  fastify.get<{ Querystring: LampQuery }>(
    '/lamps',
    async (request: FastifyRequest<{ Querystring: LampQuery }>, reply: FastifyReply) => {
      const offset = request.query.offset || 0;
      const limit = Math.min(request.query.limit || 100, 1000);
      
      const lamps = await repository.findAll(offset, limit);
      reply.send(lamps.map(toLampResponse));
    }
  );

  // Get lamp by ID
  fastify.get<{ Params: LampParams }>(
    '/lamps/:id',
    async (request: FastifyRequest<{ Params: LampParams }>, reply: FastifyReply) => {
      const lamp = await repository.findById(request.params.id);
      
      if (!lamp) {
        reply.code(404).send({ error: 'Lamp not found' });
        return;
      }
      
      reply.send(toLampResponse(lamp));
    }
  );

  // Update lamp
  fastify.put<{ Params: LampParams; Body: UpdateLampRequest }>(
    '/lamps/:id',
    async (
      request: FastifyRequest<{ Params: LampParams; Body: UpdateLampRequest }>,
      reply: FastifyReply
    ) => {
      const lamp = await repository.update(request.params.id, request.body);
      
      if (!lamp) {
        reply.code(404).send({ error: 'Lamp not found' });
        return;
      }
      
      reply.send(toLampResponse(lamp));
    }
  );

  // Delete lamp
  fastify.delete<{ Params: LampParams }>(
    '/lamps/:id',
    async (request: FastifyRequest<{ Params: LampParams }>, reply: FastifyReply) => {
      const deleted = await repository.delete(request.params.id);
      
      if (!deleted) {
        reply.code(404).send({ error: 'Lamp not found' });
        return;
      }
      
      reply.code(204).send();
    }
  );
}
```

#### 6. **Fastify Application**

```typescript
// src/app.ts
import Fastify from 'fastify';
import { lampRoutes } from './routes/lamps';
import { closePrismaClient } from './database/client';

export async function buildApp() {
  const fastify = Fastify({
    logger: {
      level: process.env.LOG_LEVEL || 'info',
    },
  });

  // Health check
  fastify.get('/health', async () => {
    return { status: 'healthy' };
  });

  // Register routes
  await fastify.register(lampRoutes);

  // Graceful shutdown
  fastify.addHook('onClose', async () => {
    await closePrismaClient();
  });

  return fastify;
}

// Start server
async function start() {
  const app = await buildApp();
  
  try {
    await app.listen({
      port: parseInt(process.env.PORT || '3000', 10),
      host: '0.0.0.0',
    });
  } catch (err) {
    app.log.error(err);
    process.exit(1);
  }
}

if (require.main === module) {
  start();
}
```

### Configuration

#### **Environment Variables**

```bash
# .env
DATABASE_URL="postgresql://lampuser:lamppass@localhost:5432/lampcontrol?schema=public"
PORT=3000
NODE_ENV=development
LOG_LEVEL=info

# Connection pool (embedded in DATABASE_URL)
# ?connection_limit=20&pool_timeout=20
```

#### **.env.production**

```bash
DATABASE_URL="postgresql://user:pass@db.production.com:5432/lampcontrol?schema=public&connection_limit=50&pool_timeout=30"
NODE_ENV=production
LOG_LEVEL=error
```

### Dependencies

#### **package.json**

```json
{
  "name": "lamp-control-api",
  "version": "1.0.0",
  "main": "dist/app.js",
  "scripts": {
    "dev": "tsx watch src/app.ts",
    "build": "tsc",
    "start": "node dist/app.js",
    "prisma:generate": "prisma generate",
    "prisma:migrate": "prisma migrate dev",
    "prisma:deploy": "prisma migrate deploy",
    "prisma:studio": "prisma studio",
    "test": "jest",
    "test:integration": "jest --testPathPattern=integration"
  },
  "dependencies": {
    "@prisma/client": "^5.8.0",
    "fastify": "^5.3.2",
    "dotenv": "^16.3.1"
  },
  "devDependencies": {
    "@types/node": "^20.10.6",
    "prisma": "^5.8.0",
    "typescript": "^5.8.3",
    "tsx": "^4.7.0",
    "jest": "^29.7.0",
    "@types/jest": "^29.5.11",
    "ts-jest": "^29.1.1",
    "@testcontainers/postgresql": "^10.4.0",
    "testcontainers": "^10.4.0"
  }
}
```

### Migration Strategy

#### **Initialize Prisma**

```bash
# Install Prisma CLI
npm install -D prisma

# Initialize Prisma
npx prisma init

# This creates:
# - prisma/schema.prisma
# - .env
```

#### **Create Migration from Existing Schema**

```bash
# Option 1: Pull existing schema (database-first)
npx prisma db pull

# Option 2: Create migration from schema.prisma (code-first)
npx prisma migrate dev --name initial_schema

# Generate Prisma Client (auto-generated TypeScript types)
npx prisma generate
```

#### **Example Migration File**

```sql
-- prisma/migrations/20260102000000_initial_schema/migration.sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE "lamps" (
    "id" UUID NOT NULL DEFAULT uuid_generate_v4(),
    "is_on" BOOLEAN NOT NULL DEFAULT false,
    "created_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted_at" TIMESTAMPTZ,

    CONSTRAINT "lamps_pkey" PRIMARY KEY ("id")
);

CREATE INDEX "idx_lamps_is_on" ON "lamps"("is_on");
CREATE INDEX "idx_lamps_created_at" ON "lamps"("created_at");
CREATE INDEX "idx_lamps_deleted_at" ON "lamps"("deleted_at");
```

#### **Apply Migrations**

```bash
# Development
npx prisma migrate dev

# Production
npx prisma migrate deploy

# Reset database (dev only)
npx prisma migrate reset
```

### Testing Strategy

#### **Integration Test with Testcontainers**

```typescript
// tests/integration/lamp.repository.test.ts
import { describe, it, expect, beforeAll, afterAll, beforeEach } from '@jest/globals';
import { PostgreSqlContainer, StartedPostgreSqlContainer } from '@testcontainers/postgresql';
import { PrismaClient } from '@prisma/client';
import { execSync } from 'child_process';
import { LampRepository } from '../../src/repositories/LampRepository';

describe('LampRepository Integration Tests', () => {
  let container: StartedPostgreSqlContainer;
  let prisma: PrismaClient;
  let repository: LampRepository;

  beforeAll(async () => {
    // Start PostgreSQL container
    container = await new PostgreSqlContainer('postgres:16-alpine')
      .withDatabase('lampcontrol_test')
      .withUsername('test')
      .withPassword('test')
      .start();

    const connectionString = container.getConnectionUri();

    // Set environment variable for Prisma
    process.env.DATABASE_URL = connectionString;

    // Run migrations
    execSync('npx prisma migrate deploy', {
      env: { ...process.env, DATABASE_URL: connectionString },
    });

    // Create Prisma client
    prisma = new PrismaClient({
      datasources: {
        db: { url: connectionString },
      },
    });

    repository = new LampRepository(prisma);
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
    // Arrange
    const createDto = { isOn: true };

    // Act
    const lamp = await repository.create(createDto);

    // Assert
    expect(lamp.id).toBeDefined();
    expect(lamp.isOn).toBe(true);
    expect(lamp.createdAt).toBeDefined();
  });

  it('should find lamp by ID', async () => {
    // Arrange
    const created = await repository.create({ isOn: false });

    // Act
    const found = await repository.findById(created.id);

    // Assert
    expect(found).not.toBeNull();
    expect(found?.id).toBe(created.id);
    expect(found?.isOn).toBe(false);
  });

  it('should return null for non-existent lamp', async () => {
    // Act
    const found = await repository.findById('00000000-0000-0000-0000-000000000000');

    // Assert
    expect(found).toBeNull();
  });

  it('should update lamp status', async () => {
    // Arrange
    const created = await repository.create({ isOn: false });

    // Act
    const updated = await repository.update(created.id, { isOn: true });

    // Assert
    expect(updated).not.toBeNull();
    expect(updated?.isOn).toBe(true);
  });

  it('should soft delete lamp', async () => {
    // Arrange
    const created = await repository.create({ isOn: true });

    // Act
    const deleted = await repository.delete(created.id);
    const found = await repository.findById(created.id);

    // Assert
    expect(deleted).toBe(true);
    expect(found).toBeNull(); // Soft deleted, so not found
  });

  it('should find all lamps with pagination', async () => {
    // Arrange
    await Promise.all([
      repository.create({ isOn: true }),
      repository.create({ isOn: false }),
      repository.create({ isOn: true }),
      repository.create({ isOn: false }),
      repository.create({ isOn: true }),
    ]);

    // Act
    const lamps = await repository.findAll(0, 3);

    // Assert
    expect(lamps).toHaveLength(3);
  });

  it('should count active lamps', async () => {
    // Arrange
    await Promise.all([
      repository.create({ isOn: true }),
      repository.create({ isOn: false }),
      repository.create({ isOn: true }),
    ]);

    // Act
    const count = await repository.count();

    // Assert
    expect(count).toBe(3);
  });
});
```

#### **jest.config.js**

```javascript
module.exports = {
  preset: 'ts-jest',
  testEnvironment: 'node',
  testMatch: ['**/tests/**/*.test.ts'],
  collectCoverageFrom: ['src/**/*.ts'],
  coveragePathIgnorePatterns: ['/node_modules/', '/dist/'],
  testTimeout: 60000, // Testcontainers need time
};
```

### Performance Optimizations

#### **1. Connection Pooling**

```typescript
// Configure in DATABASE_URL
const url = `postgresql://user:pass@localhost:5432/db?connection_limit=20&pool_timeout=20`;

// Or in Prisma schema
datasource db {
  provider = "postgresql"
  url      = env("DATABASE_URL")
  // Connection pooling handled by PostgreSQL driver
}
```

#### **2. Batch Operations**

```typescript
async createBatch(lamps: CreateLampDto[]): Promise<number> {
  const result = await this.prisma.lamp.createMany({
    data: lamps,
    skipDuplicates: true,
  });
  return result.count;
}
```

#### **3. Query Optimization**

```typescript
// Select specific fields
async findAllIds(): Promise<string[]> {
  const lamps = await this.prisma.lamp.findMany({
    select: { id: true },
    where: { deletedAt: null },
  });
  return lamps.map(l => l.id);
}

// Count efficiently
async countByStatus(isOn: boolean): Promise<number> {
  return this.prisma.lamp.count({
    where: { isOn, deletedAt: null },
  });
}
```

#### **4. Prisma Studio**

```bash
# Launch database GUI
npx prisma studio

# Opens http://localhost:5555
```

## Rationale

### Why Prisma?

1. **Type Safety**: Auto-generated TypeScript types from schema
2. **Developer Experience**: Best-in-class DX, intuitive API, Prisma Studio
3. **Popularity**: 37k stars, 500k+ weekly downloads, massive community
4. **Migration Management**: Declarative migrations, version control
5. **Performance**: Optimized queries, connection pooling
6. **Modern**: Built for TypeScript from the ground up
7. **Tooling**: Prisma Studio, VS Code extension, CLI

### Why Not Drizzle?

- **Newer**: Less mature (2023 vs 2019)
- **Smaller Community**: 19k vs 37k stars
- **Trade-off**: Drizzle is faster and lighter, but Prisma has better tooling
- **Future**: Consider Drizzle for greenfield projects in 2026+

### Why Not TypeORM?

- **Dated**: Poor TypeScript inference, decorator-heavy
- **Losing Popularity**: Downloads declining vs Prisma
- **DX**: Less intuitive API than Prisma

### Why Not Kysely?

- **No ORM**: Query builder only, no migrations
- **Use Case**: Great for complex SQL, but Prisma is more complete

## Consequences

### Positive

- ✅ **Type Safety**: Auto-generated types, zero runtime overhead
- ✅ **Developer Experience**: Intuitive API, Prisma Studio, excellent docs
- ✅ **Migration Management**: Declarative, version-controlled migrations
- ✅ **Type Inference**: Full TypeScript inference, autocomplete
- ✅ **Testability**: Easy integration testing with Testcontainers
- ✅ **Community**: Large community, extensive resources

### Negative

- ❌ **Generated Code**: Prisma Client adds ~1-2MB to bundle
- ❌ **Schema Format**: Must learn Prisma Schema Language (DSL)
- ❌ **Migration Drift**: Schema and database can get out of sync
- ❌ **Vendor Lock-in**: Prisma-specific API, not portable

### Neutral

- 🔄 **Build Step**: Must run `prisma generate` after schema changes
- 🔄 **Complex Queries**: Raw SQL needed for very complex queries
- 🔄 **Performance**: Slightly slower than Drizzle, but acceptable

## Implementation Checklist

- [ ] Install Prisma and @prisma/client
- [ ] Create `prisma/schema.prisma` with Lamp model
- [ ] Configure DATABASE_URL in .env
- [ ] Create initial migration from existing schema
- [ ] Run `npx prisma generate` to create types
- [ ] Implement `LampRepository` with Prisma Client
- [ ] Create Fastify routes with repository
- [ ] Add graceful shutdown for Prisma
- [ ] Write integration tests with Testcontainers
- [ ] Update README with Prisma setup instructions
- [ ] Add npm scripts for migrations and generation

## References

- [Prisma Documentation](https://www.prisma.io/docs)
- [Prisma with Fastify](https://www.prisma.io/fastify)
- [Prisma Migrate](https://www.prisma.io/docs/concepts/components/prisma-migrate)
- [Prisma Client API](https://www.prisma.io/docs/reference/api-reference/prisma-client-reference)
- [PostgreSQL Schema: database/sql/postgresql/schema.sql](../../../database/sql/postgresql/schema.sql)
- [Testcontainers Node](https://node.testcontainers.org/)

## Related ADRs

- [ADR 001: TypeScript Version](001-typescript-version.md)
- [ADR 002: Fastify Framework](002-fastify-framework.md)
- [Root ADR 005: PostgreSQL Storage Support](../../../docs/adr/005-postgresql-storage-support.md)
