using System;
using LampControlApi.Controllers;
using LampControlApi.Domain.Entities;

namespace LampControlApi.Domain.Mappers
{
    /// <summary>
    /// Mapper to convert between domain entities and API models.
    /// This separation allows the internal domain model to evolve independently
    /// from the external API contract.
    /// </summary>
    public static class LampMapper
    {
        /// <summary>
        /// Convert from domain entity to API model.
        /// </summary>
        /// <param name="entity">The domain entity to convert.</param>
        /// <returns>The API model representation.</returns>
        public static Lamp ToApiModel(LampEntity entity)
        {
            if (entity == null)
            {
                throw new ArgumentNullException(nameof(entity));
            }

            return new Lamp
            {
                Id = entity.Id,
                Status = entity.Status,
                CreatedAt = entity.CreatedAt,
                UpdatedAt = entity.UpdatedAt
            };
        }

        /// <summary>
        /// Convert from API model to domain entity.
        /// </summary>
        /// <param name="apiModel">The API model to convert.</param>
        /// <returns>The domain entity representation.</returns>
        public static LampEntity ToDomainEntity(Lamp apiModel)
        {
            if (apiModel == null)
            {
                throw new ArgumentNullException(nameof(apiModel));
            }

            return new LampEntity(
                apiModel.Id,
                apiModel.Status,
                apiModel.CreatedAt,
                apiModel.UpdatedAt);
        }

        /// <summary>
        /// Convert from API create model to domain entity.
        /// </summary>
        /// <param name="createModel">The API create model.</param>
        /// <returns>A new domain entity.</returns>
        public static LampEntity ToDomainEntityCreate(LampCreate createModel)
        {
            if (createModel == null)
            {
                throw new ArgumentNullException(nameof(createModel));
            }

            return LampEntity.Create(createModel.Status);
        }

        /// <summary>
        /// Update domain entity from API update model.
        /// </summary>
        /// <param name="entity">The existing domain entity.</param>
        /// <param name="updateModel">The API update model.</param>
        /// <returns>An updated domain entity.</returns>
        public static LampEntity UpdateDomainEntity(LampEntity entity, LampUpdate updateModel)
        {
            if (entity == null)
            {
                throw new ArgumentNullException(nameof(entity));
            }

            if (updateModel == null)
            {
                throw new ArgumentNullException(nameof(updateModel));
            }

            return entity.WithUpdatedStatus(updateModel.Status);
        }
    }
}
