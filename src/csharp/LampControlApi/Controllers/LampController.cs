using LampControlApi.Services;
using Microsoft.AspNetCore.Mvc;

namespace LampControlApi.Controllers
{
    /// <summary>
    /// Custom lamp controller with proper HTTP status codes.
    /// </summary>
    [ApiController]
    [Route("v1")]
    public class LampController : ControllerBase
    {
        private readonly IController _implementation;

        /// <summary>
        /// Initializes a new instance of the <see cref="LampController"/> class.
        /// </summary>
        /// <param name="implementation">The controller implementation.</param>
        public LampController(IController implementation)
        {
            _implementation = implementation;
        }

        /// <summary>
        /// List all lamps.
        /// </summary>
        /// <returns>A list of lamps.</returns>
        [HttpGet("lamps")]
        public async Task<ActionResult<ICollection<Lamp>>> ListLamps()
        {
            var lamps = await _implementation.ListLampsAsync();
            return Ok(lamps);
        }

        /// <summary>
        /// Create a new lamp.
        /// </summary>
        /// <param name="body">The lamp creation data.</param>
        /// <returns>The created lamp.</returns>
        [HttpPost("lamps")]
        public async Task<ActionResult<Lamp>> CreateLamp([FromBody] LampCreate body)
        {
            var lamp = await _implementation.CreateLampAsync(body);
            return CreatedAtAction(nameof(GetLamp), new { lampId = lamp.Id }, lamp);
        }

        /// <summary>
        /// Get a specific lamp.
        /// </summary>
        /// <param name="lampId">The lamp ID.</param>
        /// <returns>The lamp details.</returns>
        [HttpGet("lamps/{lampId}")]
        public async Task<ActionResult<Lamp>> GetLamp(string lampId)
        {
            try
            {
                var lamp = await _implementation.GetLampAsync(lampId);
                return Ok(lamp);
            }
            catch (ArgumentException)
            {
                // Invalid GUID format should be treated as not found per OpenAPI spec
                return NotFound();
            }
            catch (KeyNotFoundException)
            {
                return NotFound();
            }
        }

        /// <summary>
        /// Update a lamp's status.
        /// </summary>
        /// <param name="lampId">The lamp ID.</param>
        /// <param name="body">The lamp update data.</param>
        /// <returns>The updated lamp.</returns>
        [HttpPut("lamps/{lampId}")]
        public async Task<ActionResult<Lamp>> UpdateLamp(string lampId, [FromBody] LampUpdate body)
        {
            try
            {
                var lamp = await _implementation.UpdateLampAsync(lampId, body);
                return Ok(lamp);
            }
            catch (ArgumentException)
            {
                // Invalid GUID format should be treated as not found per OpenAPI spec
                return NotFound();
            }
            catch (KeyNotFoundException)
            {
                return NotFound();
            }
        }

        /// <summary>
        /// Delete a lamp.
        /// </summary>
        /// <param name="lampId">The lamp ID.</param>
        /// <returns>No content on successful deletion.</returns>
        [HttpDelete("lamps/{lampId}")]
        public async Task<ActionResult> DeleteLamp(string lampId)
        {
            try
            {
                await _implementation.DeleteLampAsync(lampId);
                return NoContent();
            }
            catch (ArgumentException)
            {
                // Invalid GUID format should be treated as not found per OpenAPI spec
                return NotFound();
            }
            catch (KeyNotFoundException)
            {
                return NotFound();
            }
        }
    }
}
