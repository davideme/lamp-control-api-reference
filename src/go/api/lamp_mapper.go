package api

import (
	"time"

	"github.com/davideme/lamp-control-api-reference/api/entities"
)

// ToAPIModel converts from domain entity to API model
func ToAPIModel(entity *entities.LampEntity) Lamp {
	return Lamp{
		Id:        entity.ID,
		Status:    entity.Status,
		CreatedAt: entity.CreatedAt,
		UpdatedAt: entity.UpdatedAt,
	}
}

// ToEntity converts from API model to domain entity
func ToEntity(apiModel Lamp) *entities.LampEntity {
	return &entities.LampEntity{
		ID:        apiModel.Id,
		Status:    apiModel.Status,
		CreatedAt: apiModel.CreatedAt,
		UpdatedAt: apiModel.UpdatedAt,
	}
}

// CreateEntityFromAPICreate converts from API create model to domain entity
func CreateEntityFromAPICreate(createModel LampCreate) *entities.LampEntity {
	return entities.NewLampEntity(createModel.Status)
}

// UpdateEntityFromAPIUpdate creates a new domain entity with updated data from API update model
func UpdateEntityFromAPIUpdate(entity *entities.LampEntity, updateModel LampUpdate) *entities.LampEntity {
	// Create a new entity instead of modifying the original to avoid race conditions
	updatedEntity := &entities.LampEntity{
		ID:        entity.ID,
		Status:    updateModel.Status,
		CreatedAt: entity.CreatedAt,
		UpdatedAt: time.Now(),
	}

	return updatedEntity
}
