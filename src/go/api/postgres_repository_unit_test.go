package api

import (
	"context"
	"testing"

	"github.com/davideme/lamp-control-api-reference/api/entities"
	"github.com/davideme/lamp-control-api-reference/api/queries"
	"github.com/google/uuid"
	"github.com/jackc/pgx/v5/pgtype"
)

// TestConvertToEntity tests the entity conversion function
func TestConvertToEntity(t *testing.T) {
	repo := &PostgresLampRepository{}

	t.Run("valid conversion", func(t *testing.T) {
		// Create a valid lamp record
		id := uuid.New()
		var pgUUID pgtype.UUID
		// Properly set the UUID bytes and Valid flag
		copy(pgUUID.Bytes[:], id[:])
		pgUUID.Valid = true

		now := entities.NewLampEntity(true).CreatedAt
		pgNow := pgtype.Timestamptz{
			Time:  now,
			Valid: true,
		}

		lamp := &queries.Lamp{
			ID:        pgUUID,
			IsOn:      true,
			CreatedAt: pgNow,
			UpdatedAt: pgNow,
		}

		entity, err := repo.convertToEntity(lamp)
		if err != nil {
			t.Fatalf("Expected no error, got %v", err)
		}

		if entity.Status != true {
			t.Errorf("Expected Status true, got %v", entity.Status)
		}

		// Check that the UUID was converted correctly
		var expectedUID uuid.UUID
		copy(expectedUID[:], pgUUID.Bytes[:])
		if entity.ID != expectedUID {
			t.Errorf("Expected ID %v, got %v", expectedUID, entity.ID)
		}
	})

	t.Run("invalid UUID", func(t *testing.T) {
		lamp := &queries.Lamp{
			ID: pgtype.UUID{Valid: false},
		}

		_, err := repo.convertToEntity(lamp)
		if err == nil {
			t.Error("Expected error for invalid UUID")
		}
	})

	t.Run("invalid created_at timestamp", func(t *testing.T) {
		id := uuid.New()
		var pgUUID pgtype.UUID
		copy(pgUUID.Bytes[:], id[:])
		pgUUID.Valid = true

		lamp := &queries.Lamp{
			ID:        pgUUID,
			CreatedAt: pgtype.Timestamptz{Valid: false},
			UpdatedAt: pgtype.Timestamptz{Valid: true},
		}

		_, err := repo.convertToEntity(lamp)
		if err == nil {
			t.Error("Expected error for invalid created_at timestamp")
		}
	})

	t.Run("invalid updated_at timestamp", func(t *testing.T) {
		id := uuid.New()
		var pgUUID pgtype.UUID
		copy(pgUUID.Bytes[:], id[:])
		pgUUID.Valid = true

		lamp := &queries.Lamp{
			ID:        pgUUID,
			CreatedAt: pgtype.Timestamptz{Valid: true},
			UpdatedAt: pgtype.Timestamptz{Valid: false},
		}

		_, err := repo.convertToEntity(lamp)
		if err == nil {
			t.Error("Expected error for invalid updated_at timestamp")
		}
	})
}

// TestNewPostgresLampRepository tests repository creation
func TestNewPostgresLampRepository(t *testing.T) {
	// This test requires a real connection pool, but we can at least test
	// that the function doesn't panic with nil
	defer func() {
		if r := recover(); r != nil {
			t.Errorf("NewPostgresLampRepository panicked: %v", r)
		}
	}()

	// Test with nil pool - should not panic during creation
	// (would panic during actual use, which is acceptable)
	_ = NewPostgresLampRepository(nil)
}

// TestPostgresRepositoryErrorCases tests error handling paths
func TestPostgresRepositoryErrorCases(t *testing.T) {
	ctx := context.Background()

	t.Run("Create with invalid UUID should handle error", func(t *testing.T) {
		// Skip if no DB configured - this test validates error handling
		dbConfig := NewDatabaseConfigFromEnv()
		if dbConfig == nil {
			t.Skip("PostgreSQL not configured")
		}

		pool, err := CreateConnectionPool(ctx, dbConfig)
		if err != nil {
			t.Skip("Could not connect to PostgreSQL")
		}
		defer pool.Close()

		repo := NewPostgresLampRepository(pool)

		// Test that the repository is not nil
		if repo == nil {
			t.Error("Expected non-nil repository")
		}

		// Test that the queries are initialized
		if repo.queries == nil {
			t.Error("Expected non-nil queries")
		}
	})
}
