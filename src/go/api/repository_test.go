package api

import (
	"context"
	"errors"
	"sync"
	"testing"
	"time"

	"github.com/davideme/lamp-control-api-reference/api/entities"
	"github.com/google/uuid"
)

func TestNewInMemoryLampRepository(t *testing.T) {
	repo := NewInMemoryLampRepository()

	if repo == nil {
		t.Fatal("NewInMemoryLampRepository should not return nil")
	}

	if repo.lamps == nil {
		t.Error("Repository lamps map should be initialized")
	}

	if len(repo.lamps) != 0 {
		t.Error("Repository should start empty")
	}
}

func TestInMemoryLampRepository_Create(t *testing.T) {
	repo := NewInMemoryLampRepository()
	ctx := context.Background()

	lamp := &entities.LampEntity{
		ID:        uuid.New(),
		Status:    true,
		CreatedAt: time.Now(),
		UpdatedAt: time.Now(),
	}

	// Test successful creation
	err := repo.Create(ctx, lamp)
	if err != nil {
		t.Fatalf("Create should not return error: %v", err)
	}

	// Verify lamp was stored
	storedLamp, err := repo.GetByID(ctx, lamp.ID.String())
	if err != nil {
		t.Fatalf("GetByID failed after Create: %v", err)
	}

	if storedLamp.ID != lamp.ID {
		t.Errorf("Expected ID %v, got %v", lamp.ID, storedLamp.ID)
	}

	if storedLamp.Status != lamp.Status {
		t.Errorf("Expected Status %v, got %v", lamp.Status, storedLamp.Status)
	}
}

func TestInMemoryLampRepository_Create_Multiple(t *testing.T) {
	repo := NewInMemoryLampRepository()
	ctx := context.Background()

	lamp1 := &entities.LampEntity{
		ID:        uuid.New(),
		Status:    true,
		CreatedAt: time.Now(),
		UpdatedAt: time.Now(),
	}
	lamp2 := &entities.LampEntity{
		ID:        uuid.New(),
		Status:    false,
		CreatedAt: time.Now(),
		UpdatedAt: time.Now(),
	}

	err := repo.Create(ctx, lamp1)
	if err != nil {
		t.Fatalf("Create lamp1 failed: %v", err)
	}

	err = repo.Create(ctx, lamp2)
	if err != nil {
		t.Fatalf("Create lamp2 failed: %v", err)
	}

	// Verify both lamps exist
	lamps, err := repo.List(ctx)
	if err != nil {
		t.Fatalf("List failed: %v", err)
	}

	if len(lamps) != 2 {
		t.Errorf("Expected 2 lamps, got %d", len(lamps))
	}
}

func TestInMemoryLampRepository_GetByID(t *testing.T) {
	repo := NewInMemoryLampRepository()
	ctx := context.Background()

	lamp := &entities.LampEntity{
		ID:        uuid.New(),
		Status:    true,
		CreatedAt: time.Now(),
		UpdatedAt: time.Now(),
	}

	// Test getting non-existent lamp
	_, err := repo.GetByID(ctx, lamp.ID.String())
	if err == nil {
		t.Error("GetByID should return error for non-existent lamp")
	}

	if !errors.Is(err, ErrLampNotFound) {
		t.Errorf("Expected ErrLampNotFound, got %v", err)
	}

	// Create lamp and test getting it
	err = repo.Create(ctx, lamp)
	if err != nil {
		t.Fatalf("Create failed: %v", err)
	}

	retrievedLamp, err := repo.GetByID(ctx, lamp.ID.String())
	if err != nil {
		t.Fatalf("GetByID failed: %v", err)
	}

	if retrievedLamp.ID != lamp.ID {
		t.Errorf("Expected ID %v, got %v", lamp.ID, retrievedLamp.ID)
	}

	if retrievedLamp.Status != lamp.Status {
		t.Errorf("Expected Status %v, got %v", lamp.Status, retrievedLamp.Status)
	}
}

func TestInMemoryLampRepository_Update(t *testing.T) {
	repo := NewInMemoryLampRepository()
	ctx := context.Background()

	lamp := &entities.LampEntity{
		ID:        uuid.New(),
		Status:    true,
		CreatedAt: time.Now(),
		UpdatedAt: time.Now(),
	}

	// Test updating non-existent lamp
	err := repo.Update(ctx, lamp)
	if err == nil {
		t.Error("Update should return error for non-existent lamp")
	}

	if !errors.Is(err, ErrLampNotFound) {
		t.Errorf("Expected ErrLampNotFound, got %v", err)
	}

	// Create lamp
	err = repo.Create(ctx, lamp)
	if err != nil {
		t.Fatalf("Create failed: %v", err)
	}

	// Update lamp status
	updatedLamp := &entities.LampEntity{
		ID:        lamp.ID,
		Status:    false,
		CreatedAt: lamp.CreatedAt,
		UpdatedAt: time.Now(),
	}

	err = repo.Update(ctx, updatedLamp)
	if err != nil {
		t.Fatalf("Update failed: %v", err)
	}

	// Verify update
	retrievedLamp, err := repo.GetByID(ctx, lamp.ID.String())
	if err != nil {
		t.Fatalf("GetByID failed after update: %v", err)
	}

	if retrievedLamp.Status != false {
		t.Errorf("Expected Status false after update, got %v", retrievedLamp.Status)
	}
}

func TestInMemoryLampRepository_Delete(t *testing.T) {
	repo := NewInMemoryLampRepository()
	ctx := context.Background()

	lamp := &entities.LampEntity{
		ID:        uuid.New(),
		Status:    true,
		CreatedAt: time.Now(),
		UpdatedAt: time.Now(),
	}

	// Test deleting non-existent lamp
	err := repo.Delete(ctx, lamp.ID.String())
	if err == nil {
		t.Error("Delete should return error for non-existent lamp")
	}

	if !errors.Is(err, ErrLampNotFound) {
		t.Errorf("Expected ErrLampNotFound, got %v", err)
	}

	// Create lamp
	err = repo.Create(ctx, lamp)
	if err != nil {
		t.Fatalf("Create failed: %v", err)
	}

	// Verify lamp exists
	exists := repo.Exists(ctx, lamp.ID.String())
	if !exists {
		t.Error("Lamp should exist before deletion")
	}

	// Delete lamp
	err = repo.Delete(ctx, lamp.ID.String())
	if err != nil {
		t.Fatalf("Delete failed: %v", err)
	}

	// Verify lamp no longer exists
	exists = repo.Exists(ctx, lamp.ID.String())
	if exists {
		t.Error("Lamp should not exist after deletion")
	}

	// Verify GetByID returns error
	_, err = repo.GetByID(ctx, lamp.ID.String())
	if err == nil {
		t.Error("GetByID should return error after deletion")
	}

	if !errors.Is(err, ErrLampNotFound) {
		t.Errorf("Expected ErrLampNotFound after deletion, got %v", err)
	}
}

func TestInMemoryLampRepository_List(t *testing.T) {
	repo := NewInMemoryLampRepository()
	ctx := context.Background()

	// Test empty repository
	lamps, err := repo.List(ctx)
	if err != nil {
		t.Fatalf("List failed on empty repository: %v", err)
	}

	if len(lamps) != 0 {
		t.Errorf("Expected 0 lamps in empty repository, got %d", len(lamps))
	}

	// Add some lamps
	lamp1 := &entities.LampEntity{
		ID:        uuid.New(),
		Status:    true,
		CreatedAt: time.Now(),
		UpdatedAt: time.Now(),
	}
	lamp2 := &entities.LampEntity{
		ID:        uuid.New(),
		Status:    false,
		CreatedAt: time.Now(),
		UpdatedAt: time.Now(),
	}
	lamp3 := &entities.LampEntity{
		ID:        uuid.New(),
		Status:    true,
		CreatedAt: time.Now(),
		UpdatedAt: time.Now(),
	}

	err = repo.Create(ctx, lamp1)
	if err != nil {
		t.Fatalf("Create lamp1 failed: %v", err)
	}

	err = repo.Create(ctx, lamp2)
	if err != nil {
		t.Fatalf("Create lamp2 failed: %v", err)
	}

	err = repo.Create(ctx, lamp3)
	if err != nil {
		t.Fatalf("Create lamp3 failed: %v", err)
	}

	// Test list with multiple lamps
	lamps, err = repo.List(ctx)
	if err != nil {
		t.Fatalf("List failed: %v", err)
	}

	if len(lamps) != 3 {
		t.Errorf("Expected 3 lamps, got %d", len(lamps))
	}

	// Verify all lamps are present (order may vary)
	lampIds := make(map[uuid.UUID]bool)
	for _, lamp := range lamps {
		lampIds[lamp.ID] = true
	}

	if !lampIds[lamp1.ID] {
		t.Error("lamp1 not found in list")
	}
	if !lampIds[lamp2.ID] {
		t.Error("lamp2 not found in list")
	}
	if !lampIds[lamp3.ID] {
		t.Error("lamp3 not found in list")
	}
}

func TestInMemoryLampRepository_Exists(t *testing.T) {
	repo := NewInMemoryLampRepository()
	ctx := context.Background()

	lamp := &entities.LampEntity{
		ID:        uuid.New(),
		Status:    true,
		CreatedAt: time.Now(),
		UpdatedAt: time.Now(),
	}

	// Test non-existent lamp
	exists := repo.Exists(ctx, lamp.ID.String())
	if exists {
		t.Error("Exists should return false for non-existent lamp")
	}

	// Create lamp
	err := repo.Create(ctx, lamp)
	if err != nil {
		t.Fatalf("Create failed: %v", err)
	}

	// Test existing lamp
	exists = repo.Exists(ctx, lamp.ID.String())
	if !exists {
		t.Error("Exists should return true for existing lamp")
	}

	// Delete lamp
	err = repo.Delete(ctx, lamp.ID.String())
	if err != nil {
		t.Fatalf("Delete failed: %v", err)
	}

	// Test deleted lamp
	exists = repo.Exists(ctx, lamp.ID.String())
	if exists {
		t.Error("Exists should return false for deleted lamp")
	}
}

func TestInMemoryLampRepository_ConcurrentAccess(t *testing.T) {
	repo := NewInMemoryLampRepository()
	ctx := context.Background()

	const numGoroutines = 100
	const numOperationsPerGoroutine = 10

	var wg sync.WaitGroup

	// Test concurrent creates
	wg.Add(numGoroutines)
	for i := 0; i < numGoroutines; i++ {
		go func(routineID int) {
			defer wg.Done()

			for j := 0; j < numOperationsPerGoroutine; j++ {
				lamp := &entities.LampEntity{
					ID:        uuid.New(),
					Status:    routineID%2 == 0, // Alternate between true/false
					CreatedAt: time.Now(),
					UpdatedAt: time.Now(),
				}

				err := repo.Create(ctx, lamp)
				if err != nil {
					t.Errorf("Concurrent create failed: %v", err)
					return
				}

				// Verify the lamp can be retrieved
				retrievedLamp, err := repo.GetByID(ctx, lamp.ID.String())
				if err != nil {
					t.Errorf("Concurrent get failed: %v", err)
					return
				}

				if retrievedLamp.ID != lamp.ID {
					t.Errorf("Concurrent access returned wrong lamp")
					return
				}
			}
		}(i)
	}

	wg.Wait()

	// Verify final count
	lamps, err := repo.List(ctx)
	if err != nil {
		t.Fatalf("List failed after concurrent access: %v", err)
	}

	expectedCount := numGoroutines * numOperationsPerGoroutine
	if len(lamps) != expectedCount {
		t.Errorf("Expected %d lamps after concurrent access, got %d", expectedCount, len(lamps))
	}
}

func TestInMemoryLampRepository_ConcurrentReadWrite(t *testing.T) {
	repo := NewInMemoryLampRepository()
	ctx := context.Background()

	// Create initial lamps
	var initialLamps []*entities.LampEntity
	for i := 0; i < 10; i++ {
		lamp := &entities.LampEntity{
			ID:        uuid.New(),
			Status:    true,
			CreatedAt: time.Now(),
			UpdatedAt: time.Now(),
		}
		err := repo.Create(ctx, lamp)
		if err != nil {
			t.Fatalf("Failed to create initial lamp: %v", err)
		}
		initialLamps = append(initialLamps, lamp)
	}

	var wg sync.WaitGroup

	// Concurrent readers
	wg.Add(50)
	for i := 0; i < 50; i++ {
		go func() {
			defer wg.Done()

			for j := 0; j < 10; j++ {
				// Random read operations
				lamps, err := repo.List(ctx)
				if err != nil {
					t.Errorf("Concurrent list failed: %v", err)
					return
				}

				if len(lamps) < 10 {
					t.Errorf("Expected at least 10 lamps, got %d", len(lamps))
					return
				}

				// Check if a specific lamp exists
				lampId := initialLamps[j%len(initialLamps)].ID.String()
				exists := repo.Exists(ctx, lampId)
				if !exists {
					t.Errorf("Expected lamp %s to exist", lampId)
					return
				}
			}
		}()
	}

	// Concurrent writers (updates)
	wg.Add(10)
	for i := 0; i < 10; i++ {
		go func(index int) {
			defer wg.Done()

			lamp := initialLamps[index]
			updatedLamp := &entities.LampEntity{
				ID:        lamp.ID,
				Status:    false, // Toggle status
				CreatedAt: lamp.CreatedAt,
				UpdatedAt: time.Now(),
			}

			err := repo.Update(ctx, updatedLamp)
			if err != nil {
				t.Errorf("Concurrent update failed: %v", err)
				return
			}
		}(i)
	}

	wg.Wait()

	// Verify all updates were applied
	for _, originalLamp := range initialLamps {
		updatedLamp, err := repo.GetByID(ctx, originalLamp.ID.String())
		if err != nil {
			t.Fatalf("Failed to get updated lamp: %v", err)
		}

		if updatedLamp.Status != false {
			t.Errorf("Expected lamp %s to have status false after update, got %v",
				originalLamp.ID.String(), updatedLamp.Status)
		}
	}
}

func TestInMemoryLampRepository_ContextHandling(t *testing.T) {
	repo := NewInMemoryLampRepository()

	// Test with background context
	ctx := context.Background()
	lamp := &entities.LampEntity{
		ID:        uuid.New(),
		Status:    true,
		CreatedAt: time.Now(),
		UpdatedAt: time.Now(),
	}

	err := repo.Create(ctx, lamp)
	if err != nil {
		t.Fatalf("Create with background context failed: %v", err)
	}

	// Test with canceled context (repository should still work as it doesn't use context for cancellation)
	cancelCtx, cancel := context.WithCancel(context.Background())
	cancel() // Cancel immediately

	_, err = repo.GetByID(cancelCtx, lamp.ID.String())
	if err != nil {
		t.Fatalf("GetByID with canceled context failed: %v", err)
	}

	// Repository operations should still work with canceled context
	// since the implementation doesn't check for cancellation
	lamps, err := repo.List(cancelCtx)
	if err != nil {
		t.Fatalf("List with canceled context failed: %v", err)
	}

	if len(lamps) != 1 {
		t.Errorf("Expected 1 lamp, got %d", len(lamps))
	}
}
