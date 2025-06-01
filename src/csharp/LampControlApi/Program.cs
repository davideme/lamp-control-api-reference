using LampControlApi.Controllers;
using LampControlApi.Middleware;
using LampControlApi.Services;

var builder = WebApplication.CreateBuilder(args);

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

app.UseHttpsRedirection();

// Map controllers
app.MapControllers();

app.Run();

public partial class Program { } // Make Program public for test accessibility
