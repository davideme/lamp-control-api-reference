/**
 * Mapper to convert between domain entities and API models.
 * This separation allows the internal domain model to evolve independently
 * from the external API contract.
 */
export class LampMapper {
    /**
     * Convert from domain entity to API model
     */
    static toApiModel(entity) {
        return {
            id: entity.id,
            status: entity.status,
            createdAt: entity.createdAt,
            updatedAt: entity.updatedAt,
        };
    }
    /**
     * Convert from API model to domain entity
     */
    static toDomainEntity(apiModel) {
        return {
            id: apiModel.id,
            status: apiModel.status,
            createdAt: apiModel.createdAt,
            updatedAt: apiModel.updatedAt,
        };
    }
    /**
     * Convert from API create model to domain create model
     */
    static toDomainEntityCreate(apiModel) {
        return {
            status: apiModel.status,
        };
    }
    /**
     * Convert from API update model to domain update model
     */
    static toDomainEntityUpdate(apiModel) {
        return {
            status: apiModel.status,
        };
    }
}
