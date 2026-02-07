using LampControlApi.Infrastructure.Database;
using LampControlApi.Middleware;
using Microsoft.EntityFrameworkCore;

namespace LampControlApi.Extensions
{
    /// <summary>
    /// Extension methods for configuring the web application pipeline.
    /// </summary>
    public static class WebApplicationExtensions
    {
        /// <summary>
        /// Runs database migrations using the configured DbContext.
        /// </summary>
        /// <param name="app">The web application.</param>
        public static void RunMigrations(this WebApplication app)
        {
            Console.WriteLine("Running database migrations...");
            using (var scope = app.Services.CreateScope())
            {
                var dbContext = scope.ServiceProvider.GetRequiredService<LampControlDbContext>();
                try
                {
                    dbContext.Database.Migrate();
                    Console.WriteLine("Migrations completed successfully");
                }
                catch (Exception ex)
                {
                    Console.WriteLine($"Migration failed: {ex}");
                    Environment.Exit(1);
                }
            }
        }

        /// <summary>
        /// Configures the HTTP request pipeline with middleware, health checks, and controllers.
        /// </summary>
        /// <param name="app">The web application.</param>
        public static void ConfigurePipeline(this WebApplication app)
        {
            if (app.Environment.IsDevelopment())
            {
                app.UseSwagger();
                app.UseSwaggerUI();
            }

            app.UseMiddleware<ExceptionHandlingMiddleware>();

            // Only use HTTPS redirection if not running on Cloud Run (where TLS is terminated at the load balancer)
            if (string.IsNullOrEmpty(Environment.GetEnvironmentVariable("K_SERVICE")))
            {
                app.UseHttpsRedirection();
            }

            // Simple health check endpoint (backwards compatible)
            app.MapGet("/health", () => Results.Ok(new { status = "ok" }));

            // Detailed health check endpoint (includes database checks if PostgreSQL is configured)
            app.MapHealthChecks("/healthz");

            app.MapControllers();
        }
    }
}
