package api

import (
	"context"
	"errors"
	"fmt"
	"time"

	"github.com/davideme/lamp-control-api-reference/api/entities"
	"github.com/davideme/lamp-control-api-reference/api/queries"
	"github.com/google/uuid"
	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgtype"
	"github.com/jackc/pgx/v5/pgxpool"
)

// PostgresLampRepository implements LampRepository using PostgreSQL
type PostgresLampRepository struct {
	pool    *pgxpool.Pool
	queries *queries.Queries
}

// NewPostgresLampRepository creates a new PostgreSQL lamp repository
func NewPostgresLampRepository(pool *pgxpool.Pool) *PostgresLampRepository {
	return &PostgresLampRepository{
		pool:    pool,
		queries: queries.New(pool),
	}
}

// Create adds a new lamp to the repository
func (r *PostgresLampRepository) Create(ctx context.Context, lampEntity *entities.LampEntity) error {
	// Convert entity UUID to pgtype.UUID
	var pgUUID pgtype.UUID
	if err := pgUUID.Scan(lampEntity.ID[:]); err != nil {
		return fmt.Errorf("failed to scan UUID: %w", err)
	}

	// Convert time.Time to pgtype.Timestamptz
	createdAt := pgtype.Timestamptz{
		Time:  lampEntity.CreatedAt,
		Valid: true,
	}
	updatedAt := pgtype.Timestamptz{
		Time:  lampEntity.UpdatedAt,
		Valid: true,
	}

	_, err := r.queries.CreateLamp(ctx, queries.CreateLampParams{
		ID:        pgUUID,
		IsOn:      lampEntity.Status,
		CreatedAt: createdAt,
		UpdatedAt: updatedAt,
	})
	if err != nil {
		return fmt.Errorf("failed to create lamp: %w", err)
	}

	return nil
}

// GetByID retrieves a lamp by its ID
func (r *PostgresLampRepository) GetByID(ctx context.Context, id string) (*entities.LampEntity, error) {
	// Parse UUID string
	uid, err := uuid.Parse(id)
	if err != nil {
		return nil, fmt.Errorf("failed to parse UUID: %w", err)
	}

	// Convert to pgtype.UUID
	var pgUUID pgtype.UUID
	if scanErr := pgUUID.Scan(uid[:]); scanErr != nil {
		return nil, fmt.Errorf("failed to scan UUID: %w", scanErr)
	}

	lamp, err := r.queries.GetLampByID(ctx, pgUUID)
	if err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			return nil, ErrLampNotFound
		}

		return nil, fmt.Errorf("failed to get lamp: %w", err)
	}

	return r.convertToEntity(&lamp)
}

// Update modifies an existing lamp in the repository
func (r *PostgresLampRepository) Update(ctx context.Context, lampEntity *entities.LampEntity) error {
	// Convert entity UUID to pgtype.UUID
	var pgUUID pgtype.UUID
	if err := pgUUID.Scan(lampEntity.ID[:]); err != nil {
		return fmt.Errorf("failed to scan UUID: %w", err)
	}

	// Note: updated_at is automatically set by the database trigger
	_, err := r.queries.UpdateLamp(ctx, queries.UpdateLampParams{
		ID:   pgUUID,
		IsOn: lampEntity.Status,
	})

	if err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			return ErrLampNotFound
		}

		return fmt.Errorf("failed to update lamp: %w", err)
	}

	return nil
}

// Delete removes a lamp from the repository (soft delete)
func (r *PostgresLampRepository) Delete(ctx context.Context, id string) error {
	// Parse UUID string
	uid, err := uuid.Parse(id)
	if err != nil {
		return fmt.Errorf("failed to parse UUID: %w", err)
	}

	// Convert to pgtype.UUID
	var pgUUID pgtype.UUID
	if scanErr := pgUUID.Scan(uid[:]); scanErr != nil {
		return fmt.Errorf("failed to scan UUID: %w", scanErr)
	}

	// Soft delete with current timestamp
	deletedAt := pgtype.Timestamptz{
		Time:  time.Now(),
		Valid: true,
	}

	rowsAffected, err := r.queries.DeleteLamp(ctx, queries.DeleteLampParams{
		ID:        pgUUID,
		DeletedAt: deletedAt,
	})

	if err != nil {
		return fmt.Errorf("failed to delete lamp: %w", err)
	}

	if rowsAffected == 0 {
		return ErrLampNotFound
	}

	return nil
}

// List returns all lamps in the repository
func (r *PostgresLampRepository) List(ctx context.Context) ([]*entities.LampEntity, error) {
	// For simplicity, list all lamps without pagination
	// In production, you'd want to use proper pagination
	const defaultLimit = 1000 // Reasonable default limit to prevent unbounded queries

	lamps, err := r.queries.ListLamps(ctx, queries.ListLampsParams{
		Limit:  defaultLimit,
		Offset: 0,
	})

	if err != nil {
		return nil, fmt.Errorf("failed to list lamps: %w", err)
	}

	lampEntities := make([]*entities.LampEntity, 0, len(lamps))
	for i := range lamps {
		entity, err := r.convertToEntity(&lamps[i])
		if err != nil {
			return nil, err
		}
		lampEntities = append(lampEntities, entity)
	}

	return lampEntities, nil
}

// Exists checks if a lamp exists in the repository
func (r *PostgresLampRepository) Exists(ctx context.Context, id string) bool {
	_, err := r.GetByID(ctx, id)

	return err == nil
}

// convertToEntity converts a sqlc Lamp model to a domain LampEntity
func (r *PostgresLampRepository) convertToEntity(lamp *queries.Lamp) (*entities.LampEntity, error) {
	// Convert pgtype.UUID to uuid.UUID
	if !lamp.ID.Valid {
		return nil, fmt.Errorf("invalid UUID in database record")
	}
	var uid uuid.UUID
	copy(uid[:], lamp.ID.Bytes[:])

	// Convert pgtype.Timestamptz to time.Time
	if !lamp.CreatedAt.Valid || !lamp.UpdatedAt.Valid {
		return nil, fmt.Errorf("invalid timestamps in database record")
	}
	createdAt := lamp.CreatedAt.Time
	updatedAt := lamp.UpdatedAt.Time

	return &entities.LampEntity{
		ID:        uid,
		Status:    lamp.IsOn,
		CreatedAt: createdAt,
		UpdatedAt: updatedAt,
	}, nil
}
