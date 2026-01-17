package api

import (
	"embed"
	"fmt"
	"log"

	"github.com/golang-migrate/migrate/v4"
	_ "github.com/golang-migrate/migrate/v4/database/postgres" // postgres driver
	"github.com/golang-migrate/migrate/v4/source/iofs"
)

//go:embed migrations/*.sql
var migrationsFS embed.FS

// RunMigrations executes database migrations using golang-migrate
// Returns an error if migrations fail
func RunMigrations(connectionString string) error {
	log.Println("Starting database migrations...")

	// Create source driver from embedded filesystem
	sourceDriver, err := iofs.New(migrationsFS, "migrations")
	if err != nil {
		return fmt.Errorf("failed to create migration source driver: %w", err)
	}

	// Create migrate instance
	m, err := migrate.NewWithSourceInstance("iofs", sourceDriver, connectionString)
	if err != nil {
		return fmt.Errorf("failed to create migrate instance: %w", err)
	}
	defer func() {
		if srcErr, dbErr := m.Close(); srcErr != nil || dbErr != nil {
			log.Printf("Warning: error closing migrate instance: source=%v, database=%v", srcErr, dbErr)
		}
	}()

	// Run migrations
	if migErr := m.Up(); migErr != nil {
		if migErr == migrate.ErrNoChange {
			log.Println("Database schema is up to date (no migrations to apply)")

			return nil
		}

		return fmt.Errorf("failed to run migrations: %w", migErr)
	}

	// Get current version
	version, dirty, err := m.Version()
	switch {
	case err != nil && err != migrate.ErrNilVersion:
		log.Printf("Warning: could not determine migration version: %v", err)
	case dirty:
		log.Printf("Warning: database is in dirty state at version %d", version)
	default:
		log.Printf("Successfully applied migrations. Current schema version: %d", version)
	}

	return nil
}
