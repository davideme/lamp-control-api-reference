import { z } from 'zod';
import { ValidationError } from '../errors/DomainError';

const LampStateSchema = z.object({
  id: z.string().uuid(),
  isOn: z.boolean(),
  createdAt: z.date(),
  updatedAt: z.date(),
});

export type LampState = z.infer<typeof LampStateSchema>;

// Lamp domain model
export class Lamp {
  private state: LampState;

  constructor(id: string) {
    const now = new Date();
    this.state = {
      id,
      isOn: false,
      createdAt: now,
      updatedAt: now,
    };

    // Validate initial state
    try {
      LampStateSchema.parse(this.state);
    } catch (error) {
      throw new ValidationError('Invalid lamp state');
    }
  }

  // Getters
  get id(): string {
    return this.state.id;
  }

  get name(): string {
    return this.state.name;
  }

  get isOn(): boolean {
    return this.state.isOn;
  }

  get createdAt(): Date {
    return this.state.createdAt;
  }

  get updatedAt(): Date {
    return this.state.updatedAt;
  }

  // State modification methods
  turnOn(): void {
    if (!this.state.isOn) {
      this.state = { ...this.state, isOn: true, updatedAt: new Date() };
    }
  }

  turnOff(): void {
    if (this.state.isOn) {
      this.state = { ...this.state, isOn: false, updatedAt: new Date() };
    }
  }

  // Serialization
  toJSON(): LampState {
    return { ...this.state };
  }

  // Validation
  static validate(data: unknown): LampState {
    return LampStateSchema.parse(data);
  }
} 