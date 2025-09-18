"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.InMemoryLampRepository = void 0;
const DomainError_js_1 = require("../../domain/errors/DomainError.js");
class InMemoryLampRepository {
    lamps = new Map();
    async findAll(limit) {
        const lamps = Array.from(this.lamps.values());
        return limit ? lamps.slice(0, limit) : lamps;
    }
    async findById(id) {
        return this.lamps.get(id);
    }
    async create(lamp) {
        const now = new Date().toISOString();
        const newLamp = {
            id: crypto.randomUUID(),
            createdAt: now,
            updatedAt: now,
            ...lamp,
        };
        this.lamps.set(newLamp.id, newLamp);
        return newLamp;
    }
    async update(id, lamp) {
        const existingLamp = this.lamps.get(id);
        if (!existingLamp) {
            throw new DomainError_js_1.LampNotFoundError(id);
        }
        const updatedLamp = {
            ...existingLamp,
            ...lamp,
            updatedAt: new Date().toISOString(),
        };
        this.lamps.set(id, updatedLamp);
        return updatedLamp;
    }
    async delete(id) {
        if (!this.lamps.has(id)) {
            throw new DomainError_js_1.LampNotFoundError(id);
        }
        this.lamps.delete(id);
    }
}
exports.InMemoryLampRepository = InMemoryLampRepository;
