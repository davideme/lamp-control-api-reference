using LampControlApi.Extensions;

// Parse operation mode from command line arguments
var mode = args.FirstOrDefault(arg => arg.StartsWith("--mode="))?.Split('=')[1] ?? "serve-only";

// Handle migrate-only mode
if (mode == "migrate")
{
    MigrationRunner.RunMigrationsOnly(args);
    return;
}

var builder = WebApplication.CreateBuilder(args);

// Configure Kestrel to use PORT environment variable if set (required for Cloud Run)
var port = Environment.GetEnvironmentVariable("PORT");
if (!string.IsNullOrEmpty(port))
{
    builder.WebHost.UseUrls($"http://0.0.0.0:{port}");
}

var usePostgres = builder.Services.AddLampControlServices(builder.Configuration, builder.Environment);

var app = builder.Build();

// Run migrations if in 'serve' mode and PostgreSQL is configured
if (mode == "serve" && usePostgres)
{
    app.RunMigrations();
}
else if (mode == "serve-only")
{
    Console.WriteLine("Starting server without running migrations...");
}

app.ConfigurePipeline();

app.Run();

/// <summary>
/// Entry point for the LampControlApi application. This partial class is used for test accessibility.
/// </summary>
public partial class Program
{
    // Intentionally left blank. Used for test accessibility.
}
