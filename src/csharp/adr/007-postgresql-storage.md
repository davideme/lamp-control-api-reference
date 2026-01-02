# ADR 007: PostgreSQL Storage with Entity Framework Core

## Status

Accepted

## Context

The C# implementation of the Lamp Control API currently uses an in-memory repository (`InMemoryLampRepository`) for data persistence. While suitable for development and testing, production deployments require durable, ACID-compliant data storage with proper transaction support, concurrency handling, and scalability.

### Current State

- **Framework**: ASP.NET Core 8.0 (.NET 8)
- **Architecture**: Repository pattern with `ILampRepository` interface
- **Storage**: `InMemoryLampRepository` using `ConcurrentDictionary<Guid, Lamp>`
- **Dependencies**: No database libraries currently integrated

### Requirements

1. **Production-Ready Persistence**: Durable storage with ACID guarantees
2. **Type Safety**: Strong typing and compile-time query validation
3. **Performance**: Efficient queries, connection pooling, async operations
4. **Maintainability**: Clear separation of concerns, testable code
5. **Schema Compatibility**: Use existing PostgreSQL schema at `database/sql/postgresql/schema.sql`
6. **Testing**: Integration tests with real PostgreSQL instances

### Technology Landscape (2025-2026)

**Entity Framework Core 8.0+**
- Industry standard for .NET (70%+ adoption)
- First-class async/await support
- LINQ with compile-time type checking
- Automatic migrations and schema management
- Excellent tooling and IntelliSense
- Native JSON support, compiled queries, bulk operations

**Dapper**
- Micro-ORM, 50% faster than EF Core
- Manual SQL and mapping
- No migrations or change tracking
- Best for read-heavy, high-performance scenarios

**Npgsql (Direct)**
- Direct ADO.NET provider
- Maximum control, zero ORM overhead
- Verbose, requires manual mapping
- Rarely needed for REST APIs

## Decision

We will implement **Entity Framework Core 8.0+ with Npgsql** as the PostgreSQL data access layer for the C# Lamp Control API implementation.

### Architecture

```
Controllers → ILampRepository → PostgresLampRepository → EF Core DbContext → Npgsql → PostgreSQL
```

### Core Components

#### 1. **DbContext Configuration**

```csharp
public class LampControlDbContext : DbContext
{
    public DbSet<LampEntity> Lamps { get; set; }

    public LampControlDbContext(DbContextOptions<LampControlDbContext> options)
        : base(options)
    {
    }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<LampEntity>(entity =>
        {
            entity.ToTable("lamps");
            entity.HasKey(e => e.Id);
            entity.Property(e => e.Id).HasColumnName("id");
            entity.Property(e => e.IsOn).HasColumnName("is_on").IsRequired();
            entity.Property(e => e.CreatedAt).HasColumnName("created_at").IsRequired();
            entity.Property(e => e.UpdatedAt).HasColumnName("updated_at").IsRequired();
            entity.Property(e => e.DeletedAt).HasColumnName("deleted_at");
            
            // Indexes
            entity.HasIndex(e => e.IsOn).HasDatabaseName("idx_lamps_is_on");
            entity.HasIndex(e => e.CreatedAt).HasDatabaseName("idx_lamps_created_at");
            entity.HasIndex(e => e.DeletedAt).HasDatabaseName("idx_lamps_deleted_at");
            
            // Global query filter for soft deletes
            entity.HasQueryFilter(e => e.DeletedAt == null);
        });
    }
}
```

#### 2. **Entity Model**

```csharp
public class LampEntity
{
    public Guid Id { get; set; }
    public bool IsOn { get; set; }
    public DateTimeOffset CreatedAt { get; set; }
    public DateTimeOffset UpdatedAt { get; set; }
    public DateTimeOffset? DeletedAt { get; set; }
}
```

#### 3. **Repository Implementation**

```csharp
public class PostgresLampRepository : ILampRepository
{
    private readonly LampControlDbContext _context;
    private readonly ILogger<PostgresLampRepository> _logger;

    public PostgresLampRepository(
        LampControlDbContext context,
        ILogger<PostgresLampRepository> logger)
    {
        _context = context;
        _logger = logger;
    }

    public async Task<Lamp> CreateAsync(Lamp lamp)
    {
        var entity = MapToEntity(lamp);
        entity.CreatedAt = DateTimeOffset.UtcNow;
        entity.UpdatedAt = DateTimeOffset.UtcNow;
        
        _context.Lamps.Add(entity);
        await _context.SaveChangesAsync();
        
        return MapToDomain(entity);
    }

    public async Task<Lamp?> GetByIdAsync(Guid id)
    {
        var entity = await _context.Lamps.FindAsync(id);
        return entity != null ? MapToDomain(entity) : null;
    }

    public async Task<IEnumerable<Lamp>> GetAllAsync(int offset = 0, int limit = 100)
    {
        var entities = await _context.Lamps
            .OrderBy(l => l.CreatedAt)
            .Skip(offset)
            .Take(limit)
            .ToListAsync();
            
        return entities.Select(MapToDomain);
    }

    public async Task<Lamp?> UpdateAsync(Guid id, Lamp lamp)
    {
        var entity = await _context.Lamps.FindAsync(id);
        if (entity == null) return null;
        
        entity.IsOn = lamp.IsOn;
        entity.UpdatedAt = DateTimeOffset.UtcNow;
        
        await _context.SaveChangesAsync();
        
        return MapToDomain(entity);
    }

    public async Task<bool> DeleteAsync(Guid id)
    {
        var entity = await _context.Lamps.FindAsync(id);
        if (entity == null) return false;
        
        entity.DeletedAt = DateTimeOffset.UtcNow;
        await _context.SaveChangesAsync();
        
        return true;
    }
}
```

#### 4. **Dependency Injection Configuration**

```csharp
// Program.cs
builder.Services.AddDbContext<LampControlDbContext>(options =>
{
    var connectionString = builder.Configuration.GetConnectionString("LampControl")
        ?? throw new InvalidOperationException("Connection string 'LampControl' not found.");
    
    options.UseNpgsql(connectionString, npgsqlOptions =>
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

// Register repository
builder.Services.AddScoped<ILampRepository, PostgresLampRepository>();

// Health checks
builder.Services.AddHealthChecks()
    .AddDbContextCheck<LampControlDbContext>("database");
```

### Configuration

#### **appsettings.json**

```json
{
  "ConnectionStrings": {
    "LampControl": "Host=localhost;Port=5432;Database=lampcontrol;Username=lampuser;Password=lamppass;Pooling=true;Maximum Pool Size=50;Connection Idle Lifetime=300"
  }
}
```

#### **Environment Variables** (Production)

```bash
ConnectionStrings__LampControl="Host=db.production.com;Port=5432;Database=lampcontrol;Username=${DB_USER};Password=${DB_PASSWORD};SSL Mode=Require;Trust Server Certificate=false"
```

### Dependencies

```xml
<ItemGroup>
  <PackageReference Include="Npgsql.EntityFrameworkCore.PostgreSQL" Version="8.0.0" />
  <PackageReference Include="Microsoft.EntityFrameworkCore.Design" Version="8.0.0">
    <PrivateAssets>all</PrivateAssets>
    <IncludeAssets>runtime; build; native; contentfiles; analyzers</IncludeAssets>
  </PackageReference>
  <PackageReference Include="Microsoft.Extensions.Diagnostics.HealthChecks.EntityFrameworkCore" Version="8.0.0" />
</ItemGroup>
```

### Migration Strategy

#### **Option 1: Apply Existing Schema (Recommended)**

Use the existing `database/sql/postgresql/schema.sql`:

```bash
# Apply schema directly
psql -h localhost -U lampuser -d lampcontrol -f ../../../database/sql/postgresql/schema.sql

# Then create EF Core model matching the schema
# No EF migrations needed initially
```

#### **Option 2: EF Core Migrations**

Generate migrations from code-first models:

```bash
# Install EF Core CLI tools
dotnet tool install --global dotnet-ef

# Create initial migration
dotnet ef migrations add InitialCreate --project LampControlApi

# Apply migration
dotnet ef database update --project LampControlApi

# Generate migration script for review
dotnet ef migrations script --output migration.sql
```

**Recommendation**: Use Option 1 initially to leverage existing schema, then use EF migrations for future schema changes.

### Connection Pooling

**Npgsql Connection Pooling** (built-in):

```
Pooling=true;                     # Enable pooling (default: true)
Minimum Pool Size=5;              # Minimum connections maintained
Maximum Pool Size=50;             # Maximum connections allowed
Connection Idle Lifetime=300;     # Close idle connections after 5 min
Connection Pruning Interval=10;   # Check for idle connections every 10s
```

**DbContext Pooling** (for high-performance scenarios):

```csharp
builder.Services.AddDbContextPool<LampControlDbContext>(options =>
    options.UseNpgsql(connectionString),
    poolSize: 128);  // Default: 1024
```

### Testing Strategy

#### **Integration Tests with Testcontainers**

```csharp
[Collection("Database")]
public class PostgresLampRepositoryTests : IAsyncLifetime
{
    private PostgreSqlContainer _postgres;
    private LampControlDbContext _context;
    private PostgresLampRepository _repository;

    public async Task InitializeAsync()
    {
        _postgres = new PostgreSqlBuilder()
            .WithImage("postgres:16-alpine")
            .WithDatabase("lampcontrol_test")
            .WithUsername("test")
            .WithPassword("test")
            .Build();
            
        await _postgres.StartAsync();
        
        var options = new DbContextOptionsBuilder<LampControlDbContext>()
            .UseNpgsql(_postgres.GetConnectionString())
            .Options;
            
        _context = new LampControlDbContext(options);
        await _context.Database.EnsureCreatedAsync();
        
        _repository = new PostgresLampRepository(_context, 
            NullLogger<PostgresLampRepository>.Instance);
    }

    [Fact]
    public async Task CreateAsync_ShouldPersistLamp()
    {
        // Arrange
        var lamp = new Lamp { Id = Guid.NewGuid(), IsOn = true };
        
        // Act
        var created = await _repository.CreateAsync(lamp);
        
        // Assert
        var retrieved = await _repository.GetByIdAsync(created.Id);
        Assert.NotNull(retrieved);
        Assert.True(retrieved.IsOn);
    }

    public async Task DisposeAsync()
    {
        await _context.DisposeAsync();
        await _postgres.DisposeAsync();
    }
}
```

#### **Required Test Dependencies**

```xml
<PackageReference Include="Testcontainers.PostgreSql" Version="3.7.0" />
<PackageReference Include="Microsoft.EntityFrameworkCore.InMemory" Version="8.0.0" />
```

### Performance Considerations

1. **Query Optimization**
   - Use `.AsNoTracking()` for read-only queries
   - Leverage compiled queries for repeated operations
   - Use projection (`.Select()`) to fetch only needed columns

2. **Bulk Operations**
   - Use `AddRange()` for bulk inserts
   - Consider `ExecuteUpdateAsync()` for bulk updates (EF Core 7+)

3. **Monitoring**
   - Enable logging in development
   - Use Application Insights or similar in production
   - Monitor connection pool metrics

## Rationale

### Why Entity Framework Core?

1. **Industry Standard**: De facto ORM for .NET, massive ecosystem
2. **Productivity**: LINQ queries, automatic change tracking, migrations
3. **Type Safety**: Compile-time query validation, refactoring support
4. **Async-First**: Native async/await, perfect for ASP.NET Core
5. **Tooling**: Visual Studio integration, migrations CLI, design-time support
6. **Modern Features**: EF Core 8 adds compiled queries, JSON columns, bulk operations

### Why Not Dapper?

- **Trade-off**: Performance vs developer productivity
- **Use Case**: Dapper excels in read-heavy, performance-critical scenarios
- **Decision**: EF Core provides better balance for a reference API project
- **Future**: Can add Dapper for specific high-performance endpoints if needed

### Why Not Direct Npgsql?

- **Too Low-Level**: Excessive boilerplate for CRUD operations
- **Maintenance**: Manual SQL strings are error-prone and hard to refactor
- **Use Case**: Only for stored procedures or very complex queries

## Consequences

### Positive

- ✅ **Production-Ready**: ACID transactions, connection pooling, retry logic
- ✅ **Type Safety**: Compile-time query validation, IntelliSense support
- ✅ **Testability**: Easy integration testing with Testcontainers
- ✅ **Maintainability**: LINQ queries are readable and refactorable
- ✅ **Best Practices**: Demonstrates modern .NET data access patterns
- ✅ **Documentation**: Extensive EF Core documentation and community support

### Negative

- ❌ **Performance Overhead**: 10-20% slower than Dapper/raw SQL
- ❌ **Complexity**: Learning curve for EF Core abstractions
- ❌ **Dependencies**: Additional NuGet packages (~5MB)
- ❌ **Query Translation**: Complex queries may not translate efficiently
- ❌ **Testing Complexity**: Requires Docker for integration tests

### Neutral

- 🔄 **Migration Strategy**: Must choose between SQL scripts vs EF migrations
- 🔄 **Schema Synchronization**: Code and database schema must stay in sync
- 🔄 **Version Compatibility**: Must update EF Core with .NET updates

## Implementation Checklist

- [ ] Add Npgsql.EntityFrameworkCore.PostgreSQL package
- [ ] Create `LampEntity` model class
- [ ] Create `LampControlDbContext` with entity configuration
- [ ] Implement `PostgresLampRepository : ILampRepository`
- [ ] Configure dependency injection in `Program.cs`
- [ ] Add connection string to `appsettings.json`
- [ ] Apply existing PostgreSQL schema to database
- [ ] Add health check endpoint for database connectivity
- [ ] Write integration tests with Testcontainers
- [ ] Update README with database setup instructions
- [ ] Document environment variables
- [ ] Add migration instructions for schema changes

## References

- [Entity Framework Core Documentation](https://docs.microsoft.com/ef/core/)
- [Npgsql Documentation](https://www.npgsql.org/doc/)
- [PostgreSQL Schema: database/sql/postgresql/schema.sql](../../../database/sql/postgresql/schema.sql)
- [ASP.NET Core Data Access](https://docs.microsoft.com/aspnet/core/data/)
- [Testcontainers for .NET](https://dotnet.testcontainers.org/)

## Related ADRs

- [ADR 001: C# Version](001-csharp-version.md)
- [ADR 002: Framework](002-framework.md)
- [ADR 005: Schemathesis Integration](005-schemathesis-integration.md)
- [Root ADR 005: PostgreSQL Storage Support](../../../docs/adr/005-postgresql-storage-support.md)
