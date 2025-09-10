<?php

namespace OpenAPIServer\Service;

use OpenAPIServer\Entity\LampEntity;
use OpenAPIServer\Mapper\LampMapper;
use OpenAPIServer\Model\Lamp;
use OpenAPIServer\Model\LampCreate;
use OpenAPIServer\Model\LampUpdate;
use OpenAPIServer\Repository\LampRepository;
use Ramsey\Uuid\Uuid;

/**
 * Service layer that handles business logic and coordinates between API and domain layers.
 * Uses mappers to maintain separation between API models and domain entities.
 */
class LampService
{
    private LampRepository $repository;
    private LampMapper $mapper;

    public function __construct(LampRepository $repository, LampMapper $mapper)
    {
        $this->repository = $repository;
        $this->mapper = $mapper;
    }

    /**
     * Create a new lamp from API model
     *
     * @param LampCreate $lampCreate
     * @return Lamp
     */
    public function create(LampCreate $lampCreate): Lamp
    {
        $entity = $this->mapper->toDomainEntityCreate($lampCreate);
        $createdEntity = $this->repository->create($entity);
        return $this->mapper->toApiModel($createdEntity);
    }

    /**
     * Get all lamps as API models
     *
     * @return Lamp[]
     */
    public function all(): array
    {
        $entities = $this->repository->all();
        return array_map([$this->mapper, 'toApiModel'], $entities);
    }

    /**
     * Get a lamp by string ID and return as API model
     *
     * @param string $lampId
     * @return Lamp|null
     */
    public function get(string $lampId): ?Lamp
    {
        $entity = $this->repository->get($lampId);
        return $entity ? $this->mapper->toApiModel($entity) : null;
    }

    /**
     * Update a lamp by string ID with API update model
     *
     * @param string $lampId
     * @param LampUpdate $lampUpdate
     * @return Lamp|null
     */
    public function update(string $lampId, LampUpdate $lampUpdate): ?Lamp
    {
        $existingEntity = $this->repository->get($lampId);
        if (!$existingEntity) {
            return null;
        }

        $updatedEntity = $this->mapper->updateDomainEntity($existingEntity, $lampUpdate);
        $savedEntity = $this->repository->update($updatedEntity);
        return $savedEntity ? $this->mapper->toApiModel($savedEntity) : null;
    }

    /**
     * Delete a lamp by string ID
     *
     * @param string $lampId
     * @return bool
     */
    public function delete(string $lampId): bool
    {
        return $this->repository->delete($lampId);
    }
}
