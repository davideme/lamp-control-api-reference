import { z } from 'zod';
import { ValidationError } from '../errors/DomainError';

// Validation constants
export const LAMP_VALIDATION = {
  NAME: {
    MIN_LENGTH: 1,
    MAX_LENGTH: 100,
  },
};

const LampStateSchema = z.object({
  id: z.string().uuid(),
  name: z.string().min(LAMP_VALIDATION.NAME.MIN_LENGTH).max(LAMP_VALIDATION.NAME.MAX_LENGTH),
  isOn: z.boolean(),
  createdAt: z.date(),
  updatedAt: z.date(),
});

export type LampState = z.infer<typeof LampStateSchema>;

export interface LampOptions {
  isOn?: boolean;
}

// Lamp domain model
export class Lamp {
  private state: LampState;
  readonly id: string;

  constructor(id: string, name: string, options: LampOptions = {}) {
    this.id = id;
    const now = new Date();
    this.state = {
      id,
      name,
      isOn: options.isOn ?? false,
      createdAt: now,
      updatedAt: now,
    };

    // Validate initial state
    try {
      LampStateSchema.parse(this.state);
    } catch (error) {
      throw new ValidationError(`Invalid lamp state: ${error}`);
    }
  }

  // Getters
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
  setName(name: string): void {
    const newState = { ...this.state, name, updatedAt: new Date() };
    LampStateSchema.parse(newState);
    this.state = newState;
  }

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

  toggle(): void {
    this.state = { ...this.state, isOn: !this.state.isOn, updatedAt: new Date() };
  }

  // Serialization
  toJSON(): { id: string } & Omit<LampState, 'id'> {
    return {
      id: this.id,
      name: this.state.name,
      isOn: this.state.isOn,
      createdAt: this.state.createdAt,
      updatedAt: this.state.updatedAt,
    };
  }

  // Validation
  static validate(data: unknown): LampState {
    return LampStateSchema.parse(data);
  }
}
