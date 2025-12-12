using LampControlApi.Controllers;
using LampControlApi.Middleware;
using LampControlApi.Services;

var builder = WebApplication.CreateBuilder(args);

// Configure Kestrel to use PORT environment variable if set (required for Cloud Run)
var port = Environment.GetEnvironmentVariable("PORT") ?? "8080";
builder.WebHost.UseUrls($"http://0.0.0.0:{port}");

// Add services to the container.
builder.Services.AddControllers();

// Register our services
builder.Services.AddSingleton<ILampRepository, InMemoryLampRepository>();
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

// Health check endpoint
app.MapGet("/health", () => Results.Ok(new { status = "ok" }));

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
