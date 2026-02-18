package api

import (
	"context"
	"net/http"
	"testing"
)

func strPtr(s string) *string { return &s }
func intPtr(i int) *int       { return &i }

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

	// Verify the lamp was stored by listing lamps
	listResp, err := api.ListLamps(context.Background(), ListLampsRequestObject{})
	if err != nil {
		t.Fatalf("ListLamps failed: %v", err)
	}

	listResult, ok := listResp.(ListLamps200JSONResponse)
	if !ok {
		t.Fatalf("Expected ListLamps200JSONResponse, got %T", listResp)
	}

	if len(listResult.Data) != 1 {
		t.Errorf("Expected 1 lamp in storage, got %d", len(listResult.Data))
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

	if len(listResp.Data) != 0 {
		t.Errorf("Expected empty list, got %d lamps", len(listResp.Data))
	}

	// Add a lamp and verify it appears in the list
	createReq := CreateLampRequestObject{
		Body: &LampCreate{Status: true},
	}
	_, err = api.CreateLamp(context.Background(), createReq)
	if err != nil {
		t.Fatalf("CreateLamp failed: %v", err)
	}

	resp, err = api.ListLamps(context.Background(), ListLampsRequestObject{})
	if err != nil {
		t.Fatalf("ListLamps failed: %v", err)
	}

	listResp, ok = resp.(ListLamps200JSONResponse)
	if !ok {
		t.Fatalf("Expected ListLamps200JSONResponse, got %T", resp)
	}

	if len(listResp.Data) != 1 {
		t.Errorf("Expected 1 lamp, got %d lamps", len(listResp.Data))
	}
}

func TestLampAPI_ListLamps_DefaultPageSize(t *testing.T) {
	api := NewLampAPI()

	for i := 0; i < 30; i++ {
		_, err := api.CreateLamp(context.Background(), CreateLampRequestObject{
			Body: &LampCreate{Status: i%2 == 0},
		})
		if err != nil {
			t.Fatalf("CreateLamp failed: %v", err)
		}
	}

	resp, err := api.ListLamps(context.Background(), ListLampsRequestObject{})
	if err != nil {
		t.Fatalf("ListLamps failed: %v", err)
	}
	listResp, ok := resp.(ListLamps200JSONResponse)
	if !ok {
		t.Fatalf("Expected ListLamps200JSONResponse, got %T", resp)
	}

	if len(listResp.Data) != 25 {
		t.Fatalf("Expected default page size of 25, got %d", len(listResp.Data))
	}
	if !listResp.HasMore {
		t.Fatalf("Expected hasMore=true when more lamps are available")
	}
	if listResp.NextCursor == nil || *listResp.NextCursor != "25" {
		t.Fatalf("Expected nextCursor=25, got %v", listResp.NextCursor)
	}
}

func TestLampAPI_ListLamps_PaginationProgression(t *testing.T) {
	api := NewLampAPI()

	for i := 0; i < 3; i++ {
		_, err := api.CreateLamp(context.Background(), CreateLampRequestObject{
			Body: &LampCreate{Status: true},
		})
		if err != nil {
			t.Fatalf("CreateLamp failed: %v", err)
		}
	}

	firstRespRaw, err := api.ListLamps(context.Background(), ListLampsRequestObject{
		Params: ListLampsParams{PageSize: intPtr(2)},
	})
	if err != nil {
		t.Fatalf("ListLamps first page failed: %v", err)
	}
	firstResp, ok := firstRespRaw.(ListLamps200JSONResponse)
	if !ok {
		t.Fatalf("Expected ListLamps200JSONResponse, got %T", firstRespRaw)
	}

	if len(firstResp.Data) != 2 {
		t.Fatalf("Expected first page size 2, got %d", len(firstResp.Data))
	}
	if !firstResp.HasMore {
		t.Fatalf("Expected hasMore=true on first page")
	}
	if firstResp.NextCursor == nil || *firstResp.NextCursor != "2" {
		t.Fatalf("Expected nextCursor=2, got %v", firstResp.NextCursor)
	}

	secondRespRaw, err := api.ListLamps(context.Background(), ListLampsRequestObject{
		Params: ListLampsParams{Cursor: strPtr("2"), PageSize: intPtr(2)},
	})
	if err != nil {
		t.Fatalf("ListLamps second page failed: %v", err)
	}
	secondResp, ok := secondRespRaw.(ListLamps200JSONResponse)
	if !ok {
		t.Fatalf("Expected ListLamps200JSONResponse, got %T", secondRespRaw)
	}

	if len(secondResp.Data) != 1 {
		t.Fatalf("Expected second page size 1, got %d", len(secondResp.Data))
	}
	if secondResp.HasMore {
		t.Fatalf("Expected hasMore=false on terminal page")
	}
	if secondResp.NextCursor != nil {
		t.Fatalf("Expected nil nextCursor on terminal page, got %v", secondResp.NextCursor)
	}
}

func TestLampAPI_ListLamps_InvalidPaginationParams(t *testing.T) {
	api := NewLampAPI()

	tests := []struct {
		name   string
		params ListLampsParams
	}{
		{name: "invalid cursor", params: ListLampsParams{Cursor: strPtr("abc"), PageSize: intPtr(10)}},
		{name: "negative cursor", params: ListLampsParams{Cursor: strPtr("-1"), PageSize: intPtr(10)}},
		{name: "cursor out of int32 range", params: ListLampsParams{Cursor: strPtr("2147483648"), PageSize: intPtr(10)}},
		{name: "pageSize too small", params: ListLampsParams{PageSize: intPtr(0)}},
		{name: "pageSize too large", params: ListLampsParams{PageSize: intPtr(101)}},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			resp, err := api.ListLamps(context.Background(), ListLampsRequestObject{Params: tt.params})
			if err != nil {
				t.Fatalf("Expected nil error for validation response, got %v", err)
			}

			invalidResp, ok := resp.(ListLamps400JSONResponse)
			if !ok {
				t.Fatalf("Expected ListLamps400JSONResponse, got %T", resp)
			}
			if invalidResp.Error == "" {
				t.Fatalf("Expected non-empty error code in response")
			}
		})
	}
}

func TestLampAPI_GetLamp(t *testing.T) {
	api := NewLampAPI()

	// Create a lamp using the API
	createReq := CreateLampRequestObject{
		Body: &LampCreate{Status: true},
	}
	createResp, err := api.CreateLamp(context.Background(), createReq)
	if err != nil {
		t.Fatalf("CreateLamp failed: %v", err)
	}

	createResult, ok := createResp.(CreateLamp201JSONResponse)
	if !ok {
		t.Fatalf("Expected CreateLamp201JSONResponse, got %T", createResp)
	}
	createdLamp := Lamp(createResult)

	// Test getting existing lamp
	req := GetLampRequestObject{LampId: createdLamp.Id.String()}
	resp, err := api.GetLamp(context.Background(), req)
	if err != nil {
		t.Fatalf("GetLamp failed: %v", err)
	}

	getResp, ok := resp.(GetLamp200JSONResponse)
	if !ok {
		t.Fatalf("Expected GetLamp200JSONResponse, got %T", resp)
	}

	retrievedLamp := Lamp(getResp)
	if retrievedLamp.Id != createdLamp.Id || retrievedLamp.Status != createdLamp.Status {
		t.Errorf("Retrieved lamp doesn't match: expected %+v, got %+v", createdLamp, retrievedLamp)
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

	// Create a lamp using the API
	createReq := CreateLampRequestObject{
		Body: &LampCreate{Status: true},
	}
	createResp, err := api.CreateLamp(context.Background(), createReq)
	if err != nil {
		t.Fatalf("CreateLamp failed: %v", err)
	}

	createResult, ok := createResp.(CreateLamp201JSONResponse)
	if !ok {
		t.Fatalf("Expected CreateLamp201JSONResponse, got %T", createResp)
	}
	createdLamp := Lamp(createResult)

	// Test updating existing lamp
	req := UpdateLampRequestObject{
		LampId: createdLamp.Id.String(),
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

	// Verify the lamp was updated in storage by getting it
	getReq := GetLampRequestObject{LampId: createdLamp.Id.String()}
	getResp, err := api.GetLamp(context.Background(), getReq)
	if err != nil {
		t.Fatalf("GetLamp failed: %v", err)
	}

	getLampResp, ok := getResp.(GetLamp200JSONResponse)
	if !ok {
		t.Fatalf("Expected GetLamp200JSONResponse, got %T", getResp)
	}

	storedLamp := Lamp(getLampResp)
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

	// Create a lamp using the API
	createReq := CreateLampRequestObject{
		Body: &LampCreate{Status: true},
	}
	createResp, err := api.CreateLamp(context.Background(), createReq)
	if err != nil {
		t.Fatalf("CreateLamp failed: %v", err)
	}

	createResult, ok := createResp.(CreateLamp201JSONResponse)
	if !ok {
		t.Fatalf("Expected CreateLamp201JSONResponse, got %T", createResp)
	}
	createdLamp := Lamp(createResult)

	// Test deleting existing lamp
	req := DeleteLampRequestObject{LampId: createdLamp.Id.String()}
	resp, err := api.DeleteLamp(context.Background(), req)
	if err != nil {
		t.Fatalf("DeleteLamp failed: %v", err)
	}

	_, ok = resp.(DeleteLamp204Response)
	if !ok {
		t.Fatalf("Expected DeleteLamp204Response, got %T", resp)
	}

	// Verify the lamp was deleted from storage by listing lamps
	listResp, err := api.ListLamps(context.Background(), ListLampsRequestObject{})
	if err != nil {
		t.Fatalf("ListLamps failed: %v", err)
	}

	listResult, ok := listResp.(ListLamps200JSONResponse)
	if !ok {
		t.Fatalf("Expected ListLamps200JSONResponse, got %T", listResp)
	}

	if len(listResult.Data) != 0 {
		t.Errorf("Expected empty storage after deletion, got %d lamps", len(listResult.Data))
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

func TestAPIError(t *testing.T) {
	err := &APIError{
		Message:    "Test error",
		StatusCode: 400,
	}

	if err.Error() != "Test error" {
		t.Errorf("Expected error message 'Test error', got '%s'", err.Error())
	}
}

func TestLampAPI_CreateLamp_NilBody(t *testing.T) {
	api := NewLampAPI()

	// Test creating a lamp with nil body
	req := CreateLampRequestObject{
		Body: nil,
	}

	_, err := api.CreateLamp(context.Background(), req)
	if err == nil {
		t.Fatal("Expected error for nil body, got nil")
	}

	apiErr, ok := err.(*APIError)
	if !ok {
		t.Fatalf("Expected APIError, got %T", err)
	}

	if apiErr.StatusCode != http.StatusBadRequest {
		t.Errorf("Expected status code %d, got %d", http.StatusBadRequest, apiErr.StatusCode)
	}
}

func TestLampAPI_UpdateLamp_NilBody(t *testing.T) {
	api := NewLampAPI()

	// Create a lamp using the API
	createReq := CreateLampRequestObject{
		Body: &LampCreate{Status: true},
	}
	createResp, err := api.CreateLamp(context.Background(), createReq)
	if err != nil {
		t.Fatalf("CreateLamp failed: %v", err)
	}

	createResult, ok := createResp.(CreateLamp201JSONResponse)
	if !ok {
		t.Fatalf("Expected CreateLamp201JSONResponse, got %T", createResp)
	}
	createdLamp := Lamp(createResult)

	// Test updating a lamp with nil body
	req := UpdateLampRequestObject{
		LampId: createdLamp.Id.String(),
		Body:   nil,
	}

	_, err = api.UpdateLamp(context.Background(), req)
	if err == nil {
		t.Fatal("Expected error for nil body, got nil")
	}

	apiErr, ok := err.(*APIError)
	if !ok {
		t.Fatalf("Expected APIError, got %T", err)
	}

	if apiErr.StatusCode != http.StatusBadRequest {
		t.Errorf("Expected status code %d, got %d", http.StatusBadRequest, apiErr.StatusCode)
	}
}
