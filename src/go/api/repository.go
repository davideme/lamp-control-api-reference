//go:generate mockgen -source=repository.go -destination=mock_repository_test.go -package=api
package api

import (
	"context"
	"errors"
	"sync"
)

// ErrLampNotFound is returned when a lamp is not found in the repository
var ErrLampNotFound = errors.New("lamp not found")

// LampRepository defines the interface for lamp data operations
type LampRepository interface {
	// Create adds a new lamp to the repository
	Create(ctx context.Context, lamp Lamp) error

	// GetByID retrieves a lamp by its ID
	GetByID(ctx context.Context, id string) (Lamp, error)

	// Update modifies an existing lamp in the repository
	Update(ctx context.Context, lamp Lamp) error

	// Delete removes a lamp from the repository
	Delete(ctx context.Context, id string) error

	// List returns all lamps in the repository
	List(ctx context.Context) ([]Lamp, error)

	// Exists checks if a lamp exists in the repository
	Exists(ctx context.Context, id string) bool
}

// InMemoryLampRepository implements LampRepository using an in-memory map
type InMemoryLampRepository struct {
	lamps map[string]Lamp
	mutex sync.RWMutex
}

// NewInMemoryLampRepository creates a new in-memory lamp repository
func NewInMemoryLampRepository() *InMemoryLampRepository {
	return &InMemoryLampRepository{
		lamps: make(map[string]Lamp),
		mutex: sync.RWMutex{},
	}
}

// Create adds a new lamp to the repository
func (r *InMemoryLampRepository) Create(ctx context.Context, lamp Lamp) error {
	r.mutex.Lock()
	defer r.mutex.Unlock()

	r.lamps[lamp.Id.String()] = lamp

	return nil
}

// GetByID retrieves a lamp by its ID
func (r *InMemoryLampRepository) GetByID(ctx context.Context, id string) (Lamp, error) {
	r.mutex.RLock()
	defer r.mutex.RUnlock()

	lamp, exists := r.lamps[id]
	if !exists {
		return Lamp{}, ErrLampNotFound
	}

	return lamp, nil
}

// Update modifies an existing lamp in the repository
func (r *InMemoryLampRepository) Update(ctx context.Context, lamp Lamp) error {
	r.mutex.Lock()
	defer r.mutex.Unlock()

	id := lamp.Id.String()
	if _, exists := r.lamps[id]; !exists {
		return ErrLampNotFound
	}

	r.lamps[id] = lamp

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
func (r *InMemoryLampRepository) List(ctx context.Context) ([]Lamp, error) {
	r.mutex.RLock()
	defer r.mutex.RUnlock()

	lamps := make([]Lamp, 0, len(r.lamps))
	for _, lamp := range r.lamps {
		lamps = append(lamps, lamp)
	}

	return lamps, nil
}

// Exists checks if a lamp exists in the repository
func (r *InMemoryLampRepository) Exists(ctx context.Context, id string) bool {
	r.mutex.RLock()
	defer r.mutex.RUnlock()

	_, exists := r.lamps[id]

	return exists
}
