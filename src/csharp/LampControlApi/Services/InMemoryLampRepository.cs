using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using LampControlApi.Domain.Entities;
using LampControlApi.Domain.Repositories;

namespace LampControlApi.Services
{
    /// <summary>
    /// In-memory implementation of the lamp repository.
    /// Uses domain entities to maintain separation from API models.
    /// </summary>
    public class InMemoryLampRepository : ILampRepository
    {
#pragma warning disable SA1000 // KeywordsMustBeSpacedCorrectly
        private readonly ConcurrentDictionary<Guid, LampEntity> _lamps = new();
#pragma warning restore SA1000 // KeywordsMustBeSpacedCorrectly

        /// <summary>
        /// Initializes a new instance of the <see cref="InMemoryLampRepository"/> class.
        /// </summary>
        public InMemoryLampRepository()
        {
        }

        /// <inheritdoc/>
        public Task<ICollection<LampEntity>> GetAllAsync(CancellationToken cancellationToken = default)
        {
            cancellationToken.ThrowIfCancellationRequested();
            var lamps = _lamps.Values.ToList();
            return Task.FromResult<ICollection<LampEntity>>(lamps);
        }

        /// <inheritdoc/>
        public Task<ICollection<LampEntity>> ListAsync(int limit, int offset, CancellationToken cancellationToken = default)
        {
            if (limit < 0)
            {
                throw new ArgumentOutOfRangeException(nameof(limit), "Limit must be greater than or equal to 0.");
            }

            if (offset < 0)
            {
                throw new ArgumentOutOfRangeException(nameof(offset), "Offset must be greater than or equal to 0.");
            }

            cancellationToken.ThrowIfCancellationRequested();
            var page = _lamps.Values
                .OrderBy(l => l.CreatedAt)
                .ThenBy(l => l.Id)
                .Skip(offset)
                .Take(limit)
                .ToList();
            return Task.FromResult<ICollection<LampEntity>>(page);
        }

        /// <inheritdoc/>
        public Task<LampEntity?> GetByIdAsync(Guid id, CancellationToken cancellationToken = default)
        {
            cancellationToken.ThrowIfCancellationRequested();
            _lamps.TryGetValue(id, out var lamp);
            return Task.FromResult(lamp);
        }

        /// <inheritdoc/>
        public Task<LampEntity> CreateAsync(LampEntity entity, CancellationToken cancellationToken = default)
        {
            cancellationToken.ThrowIfCancellationRequested();

            if (entity == null)
            {
                throw new ArgumentNullException(nameof(entity));
            }

            // Simulate Postgres DEFAULT CURRENT_TIMESTAMP: populate timestamps when unset
            // (LampEntity.Create() leaves them as default until a DB write populates them).
            var now = DateTimeOffset.UtcNow;
            var stored = entity.CreatedAt == default(DateTimeOffset)
                ? new LampEntity(entity.Id, entity.Status, now, now)
                : entity;

            _lamps[stored.Id] = stored;
            return Task.FromResult(stored);
        }

        /// <inheritdoc/>
        public Task<LampEntity?> UpdateAsync(Guid id, bool status, CancellationToken cancellationToken = default)
        {
            cancellationToken.ThrowIfCancellationRequested();

            if (_lamps.TryGetValue(id, out var existing))
            {
                // Simulate Postgres BEFORE UPDATE trigger: bump UpdatedAt on every write.
                var updated = new LampEntity(existing.Id, status, existing.CreatedAt, DateTimeOffset.UtcNow);
                _lamps[id] = updated;
                return Task.FromResult<LampEntity?>(updated);
            }

            return Task.FromResult<LampEntity?>(null);
        }

        /// <inheritdoc/>
        public Task<bool> DeleteAsync(Guid id, CancellationToken cancellationToken = default)
        {
            cancellationToken.ThrowIfCancellationRequested();
            var removed = _lamps.TryRemove(id, out _);
            return Task.FromResult(removed);
        }
    }
}
