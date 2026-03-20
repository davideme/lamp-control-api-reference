# ADR 003: .NET CLI and NuGet as Build Tool and Package Manager

## Status

Accepted

## Context

The C# ASP.NET Core Lamp Control API implementation requires a comprehensive build system and package management solution to handle:

- Project compilation and build orchestration
- Dependency resolution and package management
- Solution and project file management
- Development vs production dependency separation
- Cross-platform build consistency
- Integration with CI/CD pipelines
- Code generation and tooling integration

Available build and package management options for .NET projects include:

1. **.NET CLI with NuGet** - Microsoft's official toolchain
2. **MSBuild with NuGet** - Lower-level build engine
3. **Visual Studio with Package Manager UI** - IDE-integrated approach
4. **Paket** - Alternative package manager for .NET

## Decision

We will use the **.NET CLI as our build tool and NuGet as our package manager** for the C# ASP.NET Core implementation.

## Rationale

### Why .NET CLI + NuGet?

1. **Official Microsoft Toolchain**
   - Built and maintained by Microsoft for the .NET ecosystem
   - Default and recommended approach for .NET development
   - Continuous updates and improvements aligned with .NET releases
   - Cross-platform support (Windows, macOS, Linux)

2. **Build System Integration**
   - MSBuild integration for complex build scenarios
   - Project file-based configuration (.csproj)
   - Built-in support for multi-targeting and framework selection
   - Seamless integration with .NET workloads and SDKs

3. **Package Management**
   - Central package repository (NuGet.org) with extensive .NET library ecosystem
   - Transitive dependency resolution with conflict resolution
   - Package versioning with semantic versioning support
   - Private package source support for enterprise scenarios

4. **Development Workflow**
   - Command-line interface for all development tasks
   - Built-in project templates and scaffolding
   - Hot reload and watch capabilities for development
   - Integrated testing framework support

5. **CI/CD Integration**
   - Deterministic builds with lock files (packages.lock.json)
   - Cross-platform CI/CD pipeline support
   - Docker and container integration
   - Publication and deployment capabilities

6. **Tooling Ecosystem**
   - Global tool installation and management
   - Code analysis and formatting tool integration
   - OpenAPI generation tool support (NSwag, Swashbuckle)
   - Security scanning and vulnerability detection

## Project Configuration

### Project File Structure (.csproj)
```xml
<Project Sdk="Microsoft.NET.Sdk.Web">
  <PropertyGroup>
    <TargetFramework>net8.0</TargetFramework>
    <Nullable>enable</Nullable>
    <ImplicitUsings>enable</ImplicitUsings>
    <TreatWarningsAsErrors>false</TreatWarningsAsErrors>
    <EnableNETAnalyzers>true</EnableNETAnalyzers>
  </PropertyGroup>
  
  <ItemGroup>
    <PackageReference Include="Microsoft.AspNetCore.OpenApi" Version="8.0.15" />
    <PackageReference Include="Swashbuckle.AspNetCore" Version="6.6.2" />
    <PackageReference Include="StyleCop.Analyzers" Version="1.1.118" />
  </ItemGroup>
</Project>
```

### Package Management Features

1. **Package References**
   - Direct package references in project files
   - Automatic transitive dependency resolution
   - Version range specification support
   - Development-only package references

2. **Package Sources**
   - Multiple package source configuration
   - Private feed support for internal packages
   - Source mapping for security and compliance

3. **Lock Files**
   - `packages.lock.json` for reproducible builds
   - Dependency graph verification
   - Security vulnerability tracking

### Build and Development Commands

```bash
# Restore packages
dotnet restore

# Build project
dotnet build

# Run in development
dotnet run

# Run tests
dotnet test

# Publish for deployment
dotnet publish -c Release

# Add package reference
dotnet add package Microsoft.EntityFrameworkCore

# List packages
dotnet list package

# Check for outdated packages
dotnet list package --outdated
```

## Directory Build Configuration

### Directory.Build.props
```xml
<Project>
  <PropertyGroup>
    <TargetFramework>net8.0</TargetFramework>
    <LangVersion>latest</LangVersion>
    <Nullable>enable</Nullable>
    <TreatWarningsAsErrors>true</TreatWarningsAsErrors>
    <WarningsAsErrors />
    <WarningsNotAsErrors />
    <EnableNETAnalyzers>true</EnableNETAnalyzers>
    <AnalysisLevel>latest</AnalysisLevel>
    <EnforceCodeStyleInBuild>true</EnforceCodeStyleInBuild>
  </PropertyGroup>
</Project>
```

## Alternatives Considered

### MSBuild Directly
**Pros:**
- Lower-level control over build process
- Advanced build customization capabilities
- Integration with complex build scenarios

**Cons:**
- More complex configuration and setup
- Less user-friendly than .NET CLI
- Requires deeper MSBuild knowledge
- Platform-specific considerations

### Visual Studio with Package Manager UI
**Pros:**
- Visual interface for package management
- Integrated development experience
- Easy package browsing and installation

**Cons:**
- IDE-dependent workflow
- Not suitable for CI/CD automation
- Platform-specific (Windows-focused)
- Less scriptable and automatable

### Paket
**Pros:**
- Alternative package manager with different dependency resolution
- Better support for complex dependency scenarios
- F#-oriented package management

**Cons:**
- Additional tooling installation required
- Different workflow from standard .NET practices
- Smaller ecosystem and community
- Learning curve for team members

## Implementation Details

### Package Management Strategy

1. **Production Dependencies**
   - Core ASP.NET Core packages
   - Entity Framework Core for data access
   - Authentication and authorization libraries
   - Logging and monitoring packages

2. **Development Dependencies**
   - Code analysis tools (StyleCop, analyzers)
   - Testing frameworks and utilities
   - Development-time code generation tools
   - Documentation generation tools

3. **Build Tools**
   - .NET SDK for compilation and runtime
   - MSBuild for advanced build scenarios
   - Global tools for development utilities

### Security and Compliance

```bash
# Security vulnerability scanning
dotnet list package --vulnerable

# Package verification
dotnet nuget verify

# Package source configuration
dotnet nuget add source https://api.nuget.org/v3/index.json -n nuget.org

# Clear package cache
dotnet nuget locals all --clear
```

### CI/CD Integration

```bash
# Restore with locked versions
dotnet restore --locked-mode

# Build with specific configuration
dotnet build --configuration Release --no-restore

# Test with coverage
dotnet test --configuration Release --no-build --verbosity normal

# Publish application
dotnet publish --configuration Release --no-build --output ./publish
```

## Consequences

### Positive
- **Official Support**: Microsoft-backed toolchain with long-term support
- **Cross-Platform**: Consistent experience across Windows, macOS, and Linux
- **Ecosystem Integration**: Seamless integration with .NET ecosystem and tooling
- **Automation Ready**: Excellent CI/CD and automation support
- **Performance**: Fast builds and package restoration
- **Security**: Built-in vulnerability scanning and package verification

### Negative
- **Microsoft Ecosystem Lock-in**: Tied to Microsoft's .NET ecosystem decisions
- **Learning Curve**: Requires familiarity with .NET CLI commands and concepts
- **Configuration Complexity**: Advanced scenarios may require complex MSBuild configuration

### Neutral
- **Command-Line Focus**: Requires comfort with CLI-based development workflow
- **Version Management**: Regular updates to .NET SDK and tooling required

## Integration with Project Tools

### Code Quality Tools
- **StyleCop.Analyzers**: Code style analysis
- **Microsoft.CodeAnalysis.NetAnalyzers**: Static code analysis
- **dotnet format**: Code formatting tool

### Build Automation
- **Makefile integration**: Wrapper commands for common operations
- **GitHub Actions**: Automated CI/CD with .NET workflows
- **Docker**: Multi-stage builds with .NET SDK

### Development Tools
- **Hot Reload**: Live code updates during development
- **Watch mode**: Automatic rebuilds on file changes
- **Global tools**: Additional development utilities

## Future Considerations

1. **NuGet Central Package Management**
   - Evaluate centralized package version management for larger solutions
   - Consider `Directory.Packages.props` for multi-project solutions

2. **Package Authoring**
   - Potential for creating internal NuGet packages
   - Package publishing automation and versioning

3. **Security Enhancements**
   - Regular security scanning automation
   - Package signing and verification policies
   - Dependency update automation with testing

## References

- [.NET CLI Documentation](https://docs.microsoft.com/en-us/dotnet/core/tools/)
- [NuGet Documentation](https://docs.microsoft.com/en-us/nuget/)
- [MSBuild Reference](https://docs.microsoft.com/en-us/visualstudio/msbuild/msbuild)
- [.NET Project File Reference](https://docs.microsoft.com/en-us/dotnet/core/project-sdk/msbuild-props)
- [Package Management Best Practices](https://docs.microsoft.com/en-us/nuget/concepts/best-practices)
