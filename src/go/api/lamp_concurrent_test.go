package api

import (
	"context"
	"sync"
	"testing"
)

func TestLampAPI_ConcurrentAccess(t *testing.T) {
	api := NewLampAPI()

	// Number of concurrent goroutines
	numGoroutines := 10
	numOperationsPerGoroutine := 100

	var wg sync.WaitGroup
	wg.Add(numGoroutines)

	// Run concurrent operations
	for i := 0; i < numGoroutines; i++ {
		go func(goroutineID int) {
			defer wg.Done()

			for j := 0; j < numOperationsPerGoroutine; j++ {
				// Create a lamp
				createReq := CreateLampRequestObject{
					Body: &LampCreate{Status: j%2 == 0}, // Alternate between true/false
				}

				resp, err := api.CreateLamp(context.Background(), createReq)
				if err != nil {
					t.Errorf("Goroutine %d: CreateLamp failed: %v", goroutineID, err)
					return
				}

				createResp, ok := resp.(CreateLamp201JSONResponse)
				if !ok {
					t.Errorf("Goroutine %d: Expected CreateLamp201JSONResponse, got %T", goroutineID, resp)
					return
				}

				lamp := Lamp(createResp)
				lampID := lamp.Id.String()

				// Try to get the lamp we just created
				getReq := GetLampRequestObject{LampId: lampID}
				_, err = api.GetLamp(context.Background(), getReq)
				if err != nil {
					t.Errorf("Goroutine %d: GetLamp failed: %v", goroutineID, err)
					return
				}

				// Update the lamp
				updateReq := UpdateLampRequestObject{
					LampId: lampID,
					Body:   &LampUpdate{Status: !lamp.Status}, // Toggle status
				}

				_, err = api.UpdateLamp(context.Background(), updateReq)
				if err != nil {
					t.Errorf("Goroutine %d: UpdateLamp failed: %v", goroutineID, err)
					return
				}

				// List all lamps (this tests concurrent read access)
				_, err = api.ListLamps(context.Background(), ListLampsRequestObject{})
				if err != nil {
					t.Errorf("Goroutine %d: ListLamps failed: %v", goroutineID, err)
					return
				}
			}
		}(i)
	}

	// Wait for all goroutines to complete
	wg.Wait()

	// Verify that we have the expected number of lamps
	expectedLamps := numGoroutines * numOperationsPerGoroutine
	if len(api.lamps) != expectedLamps {
		t.Errorf("Expected %d lamps, got %d", expectedLamps, len(api.lamps))
	}

	t.Logf("Successfully created and accessed %d lamps concurrently", len(api.lamps))
}
