//go:generate go tool oapi-codegen -config cfg.yaml ../../../docs/api/openapi.yaml
package api

import (
	"context"
	"errors"
	"net/http"

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

	return ListLamps200JSONResponse(lamps), nil
}

// Create a new lamp
// (POST /lamps)
func (l *LampAPI) CreateLamp(ctx context.Context, request CreateLampRequestObject) (CreateLampResponseObject, error) {
	if request.Body == nil {
		return nil, &APIError{Message: "Request body is required", StatusCode: http.StatusBadRequest}
	}

	// Generate a new UUID for the lamp
	lampID := uuid.New()

	lamp := Lamp{
		Id:     lampID,
		Status: request.Body.Status,
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

	// Get the existing lamp to ensure it exists and get its ID
	existingLamp, err := l.repository.GetByID(ctx, request.LampId)
	if err != nil {
		if errors.Is(err, ErrLampNotFound) {
			return UpdateLamp404Response{}, nil
		}

		return nil, &APIError{Message: "Failed to retrieve lamp", StatusCode: http.StatusInternalServerError}
	}

	// Update the lamp status
	updatedLamp := Lamp{
		Id:     existingLamp.Id,
		Status: request.Body.Status,
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
