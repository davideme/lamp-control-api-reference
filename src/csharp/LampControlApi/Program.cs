using LampControlApi.Controllers;
using LampControlApi.Infrastructure.Database;
using LampControlApi.Middleware;
using LampControlApi.Services;
using Microsoft.EntityFrameworkCore;

// Parse operation mode from command line arguments
var mode = args.FirstOrDefault(arg => arg.StartsWith("--mode="))?.Split('=')[1] ?? "serve";

// Handle migrate-only mode
if (mode == "migrate")
{
    RunMigrationsOnly(args);
    return;
}

var builder = WebApplication.CreateBuilder(args);

// Configure Kestrel to use PORT environment variable if set (required for Cloud Run)
var port = Environment.GetEnvironmentVariable("PORT");
if (!string.IsNullOrEmpty(port))
{
    builder.WebHost.UseUrls($"http://0.0.0.0:{port}");
}

// Add services to the container.
builder.Services.AddControllers();

// Configure database storage based on connection string presence
var connectionString = builder.Configuration.GetConnectionString("LampControl");
var usePostgres = !string.IsNullOrWhiteSpace(connectionString);

if (usePostgres)
{
    builder.Services.AddDbContext<LampControlDbContext>(options =>
    {
        options.UseNpgsql(connectionString!, npgsqlOptions =>
        {
            npgsqlOptions.EnableRetryOnFailure(
                maxRetryCount: 3,
                maxRetryDelay: TimeSpan.FromSeconds(5),
                errorCodesToAdd: null);
        });

        if (builder.Environment.IsDevelopment())
        {
            options.EnableSensitiveDataLogging();
            options.EnableDetailedErrors();
        }
    });

    // Register PostgreSQL repository
    builder.Services.AddScoped<ILampRepository, PostgresLampRepository>();

    // Add health checks for database
    builder.Services.AddHealthChecks()
        .AddDbContextCheck<LampControlDbContext>("database");
}
else
{
    // Register in-memory repository (default for testing and development)
    builder.Services.AddSingleton<ILampRepository, InMemoryLampRepository>();
    builder.Services.AddHealthChecks();
}

builder.Services.AddScoped<IController, LampControllerImplementation>();

// Learn more about configuring Swagger/OpenAPI at https://aka.ms/aspnetcore/swashbuckle
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

var app = builder.Build();

// Run migrations if in 'serve' mode (default) and PostgreSQL is configured
if (mode == "serve" && usePostgres)
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
            Console.WriteLine($"Migration failed: {ex.Message}");
            Environment.Exit(1);
        }
    }
}
else if (mode == "serve-only")
{
    Console.WriteLine("Starting server without running migrations...");
}

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

// Add exception handling middleware
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

// Map controllers
app.MapControllers();

app.Run();

// Helper method for migrate-only mode
static void RunMigrationsOnly(string[] args)
{
    Console.WriteLine("Running migrations only...");

    var builder = WebApplication.CreateBuilder(args);
    var connectionString = builder.Configuration.GetConnectionString("LampControl");

    if (string.IsNullOrWhiteSpace(connectionString))
    {
        Console.WriteLine("No PostgreSQL configuration found, nothing to migrate");
        return;
    }

    builder.Services.AddDbContext<LampControlDbContext>(options =>
    {
        options.UseNpgsql(connectionString!);
    });

    var app = builder.Build();

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
            Console.WriteLine($"Migration failed: {ex.Message}");
            Environment.Exit(1);
        }
    }
}

/// <summary>
/// Entry point for the LampControlApi application. This partial class is used for test accessibility.
/// </summary>
public partial class Program
{
    // Intentionally left blank. Used for test accessibility.
}
