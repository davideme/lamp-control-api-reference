<?php

namespace OpenAPIServer\Mapper;

use DateTime;
use OpenAPIServer\Entity\LampEntity;
use OpenAPIServer\Model\Lamp;
use OpenAPIServer\Model\LampCreate;
use OpenAPIServer\Model\LampUpdate;
use Ramsey\Uuid\Uuid;

/**
 * Mapper to convert between domain entities and API models.
 * This separation allows the internal domain model to evolve independently
 * from the external API contract.
 */
class LampMapper
{
    /**
     * Convert from domain entity to API model
     *
     * @param LampEntity $entity
     * @return Lamp
     */
    public function toApiModel(LampEntity $entity): Lamp
    {
        $lamp = new Lamp();
        $lamp->setData([
            'id' => $entity->getId()->toString(),
            'status' => $entity->getStatus(),
            'createdAt' => $entity->getCreatedAt()->format('c'),
            'updatedAt' => $entity->getUpdatedAt()->format('c')
        ]);
        return $lamp;
    }

    /**
     * Convert from API model to domain entity
     *
     * @param Lamp $apiModel
     * @return LampEntity
     */
    public function toDomainEntity(Lamp $apiModel): LampEntity
    {
        $data = $apiModel->getData();
        $dataArray = is_object($data) ? get_object_vars($data) : (array)$data;

        return new LampEntity(
            Uuid::fromString($dataArray['id']),
            (bool)$dataArray['status'],
            new DateTime($dataArray['createdAt']),
            new DateTime($dataArray['updatedAt'])
        );
    }

    /**
     * Convert from API create model to domain entity
     *
     * @param LampCreate $createModel
     * @return LampEntity
     */
    public function toDomainEntityCreate(LampCreate $createModel): LampEntity
    {
        $data = $createModel->getData();
        $dataArray = is_object($data) ? get_object_vars($data) : (array)$data;
        $status = isset($dataArray['status']) ? (bool)$dataArray['status'] : false;

        return LampEntity::create($status);
    }

    /**
     * Update domain entity from API update model
     *
     * @param LampEntity $entity
     * @param LampUpdate $updateModel
     * @return LampEntity
     */
    public function updateDomainEntity(LampEntity $entity, LampUpdate $updateModel): LampEntity
    {
        $data = $updateModel->getData();
        $dataArray = is_object($data) ? get_object_vars($data) : (array)$data;
        $newStatus = isset($dataArray['status']) ? (bool)$dataArray['status'] : $entity->getStatus();

        return $entity->withUpdatedStatus($newStatus);
    }
}
