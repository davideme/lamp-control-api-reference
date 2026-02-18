//go:generate mockgen -source=repository.go -destination=mock_repository_test.go -package=api
package api

import (
	"context"
	"errors"
	"sort"
	"sync"

	"github.com/davideme/lamp-control-api-reference/api/entities"
)

// ErrLampNotFound is returned when a lamp is not found in the repository
var ErrLampNotFound = errors.New("lamp not found")

// ErrInvalidPagination is returned when pagination arguments are invalid.
var ErrInvalidPagination = errors.New("invalid pagination parameters")

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

	// List returns lamps in the repository with pagination.
	List(ctx context.Context, offset int, limit int) ([]*entities.LampEntity, error)

	// Exists checks if a lamp exists in the repository
	Exists(ctx context.Context, id string) (bool, error)
}

// InMemoryLampRepository implements LampRepository using a sync.Map-based in-memory store.
type InMemoryLampRepository struct {
	lamps sync.Map
}

// NewInMemoryLampRepository creates a new in-memory lamp repository
func NewInMemoryLampRepository() *InMemoryLampRepository {
	return &InMemoryLampRepository{}
}

// Create adds a new lamp to the repository
func (r *InMemoryLampRepository) Create(ctx context.Context, lampEntity *entities.LampEntity) error {
	r.lamps.Store(lampEntity.ID.String(), lampEntity)

	return nil
}

// GetByID retrieves a lamp by its ID
func (r *InMemoryLampRepository) GetByID(ctx context.Context, id string) (*entities.LampEntity, error) {
	value, exists := r.lamps.Load(id)
	if !exists {
		return nil, ErrLampNotFound
	}
	lampEntity, ok := value.(*entities.LampEntity)
	if !ok {
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
	id := lampEntity.ID.String()

	for {
		current, exists := r.lamps.Load(id)
		if !exists {
			return ErrLampNotFound
		}

		if r.lamps.CompareAndSwap(id, current, lampEntity) {
			return nil
		}
	}
}

// Delete removes a lamp from the repository
func (r *InMemoryLampRepository) Delete(ctx context.Context, id string) error {
	_, loaded := r.lamps.LoadAndDelete(id)
	if !loaded {
		return ErrLampNotFound
	}

	return nil
}

// List returns lamps in the repository with pagination.
func (r *InMemoryLampRepository) List(ctx context.Context, offset int, limit int) ([]*entities.LampEntity, error) {
	if offset < 0 {
		offset = 0
	}
	if limit <= 0 {
		return []*entities.LampEntity{}, nil
	}

	type lampRef struct {
		entity *entities.LampEntity
	}

	lampRefs := make([]lampRef, 0)
	r.lamps.Range(func(_, value any) bool {
		lampEntity, ok := value.(*entities.LampEntity)
		if !ok || lampEntity == nil {
			return true
		}
		lampRefs = append(lampRefs, lampRef{entity: lampEntity})

		return true
	})

	sort.Slice(lampRefs, func(i, j int) bool {
		if lampRefs[i].entity.CreatedAt.Equal(lampRefs[j].entity.CreatedAt) {
			idI := lampRefs[i].entity.ID
			idJ := lampRefs[j].entity.ID
			for k := range idI {
				if idI[k] == idJ[k] {
					continue
				}

				return idI[k] < idJ[k]
			}

			return false
		}

		return lampRefs[i].entity.CreatedAt.Before(lampRefs[j].entity.CreatedAt)
	})

	if offset >= len(lampRefs) {
		return []*entities.LampEntity{}, nil
	}

	end := offset + limit
	if end > len(lampRefs) {
		end = len(lampRefs)
	}

	page := make([]*entities.LampEntity, 0, end-offset)
	for i := offset; i < end; i++ {
		lampEntity := lampRefs[i].entity
		page = append(page, &entities.LampEntity{
			ID:        lampEntity.ID,
			Status:    lampEntity.Status,
			CreatedAt: lampEntity.CreatedAt,
			UpdatedAt: lampEntity.UpdatedAt,
		})
	}

	return page, nil
}

// Exists checks if a lamp exists in the repository
func (r *InMemoryLampRepository) Exists(ctx context.Context, id string) (bool, error) {
	_, exists := r.lamps.Load(id)

	return exists, nil
}
