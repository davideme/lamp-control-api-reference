using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
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
        /// List all lamps with pagination.
        /// </summary>
        /// <param name="cursor">Opaque cursor representing the starting index (stringified int).</param>
        /// <param name="pageSize">Number of items to return.</param>
        /// <returns>A paginated response containing lamps.</returns>
        public async Task<Response> ListLampsAsync(string cursor, int pageSize)
        {
            // Normalize pageSize
            if (pageSize <= 0)
            {
                pageSize = 25;
            }

            // Interpret cursor as a starting index. If parsing fails, start at 0.
            var start = 0;
            if (!string.IsNullOrWhiteSpace(cursor) && int.TryParse(cursor, out var parsed))
            {
                start = Math.Max(0, parsed);
            }

            var all = (await _lampRepository.GetAllAsync())
                .OrderByDescending(l => l.UpdatedAt)
                .ThenByDescending(l => l.Id)
                .ToList();

            var page = all.Skip(start).Take(pageSize).ToList();
            var hasMore = start + pageSize < all.Count;
            var nextCursor = hasMore ? (start + pageSize).ToString() : null;

            return new Response
            {
                Data = page,
                HasMore = hasMore,
                NextCursor = nextCursor
            };
        }

        /// <summary>
        /// Backwards-compatible parameterless ListLampsAsync returning all lamps.
        /// </summary>
        /// <returns>All lamps from the repository.</returns>
        public async Task<ICollection<Lamp>> ListLampsAsync()
        {
            // Call the paginated implementation with defaults and return the data list.
            var response = await ListLampsAsync(null, int.MaxValue);
            return response.Data;
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
