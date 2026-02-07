import type { FastifyRequest } from 'fastify';
import type { components, operations } from '../types/api.ts';

type Lamp = components['schemas']['Lamp'];

export type ListLampsRequest = FastifyRequest<{
  Querystring: { cursor?: string | null; pageSize?: number };
}>;

export type ListLampsResponse = {
  data: Lamp[];
  nextCursor?: string | null;
  hasMore: boolean;
};

export type GetLampRequest = FastifyRequest<{
  Params: operations['getLamp']['parameters']['path'];
}>;

export type CreateLampRequest = FastifyRequest<{
  Body: operations['createLamp']['requestBody']['content']['application/json'];
}>;

export type UpdateLampRequest = FastifyRequest<{
  Params: operations['updateLamp']['parameters']['path'];
  Body: operations['updateLamp']['requestBody']['content']['application/json'];
}>;

export type DeleteLampRequest = FastifyRequest<{
  Params: operations['deleteLamp']['parameters']['path'];
}>;
