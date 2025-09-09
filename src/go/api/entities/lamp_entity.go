package entities

import (
	"time"

	"github.com/google/uuid"
)

// LampEntity represents a lamp in the domain model.
// This is separate from the HTTP API model to allow independent evolution
// of the internal domain logic and external API contract.
type LampEntity struct {
	ID        uuid.UUID
	Status    bool
	CreatedAt time.Time
	UpdatedAt time.Time
}

// NewLampEntity creates a new lamp entity with generated ID and timestamps
func NewLampEntity(status bool) *LampEntity {
	now := time.Now()
	return &LampEntity{
		ID:        uuid.New(),
		Status:    status,
		CreatedAt: now,
		UpdatedAt: now,
	}
}

// UpdateStatus updates the lamp status and timestamp
func (l *LampEntity) UpdateStatus(status bool) {
	l.Status = status
	l.UpdatedAt = time.Now()
}