package main

import (
	"context"
	"encoding/json"
	"flag"
	"fmt"
	"log"
	"net"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/davideme/lamp-control-api-reference/api"
	"github.com/go-chi/chi/v5"
	middleware "github.com/oapi-codegen/nethttp-middleware"
)

// HealthResponse represents the health check response
type HealthResponse struct {
	Status string `json:"status"`
}

func healthHandler(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		w.Header().Set("Allow", http.MethodGet)
		http.Error(w, "Method Not Allowed", http.StatusMethodNotAllowed)

		return
	}
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)

	response := HealthResponse{Status: "ok"}
	if err := json.NewEncoder(w).Encode(response); err != nil {
		// If we can't encode the response, log it but don't change the status
		log.Printf("Error encoding health response: %v", err)
	}
}

// runMigrationsOnly runs database migrations and exits
func runMigrationsOnly(requireDB bool) {
	dbConfig := api.NewDatabaseConfigFromEnv()
	if dbConfig == nil {
		log.Println("No PostgreSQL configuration found, nothing to migrate")
		if requireDB {
			log.Fatal("PostgreSQL configuration required but not found (--require-db flag set)")
		}
		return
	}

	log.Printf("Running migrations for database: host=%s port=%d database=%s user=%s",
		dbConfig.Host, dbConfig.Port, dbConfig.Database, dbConfig.User)

	connectionString := dbConfig.ConnectionString()
	if len(connectionString) >= 10 && connectionString[:10] == "host=" {
		connectionString = fmt.Sprintf("postgres://%s:%s@%s:%d/%s?sslmode=disable",
			dbConfig.User, dbConfig.Password, dbConfig.Host, dbConfig.Port, dbConfig.Database)
	}

	if err := api.RunMigrations(connectionString); err != nil {
		log.Printf("Migration failed: %v", err)
		os.Exit(1)
	}

	log.Println("Migrations completed successfully")
}

// initializeRepository creates and initializes the lamp repository
func initializeRepository(ctx context.Context, runMigrations bool, requireDB bool) (*api.LampAPI, interface{ Close() }) {
	var lampAPI *api.LampAPI
	var pool interface{ Close() }
	dbConfig := api.NewDatabaseConfigFromEnv()

	if dbConfig != nil {
		log.Printf("Initializing PostgreSQL repository with config: host=%s port=%d database=%s user=%s",
			dbConfig.Host, dbConfig.Port, dbConfig.Database, dbConfig.User)

		// Run database migrations if requested
		if runMigrations {
			connectionString := dbConfig.ConnectionString()
			if len(connectionString) >= 10 && connectionString[:10] == "host=" {
				connectionString = fmt.Sprintf("postgres://%s:%s@%s:%d/%s?sslmode=disable",
					dbConfig.User, dbConfig.Password, dbConfig.Host, dbConfig.Port, dbConfig.Database)
			}

			if err := api.RunMigrations(connectionString); err != nil {
				log.Printf("Failed to run database migrations: %v", err)
				if requireDB {
					log.Fatal("Database migrations required but failed (--require-db flag set)")
				}
				log.Printf("Falling back to in-memory repository")
				lampAPI = api.NewLampAPI()
				return lampAPI, nil
			}
		}

		pgPool, err := api.CreateConnectionPool(ctx, dbConfig)
		if err != nil {
			log.Printf("Failed to connect to PostgreSQL: %v", err)
			if requireDB {
				log.Fatal("PostgreSQL connection required but failed (--require-db flag set)")
			}
			log.Printf("Falling back to in-memory repository")
			lampAPI = api.NewLampAPI()
		} else {
			log.Printf("Successfully connected to PostgreSQL")
			pool = pgPool
			postgresRepo := api.NewPostgresLampRepository(pgPool)
			lampAPI = api.NewLampAPIWithRepository(postgresRepo)
		}
	} else {
		log.Printf("No PostgreSQL configuration found, using in-memory repository")
		lampAPI = api.NewLampAPI()
	}

	return lampAPI, pool
}

func main() {
	port := flag.String("port", "8080", "Port for test HTTP server")
	requireDB := flag.Bool("require-db", false, "Fail if PostgreSQL connection is configured but fails")
	mode := flag.String("mode", "serve", "Operation mode: 'serve' (migrate and start server), 'migrate' (run migrations only), 'serve-only' (start server without migrations)")
	flag.Parse()

	// Handle migrate-only mode
	if *mode == "migrate" {
		runMigrationsOnly(*requireDB)
		return
	}

	ctx := context.Background()

	swagger, err := api.GetSwagger()
	if err != nil {
		fmt.Fprintf(os.Stderr, "Error loading swagger spec\n: %s", err)
		os.Exit(1)
	}

	// Initialize repository based on mode
	runMigrations := *mode == "serve" // Only run migrations in default 'serve' mode
	lampAPI, pool := initializeRepository(ctx, runMigrations, *requireDB)

	// Create an instance of our handler which satisfies the generated interface
	lamp := api.NewStrictHandler(lampAPI, nil)

	// This is how you set up a basic chi router
	r := chi.NewRouter()

	// Add the health endpoint (not under /v1 prefix and not validated by OpenAPI)
	r.Get("/health", healthHandler)

	// Create a subrouter for API routes that need OpenAPI validation
	r.Route("/v1", func(apiRouter chi.Router) {
		// Use our validation middleware to check all requests against the
		// OpenAPI schema. Silence the servers warning as we're handling the /v1 prefix in routing.
		validatorOptions := &middleware.Options{
			SilenceServersWarning: true,
		}
		apiRouter.Use(middleware.OapiRequestValidatorWithOptions(swagger, validatorOptions))

		// We now register our lamp above as the handler for the interface
		api.HandlerFromMux(lamp, apiRouter)
	})

	s := &http.Server{
		Handler:           r,
		Addr:              net.JoinHostPort("0.0.0.0", *port),
		ReadHeaderTimeout: 10 * time.Second,
	}

	// Set up graceful shutdown
	shutdownChan := make(chan os.Signal, 1)
	signal.Notify(shutdownChan, os.Interrupt, syscall.SIGTERM)

	go func() {
		log.Printf("Starting server on %s", s.Addr)
		if err := s.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			log.Fatalf("Server failed: %v", err)
		}
	}()

	// Wait for shutdown signal
	<-shutdownChan
	log.Println("Shutting down gracefully...")

	// Create shutdown context with timeout
	shutdownCtx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	// Shutdown the server
	if err := s.Shutdown(shutdownCtx); err != nil {
		log.Printf("Server shutdown error: %v", err)
	}

	// Close database pool if it exists
	if pool != nil {
		pool.Close()
		log.Println("Database connection closed")
	}

	log.Println("Server stopped")
}
