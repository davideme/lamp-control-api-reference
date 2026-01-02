package api

import (
	"context"
	"os"
	"testing"
	"time"

	"github.com/davideme/lamp-control-api-reference/api/entities"
	"github.com/google/uuid"
)

// TestPostgresLampRepository tests the PostgreSQL repository implementation
// These tests require a running PostgreSQL database with the schema initialized
// Set environment variables to configure the database connection:
//
//	DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
func TestPostgresLampRepository(t *testing.T) {
	// Check if PostgreSQL is configured
	dbConfig := NewDatabaseConfigFromEnv()
	if dbConfig == nil {
		t.Skip("PostgreSQL not configured, skipping integration tests")
	}

	ctx := context.Background()

	// Create connection pool
	pool, err := CreateConnectionPool(ctx, dbConfig)
	if err != nil {
		t.Skipf("Failed to connect to PostgreSQL: %v (skipping integration tests)", err)
	}
	defer pool.Close()

	// Create repository
	repo := NewPostgresLampRepository(pool)

	// Run the test suite
	t.Run("Create", func(t *testing.T) {
		testPostgresCreate(t, repo)
	})

	t.Run("GetByID", func(t *testing.T) {
		testPostgresGetByID(t, repo)
	})

	t.Run("Update", func(t *testing.T) {
		testPostgresUpdate(t, repo)
	})

	t.Run("List", func(t *testing.T) {
		testPostgresList(t, repo)
	})

	t.Run("Delete", func(t *testing.T) {
		testPostgresDelete(t, repo)
	})

	t.Run("Exists", func(t *testing.T) {
		testPostgresExists(t, repo)
	})
}

func testPostgresCreate(t *testing.T, repo *PostgresLampRepository) {
	t.Helper()

	ctx := context.Background()

	// Create a lamp entity
	lampEntity := entities.NewLampEntity(true)

	// Create in database
	err := repo.Create(ctx, lampEntity)
	if err != nil {
		t.Fatalf("Failed to create lamp: %v", err)
	}

	// Verify it was created by retrieving it
	retrieved, err := repo.GetByID(ctx, lampEntity.ID.String())
	if err != nil {
		t.Fatalf("Failed to retrieve created lamp: %v", err)
	}

	if retrieved.ID != lampEntity.ID {
		t.Errorf("Expected ID %v, got %v", lampEntity.ID, retrieved.ID)
	}

	if retrieved.Status != lampEntity.Status {
		t.Errorf("Expected Status %v, got %v", lampEntity.Status, retrieved.Status)
	}

	// Clean up
	_ = repo.Delete(ctx, lampEntity.ID.String())
}

func testPostgresGetByID(t *testing.T, repo *PostgresLampRepository) {
	t.Helper()

	ctx := context.Background()

	// Create a lamp
	lampEntity := entities.NewLampEntity(false)
	err := repo.Create(ctx, lampEntity)
	if err != nil {
		t.Fatalf("Failed to create lamp: %v", err)
	}
	defer repo.Delete(ctx, lampEntity.ID.String())

	// Test successful retrieval
	retrieved, err := repo.GetByID(ctx, lampEntity.ID.String())
	if err != nil {
		t.Fatalf("Failed to get lamp by ID: %v", err)
	}

	if retrieved.ID != lampEntity.ID {
		t.Errorf("Expected ID %v, got %v", lampEntity.ID, retrieved.ID)
	}

	// Test not found
	nonExistentID := uuid.New().String()
	_, err = repo.GetByID(ctx, nonExistentID)
	if err != ErrLampNotFound {
		t.Errorf("Expected ErrLampNotFound, got %v", err)
	}
}

func testPostgresUpdate(t *testing.T, repo *PostgresLampRepository) {
	t.Helper()

	ctx := context.Background()

	// Create a lamp
	lampEntity := entities.NewLampEntity(true)
	err := repo.Create(ctx, lampEntity)
	if err != nil {
		t.Fatalf("Failed to create lamp: %v", err)
	}
	defer repo.Delete(ctx, lampEntity.ID.String())

	// Update the lamp
	lampEntity.UpdateStatus(false)
	err = repo.Update(ctx, lampEntity)
	if err != nil {
		t.Fatalf("Failed to update lamp: %v", err)
	}

	// Verify the update
	retrieved, err := repo.GetByID(ctx, lampEntity.ID.String())
	if err != nil {
		t.Fatalf("Failed to retrieve updated lamp: %v", err)
	}

	if retrieved.Status != false {
		t.Errorf("Expected Status false, got %v", retrieved.Status)
	}

	// Test updating non-existent lamp
	nonExistentLamp := entities.NewLampEntity(true)
	err = repo.Update(ctx, nonExistentLamp)
	if err != ErrLampNotFound {
		t.Errorf("Expected ErrLampNotFound, got %v", err)
	}
}

func testPostgresList(t *testing.T, repo *PostgresLampRepository) {
	t.Helper()

	ctx := context.Background()

	// Get initial count
	initialLamps, err := repo.List(ctx)
	if err != nil {
		t.Fatalf("Failed to list lamps: %v", err)
	}
	initialCount := len(initialLamps)

	// Create multiple lamps
	lamp1 := entities.NewLampEntity(true)
	lamp2 := entities.NewLampEntity(false)
	lamp3 := entities.NewLampEntity(true)

	err = repo.Create(ctx, lamp1)
	if err != nil {
		t.Fatalf("Failed to create lamp1: %v", err)
	}
	defer repo.Delete(ctx, lamp1.ID.String())

	// Note: In a real application, proper ordering would be handled by database
	// or cursor-based pagination. Using short sleeps here only for test purposes
	// to ensure distinct timestamps for ordering verification.
	time.Sleep(10 * time.Millisecond)

	err = repo.Create(ctx, lamp2)
	if err != nil {
		t.Fatalf("Failed to create lamp2: %v", err)
	}
	defer repo.Delete(ctx, lamp2.ID.String())

	time.Sleep(10 * time.Millisecond)

	err = repo.Create(ctx, lamp3)
	if err != nil {
		t.Fatalf("Failed to create lamp3: %v", err)
	}
	defer repo.Delete(ctx, lamp3.ID.String())

	// List all lamps
	lamps, err := repo.List(ctx)
	if err != nil {
		t.Fatalf("Failed to list lamps: %v", err)
	}

	if len(lamps) != initialCount+3 {
		t.Errorf("Expected %d lamps, got %d", initialCount+3, len(lamps))
	}
}

func testPostgresDelete(t *testing.T, repo *PostgresLampRepository) {
	t.Helper()

	ctx := context.Background()

	// Create a lamp
	lampEntity := entities.NewLampEntity(true)
	err := repo.Create(ctx, lampEntity)
	if err != nil {
		t.Fatalf("Failed to create lamp: %v", err)
	}

	// Delete the lamp
	err = repo.Delete(ctx, lampEntity.ID.String())
	if err != nil {
		t.Fatalf("Failed to delete lamp: %v", err)
	}

	// Verify it's gone (soft deleted)
	_, err = repo.GetByID(ctx, lampEntity.ID.String())
	if err != ErrLampNotFound {
		t.Errorf("Expected ErrLampNotFound after delete, got %v", err)
	}

	// Test deleting non-existent lamp
	nonExistentID := uuid.New().String()
	err = repo.Delete(ctx, nonExistentID)
	if err != ErrLampNotFound {
		t.Errorf("Expected ErrLampNotFound, got %v", err)
	}
}

func testPostgresExists(t *testing.T, repo *PostgresLampRepository) {
	t.Helper()

	ctx := context.Background()

	// Create a lamp
	lampEntity := entities.NewLampEntity(true)
	err := repo.Create(ctx, lampEntity)
	if err != nil {
		t.Fatalf("Failed to create lamp: %v", err)
	}
	defer repo.Delete(ctx, lampEntity.ID.String())

	// Test that it exists
	exists := repo.Exists(ctx, lampEntity.ID.String())
	if !exists {
		t.Error("Expected lamp to exist")
	}

	// Test non-existent lamp
	nonExistentID := uuid.New().String()
	exists = repo.Exists(ctx, nonExistentID)
	if exists {
		t.Error("Expected lamp to not exist")
	}
}

// TestNewDatabaseConfigFromEnv tests the configuration parsing
func TestNewDatabaseConfigFromEnv(t *testing.T) {
	// Save original environment
	originalEnv := make(map[string]string)
	envVars := []string{"DATABASE_URL", "DB_HOST", "DB_PORT", "DB_NAME", "DB_USER", "DB_PASSWORD", "DB_POOL_MIN_SIZE", "DB_POOL_MAX_SIZE"}
	for _, key := range envVars {
		originalEnv[key] = os.Getenv(key)
		os.Unsetenv(key)
	}
	defer func() {
		for key, value := range originalEnv {
			if value != "" {
				os.Setenv(key, value)
			} else {
				os.Unsetenv(key)
			}
		}
	}()

	// Test with no environment variables
	config := NewDatabaseConfigFromEnv()
	if config != nil {
		t.Error("Expected nil config when no environment variables are set")
	}

	// Test with DATABASE_URL
	os.Setenv("DATABASE_URL", "postgres://user:pass@localhost/mydb")
	config = NewDatabaseConfigFromEnv()
	if config == nil {
		t.Fatal("Expected non-nil config with DATABASE_URL")
	}

	// Test with individual parameters
	os.Unsetenv("DATABASE_URL")
	os.Setenv("DB_HOST", "testhost")
	os.Setenv("DB_PORT", "5433")
	os.Setenv("DB_NAME", "testdb")
	os.Setenv("DB_USER", "testuser")
	os.Setenv("DB_PASSWORD", "testpass")
	os.Setenv("DB_POOL_MIN_SIZE", "2")
	os.Setenv("DB_POOL_MAX_SIZE", "10")

	config = NewDatabaseConfigFromEnv()
	if config == nil {
		t.Fatal("Expected non-nil config with environment variables")
	}

	if config.Host != "testhost" {
		t.Errorf("Expected Host 'testhost', got '%s'", config.Host)
	}
	if config.Port != 5433 {
		t.Errorf("Expected Port 5433, got %d", config.Port)
	}
	if config.Database != "testdb" {
		t.Errorf("Expected Database 'testdb', got '%s'", config.Database)
	}
	if config.User != "testuser" {
		t.Errorf("Expected User 'testuser', got '%s'", config.User)
	}
	if config.Password != "testpass" {
		t.Errorf("Expected Password 'testpass', got '%s'", config.Password)
	}
	if config.PoolMin != 2 {
		t.Errorf("Expected PoolMin 2, got %d", config.PoolMin)
	}
	if config.PoolMax != 10 {
		t.Errorf("Expected PoolMax 10, got %d", config.PoolMax)
	}
}

// TestDatabaseConfigConnectionString tests connection string generation
func TestDatabaseConfigConnectionString(t *testing.T) {
	// Save and clear DATABASE_URL
	originalURL := os.Getenv("DATABASE_URL")
	os.Unsetenv("DATABASE_URL")
	defer func() {
		if originalURL != "" {
			os.Setenv("DATABASE_URL", originalURL)
		}
	}()

	config := &DatabaseConfig{
		Host:     "localhost",
		Port:     5432,
		Database: "testdb",
		User:     "testuser",
		Password: "testpass",
		PoolMin:  0,
		PoolMax:  4,
	}

	connStr := config.ConnectionString()
	expected := "host=localhost port=5432 dbname=testdb user=testuser password=testpass"
	if connStr != expected {
		t.Errorf("Expected connection string '%s', got '%s'", expected, connStr)
	}

	// Test with DATABASE_URL environment variable
	os.Setenv("DATABASE_URL", "postgres://user:pass@host/db")
	connStr = config.ConnectionString()
	if connStr != "postgres://user:pass@host/db" {
		t.Errorf("Expected DATABASE_URL value, got '%s'", connStr)
	}
}
