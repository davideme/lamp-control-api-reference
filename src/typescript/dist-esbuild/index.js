// src/infrastructure/app.ts
import { fileURLToPath } from "url";
import { dirname, join } from "path";
import fastify from "fastify";
import fastifyOpenapiGlue from "fastify-openapi-glue";

// src/infrastructure/security.ts
var Security = class {
  async apiKeyAuth(_request, _reply, _key) {
    return true;
  }
};

// src/domain/errors/DomainError.ts
var DomainError = class extends Error {
  constructor(message) {
    super(message);
    this.name = this.constructor.name;
    Error.captureStackTrace(this, this.constructor);
  }
};
var LampNotFoundError = class extends DomainError {
  constructor(id) {
    super(`Lamp with ID ${id} not found`);
  }
};

// src/infrastructure/repositories/InMemoryLampRepository.ts
var InMemoryLampRepository = class {
  lamps = /* @__PURE__ */ new Map();
  async findAll(limit) {
    const lamps = Array.from(this.lamps.values());
    return limit ? lamps.slice(0, limit) : lamps;
  }
  async findById(id) {
    return this.lamps.get(id);
  }
  async create(lamp) {
    const now = (/* @__PURE__ */ new Date()).toISOString();
    const newLamp = {
      id: crypto.randomUUID(),
      createdAt: now,
      updatedAt: now,
      ...lamp
    };
    this.lamps.set(newLamp.id, newLamp);
    return newLamp;
  }
  async update(id, lamp) {
    const existingLamp = this.lamps.get(id);
    if (!existingLamp) {
      throw new LampNotFoundError(id);
    }
    const updatedLamp = {
      ...existingLamp,
      ...lamp,
      updatedAt: (/* @__PURE__ */ new Date()).toISOString()
    };
    this.lamps.set(id, updatedLamp);
    return updatedLamp;
  }
  async delete(id) {
    if (!this.lamps.has(id)) {
      throw new LampNotFoundError(id);
    }
    this.lamps.delete(id);
  }
};

// src/infrastructure/mappers/LampMapper.ts
var LampMapper = class {
  /**
   * Convert from domain entity to API model
   */
  static toApiModel(entity) {
    return {
      id: entity.id,
      status: entity.status,
      createdAt: entity.createdAt,
      updatedAt: entity.updatedAt
    };
  }
  /**
   * Convert from API model to domain entity
   */
  static toDomainEntity(apiModel) {
    return {
      id: apiModel.id,
      status: apiModel.status,
      createdAt: apiModel.createdAt,
      updatedAt: apiModel.updatedAt
    };
  }
  /**
   * Convert from API create model to domain create model
   */
  static toDomainEntityCreate(apiModel) {
    return {
      status: apiModel.status
    };
  }
  /**
   * Convert from API update model to domain update model
   */
  static toDomainEntityUpdate(apiModel) {
    return {
      status: apiModel.status
    };
  }
};

// src/infrastructure/services/service.ts
var Service = class {
  constructor(repository) {
    this.repository = repository;
  }
  async listLamps(request, reply) {
    const { pageSize = 25 } = request.query;
    const lampEntities = await this.repository.findAll(pageSize);
    const lamps = lampEntities.map(LampMapper.toApiModel);
    const response = {
      data: lamps,
      hasMore: false,
      // Simple implementation assumes no more pages
      nextCursor: null
    };
    return reply.code(200).send(response);
  }
  async getLamp(request, reply) {
    const { lampId } = request.params;
    const lampEntity = await this.repository.findById(lampId);
    if (!lampEntity) {
      return reply.code(404).send();
    }
    const lamp = LampMapper.toApiModel(lampEntity);
    return reply.code(200).send(lamp);
  }
  async createLamp(request, reply) {
    const body = request.body;
    const lampEntityCreate = LampMapper.toDomainEntityCreate(body);
    const newLampEntity = await this.repository.create(lampEntityCreate);
    const newLamp = LampMapper.toApiModel(newLampEntity);
    return reply.code(201).send(newLamp);
  }
  async updateLamp(request, reply) {
    const { lampId } = request.params;
    const body = request.body;
    try {
      const lampEntityUpdate = LampMapper.toDomainEntityUpdate(body);
      const updatedLampEntity = await this.repository.update(lampId, lampEntityUpdate);
      const updatedLamp = LampMapper.toApiModel(updatedLampEntity);
      return reply.code(200).send(updatedLamp);
    } catch (error) {
      if (error instanceof LampNotFoundError) {
        return reply.code(404).send();
      }
      throw error;
    }
  }
  async deleteLamp(request, reply) {
    const { lampId } = request.params;
    try {
      await this.repository.delete(lampId);
      return reply.code(204).send();
    } catch (error) {
      if (error instanceof LampNotFoundError) {
        return reply.code(404).send();
      }
      throw error;
    }
  }
};
var service_default = Service;

// src/infrastructure/app.ts
var __filename = fileURLToPath(import.meta.url);
var currentDir = dirname(__filename);
var projectRoot = process.cwd();
var specPath = join(projectRoot, "../../docs/api/openapi.yaml");
var options = {
  specification: specPath,
  service: new service_default(new InMemoryLampRepository()),
  securityHandlers: new Security(),
  prefix: "v1"
};
async function buildApp() {
  const server = fastify({
    logger: true
  });
  server.get("/health", async (_request, _reply) => {
    return { status: "ok" };
  });
  server.register(fastifyOpenapiGlue, options);
  return server;
}

// src/index.test.ts
buildApp().then((server) => {
  server.listen({ port: 8080 }, (err, address) => {
    if (err) {
      server.log.error(err);
      process.exit(1);
    }
    server.log.info(`Server listening at ${address}`);
  });
});
