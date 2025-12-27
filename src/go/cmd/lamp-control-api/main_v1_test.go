package main

import (
"context"
"encoding/json"
"net/http"
"net/http/httptest"
"strings"
"testing"

"github.com/davideme/lamp-control-api-reference/api"
"github.com/go-chi/chi/v5"
middleware "github.com/oapi-codegen/nethttp-middleware"
)

func TestV1PrefixRouting(t *testing.T) {
// Set up the router exactly as main does
swagger, err := api.GetSwagger()
if err != nil {
t.Fatalf("Failed to get swagger spec: %v", err)
}
// Keep servers array for proper validation

lamp := api.NewStrictHandler(api.NewLampAPI(), nil)
r := chi.NewRouter()

r.Get("/health", healthHandler)

r.Route("/v1", func(apiRouter chi.Router) {
validatorOptions := &middleware.Options{
SilenceServersWarning: true,
}
apiRouter.Use(middleware.OapiRequestValidatorWithOptions(swagger, validatorOptions))
api.HandlerFromMux(lamp, apiRouter)
})

// Create test server
ts := httptest.NewServer(r)
defer ts.Close()

tests := []struct {
name           string
method         string
path           string
body           string
expectedStatus int
}{
{
name:           "health check without v1 prefix",
method:         "GET",
path:           "/health",
expectedStatus: http.StatusOK,
},
{
name:           "list lamps with v1 prefix",
method:         "GET",
path:           "/v1/lamps",
expectedStatus: http.StatusOK,
},
{
name:           "create lamp with v1 prefix",
method:         "POST",
path:           "/v1/lamps",
body:           `{"status": true}`,
expectedStatus: http.StatusCreated,
},
{
name:           "list lamps without v1 prefix should fail",
method:         "GET",
path:           "/lamps",
expectedStatus: http.StatusNotFound,
},
}

for _, tt := range tests {
t.Run(tt.name, func(t *testing.T) {
var req *http.Request
var err error

if tt.body != "" {
req, err = http.NewRequestWithContext(
context.Background(),
tt.method,
ts.URL+tt.path,
strings.NewReader(tt.body),
)
req.Header.Set("Content-Type", "application/json")
} else {
req, err = http.NewRequestWithContext(
context.Background(),
tt.method,
ts.URL+tt.path,
nil,
)
}

if err != nil {
t.Fatalf("Failed to create request: %v", err)
}

client := &http.Client{}
resp, err := client.Do(req)
if err != nil {
t.Fatalf("Failed to make request: %v", err)
}
defer resp.Body.Close()

if resp.StatusCode != tt.expectedStatus {
t.Errorf("Expected status %d, got %d", tt.expectedStatus, resp.StatusCode)
}

// For successful responses, verify JSON structure
if tt.expectedStatus == http.StatusOK || tt.expectedStatus == http.StatusCreated {
var result map[string]interface{}
if err := json.NewDecoder(resp.Body).Decode(&result); err != nil {
t.Errorf("Failed to decode response: %v", err)
}
}
})
}
}
