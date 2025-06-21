package api

import (
	"context"
	"testing"

	"github.com/google/uuid"
	openapi_types "github.com/oapi-codegen/runtime/types"
)

func TestLampAPI_CreateLamp(t *testing.T) {
	api := NewLampAPI()

	// Test creating a lamp
	req := CreateLampRequestObject{
		Body: &LampCreate{Status: true},
	}

	resp, err := api.CreateLamp(context.Background(), req)
	if err != nil {
		t.Fatalf("CreateLamp failed: %v", err)
	}

	createResp, ok := resp.(CreateLamp201JSONResponse)
	if !ok {
		t.Fatalf("Expected CreateLamp201JSONResponse, got %T", resp)
	}

	lamp := Lamp(createResp)
	if lamp.Status != true {
		t.Errorf("Expected status true, got %v", lamp.Status)
	}

	// Verify the lamp was stored
	if len(api.lamps) != 1 {
		t.Errorf("Expected 1 lamp in storage, got %d", len(api.lamps))
	}
}

func TestLampAPI_ListLamps(t *testing.T) {
	api := NewLampAPI()

	// Initially should be empty
	resp, err := api.ListLamps(context.Background(), ListLampsRequestObject{})
	if err != nil {
		t.Fatalf("ListLamps failed: %v", err)
	}

	listResp, ok := resp.(ListLamps200JSONResponse)
	if !ok {
		t.Fatalf("Expected ListLamps200JSONResponse, got %T", resp)
	}

	if len(listResp) != 0 {
		t.Errorf("Expected empty list, got %d lamps", len(listResp))
	}

	// Add a lamp and verify it appears in the list
	lampID := uuid.New()
	api.lamps[lampID.String()] = Lamp{
		Id:     openapi_types.UUID(lampID),
		Status: true,
	}

	resp, err = api.ListLamps(context.Background(), ListLampsRequestObject{})
	if err != nil {
		t.Fatalf("ListLamps failed: %v", err)
	}

	listResp, ok = resp.(ListLamps200JSONResponse)
	if !ok {
		t.Fatalf("Expected ListLamps200JSONResponse, got %T", resp)
	}

	if len(listResp) != 1 {
		t.Errorf("Expected 1 lamp, got %d lamps", len(listResp))
	}
}

func TestLampAPI_GetLamp(t *testing.T) {
	api := NewLampAPI()
	lampID := uuid.New()
	lamp := Lamp{
		Id:     openapi_types.UUID(lampID),
		Status: true,
	}
	api.lamps[lampID.String()] = lamp

	// Test getting existing lamp
	req := GetLampRequestObject{LampId: lampID.String()}
	resp, err := api.GetLamp(context.Background(), req)
	if err != nil {
		t.Fatalf("GetLamp failed: %v", err)
	}

	getResp, ok := resp.(GetLamp200JSONResponse)
	if !ok {
		t.Fatalf("Expected GetLamp200JSONResponse, got %T", resp)
	}

	retrievedLamp := Lamp(getResp)
	if retrievedLamp.Id != lamp.Id || retrievedLamp.Status != lamp.Status {
		t.Errorf("Retrieved lamp doesn't match: expected %+v, got %+v", lamp, retrievedLamp)
	}

	// Test getting non-existent lamp
	req = GetLampRequestObject{LampId: "nonexistent"}
	resp, err = api.GetLamp(context.Background(), req)
	if err != nil {
		t.Fatalf("GetLamp failed: %v", err)
	}

	_, ok = resp.(GetLamp404Response)
	if !ok {
		t.Fatalf("Expected GetLamp404Response for non-existent lamp, got %T", resp)
	}
}

func TestLampAPI_UpdateLamp(t *testing.T) {
	api := NewLampAPI()
	lampID := uuid.New()
	lamp := Lamp{
		Id:     openapi_types.UUID(lampID),
		Status: true,
	}
	api.lamps[lampID.String()] = lamp

	// Test updating existing lamp
	req := UpdateLampRequestObject{
		LampId: lampID.String(),
		Body:   &LampUpdate{Status: false},
	}

	resp, err := api.UpdateLamp(context.Background(), req)
	if err != nil {
		t.Fatalf("UpdateLamp failed: %v", err)
	}

	updateResp, ok := resp.(UpdateLamp200JSONResponse)
	if !ok {
		t.Fatalf("Expected UpdateLamp200JSONResponse, got %T", resp)
	}

	updatedLamp := Lamp(updateResp)
	if updatedLamp.Status != false {
		t.Errorf("Expected status false, got %v", updatedLamp.Status)
	}

	// Verify the lamp was updated in storage
	storedLamp := api.lamps[lampID.String()]
	if storedLamp.Status != false {
		t.Errorf("Lamp not updated in storage: expected status false, got %v", storedLamp.Status)
	}

	// Test updating non-existent lamp
	req = UpdateLampRequestObject{
		LampId: "nonexistent",
		Body:   &LampUpdate{Status: true},
	}

	resp, err = api.UpdateLamp(context.Background(), req)
	if err != nil {
		t.Fatalf("UpdateLamp failed: %v", err)
	}

	_, ok = resp.(UpdateLamp404Response)
	if !ok {
		t.Fatalf("Expected UpdateLamp404Response for non-existent lamp, got %T", resp)
	}
}

func TestLampAPI_DeleteLamp(t *testing.T) {
	api := NewLampAPI()
	lampID := uuid.New()
	lamp := Lamp{
		Id:     openapi_types.UUID(lampID),
		Status: true,
	}
	api.lamps[lampID.String()] = lamp

	// Test deleting existing lamp
	req := DeleteLampRequestObject{LampId: lampID.String()}
	resp, err := api.DeleteLamp(context.Background(), req)
	if err != nil {
		t.Fatalf("DeleteLamp failed: %v", err)
	}

	_, ok := resp.(DeleteLamp204Response)
	if !ok {
		t.Fatalf("Expected DeleteLamp204Response, got %T", resp)
	}

	// Verify the lamp was deleted from storage
	if len(api.lamps) != 0 {
		t.Errorf("Expected empty storage after deletion, got %d lamps", len(api.lamps))
	}

	// Test deleting non-existent lamp
	req = DeleteLampRequestObject{LampId: "nonexistent"}
	resp, err = api.DeleteLamp(context.Background(), req)
	if err != nil {
		t.Fatalf("DeleteLamp failed: %v", err)
	}

	_, ok = resp.(DeleteLamp404Response)
	if !ok {
		t.Fatalf("Expected DeleteLamp404Response for non-existent lamp, got %T", resp)
	}
}
