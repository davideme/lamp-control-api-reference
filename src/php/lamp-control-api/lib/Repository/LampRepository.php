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
        $lamp->setData([
            'id' => $lampId,
            'status' => $lampCreate->status ?? false
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
        $lampData = $lamp->getData();
        if (isset($lampUpdate->status)) {
            $lampData['status'] = $lampUpdate->status;
        }
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
