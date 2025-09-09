package api_test

import (
	"context"
	"testing"

	"github.com/davideme/lamp-control-api-reference/api"
	"github.com/davideme/lamp-control-api-reference/api/entities"
)

func TestLampAPIIntegration(t *testing.T) {
	// Create a new LampAPI with repository
	lampAPI := api.NewLampAPI()
	
	ctx := context.Background()

	// Test create operation
	createRequest := api.CreateLampRequestObject{
		Body: &api.LampCreate{
			Status: true,
		},
	}

	createResponse, err := lampAPI.CreateLamp(ctx, createRequest)
	if err != nil {
		t.Fatalf("CreateLamp failed: %v", err)
	}

	// Check that we got a successful response
	createResult, ok := createResponse.(api.CreateLamp201JSONResponse)
	if !ok {
		t.Fatalf("Expected CreateLamp201JSONResponse, got %T", createResponse)
	}

	lamp := api.Lamp(createResult)
	if !lamp.Status {
		t.Error("Expected lamp status to be true")
	}

	if lamp.Id == [16]byte{} {
		t.Error("Expected lamp to have a valid ID")
	}

	// Test list operation
	listRequest := api.ListLampsRequestObject{}
	listResponse, err := lampAPI.ListLamps(ctx, listRequest)
	if err != nil {
		t.Fatalf("ListLamps failed: %v", err)
	}

	listResult, ok := listResponse.(api.ListLamps200JSONResponse)
	if !ok {
		t.Fatalf("Expected ListLamps200JSONResponse, got %T", listResponse)
	}

	if len(listResult.Data) != 1 {
		t.Errorf("Expected 1 lamp in list, got %d", len(listResult.Data))
	}

	// Test get operation
	getRequest := api.GetLampRequestObject{
		LampId: lamp.Id.String(),
	}
	
	getResponse, err := lampAPI.GetLamp(ctx, getRequest)
	if err != nil {
		t.Fatalf("GetLamp failed: %v", err)
	}

	_, ok = getResponse.(api.GetLamp200JSONResponse)
	if !ok {
		t.Fatalf("Expected GetLamp200JSONResponse, got %T", getResponse)
	}
}

func TestDomainEntitySeparation(t *testing.T) {
	// This test verifies that domain entities are separate from API models
	
	// Create a domain entity
	lampEntity := entities.NewLampEntity(true)
	
	// Create a mapper
	mapper := api.NewLampMapper()
	
	// Convert entity to API model
	apiModel := mapper.ToAPIModel(lampEntity)
	
	// Convert API model back to entity
	convertedEntity := mapper.ToEntity(apiModel)
	
	// Verify they have the same data
	if lampEntity.ID != convertedEntity.ID {
		t.Errorf("Expected ID %v, got %v", lampEntity.ID, convertedEntity.ID)
	}
	
	if lampEntity.Status != convertedEntity.Status {
		t.Errorf("Expected Status %v, got %v", lampEntity.Status, convertedEntity.Status)
	}
}