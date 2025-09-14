<?php

declare(strict_types=1);

namespace Tests\Service;

use OpenAPIServer\Entity\LampEntity;
use OpenAPIServer\Mapper\LampMapper;
use OpenAPIServer\Model\LampCreate;
use OpenAPIServer\Model\LampUpdate;
use OpenAPIServer\Repository\LampRepository;
use OpenAPIServer\Service\LampService;
use PHPUnit\Framework\TestCase;
use Ramsey\Uuid\Uuid;

class LampServiceTest extends TestCase
{
    private LampRepository $repository;
    private LampMapper $mapper;
    private LampService $service;

    protected function setUp(): void
    {
        $this->repository = new LampRepository();
        $this->mapper = new LampMapper();
        $this->service = new LampService($this->repository, $this->mapper);
    }

    public function testCreateReturnsApiModel(): void
    {
        $create = new LampCreate();
        // Using reflection or dynamic property if model permits; if strict, adapt test once model has setters
        $create->status = true; // property expected in generated model
        $apiModel = $this->service->create($create);
        $data = (array)$apiModel->getData();
        $this->assertArrayHasKey('status', $data);
        $this->assertTrue($data['status']);
        $this->assertArrayHasKey('id', $data);
        $this->assertNotEmpty($data['id']);
    }

    public function testAllReturnsMappedModels(): void
    {
        // Seed repository directly with domain entities
        $id = Uuid::uuid4();
        $entity = LampEntity::create(true)->withUpdatedStatus(true); // create + mutate for diversity
        // Force ID replacement to stable one if method exists; fallback uses create()
        $this->repository->create($entity);
        $all = $this->service->all();
        $this->assertNotEmpty($all);
        $data = (array)$all[0]->getData();
        $this->assertTrue($data['status']);
    }

    public function testGetReturnsNullWhenMissing(): void
    {
        $this->assertNull($this->service->get('non-existent'));
    }

    public function testUpdateReturnsNullForMissingEntity(): void
    {
        $update = new LampUpdate();
        $update->status = false;
        $this->assertNull($this->service->update('missing', $update));
    }

    public function testUpdateChangesStatus(): void
    {
        $create = new LampCreate();
        $create->status = true;
        $created = $this->service->create($create);
        $lampId = (array)$created->getData();
        $lampId = $lampId['id'];

        $update = new LampUpdate();
        $update->status = false;
        $updated = $this->service->update($lampId, $update);
        $this->assertNotNull($updated);
        $updatedData = (array)$updated->getData();
        $this->assertFalse($updatedData['status']);
    }

    public function testDeleteRemovesLamp(): void
    {
        $create = new LampCreate();
        $create->status = true;
        $created = $this->service->create($create);
        $lampId = (array)$created->getData();
        $lampId = $lampId['id'];
        $this->assertTrue($this->service->delete($lampId));
        $this->assertNull($this->service->get($lampId));
    }
}
