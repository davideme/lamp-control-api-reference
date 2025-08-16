using LampControlApi.Controllers;

namespace LampControlApi.Services
{
    /// <summary>
    /// Implementation of the IController interface.
    /// </summary>
    public class LampControllerImplementation : IController
    {
        private readonly ILampRepository _lampRepository;

        /// <summary>
        /// Initializes a new instance of the <see cref="LampControllerImplementation"/> class.
        /// </summary>
        /// <param name="lampRepository">The lamp repository.</param>
        public LampControllerImplementation(ILampRepository lampRepository)
        {
            _lampRepository = lampRepository ?? throw new ArgumentNullException(nameof(lampRepository));
        }

        /// <summary>
        /// List all lamps.
        /// </summary>
        /// <returns>A list of lamps.</returns>
        public async Task<ICollection<Lamp>> ListLampsAsync()
        {
            return await _lampRepository.GetAllAsync();
        }

        /// <summary>
        /// Create a new lamp.
        /// </summary>
        /// <param name="body">The lamp to create.</param>
        /// <returns>Lamp created successfully.</returns>
        public async Task<Lamp> CreateLampAsync(LampCreate body)
        {
            if (body == null)
            {
                throw new ArgumentNullException(nameof(body));
            }

            var lamp = new Lamp
            {
                Id = Guid.NewGuid(),
                Status = body.Status
            };

            return await _lampRepository.CreateAsync(lamp);
        }

        /// <summary>
        /// Get a specific lamp.
        /// </summary>
        /// <param name="lampId">Identifier of the lamp to fetch.</param>
        /// <returns>Lamp details.</returns>
        public async Task<Lamp> GetLampAsync(string lampId)
        {
            if (string.IsNullOrWhiteSpace(lampId))
            {
                throw new ArgumentException("Lamp ID cannot be null or empty.", nameof(lampId));
            }

            if (!Guid.TryParse(lampId, out var id))
            {
                throw new ArgumentException("Invalid lamp ID format.", nameof(lampId));
            }

            var lamp = await _lampRepository.GetByIdAsync(id);
            if (lamp == null)
            {
                throw new KeyNotFoundException($"Lamp with ID {lampId} not found.");
            }

            return lamp;
        }

        /// <summary>
        /// Update a lamp's status.
        /// </summary>
        /// <param name="lampId">Identifier of the lamp to update.</param>
        /// <param name="body">Updated lamp fields.</param>
        /// <returns>Lamp updated successfully.</returns>
        public async Task<Lamp> UpdateLampAsync(string lampId, LampUpdate body)
        {
            if (string.IsNullOrWhiteSpace(lampId))
            {
                throw new ArgumentException("Lamp ID cannot be null or empty.", nameof(lampId));
            }

            if (body == null)
            {
                throw new ArgumentNullException(nameof(body));
            }

            if (!Guid.TryParse(lampId, out var id))
            {
                throw new ArgumentException("Invalid lamp ID format.", nameof(lampId));
            }

            var existingLamp = await _lampRepository.GetByIdAsync(id);
            if (existingLamp == null)
            {
                throw new KeyNotFoundException($"Lamp with ID {lampId} not found.");
            }

            existingLamp.Status = body.Status;
            var updatedLamp = await _lampRepository.UpdateAsync(existingLamp);

            return updatedLamp!; // We know it exists since we just checked.
        }

        /// <summary>
        /// Delete a lamp.
        /// </summary>
        /// <param name="lampId">Identifier of the lamp to delete.</param>
        /// <returns>Task representing the delete operation.</returns>
        public async Task DeleteLampAsync(string lampId)
        {
            if (string.IsNullOrWhiteSpace(lampId))
            {
                throw new ArgumentException("Lamp ID cannot be null or empty.", nameof(lampId));
            }

            if (!Guid.TryParse(lampId, out var id))
            {
                throw new ArgumentException("Invalid lamp ID format.", nameof(lampId));
            }

            var deleted = await _lampRepository.DeleteAsync(id);
            if (!deleted)
            {
                throw new KeyNotFoundException($"Lamp with ID {lampId} not found");
            }
        }
    }
}
