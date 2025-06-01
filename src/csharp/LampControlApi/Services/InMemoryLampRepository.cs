using System.Collections.Concurrent;
using LampControlApi.Controllers;

namespace LampControlApi.Services
{
    /// <summary>
    /// In-memory implementation of the lamp repository
    /// </summary>
    public class InMemoryLampRepository : ILampRepository
    {
        private readonly ConcurrentDictionary<Guid, Lamp> _lamps = new();

        /// <summary>
        /// Initializes a new instance of the <see cref="InMemoryLampRepository"/> class.
        /// </summary>
        public InMemoryLampRepository()
        {
            // Seed with some initial data
            SeedData();
        }

        /// <inheritdoc/>
        public Task<ICollection<Lamp>> GetAllAsync()
        {
            var lamps = _lamps.Values.ToList();
            return Task.FromResult<ICollection<Lamp>>(lamps);
        }

        /// <inheritdoc/>
        public Task<Lamp?> GetByIdAsync(Guid id)
        {
            _lamps.TryGetValue(id, out var lamp);
            return Task.FromResult(lamp);
        }

        /// <inheritdoc/>
        public Task<Lamp> CreateAsync(Lamp lamp)
        {
            if (lamp.Id == Guid.Empty)
            {
                lamp.Id = Guid.NewGuid();
            }

            _lamps[lamp.Id] = lamp;
            return Task.FromResult(lamp);
        }

        /// <inheritdoc/>
        public Task<Lamp?> UpdateAsync(Lamp lamp)
        {
            if (_lamps.ContainsKey(lamp.Id))
            {
                _lamps[lamp.Id] = lamp;
                return Task.FromResult<Lamp?>(lamp);
            }

            return Task.FromResult<Lamp?>(null);
        }

        /// <inheritdoc/>
        public Task<bool> DeleteAsync(Guid id)
        {
            var removed = _lamps.TryRemove(id, out _);
            return Task.FromResult(removed);
        }

        /// <summary>
        /// Seeds the repository with initial data
        /// </summary>
        private void SeedData()
        {
            var lamp1 = new Lamp
            {
                Id = Guid.Parse("123e4567-e89b-12d3-a456-426614174000"),
                Status = true
            };

            var lamp2 = new Lamp
            {
                Id = Guid.Parse("123e4567-e89b-12d3-a456-426614174001"),
                Status = false
            };

            var lamp3 = new Lamp
            {
                Id = Guid.Parse("123e4567-e89b-12d3-a456-426614174002"),
                Status = true
            };

            _lamps[lamp1.Id] = lamp1;
            _lamps[lamp2.Id] = lamp2;
            _lamps[lamp3.Id] = lamp3;
        }
    }
}
