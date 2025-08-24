//go:generate go tool oapi-codegen -config cfg.yaml ../../../docs/api/openapi.yaml
package api

import (
	"context"
	"errors"
	"net/http"
	"time"

	"github.com/google/uuid"
)

type LampAPI struct {
	repository LampRepository
}

var _ StrictServerInterface = (*LampAPI)(nil)

func NewLampAPI() *LampAPI {
	return &LampAPI{
		repository: NewInMemoryLampRepository(),
	}
}

// NewLampAPIWithRepository creates a new LampAPI with a custom repository
func NewLampAPIWithRepository(repo LampRepository) *LampAPI {
	return &LampAPI{
		repository: repo,
	}
}

// List all lamps
// (GET /lamps)
func (l *LampAPI) ListLamps(ctx context.Context, request ListLampsRequestObject) (ListLampsResponseObject, error) {
	lamps, err := l.repository.List(ctx)
	if err != nil {
		return nil, &APIError{Message: "Failed to retrieve lamps", StatusCode: http.StatusInternalServerError}
	}

	// For simplicity, we're returning all lamps without actual pagination
	// In a real implementation, you would implement cursor-based pagination
	return ListLamps200JSONResponse{
		Data:       lamps,
		HasMore:    false, // No more pages for now
		NextCursor: nil,   // No next cursor since we're showing all
	}, nil
}

// Create a new lamp
// (POST /lamps)
func (l *LampAPI) CreateLamp(ctx context.Context, request CreateLampRequestObject) (CreateLampResponseObject, error) {
	if request.Body == nil {
		return nil, &APIError{Message: "Request body is required", StatusCode: http.StatusBadRequest}
	}

	// Generate a new UUID for the lamp
	lampID := uuid.New()
	now := time.Now()

	lamp := Lamp{
		Id:        lampID,
		Status:    request.Body.Status,
		CreatedAt: now,
		UpdatedAt: now,
	}

	err := l.repository.Create(ctx, lamp)
	if err != nil {
		return nil, &APIError{Message: "Failed to create lamp", StatusCode: http.StatusInternalServerError}
	}

	return CreateLamp201JSONResponse(lamp), nil
}

// Delete a lamp
// (DELETE /lamps/{lampId})
func (l *LampAPI) DeleteLamp(ctx context.Context, request DeleteLampRequestObject) (DeleteLampResponseObject, error) {
	err := l.repository.Delete(ctx, request.LampId)
	if err != nil {
		if errors.Is(err, ErrLampNotFound) {
			return DeleteLamp404Response{}, nil
		}

		return nil, &APIError{Message: "Failed to delete lamp", StatusCode: http.StatusInternalServerError}
	}

	return DeleteLamp204Response{}, nil
}

// Get a specific lamp
// (GET /lamps/{lampId})
func (l *LampAPI) GetLamp(ctx context.Context, request GetLampRequestObject) (GetLampResponseObject, error) {
	lamp, err := l.repository.GetByID(ctx, request.LampId)
	if err != nil {
		if errors.Is(err, ErrLampNotFound) {
			return GetLamp404Response{}, nil
		}

		return nil, &APIError{Message: "Failed to retrieve lamp", StatusCode: http.StatusInternalServerError}
	}

	return GetLamp200JSONResponse(lamp), nil
}

// Update a lamp's status
// (PUT /lamps/{lampId})
func (l *LampAPI) UpdateLamp(ctx context.Context, request UpdateLampRequestObject) (UpdateLampResponseObject, error) {
	if request.Body == nil {
		return nil, &APIError{Message: "Request body is required", StatusCode: http.StatusBadRequest}
	}

	// Get the existing lamp to ensure it exists and get its ID and CreatedAt
	existingLamp, err := l.repository.GetByID(ctx, request.LampId)
	if err != nil {
		if errors.Is(err, ErrLampNotFound) {
			return UpdateLamp404Response{}, nil
		}

		return nil, &APIError{Message: "Failed to retrieve lamp", StatusCode: http.StatusInternalServerError}
	}

	// Update the lamp status while preserving CreatedAt and updating UpdatedAt
	updatedLamp := Lamp{
		Id:        existingLamp.Id,
		Status:    request.Body.Status,
		CreatedAt: existingLamp.CreatedAt, // Preserve original creation time
		UpdatedAt: time.Now(),             // Update modification time
	}

	err = l.repository.Update(ctx, updatedLamp)
	if err != nil {
		return nil, &APIError{Message: "Failed to update lamp", StatusCode: http.StatusInternalServerError}
	}

	return UpdateLamp200JSONResponse(updatedLamp), nil
}

// APIError represents an API error with status code
type APIError struct {
	Message    string
	StatusCode int
}

func (e *APIError) Error() string {
	return e.Message
}
