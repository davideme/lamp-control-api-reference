<?php

namespace OpenAPIServer\Entity;

use DateTime;
use DateTimeInterface;
use Ramsey\Uuid\Uuid;
use Ramsey\Uuid\UuidInterface;

/**
 * Domain entity representing a Lamp in the system.
 * This entity is independent of the API model and represents the core business object.
 */
class LampEntity
{
    private UuidInterface $id;
    private bool $status;
    private DateTimeInterface $createdAt;
    private DateTimeInterface $updatedAt;

    /**
     * Constructor
     *
     * @param UuidInterface $id
     * @param bool $status
     * @param DateTimeInterface $createdAt
     * @param DateTimeInterface $updatedAt
     */
    public function __construct(
        UuidInterface $id,
        bool $status,
        DateTimeInterface $createdAt,
        DateTimeInterface $updatedAt
    ) {
        $this->id = $id;
        $this->status = $status;
        $this->createdAt = $createdAt;
        $this->updatedAt = $updatedAt;
    }

    /**
     * Create a new LampEntity with a generated ID and current timestamps
     *
     * @param bool $status
     * @return static
     */
    public static function create(bool $status): self
    {
        $now = new DateTime();
        return new self(Uuid::uuid4(), $status, $now, $now);
    }

    /**
     * Create an updated copy of this entity with a new status and updated timestamp
     *
     * @param bool $newStatus
     * @return static
     */
    public function withUpdatedStatus(bool $newStatus): self
    {
        return new self($this->id, $newStatus, $this->createdAt, new DateTime());
    }

    /**
     * @return UuidInterface
     */
    public function getId(): UuidInterface
    {
        return $this->id;
    }

    /**
     * @return bool
     */
    public function getStatus(): bool
    {
        return $this->status;
    }

    /**
     * @return DateTimeInterface
     */
    public function getCreatedAt(): DateTimeInterface
    {
        return $this->createdAt;
    }

    /**
     * @return DateTimeInterface
     */
    public function getUpdatedAt(): DateTimeInterface
    {
        return $this->updatedAt;
    }

    /**
     * Check equality based on ID
     *
     * @param mixed $other
     * @return bool
     */
    public function equals($other): bool
    {
        return $other instanceof self && $this->id->equals($other->id);
    }

    /**
     * String representation
     *
     * @return string
     */
    public function __toString(): string
    {
        return sprintf(
            'LampEntity(id=%s, status=%s, createdAt=%s, updatedAt=%s)',
            $this->id->toString(),
            $this->status ? 'true' : 'false',
            $this->createdAt->format('c'),
            $this->updatedAt->format('c')
        );
    }
}
