//go:generate go tool oapi-codegen -config cfg.yaml ../../../docs/api/openapi.yaml
package api

import "context"

type LampAPI struct {
}

var _ StrictServerInterface = (*LampAPI)(nil)

func NewLampAPI() *LampAPI {
	return &LampAPI{}
}

// List all lamps
// (GET /lamps)
func (l LampAPI) ListLamps(ctx context.Context, request ListLampsRequestObject) (_ ListLampsResponseObject, _ error) {
	panic("not implemented") // TODO: Implement
}

// Create a new lamp
// (POST /lamps)
func (l LampAPI) CreateLamp(ctx context.Context, request CreateLampRequestObject) (_ CreateLampResponseObject, _ error) {
	panic("not implemented") // TODO: Implement
}

// Delete a lamp
// (DELETE /lamps/{lampId})
func (l LampAPI) DeleteLamp(ctx context.Context, request DeleteLampRequestObject) (_ DeleteLampResponseObject, _ error) {
	panic("not implemented") // TODO: Implement
}

// Get a specific lamp
// (GET /lamps/{lampId})
func (l LampAPI) GetLamp(ctx context.Context, request GetLampRequestObject) (_ GetLampResponseObject, _ error) {
	panic("not implemented") // TODO: Implement
}

// Update a lamp's status
// (PUT /lamps/{lampId})
func (l LampAPI) UpdateLamp(ctx context.Context, request UpdateLampRequestObject) (_ UpdateLampResponseObject, _ error) {
	panic("not implemented") // TODO: Implement
}
