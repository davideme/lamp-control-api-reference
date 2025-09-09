using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using LampControlApi.Controllers;
using LampControlApi.Domain.Mappers;
using Microsoft.AspNetCore.Mvc;

namespace LampControlApi.Services
{
    /// <summary>
    /// Implementation of the IController interface.
    /// Uses mappers to maintain separation between domain entities and API models.
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
        public async Task<ActionResult<Response>> ListLampsAsync(string cursor, int pageSize)
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

            var entities = await _lampRepository.GetAllAsync();
            var lamps = entities.Select(LampMapper.ToApiModel)
                .OrderByDescending(l => l.UpdatedAt)
                .ThenByDescending(l => l.Id)
                .ToList();

            var page = lamps.Skip(start).Take(pageSize).ToList();
            var hasMore = start + pageSize < lamps.Count;
            var nextCursor = hasMore ? (start + pageSize).ToString() : string.Empty;

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
            // Call the paginated implementation with defaults and return the data list.
            var response = await ListLampsAsync(string.Empty, int.MaxValue);

            // If the paginated overload returns an ActionResult, unwrap the value.
            if (response is ActionResult<Response> ar && ar.Value != null)
            {
                return ar.Value.Data;
            }

            // Fallback: return empty list if something unexpected happens.
            return new List<Lamp>();
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

            var existingEntity = await _lampRepository.GetByIdAsync(id);
            if (existingEntity == null)
            {
                throw new KeyNotFoundException($"Lamp with ID {lampId} not found.");
            }

            var updatedEntity = LampMapper.UpdateDomainEntity(existingEntity, body);
            var updated = await _lampRepository.UpdateAsync(updatedEntity);
            var apiModel = LampMapper.ToApiModel(updated!); // We know it exists since we just checked.

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
