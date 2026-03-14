package entities

import (
	"time"

	"github.com/google/uuid"
)

// LampEntity represents a lamp in the domain model.
// This is separate from the HTTP API model to allow independent evolution
// of the internal domain logic and external API contract.
//
// Timestamps are managed by the database (DEFAULT CURRENT_TIMESTAMP and
// BEFORE UPDATE trigger) and populated from the RETURNING clause after
// each write.
type LampEntity struct {
	ID        uuid.UUID
	Status    bool
	CreatedAt time.Time
	UpdatedAt time.Time
}

// NewLampEntity creates a new lamp entity with a generated ID.
// Timestamps are set by the database on INSERT and must not be pre-populated.
func NewLampEntity(status bool) *LampEntity {
	return &LampEntity{
		ID:     uuid.New(),
		Status: status,
	}
}

// UpdateStatus updates the lamp status.
// updated_at is managed by the database BEFORE UPDATE trigger.
func (l *LampEntity) UpdateStatus(status bool) {
	l.Status = status
}
