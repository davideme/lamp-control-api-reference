import { Router } from 'express';
import { LampService } from '../../domain/services/LampService';

export function createLampRouter(lampService: LampService): Router {
  const router = Router();

  // Create a new lamp
  router.post('/', async (req, res, next) => {
    try {
      const lamp = await lampService.createLamp(req.body);
      res.status(201).json(lamp);
    } catch (error) {
      next(error);
    }
  });

  // Get all lamps
  router.get('/', async (_req, res, next) => {
    try {
      const lamps = await lampService.getAllLamps();
      res.json(lamps);
    } catch (error) {
      next(error);
    }
  });

  // Get a specific lamp
  router.get('/:id', async (req, res, next) => {
    try {
      const lamp = await lampService.getLamp(req.params.id);
      res.json(lamp);
    } catch (error) {
      next(error);
    }
  });

  // Update a lamp
  router.patch('/:id', async (req, res, next) => {
    try {
      const lamp = await lampService.updateLamp(req.params.id, req.body);
      res.json(lamp);
    } catch (error) {
      next(error);
    }
  });

  // Delete a lamp
  router.delete('/:id', async (req, res, next) => {
    try {
      await lampService.deleteLamp(req.params.id);
      res.status(204).send();
    } catch (error) {
      next(error);
    }
  });

  // Toggle a lamp
  router.post('/:id/toggle', async (req, res, next) => {
    try {
      const lamp = await lampService.toggleLamp(req.params.id);
      res.json(lamp);
    } catch (error) {
      next(error);
    }
  });

  return router;
} 