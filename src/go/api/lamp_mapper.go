package api

import (
	"github.com/davideme/lamp-control-api-reference/api/entities"
	"github.com/google/uuid"
	openapi_types "github.com/oapi-codegen/runtime/types"
)

// LampMapper handles conversion between domain entities and API models.
// This separation allows the internal domain model to evolve independently
// from the external API contract.
type LampMapper struct{}

// NewLampMapper creates a new instance of LampMapper
func NewLampMapper() *LampMapper {
	return &LampMapper{}
}

// ToAPIModel converts from domain entity to API model
func (m *LampMapper) ToAPIModel(entity *entities.LampEntity) Lamp {
	return Lamp{
		Id:        openapi_types.UUID(entity.ID),
		Status:    entity.Status,
		CreatedAt: entity.CreatedAt,
		UpdatedAt: entity.UpdatedAt,
	}
}

// ToEntity converts from API model to domain entity
func (m *LampMapper) ToEntity(apiModel Lamp) *entities.LampEntity {
	return &entities.LampEntity{
		ID:        uuid.UUID(apiModel.Id),
		Status:    apiModel.Status,
		CreatedAt: apiModel.CreatedAt,
		UpdatedAt: apiModel.UpdatedAt,
	}
}

// CreateEntityFromAPICreate converts from API create model to domain entity
func (m *LampMapper) CreateEntityFromAPICreate(createModel LampCreate) *entities.LampEntity {
	return entities.NewLampEntity(createModel.Status)
}

// UpdateEntityFromAPIUpdate updates a domain entity with data from API update model
func (m *LampMapper) UpdateEntityFromAPIUpdate(entity *entities.LampEntity, updateModel LampUpdate) *entities.LampEntity {
	entity.UpdateStatus(updateModel.Status)
	return entity
}