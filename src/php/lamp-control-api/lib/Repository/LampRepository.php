<?php

namespace OpenAPIServer\Repository;

use OpenAPIServer\Entity\LampEntity;
use Ramsey\Uuid\UuidInterface;

/**
 * Repository for managing Lamp domain entities.
 * Uses domain entities to maintain separation from API models.
 */
class LampRepository
{
    /** @var LampEntity[] */
    private array $lamps = [];

    /**
     * Create a new lamp entity
     *
     * @param LampEntity $entity
     * @return LampEntity
     */
    public function create(LampEntity $entity): LampEntity
    {
        $this->lamps[$entity->getId()->toString()] = $entity;
        return $entity;
    }

    /**
     * Get all lamp entities
     *
     * @return LampEntity[]
     */
    public function all(): array
    {
        return array_values($this->lamps);
    }

    /**
     * Get lamp entity by UUID
     *
     * @param UuidInterface $lampId
     * @return LampEntity|null
     */
    public function getById(UuidInterface $lampId): ?LampEntity
    {
        return $this->lamps[$lampId->toString()] ?? null;
    }

    /**
     * Get lamp entity by string ID
     *
     * @param string $lampId
     * @return LampEntity|null
     */
    public function get(string $lampId): ?LampEntity
    {
        return $this->lamps[$lampId] ?? null;
    }

    /**
     * Update an existing lamp entity
     *
     * @param LampEntity $entity
     * @return LampEntity|null
     */
    public function update(LampEntity $entity): ?LampEntity
    {
        $lampId = $entity->getId()->toString();
        if (!isset($this->lamps[$lampId])) {
            return null;
        }
        $this->lamps[$lampId] = $entity;
        return $entity;
    }

    /**
     * Delete a lamp by UUID
     *
     * @param UuidInterface $lampId
     * @return bool
     */
    public function deleteById(UuidInterface $lampId): bool
    {
        $key = $lampId->toString();
        if (!isset($this->lamps[$key])) {
            return false;
        }
        unset($this->lamps[$key]);
        return true;
    }

    /**
     * Delete a lamp by string ID
     *
     * @param string $lampId
     * @return bool
     */
    public function delete(string $lampId): bool
    {
        if (!isset($this->lamps[$lampId])) {
            return false;
        }
        unset($this->lamps[$lampId]);
        return true;
    }
}
