//go:generate go tool oapi-codegen -config cfg.yaml ../../../docs/api/openapi.yaml
package api

import (
	"context"
	"errors"
	"net/http"
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
	lampEntities, err := l.repository.List(ctx)
	if err != nil {
		return nil, &APIError{Message: "Failed to retrieve lamps", StatusCode: http.StatusInternalServerError, Err: err}
	}

	// Convert domain entities to API models
	lamps := make([]Lamp, len(lampEntities))
	for i, entity := range lampEntities {
		lamps[i] = ToAPIModel(entity)
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

	// Create domain entity from API model
	lampEntity := CreateEntityFromAPICreate(*request.Body)

	err := l.repository.Create(ctx, lampEntity)
	if err != nil {
		return nil, &APIError{Message: "Failed to create lamp", StatusCode: http.StatusInternalServerError, Err: err}
	}

	// Convert domain entity back to API model
	lamp := ToAPIModel(lampEntity)

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

		return nil, &APIError{Message: "Failed to delete lamp", StatusCode: http.StatusInternalServerError, Err: err}
	}

	return DeleteLamp204Response{}, nil
}

// Get a specific lamp
// (GET /lamps/{lampId})
func (l *LampAPI) GetLamp(ctx context.Context, request GetLampRequestObject) (GetLampResponseObject, error) {
	lampEntity, err := l.repository.GetByID(ctx, request.LampId)
	if err != nil {
		if errors.Is(err, ErrLampNotFound) {
			return GetLamp404Response{}, nil
		}

		return nil, &APIError{Message: "Failed to retrieve lamp", StatusCode: http.StatusInternalServerError, Err: err}
	}

	// Convert domain entity to API model
	lamp := ToAPIModel(lampEntity)

	return GetLamp200JSONResponse(lamp), nil
}

// Update a lamp's status
// (PUT /lamps/{lampId})
func (l *LampAPI) UpdateLamp(ctx context.Context, request UpdateLampRequestObject) (UpdateLampResponseObject, error) {
	if request.Body == nil {
		return nil, &APIError{Message: "Request body is required", StatusCode: http.StatusBadRequest}
	}

	// Get the existing lamp entity to ensure it exists
	existingEntity, err := l.repository.GetByID(ctx, request.LampId)
	if err != nil {
		if errors.Is(err, ErrLampNotFound) {
			return UpdateLamp404Response{}, nil
		}

		return nil, &APIError{Message: "Failed to retrieve lamp", StatusCode: http.StatusInternalServerError, Err: err}
	}

	// Update the domain entity using the mapper
	updatedEntity := UpdateEntityFromAPIUpdate(existingEntity, *request.Body)

	err = l.repository.Update(ctx, updatedEntity)
	if err != nil {
		return nil, &APIError{Message: "Failed to update lamp", StatusCode: http.StatusInternalServerError, Err: err}
	}

	// Convert domain entity back to API model
	updatedLamp := ToAPIModel(updatedEntity)

	return UpdateLamp200JSONResponse(updatedLamp), nil
}

// APIError represents an API error with status code
type APIError struct {
	Message    string
	StatusCode int
	Err        error
}

func (e *APIError) Error() string {
	return e.Message
}

// Unwrap returns the underlying error for use with errors.Is and errors.As.
func (e *APIError) Unwrap() error {
	return e.Err
}
