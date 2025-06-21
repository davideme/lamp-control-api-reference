package api

import (
	"context"
	"errors"
	"net/http"
	"testing"

	"github.com/google/uuid"
	"go.uber.org/mock/gomock"
)

// Test helper to create API with mock repository
func newTestLampAPI(t *testing.T) (*LampAPI, *MockLampRepository) {
	t.Helper()
	ctrl := gomock.NewController(t)
	t.Cleanup(ctrl.Finish)

	mockRepo := NewMockLampRepository(ctrl)
	api := NewLampAPIWithRepository(mockRepo)

	return api, mockRepo
}

// TestLampAPI_CreateLamp_WithMock tests CreateLamp with mocked repository
func TestLampAPI_CreateLamp_WithMock(t *testing.T) {
	tests := []struct {
		name           string
		request        CreateLampRequestObject
		setupMock      func(*MockLampRepository)
		expectError    bool
		expectResponse func(CreateLampResponseObject) bool
	}{
		{
			name: "successful creation",
			request: CreateLampRequestObject{
				Body: &LampCreate{Status: true},
			},
			setupMock: func(m *MockLampRepository) {
				m.EXPECT().Create(gomock.Any(), gomock.Any()).Return(nil)
			},
			expectError: false,
			expectResponse: func(resp CreateLampResponseObject) bool {
				createResp, ok := resp.(CreateLamp201JSONResponse)
				if !ok {
					return false
				}
				lamp := Lamp(createResp)

				return lamp.Status == true && lamp.Id != uuid.Nil
			},
		},
		{
			name: "nil body",
			request: CreateLampRequestObject{
				Body: nil,
			},
			setupMock: func(m *MockLampRepository) {
				// No mock calls expected for nil body
			},
			expectError: true,
			expectResponse: func(resp CreateLampResponseObject) bool {
				return resp == nil
			},
		},
		{
			name: "repository error",
			request: CreateLampRequestObject{
				Body: &LampCreate{Status: false},
			},
			setupMock: func(m *MockLampRepository) {
				m.EXPECT().Create(gomock.Any(), gomock.Any()).Return(errors.New("database error"))
			},
			expectError: true,
			expectResponse: func(resp CreateLampResponseObject) bool {
				return resp == nil
			},
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			api, mockRepo := newTestLampAPI(t)
			tt.setupMock(mockRepo)

			resp, err := api.CreateLamp(context.Background(), tt.request)

			if tt.expectError {
				if err == nil {
					t.Error("Expected error but got none")
				}

				if err != nil {
					if apiErr, ok := err.(*APIError); ok {
						if tt.name == "nil body" && apiErr.StatusCode != http.StatusBadRequest {
							t.Errorf("Expected status %d, got %d", http.StatusBadRequest, apiErr.StatusCode)
						}
						if tt.name == "repository error" && apiErr.StatusCode != http.StatusInternalServerError {
							t.Errorf("Expected status %d, got %d", http.StatusInternalServerError, apiErr.StatusCode)
						}
					}
				}
			} else {
				if err != nil {
					t.Errorf("Expected no error but got: %v", err)
				}
			}

			if !tt.expectResponse(resp) {
				t.Errorf("Response validation failed for test: %s", tt.name)
			}
		})
	}
}

// TestLampAPI_ListLamps_WithMock tests ListLamps with mocked repository
func TestLampAPI_ListLamps_WithMock(t *testing.T) {
	tests := []struct {
		name           string
		setupMock      func(*MockLampRepository)
		expectError    bool
		expectResponse func(ListLampsResponseObject) bool
	}{
		{
			name: "successful list with lamps",
			setupMock: func(m *MockLampRepository) {
				lamps := []Lamp{
					{Id: uuid.New(), Status: true},
					{Id: uuid.New(), Status: false},
				}
				m.EXPECT().List(gomock.Any()).Return(lamps, nil)
			},
			expectError: false,
			expectResponse: func(resp ListLampsResponseObject) bool {
				listResp, ok := resp.(ListLamps200JSONResponse)
				if !ok {
					return false
				}
				return len(listResp) == 2
			},
		},
		{
			name: "successful list with empty result",
			setupMock: func(m *MockLampRepository) {
				m.EXPECT().List(gomock.Any()).Return([]Lamp{}, nil)
			},
			expectError: false,
			expectResponse: func(resp ListLampsResponseObject) bool {
				listResp, ok := resp.(ListLamps200JSONResponse)
				if !ok {
					return false
				}
				return len(listResp) == 0
			},
		},
		{
			name: "repository error",
			setupMock: func(m *MockLampRepository) {
				m.EXPECT().List(gomock.Any()).Return(nil, errors.New("database error"))
			},
			expectError: true,
			expectResponse: func(resp ListLampsResponseObject) bool {
				return resp == nil
			},
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			api, mockRepo := newTestLampAPI(t)
			tt.setupMock(mockRepo)

			resp, err := api.ListLamps(context.Background(), ListLampsRequestObject{})

			if tt.expectError {
				if err == nil {
					t.Error("Expected error but got none")
				}
				if err != nil {
					if apiErr, ok := err.(*APIError); ok && apiErr.StatusCode != http.StatusInternalServerError {
						t.Errorf("Expected status %d, got %d", http.StatusInternalServerError, apiErr.StatusCode)
					}
				}
			} else {
				if err != nil {
					t.Errorf("Expected no error but got: %v", err)
				}
			}

			if !tt.expectResponse(resp) {
				t.Errorf("Response validation failed for test: %s", tt.name)
			}
		})
	}
}

// TestLampAPI_GetLamp_WithMock tests GetLamp with mocked repository
func TestLampAPI_GetLamp_WithMock(t *testing.T) {
	testID := uuid.New().String()

	tests := []struct {
		name           string
		lampID         string
		setupMock      func(*MockLampRepository)
		expectError    bool
		expectResponse func(GetLampResponseObject) bool
	}{
		{
			name:   "successful get",
			lampID: testID,
			setupMock: func(m *MockLampRepository) {
				lamp := Lamp{Id: uuid.MustParse(testID), Status: true}
				m.EXPECT().GetByID(gomock.Any(), testID).Return(lamp, nil)
			},
			expectError: false,
			expectResponse: func(resp GetLampResponseObject) bool {
				getResp, ok := resp.(GetLamp200JSONResponse)
				if !ok {
					return false
				}
				lamp := Lamp(getResp)
				return lamp.Id.String() == testID && lamp.Status == true
			},
		},
		{
			name:   "lamp not found",
			lampID: testID,
			setupMock: func(m *MockLampRepository) {
				m.EXPECT().GetByID(gomock.Any(), testID).Return(Lamp{}, ErrLampNotFound)
			},
			expectError: false,
			expectResponse: func(resp GetLampResponseObject) bool {
				_, ok := resp.(GetLamp404Response)
				return ok
			},
		},
		{
			name:   "repository error",
			lampID: testID,
			setupMock: func(m *MockLampRepository) {
				m.EXPECT().GetByID(gomock.Any(), testID).Return(Lamp{}, errors.New("database error"))
			},
			expectError: true,
			expectResponse: func(resp GetLampResponseObject) bool {
				return resp == nil
			},
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			api, mockRepo := newTestLampAPI(t)
			tt.setupMock(mockRepo)

			req := GetLampRequestObject{LampId: tt.lampID}
			resp, err := api.GetLamp(context.Background(), req)

			if tt.expectError {
				if err == nil {
					t.Error("Expected error but got none")
				}
				if err != nil {
					if apiErr, ok := err.(*APIError); ok && apiErr.StatusCode != http.StatusInternalServerError {
						t.Errorf("Expected status %d, got %d", http.StatusInternalServerError, apiErr.StatusCode)
					}
				}
			} else {
				if err != nil {
					t.Errorf("Expected no error but got: %v", err)
				}
			}

			if !tt.expectResponse(resp) {
				t.Errorf("Response validation failed for test: %s", tt.name)
			}
		})
	}
}

// TestLampAPI_UpdateLamp_WithMock tests UpdateLamp with mocked repository
func TestLampAPI_UpdateLamp_WithMock(t *testing.T) {
	testID := uuid.New().String()

	tests := []struct {
		name           string
		lampID         string
		request        UpdateLampRequestObject
		setupMock      func(*MockLampRepository)
		expectError    bool
		expectResponse func(UpdateLampResponseObject) bool
	}{
		{
			name:   "successful update",
			lampID: testID,
			request: UpdateLampRequestObject{
				LampId: testID,
				Body:   &LampUpdate{Status: false},
			},
			setupMock: func(m *MockLampRepository) {
				existingLamp := Lamp{Id: uuid.MustParse(testID), Status: true}
				updatedLamp := Lamp{Id: uuid.MustParse(testID), Status: false}
				m.EXPECT().GetByID(gomock.Any(), testID).Return(existingLamp, nil)
				m.EXPECT().Update(gomock.Any(), updatedLamp).Return(nil)
			},
			expectError: false,
			expectResponse: func(resp UpdateLampResponseObject) bool {
				updateResp, ok := resp.(UpdateLamp200JSONResponse)
				if !ok {
					return false
				}
				lamp := Lamp(updateResp)
				return lamp.Id.String() == testID && lamp.Status == false
			},
		},
		{
			name:   "nil body",
			lampID: testID,
			request: UpdateLampRequestObject{
				LampId: testID,
				Body:   nil,
			},
			setupMock: func(m *MockLampRepository) {
				// No mock calls expected for nil body
			},
			expectError: true,
			expectResponse: func(resp UpdateLampResponseObject) bool {
				return resp == nil
			},
		},
		{
			name:   "lamp not found",
			lampID: testID,
			request: UpdateLampRequestObject{
				LampId: testID,
				Body:   &LampUpdate{Status: true},
			},
			setupMock: func(m *MockLampRepository) {
				m.EXPECT().GetByID(gomock.Any(), testID).Return(Lamp{}, ErrLampNotFound)
			},
			expectError: false,
			expectResponse: func(resp UpdateLampResponseObject) bool {
				_, ok := resp.(UpdateLamp404Response)
				return ok
			},
		},
		{
			name:   "repository error",
			lampID: testID,
			request: UpdateLampRequestObject{
				LampId: testID,
				Body:   &LampUpdate{Status: true},
			},
			setupMock: func(m *MockLampRepository) {
				m.EXPECT().GetByID(gomock.Any(), testID).Return(Lamp{}, errors.New("database error"))
			},
			expectError: true,
			expectResponse: func(resp UpdateLampResponseObject) bool {
				return resp == nil
			},
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			api, mockRepo := newTestLampAPI(t)
			tt.setupMock(mockRepo)

			resp, err := api.UpdateLamp(context.Background(), tt.request)

			if tt.expectError {
				if err == nil {
					t.Error("Expected error but got none")
				}
				if err != nil {
					if apiErr, ok := err.(*APIError); ok {
						if tt.name == "nil body" && apiErr.StatusCode != http.StatusBadRequest {
							t.Errorf("Expected status %d, got %d", http.StatusBadRequest, apiErr.StatusCode)
						}
						if tt.name == "repository error" && apiErr.StatusCode != http.StatusInternalServerError {
							t.Errorf("Expected status %d, got %d", http.StatusInternalServerError, apiErr.StatusCode)
						}
					}
				}
			} else {
				if err != nil {
					t.Errorf("Expected no error but got: %v", err)
				}
			}

			if !tt.expectResponse(resp) {
				t.Errorf("Response validation failed for test: %s", tt.name)
			}
		})
	}
}

// TestLampAPI_DeleteLamp_WithMock tests DeleteLamp with mocked repository
func TestLampAPI_DeleteLamp_WithMock(t *testing.T) {
	testID := uuid.New().String()

	tests := []struct {
		name           string
		lampID         string
		setupMock      func(*MockLampRepository)
		expectError    bool
		expectResponse func(DeleteLampResponseObject) bool
	}{
		{
			name:   "successful delete",
			lampID: testID,
			setupMock: func(m *MockLampRepository) {
				m.EXPECT().Delete(gomock.Any(), testID).Return(nil)
			},
			expectError: false,
			expectResponse: func(resp DeleteLampResponseObject) bool {
				_, ok := resp.(DeleteLamp204Response)
				return ok
			},
		},
		{
			name:   "lamp not found",
			lampID: testID,
			setupMock: func(m *MockLampRepository) {
				m.EXPECT().Delete(gomock.Any(), testID).Return(ErrLampNotFound)
			},
			expectError: false,
			expectResponse: func(resp DeleteLampResponseObject) bool {
				_, ok := resp.(DeleteLamp404Response)
				return ok
			},
		},
		{
			name:   "repository error",
			lampID: testID,
			setupMock: func(m *MockLampRepository) {
				m.EXPECT().Delete(gomock.Any(), testID).Return(errors.New("database error"))
			},
			expectError: true,
			expectResponse: func(resp DeleteLampResponseObject) bool {
				return resp == nil
			},
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			api, mockRepo := newTestLampAPI(t)
			tt.setupMock(mockRepo)

			req := DeleteLampRequestObject{LampId: tt.lampID}
			resp, err := api.DeleteLamp(context.Background(), req)

			if tt.expectError {
				if err == nil {
					t.Error("Expected error but got none")
				}
				if err != nil {
					if apiErr, ok := err.(*APIError); ok && apiErr.StatusCode != http.StatusInternalServerError {
						t.Errorf("Expected status %d, got %d", http.StatusInternalServerError, apiErr.StatusCode)
					}
				}
			} else {
				if err != nil {
					t.Errorf("Expected no error but got: %v", err)
				}
			}

			if !tt.expectResponse(resp) {
				t.Errorf("Response validation failed for test: %s", tt.name)
			}
		})
	}
}

// TestLampAPI_Interface_Compliance tests that LampAPI implements StrictServerInterface
func TestLampAPI_Interface_Compliance(t *testing.T) {
	api := NewLampAPI()

	// This test will fail to compile if LampAPI doesn't implement StrictServerInterface
	var _ StrictServerInterface = api

	// Test that we can also use a custom repository
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockRepo := NewMockLampRepository(ctrl)
	apiWithMock := NewLampAPIWithRepository(mockRepo)
	var _ StrictServerInterface = apiWithMock
}
