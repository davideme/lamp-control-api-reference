//go:generate mockgen -source=repository.go -destination=mock_repository_test.go -package=api
package api

import (
	"context"
	"errors"
	"sort"

	"github.com/davideme/lamp-control-api-reference/api/entities"
	"sync"
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

// InMemoryLampRepository implements LampRepository using an in-memory map
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
	if _, exists := r.lamps.Load(id); !exists {
		return ErrLampNotFound
	}

	r.lamps.Store(id, lampEntity)

	return nil
}

// Delete removes a lamp from the repository
func (r *InMemoryLampRepository) Delete(ctx context.Context, id string) error {
	if _, exists := r.lamps.Load(id); !exists {
		return ErrLampNotFound
	}

	r.lamps.Delete(id)

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
		id     string
		entity *entities.LampEntity
	}

	lampRefs := make([]lampRef, 0)
	r.lamps.Range(func(key, value any) bool {
		id, ok := key.(string)
		if !ok {
			return true
		}
		lampEntity, ok := value.(*entities.LampEntity)
		if !ok || lampEntity == nil {
			return true
		}
		lampRefs = append(lampRefs, lampRef{id: id, entity: lampEntity})
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
