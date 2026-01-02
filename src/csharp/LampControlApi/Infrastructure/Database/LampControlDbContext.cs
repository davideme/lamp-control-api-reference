using Microsoft.EntityFrameworkCore;

namespace LampControlApi.Infrastructure.Database
{
    /// <summary>
    /// Entity Framework Core database context for the Lamp Control API.
    /// Configures the database schema and query filters.
    /// </summary>
    public class LampControlDbContext : DbContext
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="LampControlDbContext"/> class.
        /// </summary>
        /// <param name="options">The options for configuring the context.</param>
        public LampControlDbContext(DbContextOptions<LampControlDbContext> options)
            : base(options)
        {
        }

        /// <summary>
        /// Gets or sets the DbSet for lamps.
        /// </summary>
        public DbSet<LampDbEntity> Lamps { get; set; } = null!;

        /// <summary>
        /// Configures the entity mappings and relationships.
        /// </summary>
        /// <param name="modelBuilder">The model builder being used to construct the model.</param>
        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);

            modelBuilder.Entity<LampDbEntity>(entity =>
            {
                // Table mapping
                entity.ToTable("lamps");

                // Primary key
                entity.HasKey(e => e.Id);

                // Column mappings
                entity.Property(e => e.Id)
                    .HasColumnName("id")
                    .IsRequired();

                entity.Property(e => e.IsOn)
                    .HasColumnName("is_on")
                    .IsRequired();

                entity.Property(e => e.CreatedAt)
                    .HasColumnName("created_at")
                    .IsRequired();

                entity.Property(e => e.UpdatedAt)
                    .HasColumnName("updated_at")
                    .IsRequired();

                entity.Property(e => e.DeletedAt)
                    .HasColumnName("deleted_at");

                // Indexes
                entity.HasIndex(e => e.IsOn)
                    .HasDatabaseName("idx_lamps_is_on");

                entity.HasIndex(e => e.CreatedAt)
                    .HasDatabaseName("idx_lamps_created_at");

                entity.HasIndex(e => e.DeletedAt)
                    .HasDatabaseName("idx_lamps_deleted_at");

                // Global query filter for soft deletes
                entity.HasQueryFilter(e => e.DeletedAt == null);
            });
        }
    }
}
