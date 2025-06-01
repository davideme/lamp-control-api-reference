using LampControlApi.Controllers;

namespace LampControlApi.Services
{
    /// <summary>
    /// Repository interface for lamp data operations
    /// </summary>
    public interface ILampRepository
    {
        /// <summary>
        /// Get all lamps
        /// </summary>
        /// <returns>Collection of all lamps</returns>
        Task<ICollection<Lamp>> GetAllAsync();

        /// <summary>
        /// Get a lamp by ID
        /// </summary>
        /// <param name="id">Lamp ID</param>
        /// <returns>Lamp if found, null otherwise</returns>
        Task<Lamp?> GetByIdAsync(Guid id);

        /// <summary>
        /// Create a new lamp
        /// </summary>
        /// <param name="lamp">Lamp to create</param>
        /// <returns>Created lamp</returns>
        Task<Lamp> CreateAsync(Lamp lamp);

        /// <summary>
        /// Update an existing lamp
        /// </summary>
        /// <param name="lamp">Lamp to update</param>
        /// <returns>Updated lamp if found, null otherwise</returns>
        Task<Lamp?> UpdateAsync(Lamp lamp);

        /// <summary>
        /// Delete a lamp by ID
        /// </summary>
        /// <param name="id">Lamp ID</param>
        /// <returns>True if deleted, false if not found</returns>
        Task<bool> DeleteAsync(Guid id);
    }
}
