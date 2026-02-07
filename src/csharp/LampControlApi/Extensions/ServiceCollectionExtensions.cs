using LampControlApi.Controllers;
using LampControlApi.Domain.Repositories;
using LampControlApi.Infrastructure.Database;
using LampControlApi.Services;
using Microsoft.EntityFrameworkCore;

namespace LampControlApi.Extensions
{
    /// <summary>
    /// Extension methods for configuring application services.
    /// </summary>
    public static class ServiceCollectionExtensions
    {
        /// <summary>
        /// Adds Lamp Control API services to the service collection, including
        /// controllers, database context, repository, health checks, and Swagger.
        /// </summary>
        /// <param name="services">The service collection.</param>
        /// <param name="configuration">The application configuration.</param>
        /// <param name="environment">The web host environment.</param>
        /// <returns>True if PostgreSQL is configured, false if using in-memory storage.</returns>
        public static bool AddLampControlServices(
            this IServiceCollection services,
            IConfiguration configuration,
            IWebHostEnvironment environment)
        {
            services.AddControllers();

            var connectionString = ResolveConnectionString(configuration);
            var usePostgres = !string.IsNullOrWhiteSpace(connectionString);

            if (usePostgres)
            {
                services.AddDbContext<LampControlDbContext>(options =>
                {
                    options.UseNpgsql(connectionString!, npgsqlOptions =>
                    {
                        npgsqlOptions.EnableRetryOnFailure(
                            maxRetryCount: 3,
                            maxRetryDelay: TimeSpan.FromSeconds(5),
                            errorCodesToAdd: null);
                    });

                    if (environment.IsDevelopment())
                    {
                        options.EnableSensitiveDataLogging();
                        options.EnableDetailedErrors();
                    }
                });

                services.AddScoped<ILampRepository, PostgresLampRepository>();

                services.AddHealthChecks()
                    .AddDbContextCheck<LampControlDbContext>("database");
            }
            else
            {
                services.AddSingleton<ILampRepository, InMemoryLampRepository>();
                services.AddHealthChecks();
            }

            services.AddScoped<IController, LampControllerImplementation>();

            services.AddEndpointsApiExplorer();
            services.AddSwaggerGen();

            return usePostgres;
        }

        /// <summary>
        /// Resolves the database connection string from configuration or environment variables.
        /// </summary>
        /// <param name="configuration">The application configuration.</param>
        /// <returns>The connection string, or null if not configured.</returns>
        internal static string? ResolveConnectionString(IConfiguration configuration)
        {
            var connectionString = configuration.GetConnectionString("LampControl");

            if (string.IsNullOrWhiteSpace(connectionString))
            {
                connectionString = Environment.GetEnvironmentVariable("ConnectionStrings__LampControl");
            }

            return connectionString;
        }
    }
}
