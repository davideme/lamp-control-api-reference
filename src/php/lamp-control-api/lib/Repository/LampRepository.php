<?php

namespace OpenAPIServer\Repository;

use OpenAPIServer\Model\Lamp;
use OpenAPIServer\Model\LampCreate;
use OpenAPIServer\Model\LampUpdate;
use Ramsey\Uuid\Uuid;

class LampRepository
{
    /** @var Lamp[] */
    private array $lamps = [];

    public function create(LampCreate $lampCreate): Lamp
    {
        $lamp = new Lamp();
        $lampId = Uuid::uuid4()->toString();
        $now = (new \DateTime())->format(\DateTime::ATOM);
        $lamp->setData([
            'id' => $lampId,
            'status' => $lampCreate->status ?? false,
            'createdAt' => $now,
            'updatedAt' => $now
        ]);
        $this->lamps[$lampId] = $lamp;
        return $lamp;
    }

    /**
     * @return Lamp[]
     */
    public function all(): array
    {
        return array_values($this->lamps);
    }

    public function get(string $lampId): ?Lamp
    {
        return $this->lamps[$lampId] ?? null;
    }

    public function update(string $lampId, LampUpdate $lampUpdate): ?Lamp
    {
        if (!isset($this->lamps[$lampId])) {
            return null;
        }
        $lamp = $this->lamps[$lampId];
        $lampDataObj = $lamp->getData();
        $lampData = is_object($lampDataObj) ? get_object_vars($lampDataObj) : (array)$lampDataObj;

        // LampUpdate may be an OpenApi model; prefer getData() to access provided fields.
        $updateDataObj = $lampUpdate->getData();
        $updateData = is_object($updateDataObj) ? get_object_vars($updateDataObj) : (array)$updateDataObj;

        if (array_key_exists('status', $updateData)) {
            $lampData['status'] = $updateData['status'];
        }
        // Update the updatedAt timestamp
        $lampData['updatedAt'] = (new \DateTime())->format(\DateTime::ATOM);
        $lamp->setData($lampData);
        $this->lamps[$lampId] = $lamp;
        return $lamp;
    }

    public function delete(string $lampId): bool
    {
        if (!isset($this->lamps[$lampId])) {
            return false;
        }
        unset($this->lamps[$lampId]);
        return true;
    }
}
