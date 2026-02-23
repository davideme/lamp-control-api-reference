using System;

namespace LampControlApi.Infrastructure.Database
{
    /// <summary>
    /// Database entity for the lamp table.
    /// Uses set accessors for EF Core compatibility to allow database-generated values.
    /// </summary>
    public sealed record LampDbEntity
    {
        /// <summary>
        /// Gets or sets the unique identifier for the lamp.
        /// </summary>
        public Guid Id { get; set; }

        /// <summary>
        /// Gets or sets a value indicating whether the lamp is turned on (true) or off (false).
        /// </summary>
        public bool IsOn { get; set; }

        /// <summary>
        /// Gets or sets the timestamp when the lamp was created.
        /// Set by the application on insert.
        /// </summary>
        public DateTimeOffset CreatedAt { get; set; }

        /// <summary>
        /// Gets or sets the timestamp when the lamp was last updated.
        /// Set by the application on insert and update.
        /// </summary>
        public DateTimeOffset UpdatedAt { get; set; }

        /// <summary>
        /// Gets or sets the timestamp when the lamp was soft deleted, or null if active.
        /// </summary>
        public DateTimeOffset? DeletedAt { get; set; }
    }
}
