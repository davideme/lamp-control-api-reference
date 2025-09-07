package main

import (
	"encoding/json"
	"flag"
	"fmt"
	"log"
	"net"
	"net/http"
	"os"

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

func main() {
	port := flag.String("port", "8080", "Port for test HTTP server")
	flag.Parse()

	swagger, err := api.GetSwagger()
	if err != nil {
		fmt.Fprintf(os.Stderr, "Error loading swagger spec\n: %s", err)
		os.Exit(1)
	}

	// Clear out the servers array in the swagger spec, that skips validating
	// that server names match. We don't know how this thing will be run.
	swagger.Servers = nil

	// Create an instance of our handler which satisfies the generated interface
	lamp := api.NewStrictHandler(api.NewLampAPI(), nil)

	// This is how you set up a basic chi router
	r := chi.NewRouter()

	// Add the health endpoint (not under /v1 prefix and not validated by OpenAPI)
	r.Get("/health", healthHandler)

	// Create a subrouter for API routes that need OpenAPI validation
	r.Route("/", func(r chi.Router) {
		// Use our validation middleware to check all requests against the
		// OpenAPI schema.
		r.Use(middleware.OapiRequestValidator(swagger))

		// We now register our lamp above as the handler for the interface
		api.HandlerFromMux(lamp, r)
	})

	s := &http.Server{
		Handler:           r,
		Addr:              net.JoinHostPort("0.0.0.0", *port),
		ReadHeaderTimeout: 10 * 1e9, // 10 seconds
	}

	// And we serve HTTP until the world ends.
	log.Fatal(s.ListenAndServe())
}
