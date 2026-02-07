using LampControlApi.Infrastructure.Database;
using Microsoft.EntityFrameworkCore;

namespace LampControlApi.Extensions
{
    /// <summary>
    /// Handles standalone migration execution for the migrate-only operation mode.
    /// </summary>
    public static class MigrationRunner
    {
        /// <summary>
        /// Runs database migrations and exits. Used for the "migrate" operation mode.
        /// </summary>
        /// <param name="args">Command line arguments.</param>
        public static void RunMigrationsOnly(string[] args)
        {
            Console.WriteLine("Running migrations only...");

            var builder = WebApplication.CreateBuilder(args);
            var connectionString = ServiceCollectionExtensions.ResolveConnectionString(builder.Configuration);

            if (string.IsNullOrWhiteSpace(connectionString))
            {
                Console.WriteLine("No PostgreSQL configuration found, nothing to migrate");
                return;
            }

            builder.Services.AddDbContext<LampControlDbContext>(options =>
            {
                options.UseNpgsql(connectionString!);
            });

            using var app = builder.Build();
            app.RunMigrations();
        }
    }
}
