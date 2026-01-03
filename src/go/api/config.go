package api

import (
	"context"
	"fmt"
	"log"
	"os"
	"strconv"
	"time"

	"github.com/jackc/pgx/v5/pgxpool"
)

// DatabaseConfig holds PostgreSQL connection configuration
type DatabaseConfig struct {
	Host     string
	Port     int
	Database string
	User     string
	Password string
	PoolMin  int
	PoolMax  int
}

// NewDatabaseConfigFromEnv creates a DatabaseConfig from environment variables
// Returns nil if no PostgreSQL connection parameters are set
func NewDatabaseConfigFromEnv() *DatabaseConfig {
	// Check if PostgreSQL is configured
	// Require DATABASE_URL OR explicit database name OR both host and user to be set
	databaseURL := os.Getenv("DATABASE_URL")
	host := os.Getenv("DB_HOST")
	portStr := os.Getenv("DB_PORT")
	database := os.Getenv("DB_NAME")
	user := os.Getenv("DB_USER")
	password := os.Getenv("DB_PASSWORD")

	// Determine if PostgreSQL is actually configured
	// Consider it configured if:
	//   - DATABASE_URL is set, or
	//   - database name is explicitly provided, or
	//   - both host and user are explicitly provided
	postgresConfigured := databaseURL != "" || database != "" || (host != "" && user != "")
	if !postgresConfigured {
		return nil
	}

	// Use defaults from pgx library
	config := &DatabaseConfig{
		Host:     "localhost",       // pgx default
		Port:     5432,              // pgx default
		Database: "postgres",        // pgx default
		User:     os.Getenv("USER"), // pgx uses current user by default
		Password: "",
		PoolMin:  0, // pgxpool default
		PoolMax:  4, // pgxpool default
	}

	// Override with environment variables if set
	if host != "" {
		config.Host = host
	}
	if portStr != "" {
		port, err := strconv.Atoi(portStr)
		if err != nil {
			log.Printf("Warning: Invalid DB_PORT value '%s', using default %d: %v", portStr, config.Port, err)
		} else {
			config.Port = port
		}
	}
	if database != "" {
		config.Database = database
	}
	if user != "" {
		config.User = user
	}
	if password != "" {
		config.Password = password
	}

	// Pool configuration
	if poolMinStr := os.Getenv("DB_POOL_MIN_SIZE"); poolMinStr != "" {
		poolMin, err := strconv.Atoi(poolMinStr)
		if err != nil {
			log.Printf("Warning: Invalid DB_POOL_MIN_SIZE value '%s', using default %d: %v", poolMinStr, config.PoolMin, err)
		} else {
			config.PoolMin = poolMin
		}
	}
	if poolMaxStr := os.Getenv("DB_POOL_MAX_SIZE"); poolMaxStr != "" {
		poolMax, err := strconv.Atoi(poolMaxStr)
		if err != nil {
			log.Printf("Warning: Invalid DB_POOL_MAX_SIZE value '%s', using default %d: %v", poolMaxStr, config.PoolMax, err)
		} else {
			config.PoolMax = poolMax
		}
	}

	return config
}

// ConnectionString returns a PostgreSQL connection string
func (c *DatabaseConfig) ConnectionString() string {
	// If DATABASE_URL is set, use it directly
	if databaseURL := os.Getenv("DATABASE_URL"); databaseURL != "" {
		return databaseURL
	}

	// Otherwise, build from components
	return fmt.Sprintf(
		"host=%s port=%d dbname=%s user=%s password=%s",
		c.Host, c.Port, c.Database, c.User, c.Password,
	)
}

// CreateConnectionPool creates a pgxpool connection pool
func CreateConnectionPool(ctx context.Context, config *DatabaseConfig) (*pgxpool.Pool, error) {
	// Parse the connection string using pgxpool
	poolConfig, err := pgxpool.ParseConfig(config.ConnectionString())
	if err != nil {
		return nil, fmt.Errorf("failed to parse connection string: %w", err)
	}

	// Configure pool settings
	if config.PoolMin > 0 && config.PoolMin <= 2147483647 {
		poolConfig.MinConns = int32(config.PoolMin) // #nosec G115
	}

	if config.PoolMax > 0 && config.PoolMax <= 2147483647 {
		poolConfig.MaxConns = int32(config.PoolMax) // #nosec G115
	}

	poolConfig.MaxConnLifetime = time.Hour
	poolConfig.MaxConnIdleTime = 30 * time.Minute
	poolConfig.HealthCheckPeriod = time.Minute

	// Create the connection pool
	pool, err := pgxpool.NewWithConfig(ctx, poolConfig)
	if err != nil {
		return nil, fmt.Errorf("failed to create connection pool: %w", err)
	}

	// Verify the connection works
	if err := pool.Ping(ctx); err != nil {
		pool.Close()

		return nil, fmt.Errorf("failed to ping database: %w", err)
	}

	return pool, nil
}
