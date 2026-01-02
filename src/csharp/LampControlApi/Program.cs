using LampControlApi.Controllers;
using LampControlApi.Infrastructure.Database;
using LampControlApi.Middleware;
using LampControlApi.Services;
using Microsoft.EntityFrameworkCore;

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

/// <summary>
/// Entry point for the LampControlApi application. This partial class is used for test accessibility.
/// </summary>
public partial class Program
{
    // Intentionally left blank. Used for test accessibility.
}
