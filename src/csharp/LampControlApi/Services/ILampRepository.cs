using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using LampControlApi.Domain.Entities;

namespace LampControlApi.Services
{
    /// <summary>
    /// Repository interface for lamp data operations.
    /// Works with domain entities to maintain separation from API models.
    /// </summary>
    public interface ILampRepository
    {
        /// <summary>
        /// Get all lamps.
        /// </summary>
        /// <returns>Collection of all lamps.</returns>
        Task<ICollection<LampEntity>> GetAllAsync();

        /// <summary>
        /// Get a lamp by ID.
        /// </summary>
        /// <param name="id">Lamp ID.</param>
        /// <returns>Lamp if found, null otherwise.</returns>
        Task<LampEntity?> GetByIdAsync(Guid id);

        /// <summary>
        /// Create a new lamp.
        /// </summary>
        /// <param name="entity">Lamp entity to create.</param>
        /// <returns>Created lamp entity.</returns>
        Task<LampEntity> CreateAsync(LampEntity entity);

        /// <summary>
        /// Update an existing lamp.
        /// </summary>
        /// <param name="entity">Lamp entity to update.</param>
        /// <returns>Updated lamp entity if found, null otherwise.</returns>
        Task<LampEntity?> UpdateAsync(LampEntity entity);

        /// <summary>
        /// Delete a lamp by ID.
        /// </summary>
        /// <param name="id">Lamp ID.</param>
        /// <returns>True if deleted, false if not found.</returns>
        Task<bool> DeleteAsync(Guid id);
    }
}
