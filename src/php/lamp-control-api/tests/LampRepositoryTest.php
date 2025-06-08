<?php

use OpenAPIServer\Repository\LampRepository;
use OpenAPIServer\Model\LampCreate;
use OpenAPIServer\Model\LampUpdate;
use PHPUnit\Framework\TestCase;

class LampRepositoryTest extends TestCase
{
    public function testCreateAndGetLamp()
    {
        $repo = new LampRepository();
        $lampCreate = new LampCreate();
        $lampCreate->status = true;
        $lamp = $repo->create($lampCreate);
        $this->assertNotNull($lamp);
        $this->assertEquals(true, $lamp->getData()->status);
        $fetched = $repo->get($lamp->getData()->id);
        $this->assertEquals($lamp, $fetched);
    }

    public function testAllReturnsAllLamps()
    {
        $repo = new LampRepository();
        $lampCreate1 = new LampCreate();
        $lampCreate1->status = true;
        $lampCreate2 = new LampCreate();
        $lampCreate2->status = false;
        $repo->create($lampCreate1);
        $repo->create($lampCreate2);
        $all = $repo->all();
        $this->assertCount(2, $all);
    }

    public function testUpdateLamp()
    {
        $repo = new LampRepository();
        $lampCreate = new LampCreate();
        $lampCreate->status = false;
        $lamp = $repo->create($lampCreate);
        $lampId = $lamp->getData()->id;
        $update = new LampUpdate();
        $update->status = true;
        $updated = $repo->update($lampId, $update);
        $this->assertEquals(true, $updated->getData()->status);
    }

    public function testDeleteLamp()
    {
        $repo = new LampRepository();
        $lampCreate = new LampCreate();
        $lampCreate->status = true;
        $lamp = $repo->create($lampCreate);
        $lampId = $lamp->getData()->id;
        $deleted = $repo->delete($lampId);
        $this->assertTrue($deleted);
        $this->assertNull($repo->get($lampId));
    }

    public function testUpdateNonexistentLampReturnsNull()
    {
        $repo = new LampRepository();
        $update = new LampUpdate();
        $update->status = true;
        $this->assertNull($repo->update('999', $update));
    }

    public function testDeleteNonexistentLampReturnsFalse()
    {
        $repo = new LampRepository();
        $this->assertFalse($repo->delete('999'));
    }
}
