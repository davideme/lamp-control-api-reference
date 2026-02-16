//go:generate go tool oapi-codegen -config cfg.yaml ../../../docs/api/openapi.yaml
package api

import (
	"context"
	"errors"
	"net/http"
	"strconv"
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
	const (
		defaultPageSize = 25
		minPageSize     = 1
		maxPageSize     = 100
	)

	pageSize := defaultPageSize
	if request.Params.PageSize != nil {
		pageSize = *request.Params.PageSize
	}

	if pageSize < minPageSize || pageSize > maxPageSize {
		return ListLamps400JSONResponse{Error: "INVALID_ARGUMENT"}, nil
	}

	offset := 0
	if request.Params.Cursor != nil && *request.Params.Cursor != "" {
		parsedOffset, err := strconv.Atoi(*request.Params.Cursor)
		if err != nil || parsedOffset < 0 {
			return ListLamps400JSONResponse{Error: "INVALID_ARGUMENT"}, nil
		}
		offset = parsedOffset
	}

	lampEntities, err := l.repository.List(ctx, offset, pageSize+1)
	if err != nil {
		return nil, &APIError{Message: "Failed to retrieve lamps", StatusCode: http.StatusInternalServerError, Err: err}
	}

	hasMore := len(lampEntities) > pageSize
	if hasMore {
		lampEntities = lampEntities[:pageSize]
	}

	// Convert domain entities to API models
	lamps := make([]Lamp, len(lampEntities))
	for i, entity := range lampEntities {
		lamps[i] = ToAPIModel(entity)
	}

	var nextCursor *string
	if hasMore {
		cursor := strconv.Itoa(offset + pageSize)
		nextCursor = &cursor
	}

	return ListLamps200JSONResponse{
		Data:       lamps,
		HasMore:    hasMore,
		NextCursor: nextCursor,
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
