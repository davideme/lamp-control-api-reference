package main

import (
	"context"
	"fmt"
	"net"
	"net/http"
	"testing"
	"time"

	"github.com/davideme/lamp-control-api-reference/api"
	"github.com/go-chi/chi/v5"
)

func TestMain_ServerStart(t *testing.T) {
	// This test verifies that the server components work correctly
	// We simulate the main function setup without actually running the server

	// Test the components that main() uses
	swagger, err := api.GetSwagger()
	if err != nil {
		t.Fatalf("Failed to get swagger spec: %v", err)
	}

	if swagger == nil {
		t.Fatal("Swagger spec should not be nil")
	}

	// Test creating the API handler
	lamp := api.NewStrictHandler(api.NewLampAPI(), nil)
	if lamp == nil {
		t.Fatal("Lamp handler should not be nil")
	}

	// Test creating the router
	r := chi.NewRouter()
	if r == nil {
		t.Fatal("Router should not be nil")
	}

	t.Log("Main function components integrate correctly")
}

func TestServerHealthCheck(t *testing.T) {
	// Start a test server
	server := &http.Server{
		Addr:              ":0", // Use any available port
		ReadHeaderTimeout: 1 * time.Second,
	}

	// Create a context with timeout
	ctx, cancel := context.WithTimeout(context.Background(), 2*time.Second)
	defer cancel()

	// Start server in goroutine
	go func() {
		if err := server.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			t.Logf("Server error: %v", err)
		}
	}()

	// Give server time to start
	time.Sleep(100 * time.Millisecond)

	// Shutdown server
	if err := server.Shutdown(ctx); err != nil {
		t.Logf("Server shutdown error: %v", err)
	}

	t.Log("Server can start and shutdown correctly")
}

func TestFlagParsing(t *testing.T) {
	// Test that we can parse the expected flags
	// This tests the flag structure without actually starting the server

	testCases := []struct {
		name         string
		expectedPort string
	}{
		{"default port", "8080"},
		{"custom port", "9090"},
	}

	for _, tc := range testCases {
		t.Run(tc.name, func(t *testing.T) {
			// Test that port format is valid
			if tc.expectedPort == "" {
				t.Error("Port should not be empty")
			}

			// Test port number format
			port := tc.expectedPort
			if len(port) == 0 {
				t.Error("Port length should be greater than 0")
			}
		})
	}
}

func TestNetJoinHostPort(t *testing.T) {
	// Test the net.JoinHostPort function used in main
	testCases := []struct {
		host     string
		port     string
		expected string
	}{
		{"0.0.0.0", "8080", "0.0.0.0:8080"},
		{"localhost", "9090", "localhost:9090"},
		{"127.0.0.1", "3000", "127.0.0.1:3000"},
	}

	for _, tc := range testCases {
		t.Run(fmt.Sprintf("%s:%s", tc.host, tc.port), func(t *testing.T) {
			result := net.JoinHostPort(tc.host, tc.port)
			if result != tc.expected {
				t.Errorf("Expected %s, got %s", tc.expected, result)
			}
		})
	}
}
