import { z } from 'zod';
import { ValidationError } from '../errors/DomainError';

const LampStateSchema = z.object({
  id: z.string().uuid(),
  name: z.string().min(1).max(100),
  isOn: z.boolean(),
  brightness: z.number().min(0).max(100),
  color: z.string().regex(/^#[0-9A-Fa-f]{6}$/),
  createdAt: z.date(),
  updatedAt: z.date(),
});

export type LampState = z.infer<typeof LampStateSchema>;

export interface LampOptions {
  brightness?: number;
  color?: string;
}

// Lamp domain model
export class Lamp {
  private state: LampState;

  constructor(id: string, name: string, options: LampOptions = {}) {
    const now = new Date();
    this.state = {
      id,
      name,
      isOn: false,
      brightness: options.brightness ?? 100,
      color: options.color ?? '#FFFFFF',
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

  get brightness(): number {
    return this.state.brightness;
  }

  get color(): string {
    return this.state.color;
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

  setBrightness(brightness: number): void {
    const newState = { ...this.state, brightness, updatedAt: new Date() };
    LampStateSchema.parse(newState);
    this.state = newState;
  }

  setColor(color: string): void {
    const newState = { ...this.state, color, updatedAt: new Date() };
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

  // Serialization
  toJSON(): LampState {
    return { ...this.state };
  }

  // Validation
  static validate(data: unknown): LampState {
    return LampStateSchema.parse(data);
  }
} 