# Lamp Control API - C#

A modern ASP.NET Core Web API for controlling smart lamps, built with .NET 8.0.

## Features

- **RESTful API** for lamp control operations
- **OpenAPI/Swagger** documentation
- **Code Quality Tools** with StyleCop and .NET analyzers
- **Automated CI/CD** with GitHub Actions
- **Comprehensive formatting** and linting rules

## Development Setup

### Prerequisites

- .NET 8.0 SDK
- Your favorite IDE (VS Code, Visual Studio, Rider)

### Getting Started

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd lamp-control-api-reference/src/csharp
   ```

2. **Install dependencies**
   ```bash
   make install
   # or
   dotnet restore LampControlApi/LampControlApi.csproj
   ```

3. **Build the project**
   ```bash
   make build
   # or
   dotnet build LampControlApi/LampControlApi.csproj
   ```

4. **Run the application**
   ```bash
   make run
   # or
   cd LampControlApi && dotnet run
   ```

The API will be available at `https://localhost:7173` with Swagger UI at `/swagger`.

## Database Storage

### In-Memory Storage (Default)

By default, the application uses an in-memory repository for data persistence. This is suitable for development, testing, and demonstrations.

### PostgreSQL Storage

The application supports PostgreSQL for production-ready, durable data storage with ACID guarantees.

#### Prerequisites for PostgreSQL

- PostgreSQL 12+ (or use Docker)
- PostgreSQL client tools (optional, for schema management)

#### Setup PostgreSQL with Docker

```bash
# Start PostgreSQL container
docker run -d \
  --name lampcontrol-postgres \
  -e POSTGRES_DB=lampcontrol \
  -e POSTGRES_USER=lampuser \
  -e POSTGRES_PASSWORD=lamppass \
  -p 5432:5432 \
  postgres:16-alpine

# Apply schema (run from repository root)
docker exec -i lampcontrol-postgres psql -U lampuser -d lampcontrol < database/sql/postgresql/schema.sql
```

Alternatively, use the project's docker-compose:

```bash
# From repository root
docker-compose up -d postgres

# Apply schema
docker exec -i lamp-control-api-reference-postgres-1 psql -U lampuser -d lampcontrol < database/sql/postgresql/schema.sql
```

#### Configuration

The application automatically uses PostgreSQL when a connection string is configured.

**Option 1: appsettings.json** (Development)

```json
{
  "ConnectionStrings": {
    "LampControl": "Host=localhost;Port=5432;Database=lampcontrol;Username=lampuser;Password=lamppass;Pooling=true;Maximum Pool Size=50;Connection Idle Lifetime=300"
  }
}
```

**Option 2: Environment Variables** (Production)

```bash
# Set connection string
export ConnectionStrings__LampControl="Host=db.production.com;Port=5432;Database=lampcontrol;Username=lampuser;Password=your_secure_password;SSL Mode=Require;Trust Server Certificate=false"

# Or explicitly enable PostgreSQL
export USE_POSTGRES=true
export ConnectionStrings__LampControl="your-connection-string"

# Run application
dotnet run
```

**Option 3: User Secrets** (Development)

```bash
# Set connection string in user secrets
cd LampControlApi
dotnet user-secrets set "ConnectionStrings:LampControl" "Host=localhost;Port=5432;Database=lampcontrol;Username=lampuser;Password=lamppass"
dotnet run
```

#### Connection String Parameters

- `Host` - PostgreSQL server hostname
- `Port` - PostgreSQL server port (default: 5432)
- `Database` - Database name
- `Username` - Database user
- `Password` - Database password
- `Pooling` - Enable connection pooling (default: true)
- `Maximum Pool Size` - Max connections in pool (default: 50)
- `Connection Idle Lifetime` - Close idle connections after N seconds
- `SSL Mode` - SSL/TLS mode (Disable, Allow, Prefer, Require)
- `Trust Server Certificate` - Trust server certificate without validation

#### Schema Management

**Manual Schema Application** (Recommended)

Apply the existing schema from `database/sql/postgresql/schema.sql`:

```bash
# Using psql (from repository root)
psql -h localhost -U lampuser -d lampcontrol -f database/sql/postgresql/schema.sql

# Using Docker (from repository root)
docker exec -i lampcontrol-postgres psql -U lampuser -d lampcontrol < database/sql/postgresql/schema.sql
```

**Entity Framework Core Migrations** (Alternative)

For future schema changes, you can use EF Core migrations:

```bash
# Install EF Core CLI tools
dotnet tool install --global dotnet-ef

# Create migration
dotnet ef migrations add MigrationName --project LampControlApi

# Apply migration
dotnet ef database update --project LampControlApi

# Generate SQL script (for manual review/deployment)
dotnet ef migrations script --output migration.sql
```

#### Testing with PostgreSQL

Integration tests use Testcontainers to automatically start PostgreSQL containers:

```bash
# Run all tests (including PostgreSQL integration tests)
dotnet test

# Run only PostgreSQL integration tests
dotnet test --filter "FullyQualifiedName~PostgresLampRepositoryTests"
```

The tests will automatically:
1. Pull the `postgres:16-alpine` Docker image (if not already present)
2. Start a PostgreSQL container
3. Create the test database and schema
4. Run tests
5. Clean up the container

#### Health Checks

The API exposes two health endpoints:

- `/health` - Simple status check (always returns `ok`, backwards compatible)
- `/healthz` - Detailed health check (includes database connectivity when PostgreSQL is enabled)

```bash
# Simple liveness/status check
curl https://localhost:7173/health

# Detailed health check including database connectivity (when PostgreSQL is configured)
curl https://localhost:7173/healthz
```

#### Troubleshooting PostgreSQL

**Connection errors**
```bash
# Check PostgreSQL is running
docker ps | grep postgres

# Check connection string is correct
dotnet user-secrets list

# Check PostgreSQL logs
docker logs lampcontrol-postgres
```

**Schema not applied**
```bash
# Verify schema exists
docker exec -it lampcontrol-postgres psql -U lampuser -d lampcontrol -c "\dt"

# Reapply schema if needed (from repository root)
docker exec -i lampcontrol-postgres psql -U lampuser -d lampcontrol < database/sql/postgresql/schema.sql
```

**Permission errors**
```bash
# Grant necessary permissions
docker exec -it lampcontrol-postgres psql -U lampuser -d lampcontrol -c "GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO lampuser;"
```

## Code Quality

### Linting and Formatting

This project uses comprehensive code analysis tools:

- **StyleCop Analyzers** - Style and consistency rules
- **Microsoft .NET Analyzers** - Code quality analysis
- **EditorConfig** - Consistent formatting across editors

### Available Commands

```bash
# Check code formatting
make format-check

# Apply code formatting
make format

# Run linting (static analysis)
make lint

# Run all quality checks
make ci

# Clean build artifacts
make clean
```

### Configuration Files

- **`.editorconfig`** - Formatting rules and code style preferences
- **`stylecop.json`** - StyleCop analyzer settings
- **`.globalconfig`** - Global analyzer diagnostic rules
- **`Directory.Build.props`** - Project-wide MSBuild properties

## CI/CD Pipeline

### GitHub Actions Workflow

The project includes a comprehensive CI/CD pipeline (`.github/workflows/csharp-ci.yml`) that runs on:
- Push to `main` or `develop` branches
- Pull requests to `main` or `develop` branches
- Manual trigger via workflow dispatch

### Pipeline Jobs

#### 1. **Code Quality & Build**
- Restores NuGet packages
- Checks code formatting (`dotnet format --verify-no-changes`)
- Runs static analysis (`dotnet build` with analyzers)
- Builds the application
- Runs tests (when available)
- Uploads build artifacts

#### 2. **Security Scan**
- Scans for vulnerable NuGet packages
- Uploads security scan results
- Fails the build if vulnerabilities are found

#### 3. **Publish Ready**
- Builds for Release configuration
- Creates publish artifacts
- Verifies deployment readiness

#### 4. **Code Coverage**
- Runs tests with coverage collection
- Generates HTML coverage reports
- Uploads coverage artifacts

#### 5. **Dependency Analysis**
- Lists all project dependencies
- Checks for outdated packages
- Generates dependency reports

#### 6. **Schemathesis API Testing**
- Runs property-based tests against the running API
- Validates OpenAPI specification compliance
- Checks status code conformance, server errors, and response schemas
- Generates test reports for CI integration
- Automatically detects API violations and regressions

### Artifacts

The workflow generates several artifacts:
- **Build artifacts** (7 days retention)
- **Security scan results** (30 days retention)
- **Publish artifacts** (30 days retention)
- **Coverage reports** (30 days retention)
- **Dependency reports** (30 days retention)
- **Schemathesis test reports** (30 days retention)

## Project Structure

```
src/csharp/
├── LampControlApi/           # Main API project
│   ├── Controllers/          # API controllers
│   ├── Program.cs           # Application entry point
│   ├── appsettings.json     # Configuration
│   └── LampControlApi.csproj # Project file
├── .editorconfig            # Code formatting rules
├── .globalconfig            # Global analyzer settings
├── stylecop.json           # StyleCop configuration
├── Directory.Build.props   # MSBuild properties
└── Makefile               # Build automation
```

## Configuration

### Environment Variables

- `ASPNETCORE_ENVIRONMENT` - Set to `Development`, `Staging`, or `Production`
- `ASPNETCORE_URLS` - Configure listening URLs

### Application Settings

Key configuration options in `appsettings.json`:
- Logging levels
- CORS policies
- API documentation settings

## API Documentation

Once running, visit:
- **Swagger UI**: `https://localhost:7173/swagger`
- **OpenAPI JSON**: `https://localhost:7173/swagger/v1/swagger.json`

## Generating the Server Code

To generate the server code using NSwag, follow these steps:

1. **Install NSwag CLI**:
   ```bash
   dotnet tool install -g NSwag.ConsoleCore
   ```

2. **Generate the Server Code**:
   Run the following command, replacing `<path-to-openapi.yaml>` with the path to your OpenAPI definition file (e.g., `docs/api/openapi.yaml`):
   ```bash
   nswag openapi2cscontroller /input:docs/api/openapi.yaml /output:src/csharp/LampControlApi/Controllers/Controllers.cs /namespace:LampControlApi.Controllers /UseActionResultType:true
   ```

   This will generate a `Controllers.cs` file with the server-side code.

## Contributing

1. **Follow the code style** - The build will fail if formatting or linting issues are found
2. **Run quality checks** locally before committing:
   ```bash
   make ci
   ```
3. **Add tests** for new functionality
4. **Update documentation** as needed

### Code Style Guidelines

- Follow the configured EditorConfig and StyleCop rules
- Use meaningful variable and method names
- Add XML documentation for public APIs
- Keep methods focused and small
- Use async/await for I/O operations

## Troubleshooting

### Common Issues

**Build fails with analyzer warnings**
```bash
# Check specific issues
make lint

# Fix formatting issues
make format
```

**Missing dependencies**
```bash
# Restore packages
make install
```

**Port already in use**
```bash
# Change port in Properties/launchSettings.json
# or set ASPNETCORE_URLS environment variable
```

## License

This project is part of the Lamp Control API Reference implementation.
