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
    public partial class PostgresLampRepository : ILampRepository
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
            LogGettingAllLamps(this.logger);

            var dbEntities = await this.context.Lamps
                .OrderBy(l => l.CreatedAt)
                .AsNoTracking()
                .ToListAsync(cancellationToken);

            return dbEntities.Select(this.MapToDomain).ToList();
        }

        /// <inheritdoc/>
        public async Task<ICollection<LampEntity>> ListAsync(int limit, int offset, CancellationToken cancellationToken = default)
        {
            if (limit < 0)
            {
                throw new ArgumentOutOfRangeException(nameof(limit), "Limit must be greater than or equal to 0.");
            }

            if (offset < 0)
            {
                throw new ArgumentOutOfRangeException(nameof(offset), "Offset must be greater than or equal to 0.");
            }

            LogListingLamps(this.logger, limit, offset);

            var dbEntities = await this.context.Lamps
                .OrderBy(l => l.CreatedAt)
                .ThenBy(l => l.Id)
                .Skip(offset)
                .Take(limit)
                .AsNoTracking()
                .ToListAsync(cancellationToken);

            return dbEntities.Select(this.MapToDomain).ToList();
        }

        /// <inheritdoc/>
        public async Task<LampEntity?> GetByIdAsync(Guid id, CancellationToken cancellationToken = default)
        {
            LogGettingLampById(this.logger, id);

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

            LogCreatingLamp(this.logger, entity.Id);

            // created_at and updated_at are omitted — set by DB DEFAULT CURRENT_TIMESTAMP
            // and read back via RETURNING (Npgsql handles this automatically)
            var dbEntity = new LampDbEntity
            {
                Id = entity.Id,
                IsOn = entity.Status,
                DeletedAt = null,
            };

            this.context.Lamps.Add(dbEntity);
            await this.context.SaveChangesAsync(cancellationToken);

            return this.MapToDomain(dbEntity);
        }

        /// <inheritdoc/>
        public async Task<LampEntity?> UpdateAsync(Guid id, bool status, CancellationToken cancellationToken = default)
        {
            LogUpdatingLamp(this.logger, id);

            var existingEntity = await this.context.Lamps
                .FirstOrDefaultAsync(l => l.Id == id, cancellationToken);

            if (existingEntity == null)
            {
                LogLampNotFoundForUpdate(this.logger, id);
                return null;
            }

            // updated_at is set by the DB BEFORE UPDATE trigger; only IsOn changes here
            var updatedEntity = existingEntity with
            {
                IsOn = status,
            };

            // Update the tracked entity reference
            this.context.Entry(existingEntity).CurrentValues.SetValues(updatedEntity);

            await this.context.SaveChangesAsync(cancellationToken);

            return this.MapToDomain(existingEntity);
        }

        /// <inheritdoc/>
        public async Task<bool> DeleteAsync(Guid id, CancellationToken cancellationToken = default)
        {
            LogDeletingLamp(this.logger, id);

            var existingEntity = await this.context.Lamps
                .IgnoreQueryFilters()
                .FirstOrDefaultAsync(l => l.Id == id && l.DeletedAt == null, cancellationToken);

            if (existingEntity == null)
            {
                LogLampNotFoundForDeletion(this.logger, id);
                return false;
            }

            // Soft delete — updated_at is set by the DB BEFORE UPDATE trigger
            var deletedEntity = existingEntity with
            {
                DeletedAt = DateTimeOffset.UtcNow,
            };

            // Update the tracked entity reference
            this.context.Entry(existingEntity).CurrentValues.SetValues(deletedEntity);

            await this.context.SaveChangesAsync(cancellationToken);

            return true;
        }

        [LoggerMessage(Level = LogLevel.Debug, Message = "Getting all lamps from PostgreSQL database")]
        private static partial void LogGettingAllLamps(ILogger logger);

        [LoggerMessage(Level = LogLevel.Debug, Message = "Listing lamps from PostgreSQL database (limit: {Limit}, offset: {Offset})")]
        private static partial void LogListingLamps(ILogger logger, int limit, int offset);

        [LoggerMessage(Level = LogLevel.Debug, Message = "Getting lamp {LampId} from PostgreSQL database")]
        private static partial void LogGettingLampById(ILogger logger, Guid lampId);

        [LoggerMessage(Level = LogLevel.Debug, Message = "Creating lamp {LampId} in PostgreSQL database")]
        private static partial void LogCreatingLamp(ILogger logger, Guid lampId);

        [LoggerMessage(Level = LogLevel.Debug, Message = "Updating lamp {LampId} in PostgreSQL database")]
        private static partial void LogUpdatingLamp(ILogger logger, Guid lampId);

        [LoggerMessage(Level = LogLevel.Debug, Message = "Lamp {LampId} not found for update")]
        private static partial void LogLampNotFoundForUpdate(ILogger logger, Guid lampId);

        [LoggerMessage(Level = LogLevel.Debug, Message = "Deleting lamp {LampId} from PostgreSQL database")]
        private static partial void LogDeletingLamp(ILogger logger, Guid lampId);

        [LoggerMessage(Level = LogLevel.Debug, Message = "Lamp {LampId} not found for deletion")]
        private static partial void LogLampNotFoundForDeletion(ILogger logger, Guid lampId);

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
