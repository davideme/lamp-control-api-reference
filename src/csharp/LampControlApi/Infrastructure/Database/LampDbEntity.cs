using System;

namespace LampControlApi.Infrastructure.Database
{
    /// <summary>
    /// Database entity for the lamp table.
    /// Uses init setters for EF Core compatibility while maintaining immutability after construction.
    /// </summary>
    public sealed record LampDbEntity
    {
        /// <summary>
        /// Gets or initializes the unique identifier for the lamp.
        /// </summary>
        public Guid Id { get; init; }

        /// <summary>
        /// Gets or initializes a value indicating whether the lamp is turned on (true) or off (false).
        /// </summary>
        public bool IsOn { get; init; }

        /// <summary>
        /// Gets or initializes the timestamp when the lamp was created.
        /// </summary>
        public DateTimeOffset CreatedAt { get; init; }

        /// <summary>
        /// Gets or initializes the timestamp when the lamp was last updated.
        /// </summary>
        public DateTimeOffset UpdatedAt { get; init; }

        /// <summary>
        /// Gets or initializes the timestamp when the lamp was soft deleted, or null if active.
        /// </summary>
        public DateTimeOffset? DeletedAt { get; init; }
    }
}
