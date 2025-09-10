using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using LampControlApi.Domain.Entities;

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
        public Task<ICollection<LampEntity>> GetAllAsync()
        {
            var lamps = _lamps.Values.ToList();
            return Task.FromResult<ICollection<LampEntity>>(lamps);
        }

        /// <inheritdoc/>
        public Task<LampEntity?> GetByIdAsync(Guid id)
        {
            _lamps.TryGetValue(id, out var lamp);
            return Task.FromResult(lamp);
        }

        /// <inheritdoc/>
        public Task<LampEntity> CreateAsync(LampEntity entity)
        {
            if (entity == null)
            {
                throw new ArgumentNullException(nameof(entity));
            }

            _lamps[entity.Id] = entity;
            return Task.FromResult(entity);
        }

        /// <inheritdoc/>
        public Task<LampEntity?> UpdateAsync(LampEntity entity)
        {
            if (entity == null)
            {
                throw new ArgumentNullException(nameof(entity));
            }

            if (_lamps.ContainsKey(entity.Id))
            {
                _lamps[entity.Id] = entity;
                return Task.FromResult<LampEntity?>(entity);
            }

            return Task.FromResult<LampEntity?>(null);
        }

        /// <inheritdoc/>
        public Task<bool> DeleteAsync(Guid id)
        {
            var removed = _lamps.TryRemove(id, out _);
            return Task.FromResult(removed);
        }
    }
}
