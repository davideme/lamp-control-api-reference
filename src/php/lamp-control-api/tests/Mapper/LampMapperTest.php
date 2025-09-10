<?php

namespace OpenAPIServer\Tests\Mapper;

use DateTime;
use OpenAPIServer\Entity\LampEntity;
use OpenAPIServer\Mapper\LampMapper;
use OpenAPIServer\Model\Lamp;
use OpenAPIServer\Model\LampCreate;
use OpenAPIServer\Model\LampUpdate;
use PHPUnit\Framework\TestCase;
use Ramsey\Uuid\Uuid;

class LampMapperTest extends TestCase
{
    private LampMapper $mapper;

    protected function setUp(): void
    {
        $this->mapper = new LampMapper();
    }

    public function testToApiModelShouldConvertEntityToApiModel()
    {
        // Arrange
        $id = Uuid::uuid4();
        $createdAt = new DateTime('2023-01-01T00:00:00Z');
        $updatedAt = new DateTime('2023-01-02T00:00:00Z');
        $entity = new LampEntity($id, true, $createdAt, $updatedAt);

        // Act
        $apiModel = $this->mapper->toApiModel($entity);

        // Assert
        $data = $apiModel->getData();
        $dataArray = is_object($data) ? get_object_vars($data) : (array)$data;

        $this->assertEquals($id->toString(), $dataArray['id']);
        $this->assertTrue($dataArray['status']);
        $this->assertEquals($createdAt->format('c'), $dataArray['createdAt']);
        $this->assertEquals($updatedAt->format('c'), $dataArray['updatedAt']);
    }

    public function testToDomainEntityShouldConvertApiModelToEntity()
    {
        // Arrange
        $id = Uuid::uuid4();
        $createdAt = '2023-01-01T00:00:00+00:00';
        $updatedAt = '2023-01-02T00:00:00+00:00';

        $apiModel = new Lamp();
        $apiModel->setData([
            'id' => $id->toString(),
            'status' => false,
            'createdAt' => $createdAt,
            'updatedAt' => $updatedAt
        ]);

        // Act
        $entity = $this->mapper->toDomainEntity($apiModel);

        // Assert
        $this->assertTrue($id->equals($entity->getId()));
        $this->assertFalse($entity->getStatus());
        $this->assertEquals(new DateTime($createdAt), $entity->getCreatedAt());
        $this->assertEquals(new DateTime($updatedAt), $entity->getUpdatedAt());
    }

    public function testToDomainEntityCreateShouldCreateEntityFromCreateModel()
    {
        // Arrange
        $lampCreate = new LampCreate();
        $lampCreate->setData(['status' => true]);

        // Act
        $entity = $this->mapper->toDomainEntityCreate($lampCreate);

        // Assert
        $this->assertNotNull($entity->getId());
        $this->assertTrue($entity->getStatus());
        $this->assertInstanceOf(\DateTimeInterface::class, $entity->getCreatedAt());
        $this->assertInstanceOf(\DateTimeInterface::class, $entity->getUpdatedAt());
    }

    public function testToDomainEntityCreateWithMissingStatusShouldDefaultToFalse()
    {
        // Arrange
        $lampCreate = new LampCreate();
        $lampCreate->setData([]);

        // Act
        $entity = $this->mapper->toDomainEntityCreate($lampCreate);

        // Assert
        $this->assertFalse($entity->getStatus());
    }

    public function testUpdateDomainEntityShouldUpdateEntityFromUpdateModel()
    {
        // Arrange
        $originalEntity = LampEntity::create(false);

        $lampUpdate = new LampUpdate();
        $lampUpdate->setData(['status' => true]);

        // Act
        $updatedEntity = $this->mapper->updateDomainEntity($originalEntity, $lampUpdate);

        // Assert
        $this->assertTrue($originalEntity->getId()->equals($updatedEntity->getId()));
        $this->assertTrue($updatedEntity->getStatus());
        $this->assertEquals($originalEntity->getCreatedAt(), $updatedEntity->getCreatedAt());
        $this->assertGreaterThanOrEqual($originalEntity->getUpdatedAt(), $updatedEntity->getUpdatedAt());
    }

    public function testUpdateDomainEntityWithMissingStatusShouldKeepOriginalStatus()
    {
        // Arrange
        $originalEntity = LampEntity::create(true);

        $lampUpdate = new LampUpdate();
        $lampUpdate->setData([]);

        // Act
        $updatedEntity = $this->mapper->updateDomainEntity($originalEntity, $lampUpdate);

        // Assert
        $this->assertTrue($updatedEntity->getStatus()); // Should keep original status
    }
}
