package api

import (
	"context"
	"os"
	"testing"
)

// TestCreateConnectionPool tests the connection pool creation
func TestCreateConnectionPool(t *testing.T) {
	ctx := context.Background()

	t.Run("with valid configuration", func(t *testing.T) {
		// Skip if no PostgreSQL configured
		dbConfig := NewDatabaseConfigFromEnv()
		if dbConfig == nil {
			t.Skip("PostgreSQL not configured")
		}

		pool, err := CreateConnectionPool(ctx, dbConfig)
		if err != nil {
			t.Skipf("Could not connect to PostgreSQL: %v", err)
		}
		defer pool.Close()

		// Verify pool is not nil
		if pool == nil {
			t.Error("Expected non-nil pool")
		}

		// Test ping
		err = pool.Ping(ctx)
		if err != nil {
			t.Errorf("Failed to ping pool: %v", err)
		}
	})

	t.Run("with invalid connection string", func(t *testing.T) {
		config := &DatabaseConfig{
			Host:     "",
			Port:     0,
			Database: "",
			User:     "",
			Password: "",
		}

		// Set an invalid DATABASE_URL to test error handling
		originalURL := os.Getenv("DATABASE_URL")
		os.Setenv("DATABASE_URL", "invalid://connection/string")
		defer func() {
			if originalURL != "" {
				os.Setenv("DATABASE_URL", originalURL)
			} else {
				os.Unsetenv("DATABASE_URL")
			}
		}()

		_, err := CreateConnectionPool(ctx, config)
		if err == nil {
			t.Error("Expected error with invalid connection string")
		}
	})

	t.Run("with custom pool settings", func(t *testing.T) {
		dbConfig := NewDatabaseConfigFromEnv()
		if dbConfig == nil {
			t.Skip("PostgreSQL not configured")
		}

		// Set custom pool settings
		dbConfig.PoolMin = 1
		dbConfig.PoolMax = 5

		pool, err := CreateConnectionPool(ctx, dbConfig)
		if err != nil {
			t.Skipf("Could not connect to PostgreSQL: %v", err)
		}
		defer pool.Close()

		// Verify pool was created with custom settings
		if pool == nil {
			t.Error("Expected non-nil pool")
		}
	})

	t.Run("with only DATABASE_URL", func(t *testing.T) {
		originalEnv := make(map[string]string)
		envVars := []string{"DATABASE_URL", "DB_HOST", "DB_PORT", "DB_NAME", "DB_USER", "DB_PASSWORD"}
		for _, key := range envVars {
			originalEnv[key] = os.Getenv(key)
			os.Unsetenv(key)
		}
		defer func() {
			for key, value := range originalEnv {
				if value != "" {
					os.Setenv(key, value)
				}
			}
		}()

		// Only set DATABASE_URL
		testURL := os.Getenv("TEST_DATABASE_URL")
		if testURL == "" {
			t.Skip("TEST_DATABASE_URL not set")
		}
		os.Setenv("DATABASE_URL", testURL)

		config := NewDatabaseConfigFromEnv()
		if config == nil {
			t.Fatal("Expected non-nil config with DATABASE_URL")
		}

		pool, err := CreateConnectionPool(ctx, config)
		if err != nil {
			t.Skipf("Could not connect with DATABASE_URL: %v", err)
		}
		defer pool.Close()

		if pool == nil {
			t.Error("Expected non-nil pool")
		}
	})
}

// TestNewDatabaseConfigFromEnv_EdgeCases tests additional edge cases
func TestNewDatabaseConfigFromEnv_EdgeCases(t *testing.T) {
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
			}
		}
	}()

	t.Run("with only database name", func(t *testing.T) {
		os.Setenv("DB_NAME", "testdb")
		config := NewDatabaseConfigFromEnv()
		if config == nil {
			t.Error("Expected non-nil config with DB_NAME")
		}
	})

	t.Run("with only host and user", func(t *testing.T) {
		os.Unsetenv("DB_NAME")
		os.Setenv("DB_HOST", "localhost")
		os.Setenv("DB_USER", "testuser")
		config := NewDatabaseConfigFromEnv()
		if config == nil {
			t.Error("Expected non-nil config with DB_HOST and DB_USER")
		}
	})

	t.Run("with only host without user", func(t *testing.T) {
		os.Unsetenv("DB_NAME")
		os.Unsetenv("DB_USER")
		os.Setenv("DB_HOST", "localhost")
		config := NewDatabaseConfigFromEnv()
		if config != nil {
			t.Error("Expected nil config with only DB_HOST")
		}
	})

	t.Run("with only user without host", func(t *testing.T) {
		os.Unsetenv("DB_HOST")
		os.Setenv("DB_USER", "testuser")
		config := NewDatabaseConfigFromEnv()
		if config != nil {
			t.Error("Expected nil config with only DB_USER")
		}
	})

	t.Run("with invalid port", func(t *testing.T) {
		os.Setenv("DB_NAME", "testdb")
		os.Setenv("DB_PORT", "invalid")
		config := NewDatabaseConfigFromEnv()
		if config == nil {
			t.Fatal("Expected non-nil config")
		}
		// Should use default port on parse error
		if config.Port != 5432 {
			t.Errorf("Expected default port 5432, got %d", config.Port)
		}
	})

	t.Run("with invalid pool sizes", func(t *testing.T) {
		os.Setenv("DB_NAME", "testdb")
		os.Setenv("DB_POOL_MIN_SIZE", "invalid")
		os.Setenv("DB_POOL_MAX_SIZE", "invalid")
		config := NewDatabaseConfigFromEnv()
		if config == nil {
			t.Fatal("Expected non-nil config")
		}
		// Should use defaults on parse error
		if config.PoolMin != 0 {
			t.Errorf("Expected PoolMin 0 (default), got %d", config.PoolMin)
		}
		if config.PoolMax != 4 {
			t.Errorf("Expected PoolMax 4 (default), got %d", config.PoolMax)
		}
	})

	t.Run("with large pool sizes", func(t *testing.T) {
		os.Setenv("DB_NAME", "testdb")
		os.Setenv("DB_POOL_MIN_SIZE", "10")
		os.Setenv("DB_POOL_MAX_SIZE", "50")
		config := NewDatabaseConfigFromEnv()
		if config == nil {
			t.Fatal("Expected non-nil config")
		}
		if config.PoolMin != 10 {
			t.Errorf("Expected PoolMin 10, got %d", config.PoolMin)
		}
		if config.PoolMax != 50 {
			t.Errorf("Expected PoolMax 50, got %d", config.PoolMax)
		}
	})
}

// TestDatabaseConfigConnectionString_Variants tests different connection string scenarios
func TestDatabaseConfigConnectionString_Variants(t *testing.T) {
	// Save and clear DATABASE_URL
	originalURL := os.Getenv("DATABASE_URL")
	os.Unsetenv("DATABASE_URL")
	defer func() {
		if originalURL != "" {
			os.Setenv("DATABASE_URL", originalURL)
		}
	}()

	t.Run("with all parameters", func(t *testing.T) {
		config := &DatabaseConfig{
			Host:     "dbhost",
			Port:     5433,
			Database: "mydb",
			User:     "myuser",
			Password: "mypass",
		}

		connStr := config.ConnectionString()
		expected := "host=dbhost port=5433 dbname=mydb user=myuser password=mypass"
		if connStr != expected {
			t.Errorf("Expected '%s', got '%s'", expected, connStr)
		}
	})

	t.Run("with empty password", func(t *testing.T) {
		config := &DatabaseConfig{
			Host:     "localhost",
			Port:     5432,
			Database: "testdb",
			User:     "testuser",
			Password: "",
		}

		connStr := config.ConnectionString()
		expected := "host=localhost port=5432 dbname=testdb user=testuser password="
		if connStr != expected {
			t.Errorf("Expected '%s', got '%s'", expected, connStr)
		}
	})

	t.Run("with DATABASE_URL override", func(t *testing.T) {
		os.Setenv("DATABASE_URL", "postgres://override:pass@host/db")
		defer os.Unsetenv("DATABASE_URL")

		config := &DatabaseConfig{
			Host:     "localhost",
			Port:     5432,
			Database: "testdb",
			User:     "testuser",
			Password: "testpass",
		}

		connStr := config.ConnectionString()
		if connStr != "postgres://override:pass@host/db" {
			t.Errorf("Expected DATABASE_URL value, got '%s'", connStr)
		}
	})
}
