<?php

namespace OpenAPIServer\Repository;

use OpenAPIServer\Model\Lamp;
use OpenAPIServer\Model\LampCreate;
use OpenAPIServer\Model\LampUpdate;

class LampRepository
{
    /** @var Lamp[] */
    private array $lamps = [];
    private int $nextId = 1;

    public function create(LampCreate $lampCreate): Lamp
    {
        $lamp = new Lamp();
        $lampId = (string)$this->nextId++;
        $now = (new \DateTime())->format(\DateTime::ATOM);

        $createData = $lampCreate->getData();
        $createArray = is_object($createData) ? get_object_vars($createData) : (array)$createData;

        $lamp->setData([
            'id' => $lampId,
            'status' => $createArray['status'] ?? false,
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

        $updateData = $lampUpdate->getData();
        $updateArray = is_object($updateData) ? get_object_vars($updateData) : (array)$updateData;

        if (array_key_exists('status', $updateArray)) {
            $lampData['status'] = $updateArray['status'];
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
