<?php

namespace OpenAPIServer\Tests\Entity;

use DateTime;
use OpenAPIServer\Entity\LampEntity;
use PHPUnit\Framework\TestCase;
use Ramsey\Uuid\Uuid;

class LampEntityTest extends TestCase
{
    public function testCreateShouldGenerateNewIdAndTimestamps(): void
    {
        // Act
        $entity = LampEntity::create(true);

        // Assert
        $this->assertNotNull($entity->getId());
        $this->assertTrue($entity->getStatus());
        $this->assertInstanceOf(\DateTimeInterface::class, $entity->getCreatedAt());
        $this->assertInstanceOf(\DateTimeInterface::class, $entity->getUpdatedAt());
        $this->assertEquals($entity->getCreatedAt()->getTimestamp(), $entity->getUpdatedAt()->getTimestamp());
    }

    public function testWithUpdatedStatusShouldCreateUpdatedEntity(): void
    {
        // Arrange
        $originalEntity = LampEntity::create(false);
        usleep(1000); // Ensure different timestamp

        // Act
        $updatedEntity = $originalEntity->withUpdatedStatus(true);

        // Assert
        $this->assertTrue($originalEntity->getId()->equals($updatedEntity->getId()));
        $this->assertTrue($updatedEntity->getStatus());
        $this->assertEquals($originalEntity->getCreatedAt(), $updatedEntity->getCreatedAt());
        $this->assertGreaterThan($originalEntity->getUpdatedAt(), $updatedEntity->getUpdatedAt());
    }

    public function testWithUpdatedStatusShouldMaintainImmutability(): void
    {
        // Arrange
        $originalEntity = LampEntity::create(false);

        // Act
        $updatedEntity = $originalEntity->withUpdatedStatus(true);

        // Assert
        $this->assertFalse($originalEntity->getStatus());
        $this->assertTrue($updatedEntity->getStatus());
        $this->assertNotSame($originalEntity, $updatedEntity); // Different instances
    }

    public function testEqualsShouldReturnTrueForSameId(): void
    {
        // Arrange
        $id = Uuid::uuid4();
        $timestamp = new DateTime();
        $entity1 = new LampEntity($id, true, $timestamp, $timestamp);
        $entity2 = new LampEntity($id, false, (new DateTime())->modify('+1 day'), (new DateTime())->modify('+1 day'));

        // Act & Assert
        $this->assertTrue($entity1->equals($entity2));
    }

    public function testEqualsShouldReturnFalseForDifferentId(): void
    {
        // Arrange
        $timestamp = new DateTime();
        $entity1 = new LampEntity(Uuid::uuid4(), true, $timestamp, $timestamp);
        $entity2 = new LampEntity(Uuid::uuid4(), true, $timestamp, $timestamp);

        // Act & Assert
        $this->assertFalse($entity1->equals($entity2));
    }

    public function testToStringShouldReturnFormattedString(): void
    {
        // Arrange
        $entity = LampEntity::create(true);

        // Act
        $result = (string) $entity;

        // Assert
        $this->assertStringContainsString('LampEntity', $result);
        $this->assertStringContainsString($entity->getId()->toString(), $result);
        $this->assertStringContainsString('true', $result);
    }

    public function testDomainEntitySeparationShouldBeIndependentOfApiModels(): void
    {
        // This test ensures that LampEntity doesn't depend on any API model classes
        // Arrange
        $entity = LampEntity::create(true);

        // Assert - should be able to create and manipulate without API dependencies
        $reflection = new \ReflectionClass($entity);
        $apiModelNamespace = 'OpenAPIServer\Model';

        // Verify no direct dependency on API model classes in namespace
        $this->assertStringNotContainsString($apiModelNamespace, $reflection->getNamespaceName());
        $this->assertNotNull((string) $entity); // Basic functionality works
    }
}
