using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using LampControlApi.Domain.Entities;
using LampControlApi.Domain.Repositories;
using LampControlApi.Infrastructure.Database;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;

namespace LampControlApi.Services
{
    /// <summary>
    /// PostgreSQL implementation of the lamp repository using Entity Framework Core.
    /// </summary>
    public class PostgresLampRepository : ILampRepository
    {
        private readonly LampControlDbContext context;
        private readonly ILogger<PostgresLampRepository> logger;

        /// <summary>
        /// Initializes a new instance of the <see cref="PostgresLampRepository"/> class.
        /// </summary>
        /// <param name="context">The database context.</param>
        /// <param name="logger">The logger.</param>
        public PostgresLampRepository(
            LampControlDbContext context,
            ILogger<PostgresLampRepository> logger)
        {
            this.context = context ?? throw new ArgumentNullException(nameof(context));
            this.logger = logger ?? throw new ArgumentNullException(nameof(logger));
        }

        /// <inheritdoc/>
        public async Task<ICollection<LampEntity>> GetAllAsync(CancellationToken cancellationToken = default)
        {
            this.logger.LogDebug("Getting all lamps from PostgreSQL database");

            var dbEntities = await this.context.Lamps
                .OrderBy(l => l.CreatedAt)
                .AsNoTracking()
                .ToListAsync(cancellationToken);

            return dbEntities.Select(this.MapToDomain).ToList();
        }

        /// <inheritdoc/>
        public async Task<LampEntity?> GetByIdAsync(Guid id, CancellationToken cancellationToken = default)
        {
            this.logger.LogDebug("Getting lamp {LampId} from PostgreSQL database", id);

            var dbEntity = await this.context.Lamps
                .AsNoTracking()
                .FirstOrDefaultAsync(l => l.Id == id, cancellationToken);

            return dbEntity != null ? this.MapToDomain(dbEntity) : null;
        }

        /// <inheritdoc/>
        public async Task<LampEntity> CreateAsync(LampEntity entity, CancellationToken cancellationToken = default)
        {
            if (entity == null)
            {
                throw new ArgumentNullException(nameof(entity));
            }

            this.logger.LogDebug("Creating lamp {LampId} in PostgreSQL database", entity.Id);

            var dbEntity = new LampDbEntity
            {
                Id = entity.Id,
                IsOn = entity.Status,
                DeletedAt = null,
            };

            this.context.Lamps.Add(dbEntity);
            await this.context.SaveChangesAsync(cancellationToken);
            await this.context.Entry(dbEntity).ReloadAsync(cancellationToken);

            return this.MapToDomain(dbEntity);
        }

        /// <inheritdoc/>
        public async Task<LampEntity?> UpdateAsync(LampEntity entity, CancellationToken cancellationToken = default)
        {
            if (entity == null)
            {
                throw new ArgumentNullException(nameof(entity));
            }

            this.logger.LogDebug("Updating lamp {LampId} in PostgreSQL database", entity.Id);

            var existingEntity = await this.context.Lamps
                .FirstOrDefaultAsync(l => l.Id == entity.Id, cancellationToken);

            if (existingEntity == null)
            {
                this.logger.LogDebug("Lamp {LampId} not found for update", entity.Id);
                return null;
            }

            // Create updated entity with init setters using with-expression
            var updatedEntity = existingEntity with
            {
                IsOn = entity.Status,
            };

            // Update the tracked entity reference
            this.context.Entry(existingEntity).CurrentValues.SetValues(updatedEntity);

            await this.context.SaveChangesAsync(cancellationToken);

            // Reload to get the trigger-updated timestamp
            await this.context.Entry(existingEntity).ReloadAsync(cancellationToken);

            return this.MapToDomain(existingEntity);
        }

        /// <inheritdoc/>
        public async Task<bool> DeleteAsync(Guid id, CancellationToken cancellationToken = default)
        {
            this.logger.LogDebug("Deleting lamp {LampId} from PostgreSQL database", id);

            var existingEntity = await this.context.Lamps
                .IgnoreQueryFilters()
                .FirstOrDefaultAsync(l => l.Id == id && l.DeletedAt == null, cancellationToken);

            if (existingEntity == null)
            {
                this.logger.LogDebug("Lamp {LampId} not found for deletion", id);
                return false;
            }

            // Soft delete by setting DeletedAt timestamp
            var deletedEntity = existingEntity with
            {
                DeletedAt = DateTimeOffset.UtcNow,
            };

            // Update the tracked entity reference
            this.context.Entry(existingEntity).CurrentValues.SetValues(deletedEntity);

            await this.context.SaveChangesAsync(cancellationToken);

            return true;
        }

        /// <summary>
        /// Maps a database entity to a domain entity.
        /// </summary>
        /// <param name="dbEntity">The database entity.</param>
        /// <returns>The domain entity.</returns>
        private LampEntity MapToDomain(LampDbEntity dbEntity)
        {
            return new LampEntity(
                id: dbEntity.Id,
                status: dbEntity.IsOn,
                createdAt: dbEntity.CreatedAt,
                updatedAt: dbEntity.UpdatedAt);
        }
    }
}
