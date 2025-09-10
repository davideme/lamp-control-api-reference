using System;

namespace LampControlApi.Domain.Entities
{
    /// <summary>
    /// Domain entity representing a Lamp in the system.
    /// This entity is independent of the API model and represents the core business object.
    /// </summary>
    public sealed class LampEntity
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="LampEntity"/> class.
        /// </summary>
        /// <param name="id">The unique identifier for the lamp.</param>
        /// <param name="status">The lamp status (on/off).</param>
        /// <param name="createdAt">When the lamp was created.</param>
        /// <param name="updatedAt">When the lamp was last updated.</param>
        public LampEntity(Guid id, bool status, DateTimeOffset createdAt, DateTimeOffset updatedAt)
        {
            Id = id;
            Status = status;
            CreatedAt = createdAt;
            UpdatedAt = updatedAt;
        }

        /// <summary>
        /// Gets the unique identifier for the lamp.
        /// </summary>
        public Guid Id { get; }

        /// <summary>
        /// Gets a value indicating whether the lamp is turned on (true) or off (false).
        /// </summary>
        public bool Status { get; }

        /// <summary>
        /// Gets the timestamp when the lamp was created.
        /// </summary>
        public DateTimeOffset CreatedAt { get; }

        /// <summary>
        /// Gets the timestamp when the lamp was last updated.
        /// </summary>
        public DateTimeOffset UpdatedAt { get; }

        /// <summary>
        /// Creates a new lamp entity with a generated ID and current timestamps.
        /// </summary>
        /// <param name="status">The initial status of the lamp.</param>
        /// <returns>A new lamp entity.</returns>
        public static LampEntity Create(bool status)
        {
            var now = DateTimeOffset.UtcNow;
            return new LampEntity(Guid.NewGuid(), status, now, now);
        }

        /// <summary>
        /// Creates an updated copy of this entity with a new status and updated timestamp.
        /// </summary>
        /// <param name="newStatus">The new status to set.</param>
        /// <returns>An updated copy of this entity.</returns>
        public LampEntity WithUpdatedStatus(bool newStatus)
        {
            return new LampEntity(Id, newStatus, CreatedAt, DateTimeOffset.UtcNow);
        }

        /// <summary>
        /// Determines whether the specified object is equal to this entity.
        /// </summary>
        /// <param name="obj">The object to compare with this entity.</param>
        /// <returns>true if the objects are equal; otherwise, false.</returns>
        public override bool Equals(object? obj)
        {
            return obj is LampEntity other && Id.Equals(other.Id);
        }

        /// <summary>
        /// Returns the hash code for this entity.
        /// </summary>
        /// <returns>A hash code for this entity.</returns>
        public override int GetHashCode()
        {
            return Id.GetHashCode();
        }

        /// <summary>
        /// Returns a string representation of this entity.
        /// </summary>
        /// <returns>A string representation of this entity.</returns>
        public override string ToString()
        {
            return $"LampEntity(Id={Id}, Status={Status}, CreatedAt={CreatedAt}, UpdatedAt={UpdatedAt})";
        }
    }
}
