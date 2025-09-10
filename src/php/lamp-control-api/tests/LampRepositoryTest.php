<?php

namespace OpenAPIServer\Tests;

use OpenAPIServer\Entity\LampEntity;
use OpenAPIServer\Repository\LampRepository;
use PHPUnit\Framework\TestCase;

class LampRepositoryTest extends TestCase
{
    public function testCreateAndGetLamp()
    {
        $repo = new LampRepository();
        $entity = LampEntity::create(true);
        $createdEntity = $repo->create($entity);

        $this->assertNotNull($createdEntity);
        $this->assertTrue($createdEntity->getStatus());
        $this->assertTrue($entity->getId()->equals($createdEntity->getId()));

        $fetched = $repo->get($entity->getId()->toString());
        $this->assertTrue($entity->equals($fetched));
    }

    public function testAllReturnsAllLamps()
    {
        $repo = new LampRepository();
        $entity1 = LampEntity::create(true);
        $entity2 = LampEntity::create(false);

        $repo->create($entity1);
        $repo->create($entity2);

        $all = $repo->all();
        $this->assertCount(2, $all);
    }

    public function testUpdateLamp()
    {
        $repo = new LampRepository();
        $entity = LampEntity::create(false);
        $createdEntity = $repo->create($entity);

        $updatedEntity = $createdEntity->withUpdatedStatus(true);
        $result = $repo->update($updatedEntity);

        $this->assertNotNull($result);
        $this->assertTrue($result->getStatus());
        $this->assertTrue($entity->getId()->equals($result->getId()));
    }

    public function testDeleteLamp()
    {
        $repo = new LampRepository();
        $entity = LampEntity::create(true);
        $createdEntity = $repo->create($entity);
        $lampId = $createdEntity->getId()->toString();

        $deleted = $repo->delete($lampId);
        $this->assertTrue($deleted);
        $this->assertNull($repo->get($lampId));
    }

    public function testUpdateNonexistentLampReturnsNull()
    {
        $repo = new LampRepository();
        $entity = LampEntity::create(true);
        $this->assertNull($repo->update($entity));
    }

    public function testDeleteNonexistentLampReturnsFalse()
    {
        $repo = new LampRepository();
        $this->assertFalse($repo->delete('999'));
    }
}
