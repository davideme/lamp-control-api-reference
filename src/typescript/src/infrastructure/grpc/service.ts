import * as grpc from '@grpc/grpc-js';
import { LampService, CreateLampData, UpdateLampData } from '../../domain/services/LampService';
import { LampRepository } from '../../domain/repositories/LampRepository';
import { Lamp } from '../../domain/models/Lamp';
import { LampNotFoundError } from '../../domain/errors/DomainError';
import { appLogger } from '../../utils/logger';
import { CreateLampRequest, DeleteLampRequest, DeleteLampResponse, GetLampRequest, Lamp as LampResponse, LampServiceServer, ListLampsRequest, ListLampsResponse, UpdateLampRequest } from './generated/lamp';

export class GrpcLampService implements LampServiceServer {
  private lampService: LampService;

  constructor(lampRepository: LampRepository) {
    this.lampService = new LampService(lampRepository);
  }

  private serializeLamp(lamp: Lamp): {
    id: string;
    name: string;
    status: boolean;
    createdAt: string;
    updatedAt: string;
  } {
    return {
      id: lamp.id,
      name: lamp.name,
      status: lamp.isOn,
      createdAt: lamp.createdAt.toISOString(),
      updatedAt: lamp.updatedAt.toISOString(),
    };
  }

  [name: string]: grpc.UntypedHandleCall | any;
  

  createLamp: grpc.handleUnaryCall<CreateLampRequest, LampResponse> = async (call, callback) => {
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

  getLamp: grpc.handleUnaryCall<GetLampRequest, LampResponse> = async (call, callback) => {
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

  listLamps: grpc.handleUnaryCall<ListLampsRequest, ListLampsResponse> = async (_call, callback) => {
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

  updateLamp: grpc.handleUnaryCall<UpdateLampRequest, LampResponse> = async (call, callback) => {
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

  deleteLamp: grpc.handleUnaryCall<DeleteLampRequest, DeleteLampResponse> = async (call, callback) => {
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
