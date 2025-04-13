import * as grpc from '@grpc/grpc-js';
import { LampService, CreateLampData, UpdateLampData } from '../../domain/services/LampService';
import { LampRepository } from '../../domain/repositories/LampRepository';
import { Lamp } from '../../domain/models/Lamp';
import { LampNotFoundError } from '../../domain/errors/DomainError';
import { appLogger } from '../../utils/logger';

// Interface for gRPC handlers
interface GrpcHandlers {
  CreateLamp: grpc.handleUnaryCall<
    { name: string; status: boolean },
    { id: string; name: string; status: boolean; createdAt: string; updatedAt: string }
  >;
  GetLamp: grpc.handleUnaryCall<
    { id: string },
    { id: string; name: string; status: boolean; createdAt: string; updatedAt: string }
  >;
  ListLamps: grpc.handleUnaryCall<
    Record<string, never>,
    { lamps: { id: string; name: string; status: boolean; createdAt: string; updatedAt: string }[] }
  >;
  UpdateLamp: grpc.handleUnaryCall<
    { id: string; name?: string; status?: boolean },
    { id: string; name: string; status: boolean; createdAt: string; updatedAt: string }
  >;
  DeleteLamp: grpc.handleUnaryCall<{ id: string }, { success: boolean }>;
}

export class GrpcLampService implements GrpcHandlers {
  private lampService: LampService;

  constructor(lampRepository: LampRepository) {
    this.lampService = new LampService(lampRepository);
  }

  private serializeLamp(lamp: Lamp): { id: string; name: string; status: boolean; createdAt: string; updatedAt: string } {
    return {
      id: lamp.id,
      name: lamp.name,
      status: lamp.isOn,
      createdAt: lamp.createdAt.toISOString(),
      updatedAt: lamp.updatedAt.toISOString(),
    };
  }

  CreateLamp: GrpcHandlers['CreateLamp'] = async (call, callback) => {
    try {
      const { name, status } = call.request;

      const createLampData: CreateLampData = {
        name,
        isOn: status,
      };

      const lamp = await this.lampService.createLamp(createLampData);
      callback(null, this.serializeLamp(lamp));
    } catch (error) {
      appLogger.error('gRPC CreateLamp error', { error });
      callback({
        code: grpc.status.INTERNAL,
        message: error instanceof Error ? error.message : 'Unknown error',
      });
    }
  };

  GetLamp: GrpcHandlers['GetLamp'] = async (call, callback) => {
    try {
      const { id } = call.request;
      const lamp = await this.lampService.getLamp(id);
      callback(null, this.serializeLamp(lamp));
    } catch (error) {
      appLogger.error('gRPC GetLamp error', { error, lampId: call.request.id });

      if (error instanceof LampNotFoundError) {
        return callback({
          code: grpc.status.NOT_FOUND,
          message: error.message,
        });
      }

      callback({
        code: grpc.status.INTERNAL,
        message: error instanceof Error ? error.message : 'Unknown error',
      });
    }
  };

  ListLamps: GrpcHandlers['ListLamps'] = async (_call, callback) => {
    try {
      const lamps = await this.lampService.getAllLamps();
      callback(null, {
        lamps: lamps.map((lamp) => this.serializeLamp(lamp)),
      });
    } catch (error) {
      appLogger.error('gRPC ListLamps error', { error });
      callback({
        code: grpc.status.INTERNAL,
        message: error instanceof Error ? error.message : 'Unknown error',
      });
    }
  };

  UpdateLamp: GrpcHandlers['UpdateLamp'] = async (call, callback) => {
    try {
      const { id, name, status } = call.request;

      const updateData: UpdateLampData = {};
      if (name !== undefined) updateData.name = name;
      if (status !== undefined) updateData.isOn = status;

      const updatedLamp = await this.lampService.updateLamp(id, updateData);
      callback(null, this.serializeLamp(updatedLamp));
    } catch (error) {
      appLogger.error('gRPC UpdateLamp error', { error, lampId: call.request.id });

      if (error instanceof LampNotFoundError) {
        return callback({
          code: grpc.status.NOT_FOUND,
          message: error.message,
        });
      }

      callback({
        code: grpc.status.INTERNAL,
        message: error instanceof Error ? error.message : 'Unknown error',
      });
    }
  };

  DeleteLamp: GrpcHandlers['DeleteLamp'] = async (call, callback) => {
    try {
      const { id } = call.request;
      await this.lampService.deleteLamp(id);
      callback(null, { success: true });
    } catch (error) {
      appLogger.error('gRPC DeleteLamp error', { error, lampId: call.request.id });

      if (error instanceof LampNotFoundError) {
        return callback({
          code: grpc.status.NOT_FOUND,
          message: error.message,
        });
      }

      callback({
        code: grpc.status.INTERNAL,
        message: error instanceof Error ? error.message : 'Unknown error',
      });
    }
  };
}
