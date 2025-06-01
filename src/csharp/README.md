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

### Artifacts

The workflow generates several artifacts:
- **Build artifacts** (7 days retention)
- **Security scan results** (30 days retention)
- **Publish artifacts** (30 days retention)
- **Coverage reports** (30 days retention)
- **Dependency reports** (30 days retention)

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
