using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using LampControlApi.Domain.Entities;

namespace LampControlApi.Domain.Repositories
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
        /// <param name="cancellationToken">Cancellation token.</param>
        /// <returns>Collection of all lamps.</returns>
        Task<ICollection<LampEntity>> GetAllAsync(CancellationToken cancellationToken = default);

        /// <summary>
        /// Get a lamp by ID.
        /// </summary>
        /// <param name="id">Lamp ID.</param>
        /// <param name="cancellationToken">Cancellation token.</param>
        /// <returns>Lamp if found, null otherwise.</returns>
        Task<LampEntity?> GetByIdAsync(Guid id, CancellationToken cancellationToken = default);

        /// <summary>
        /// Create a new lamp.
        /// </summary>
        /// <param name="entity">Lamp entity to create.</param>
        /// <param name="cancellationToken">Cancellation token.</param>
        /// <returns>Created lamp entity.</returns>
        Task<LampEntity> CreateAsync(LampEntity entity, CancellationToken cancellationToken = default);

        /// <summary>
        /// Update an existing lamp.
        /// </summary>
        /// <param name="entity">Lamp entity to update.</param>
        /// <param name="cancellationToken">Cancellation token.</param>
        /// <returns>Updated lamp entity if found, null otherwise.</returns>
        Task<LampEntity?> UpdateAsync(LampEntity entity, CancellationToken cancellationToken = default);

        /// <summary>
        /// Delete a lamp by ID.
        /// </summary>
        /// <param name="id">Lamp ID.</param>
        /// <param name="cancellationToken">Cancellation token.</param>
        /// <returns>True if deleted, false if not found.</returns>
        Task<bool> DeleteAsync(Guid id, CancellationToken cancellationToken = default);
    }
}
