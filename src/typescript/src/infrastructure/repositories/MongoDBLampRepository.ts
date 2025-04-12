import mongoose, { Schema, Document } from 'mongoose';
import { v4 as uuidv4 } from 'uuid';
import { Lamp } from '../../domain/models/Lamp';
import { LampRepository } from '../../domain/repositories/LampRepository';
import { appLogger } from '../../utils/logger';

// Define MongoDB schema
interface LampDocument extends Document {
  _id: string; // UUID string as the document identifier
  isOn: boolean;
  name: string;
  createdAt: Date;
  updatedAt: Date;
  deletedAt: Date | null;
}

const lampSchema = new Schema<LampDocument>(
  {
    _id: { type: String, default: () => uuidv4() },
    name: { type: String, required: true },
    isOn: { type: Boolean, required: true, default: false },
    deletedAt: { type: Date, default: null },
  },
  {
    timestamps: true, // Automatically manage createdAt and updatedAt
    versionKey: false, // Don't include __v field
  },
);

// Create and export model (This should happen only once)
const LampModel = mongoose.models.Lamp || mongoose.model<LampDocument>('Lamp', lampSchema);

export class MongoDBLampRepository implements LampRepository {
  constructor() {
    appLogger.info('MongoDBLampRepository initialized');
  }

  // Static connection management to allow for reuse
  private static connectionPromise: Promise<typeof mongoose> | null = null;

  // Connect to MongoDB - reuse existing connection if available
  static async connect(
    connectionString: string = 'mongodb://lamp_user:lamp_password@localhost:27017/lamp_control',
  ): Promise<typeof mongoose> {
    if (!this.connectionPromise) {
      appLogger.info('Connecting to MongoDB');
      this.connectionPromise = mongoose.connect(connectionString);
    }
    return this.connectionPromise;
  }

  // Document to domain entity conversion
  private documentToDomain(doc: LampDocument | null): Lamp | null {
    if (!doc) return null;

    return new Lamp(doc._id, doc.name, {
      isOn: doc.isOn,
    });
  }

  async save(lamp: Lamp): Promise<void> {
    try {
      await MongoDBLampRepository.connect();
      const existingLamp = await LampModel.findOne({ _id: lamp.id });

      if (existingLamp) {
        // Update existing lamp
        await LampModel.updateOne(
          { _id: lamp.id },
          {
            name: lamp.name,
            isOn: lamp.isOn,
            updatedAt: new Date(),
          },
        );
      } else {
        // Create new lamp
        await LampModel.create({
          _id: lamp.id,
          name: lamp.name,
          isOn: lamp.isOn,
          createdAt: new Date(),
          updatedAt: new Date(),
        });
      }
      appLogger.debug('Lamp saved to MongoDB', { lampId: lamp.id });
    } catch (error) {
      appLogger.error('Error saving lamp to MongoDB', { error, lampId: lamp.id });
      throw error;
    }
  }

  async findById(id: string): Promise<Lamp | null> {
    try {
      await MongoDBLampRepository.connect();
      const lamp = await LampModel.findOne({ _id: id, deletedAt: null });
      appLogger.debug('Lamp retrieved from MongoDB', { lampId: id, found: !!lamp });
      return this.documentToDomain(lamp);
    } catch (error) {
      appLogger.error('Error finding lamp in MongoDB', { error, lampId: id });
      throw error;
    }
  }

  async findAll(): Promise<Lamp[]> {
    try {
      await MongoDBLampRepository.connect();
      const lamps = await LampModel.find({ deletedAt: null });
      appLogger.debug('All lamps retrieved from MongoDB', { count: lamps.length });
      return lamps.map((lamp) => this.documentToDomain(lamp)!);
    } catch (error) {
      appLogger.error('Error finding all lamps in MongoDB', { error });
      throw error;
    }
  }

  async delete(id: string): Promise<void> {
    try {
      await MongoDBLampRepository.connect();
      // Soft delete - just mark as deleted with timestamp
      await LampModel.updateOne({ _id: id }, { deletedAt: new Date() });
      appLogger.debug('Lamp soft deleted in MongoDB', { lampId: id });
    } catch (error) {
      appLogger.error('Error deleting lamp in MongoDB', { error, lampId: id });
      throw error;
    }
  }

  async clear(): Promise<void> {
    try {
      await MongoDBLampRepository.connect();
      // Only for testing purposes - hard delete everything
      await LampModel.deleteMany({});
      appLogger.debug('All lamps cleared from MongoDB');
    } catch (error) {
      appLogger.error('Error clearing lamps in MongoDB', { error });
      throw error;
    }
  }

  // Close the connection (useful for tests)
  static async disconnect(): Promise<void> {
    if (mongoose.connection.readyState !== 0) {
      await mongoose.disconnect();
      this.connectionPromise = null;
      appLogger.info('MongoDB connection closed');
    }
  }
}
