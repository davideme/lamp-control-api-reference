//go:generate go tool oapi-codegen -config cfg.yaml ../../../docs/api/openapi.yaml
package api

import (
	"context"
	"sync"

	"github.com/google/uuid"
	openapi_types "github.com/oapi-codegen/runtime/types"
)

type LampAPI struct {
	// In-memory storage using a map with RWMutex for thread safety
	lamps map[string]Lamp
	mutex sync.RWMutex
}

var _ StrictServerInterface = (*LampAPI)(nil)

func NewLampAPI() *LampAPI {
	return &LampAPI{
		lamps: make(map[string]Lamp),
		mutex: sync.RWMutex{},
	}
}

// List all lamps
// (GET /lamps)
func (l *LampAPI) ListLamps(ctx context.Context, request ListLampsRequestObject) (ListLampsResponseObject, error) {
	l.mutex.RLock()
	defer l.mutex.RUnlock()

	// Convert map values to slice
	lamps := make([]Lamp, 0, len(l.lamps))
	for _, lamp := range l.lamps {
		lamps = append(lamps, lamp)
	}

	return ListLamps200JSONResponse(lamps), nil
}

// Create a new lamp
// (POST /lamps)
func (l *LampAPI) CreateLamp(ctx context.Context, request CreateLampRequestObject) (CreateLampResponseObject, error) {
	if request.Body == nil {
		return nil, &APIError{Message: "Request body is required", StatusCode: 400}
	}

	// Generate a new UUID for the lamp
	lampID := uuid.New()

	lamp := Lamp{
		Id:     openapi_types.UUID(lampID),
		Status: request.Body.Status,
	}

	l.mutex.Lock()
	l.lamps[lampID.String()] = lamp
	l.mutex.Unlock()

	return CreateLamp201JSONResponse(lamp), nil
}

// Delete a lamp
// (DELETE /lamps/{lampId})
func (l *LampAPI) DeleteLamp(ctx context.Context, request DeleteLampRequestObject) (DeleteLampResponseObject, error) {
	l.mutex.Lock()
	defer l.mutex.Unlock()

	// Check if lamp exists
	if _, exists := l.lamps[request.LampId]; !exists {
		return DeleteLamp404Response{}, nil
	}

	// Delete the lamp
	delete(l.lamps, request.LampId)

	return DeleteLamp204Response{}, nil
}

// Get a specific lamp
// (GET /lamps/{lampId})
func (l *LampAPI) GetLamp(ctx context.Context, request GetLampRequestObject) (GetLampResponseObject, error) {
	l.mutex.RLock()
	defer l.mutex.RUnlock()

	// Check if lamp exists
	lamp, exists := l.lamps[request.LampId]
	if !exists {
		return GetLamp404Response{}, nil
	}

	return GetLamp200JSONResponse(lamp), nil
}

// Update a lamp's status
// (PUT /lamps/{lampId})
func (l *LampAPI) UpdateLamp(ctx context.Context, request UpdateLampRequestObject) (UpdateLampResponseObject, error) {
	if request.Body == nil {
		return nil, &APIError{Message: "Request body is required", StatusCode: 400}
	}

	l.mutex.Lock()
	defer l.mutex.Unlock()

	// Check if lamp exists
	lamp, exists := l.lamps[request.LampId]
	if !exists {
		return UpdateLamp404Response{}, nil
	}

	// Update the lamp status
	lamp.Status = request.Body.Status
	l.lamps[request.LampId] = lamp

	return UpdateLamp200JSONResponse(lamp), nil
}

// APIError represents an API error with status code
type APIError struct {
	Message    string
	StatusCode int
}

func (e *APIError) Error() string {
	return e.Message
}
