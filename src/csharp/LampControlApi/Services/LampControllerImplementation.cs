using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using LampControlApi.Controllers;
using LampControlApi.Domain.Mappers;
using LampControlApi.Domain.Repositories;
using Microsoft.AspNetCore.Mvc;

namespace LampControlApi.Services
{
    /// <summary>
    /// Implementation of the IController interface.
    /// Uses mappers to maintain separation between domain entities and API models.
    /// </summary>
    public class LampControllerImplementation : IController
    {
        private const int DefaultPageSize = 25;
        private const int MaxPageSize = 1000;

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
        public async Task<ActionResult<Response>> ListLampsAsync(string cursor, int pageSize)
        {
            // Normalize pageSize: default when missing/invalid, cap to prevent overflow on +1.
            if (pageSize <= 0)
            {
                pageSize = DefaultPageSize;
            }

            pageSize = Math.Min(pageSize, MaxPageSize);

            // Interpret cursor as a starting offset. If parsing fails, start at 0.
            var offset = 0;
            if (!string.IsNullOrWhiteSpace(cursor) && int.TryParse(cursor, out var parsed))
            {
                offset = Math.Max(0, parsed);
            }

            // Fetch one extra row to determine whether a next page exists,
            // avoiding a separate COUNT(*) query. pageSize <= MaxPageSize, so +1 is safe.
            var entities = await _lampRepository.ListAsync(pageSize + 1, offset);

            var hasMore = entities.Count > pageSize;
            var page = entities.Take(pageSize).Select(LampMapper.ToApiModel).ToList();
            var nextCursor = hasMore ? (offset + pageSize).ToString() : string.Empty;

            var response = new Response
            {
                Data = page,
                HasMore = hasMore,
                NextCursor = nextCursor
            };

            return new ActionResult<Response>(response);
        }

        /// <summary>
        /// Backwards-compatible parameterless ListLampsAsync returning all lamps.
        /// </summary>
        /// <returns>All lamps from the repository.</returns>
        public async Task<ICollection<Lamp>> ListLampsAsync()
        {
            // Fetch all lamps with the same deterministic ordering as the paginated endpoint.
            var entities = await _lampRepository.ListAsync(int.MaxValue, 0);
            return entities.Select(LampMapper.ToApiModel).ToList();
        }

        /// <summary>
        /// Create a new lamp.
        /// </summary>
        /// <param name="body">The lamp to create.</param>
        /// <returns>Lamp created successfully.</returns>
        public async Task<ActionResult<Lamp>> CreateLampAsync(LampCreate body)
        {
            if (body == null)
            {
                throw new ArgumentNullException(nameof(body));
            }

            var entity = LampMapper.ToDomainEntityCreate(body);
            var created = await _lampRepository.CreateAsync(entity);
            var apiModel = LampMapper.ToApiModel(created);

            // Return 201 Created with a Location header pointing to the GetLamp route.
            // Use CreatedAtAction semantics so the generated controller's URL helper can
            // resolve the action name and produce the correct Location value.
            return new CreatedAtActionResult("GetLamp", null, new { lampId = created.Id }, apiModel);
        }

        /// <summary>
        /// Get a specific lamp.
        /// </summary>
        /// <param name="lampId">Identifier of the lamp to fetch.</param>
        /// <returns>Lamp details.</returns>
        public async Task<ActionResult<Lamp>> GetLampAsync(string lampId)
        {
            if (string.IsNullOrWhiteSpace(lampId))
            {
                throw new ArgumentException("Lamp ID cannot be null or empty.", nameof(lampId));
            }

            if (!Guid.TryParse(lampId, out var id))
            {
                throw new ArgumentException("Invalid lamp ID format.", nameof(lampId));
            }

            var entity = await _lampRepository.GetByIdAsync(id);
            if (entity == null)
            {
                throw new KeyNotFoundException($"Lamp with ID {lampId} not found.");
            }

            var apiModel = LampMapper.ToApiModel(entity);
            return new ActionResult<Lamp>(apiModel);
        }

        /// <summary>
        /// Update a lamp's status.
        /// </summary>
        /// <param name="lampId">Identifier of the lamp to update.</param>
        /// <param name="body">Updated lamp fields.</param>
        /// <returns>Lamp updated successfully.</returns>
        public async Task<ActionResult<Lamp>> UpdateLampAsync(string lampId, LampUpdate body)
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

            var updated = await _lampRepository.UpdateAsync(id, body.Status);
            if (updated == null)
            {
                throw new KeyNotFoundException($"Lamp with ID {lampId} not found.");
            }

            var apiModel = LampMapper.ToApiModel(updated);

            return new ActionResult<Lamp>(apiModel);
        }

        /// <summary>
        /// Delete a lamp.
        /// </summary>
        /// <param name="lampId">Identifier of the lamp to delete.</param>
        /// <returns>Task representing the delete operation.</returns>
        public async Task<Microsoft.AspNetCore.Mvc.IActionResult> DeleteLampAsync(string lampId)
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

            // Return HTTP 204 No Content on successful delete.
            return new NoContentResult();
        }
    }
}
