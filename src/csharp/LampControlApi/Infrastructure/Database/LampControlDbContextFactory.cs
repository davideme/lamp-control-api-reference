using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Design;

namespace LampControlApi.Infrastructure.Database
{
    /// <summary>
    /// Factory for creating LampControlDbContext instances at design time.
    /// This is used by Entity Framework tools for migrations.
    /// </summary>
    public class LampControlDbContextFactory : IDesignTimeDbContextFactory<LampControlDbContext>
    {
        public LampControlDbContext CreateDbContext(string[] args)
        {
            var optionsBuilder = new DbContextOptionsBuilder<LampControlDbContext>();

            // Use a dummy connection string for design-time operations
            // The actual connection string is provided at runtime
            optionsBuilder.UseNpgsql("Host=localhost;Database=lampcontrol;Username=postgres;Password=postgres");

            return new LampControlDbContext(optionsBuilder.Options);
        }
    }
}
