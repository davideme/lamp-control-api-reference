//go:generate mockgen -source=repository.go -destination=mock_repository_test.go -package=api
package api

import (
	"context"
	"errors"
	"sync"

	"github.com/davideme/lamp-control-api-reference/api/entities"
)

// ErrLampNotFound is returned when a lamp is not found in the repository
var ErrLampNotFound = errors.New("lamp not found")

// LampRepository defines the interface for lamp data operations
type LampRepository interface {
	// Create adds a new lamp to the repository
	Create(ctx context.Context, lampEntity *entities.LampEntity) error

	// GetByID retrieves a lamp by its ID
	GetByID(ctx context.Context, id string) (*entities.LampEntity, error)

	// Update modifies an existing lamp in the repository
	Update(ctx context.Context, lampEntity *entities.LampEntity) error

	// Delete removes a lamp from the repository
	Delete(ctx context.Context, id string) error

	// List returns all lamps in the repository
	List(ctx context.Context) ([]*entities.LampEntity, error)

	// Exists checks if a lamp exists in the repository
	Exists(ctx context.Context, id string) (bool, error)
}

// InMemoryLampRepository implements LampRepository using an in-memory map
type InMemoryLampRepository struct {
	lamps map[string]*entities.LampEntity
	mutex sync.RWMutex
}

// NewInMemoryLampRepository creates a new in-memory lamp repository
func NewInMemoryLampRepository() *InMemoryLampRepository {
	return &InMemoryLampRepository{
		lamps: make(map[string]*entities.LampEntity),
		mutex: sync.RWMutex{},
	}
}

// Create adds a new lamp to the repository
func (r *InMemoryLampRepository) Create(ctx context.Context, lampEntity *entities.LampEntity) error {
	r.mutex.Lock()
	defer r.mutex.Unlock()

	r.lamps[lampEntity.ID.String()] = lampEntity

	return nil
}

// GetByID retrieves a lamp by its ID
func (r *InMemoryLampRepository) GetByID(ctx context.Context, id string) (*entities.LampEntity, error) {
	r.mutex.RLock()
	defer r.mutex.RUnlock()

	lampEntity, exists := r.lamps[id]
	if !exists {
		return nil, ErrLampNotFound
	}

	// Return a copy to avoid race conditions when the entity is modified
	return &entities.LampEntity{
		ID:        lampEntity.ID,
		Status:    lampEntity.Status,
		CreatedAt: lampEntity.CreatedAt,
		UpdatedAt: lampEntity.UpdatedAt,
	}, nil
}

// Update modifies an existing lamp in the repository
func (r *InMemoryLampRepository) Update(ctx context.Context, lampEntity *entities.LampEntity) error {
	r.mutex.Lock()
	defer r.mutex.Unlock()

	id := lampEntity.ID.String()
	if _, exists := r.lamps[id]; !exists {
		return ErrLampNotFound
	}

	r.lamps[id] = lampEntity

	return nil
}

// Delete removes a lamp from the repository
func (r *InMemoryLampRepository) Delete(ctx context.Context, id string) error {
	r.mutex.Lock()
	defer r.mutex.Unlock()

	if _, exists := r.lamps[id]; !exists {
		return ErrLampNotFound
	}

	delete(r.lamps, id)

	return nil
}

// List returns all lamps in the repository
func (r *InMemoryLampRepository) List(ctx context.Context) ([]*entities.LampEntity, error) {
	r.mutex.RLock()
	defer r.mutex.RUnlock()

	lampEntities := make([]*entities.LampEntity, 0, len(r.lamps))
	for _, lampEntity := range r.lamps {
		// Return a copy to avoid race conditions when the entity is modified
		lampEntities = append(lampEntities, &entities.LampEntity{
			ID:        lampEntity.ID,
			Status:    lampEntity.Status,
			CreatedAt: lampEntity.CreatedAt,
			UpdatedAt: lampEntity.UpdatedAt,
		})
	}

	return lampEntities, nil
}

// Exists checks if a lamp exists in the repository
func (r *InMemoryLampRepository) Exists(ctx context.Context, id string) (bool, error) {
	r.mutex.RLock()
	defer r.mutex.RUnlock()

	_, exists := r.lamps[id]

	return exists, nil
}
