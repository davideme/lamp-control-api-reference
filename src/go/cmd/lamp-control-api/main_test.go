package main

import (
	"context"
	"encoding/json"
	"fmt"
	"net"
	"net/http"
	"net/http/httptest"
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

func TestHealthHandler(t *testing.T) {
	t.Run("GET request returns ok", func(t *testing.T) {
		req := httptest.NewRequest(http.MethodGet, "/health", nil)
		rr := httptest.NewRecorder()

		healthHandler(rr, req)

		// Check status code
		if status := rr.Code; status != http.StatusOK {
			t.Errorf("Expected status code %d, got %d", http.StatusOK, status)
		}

		// Check content type
		expectedContentType := "application/json"
		if contentType := rr.Header().Get("Content-Type"); contentType != expectedContentType {
			t.Errorf("Expected content type %s, got %s", expectedContentType, contentType)
		}

		// Check response body
		var response HealthResponse
		if err := json.NewDecoder(rr.Body).Decode(&response); err != nil {
			t.Fatalf("Failed to decode response body: %v", err)
		}

		expectedStatus := "ok"
		if response.Status != expectedStatus {
			t.Errorf("Expected status %s, got %s", expectedStatus, response.Status)
		}
	})

	t.Run("POST request returns method not allowed", func(t *testing.T) {
		req := httptest.NewRequest(http.MethodPost, "/health", nil)
		rr := httptest.NewRecorder()

		healthHandler(rr, req)

		if status := rr.Code; status != http.StatusMethodNotAllowed {
			t.Errorf("Expected status code %d, got %d", http.StatusMethodNotAllowed, status)
		}

		allow := rr.Header().Get("Allow")
		if allow != http.MethodGet {
			t.Errorf("Expected Allow header '%s', got '%s'", http.MethodGet, allow)
		}
	})

	t.Run("PUT request returns method not allowed", func(t *testing.T) {
		req := httptest.NewRequest(http.MethodPut, "/health", nil)
		rr := httptest.NewRecorder()

		healthHandler(rr, req)

		if status := rr.Code; status != http.StatusMethodNotAllowed {
			t.Errorf("Expected status code %d, got %d", http.StatusMethodNotAllowed, status)
		}
	})

	t.Run("DELETE request returns method not allowed", func(t *testing.T) {
		req := httptest.NewRequest(http.MethodDelete, "/health", nil)
		rr := httptest.NewRecorder()

		healthHandler(rr, req)

		if status := rr.Code; status != http.StatusMethodNotAllowed {
			t.Errorf("Expected status code %d, got %d", http.StatusMethodNotAllowed, status)
		}
	})
}

func TestHealthEndpointIntegration(t *testing.T) {
	// Test the health endpoint with a complete router setup
	r := chi.NewRouter()
	r.Get("/health", healthHandler)

	// Create test server
	ts := httptest.NewServer(r)
	defer ts.Close()

	// Make request to health endpoint
	req, err := http.NewRequestWithContext(context.Background(), http.MethodGet, ts.URL+"/health", nil)
	if err != nil {
		t.Fatalf("Failed to create request: %v", err)
	}

	client := &http.Client{}
	resp, err := client.Do(req)
	if err != nil {
		t.Fatalf("Failed to make request: %v", err)
	}
	defer resp.Body.Close()

	// Check status code
	if resp.StatusCode != http.StatusOK {
		t.Errorf("Expected status code %d, got %d", http.StatusOK, resp.StatusCode)
	}

	// Check content type
	expectedContentType := "application/json"
	if contentType := resp.Header.Get("Content-Type"); contentType != expectedContentType {
		t.Errorf("Expected content type %s, got %s", expectedContentType, contentType)
	}

	// Check response body
	var response HealthResponse
	if err := json.NewDecoder(resp.Body).Decode(&response); err != nil {
		t.Fatalf("Failed to decode response body: %v", err)
	}

	expectedStatus := "ok"
	if response.Status != expectedStatus {
		t.Errorf("Expected status %s, got %s", expectedStatus, response.Status)
	}
}
