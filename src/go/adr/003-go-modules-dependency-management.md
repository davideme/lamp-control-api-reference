# ADR 003: Go Modules as Dependency Management and Build System

## Status

Accepted

## Context

The Go implementation of the Lamp Control API requires a dependency management and build system that handles:

- Package dependency resolution and versioning
- Module management and organization
- Build process orchestration
- Cross-compilation and deployment
- Development workflow automation
- Integration with CI/CD pipelines
- Vendor directory management (if needed)

Available dependency management options for Go projects include:

1. **Go Modules** - Official Go dependency management (Go 1.11+)
2. **dep** - Legacy official dependency management tool (deprecated)
3. **Glide** - Third-party dependency management (legacy)
4. **Vendor directories** - Manual dependency management
5. **GOPATH-based development** - Legacy Go workspace approach

## Decision

We will use **Go Modules** as our dependency management and build system for the Go implementation.

## Rationale

### Why Go Modules?

1. **Official Go Toolchain**
   - Built into Go 1.11+ as the official dependency management solution
   - Default and recommended approach for all new Go projects
   - Actively maintained and developed by the Go team
   - No additional tools or installations required

2. **Semantic Versioning**
   - Full semantic versioning support with version constraints
   - Automatic version resolution and conflict detection
   - Support for pre-release and build metadata
   - Minimal version selection algorithm ensures reproducible builds

3. **Module System**
   - Clear module boundaries and dependency isolation
   - Support for nested modules and multi-module repositories
   - Module replacement and local development support
   - Proxy support for security and reliability

4. **Build Integration**
   - Seamless integration with `go build`, `go test`, `go run`
   - Automatic dependency downloading and caching
   - Cross-compilation support
   - Binary embedding and static linking

5. **Development Workflow**
   - Simple commands for dependency management
   - Automatic go.sum generation for integrity verification
   - Easy dependency updates and maintenance
   - Support for replace directives for local development

6. **Reproducible Builds**
   - `go.mod` and `go.sum` files ensure exact dependency versions
   - Cryptographic checksums for dependency verification
   - Module proxy caching for consistent downloads
   - Version pinning for stability

## Project Configuration

### Module Initialization
```bash
# Initialize new module
go mod init github.com/organization/lamp-control-api-go

# This creates go.mod file:
module github.com/organization/lamp-control-api-go

go 1.21

require (
    github.com/gin-gonic/gin v1.9.1
    github.com/stretchr/testify v1.8.4
)
```

### go.mod Structure
```go
module github.com/organization/lamp-control-api-go

go 1.21

require (
    github.com/gin-gonic/gin v1.9.1
    github.com/gorilla/mux v1.8.0
    github.com/stretchr/testify v1.8.4
)

require (
    // Indirect dependencies (automatically managed)
    github.com/bytedance/sonic v1.9.1 // indirect
    github.com/chenzhuoyu/base64x v0.0.0-20221115062448-fe3a3abad311 // indirect
    // ... other indirect dependencies
)
```

### Dependency Management Commands

```bash
# Add a dependency
go get github.com/gin-gonic/gin

# Add specific version
go get github.com/gin-gonic/gin@v1.9.1

# Update dependencies
go get -u ./...

# Update specific dependency
go get -u github.com/gin-gonic/gin

# Remove unused dependencies
go mod tidy

# Download dependencies to local cache
go mod download

# Verify dependencies
go mod verify

# Show dependency graph
go mod graph

# Check for available updates
go list -u -m all
```

### Build and Development Workflow

```bash
# Build the application
go build ./cmd/api

# Run the application
go run ./cmd/api

# Run tests
go test ./...

# Run tests with coverage
go test -cover ./...

# Build for different platforms
GOOS=linux GOARCH=amd64 go build ./cmd/api
GOOS=windows GOARCH=amd64 go build ./cmd/api

# Install the binary
go install ./cmd/api
```

## Project Structure

```
lamp-control-api-go/
├── go.mod                 # Module definition and dependencies
├── go.sum                 # Dependency checksums
├── cmd/
│   └── api/
│       └── main.go        # Application entry point
├── internal/
│   ├── handlers/          # HTTP handlers
│   ├── models/            # Data models
│   └── service/           # Business logic
├── pkg/
│   └── api/               # Public API interfaces
└── vendor/                # Vendored dependencies (optional)
```

## Alternatives Considered

### dep (Deprecated)
**Pros:**
- Was the official experimental dependency management tool
- Good version constraint specification
- Vendor directory support

**Cons:**
- Officially deprecated in favor of Go modules
- No longer maintained or supported
- Migration required to Go modules
- Additional tool installation needed

### Glide
**Pros:**
- Popular third-party solution before Go modules
- Good version management features
- YAML-based configuration

**Cons:**
- Third-party tool requiring separate installation
- Not officially supported by Go team
- Superseded by Go modules
- No longer actively maintained

### Vendor Directories
**Pros:**
- Complete dependency control and offline builds
- No external dependencies during builds
- Version pinning by copying source code

**Cons:**
- Manual dependency management overhead
- Large repository sizes with vendored code
- Complex update and maintenance process
- No automatic conflict resolution

### GOPATH-based Development
**Pros:**
- Simple workspace model
- Direct source code organization

**Cons:**
- No dependency versioning support
- Global workspace conflicts
- Difficult to manage multiple projects
- Deprecated in favor of modules

## Implementation Details

### Module Proxy Configuration
```bash
# Configure module proxy (default: https://proxy.golang.org)
export GOPROXY=https://proxy.golang.org,direct

# Disable for private modules
export GOPRIVATE=github.com/yourorg/*

# Configure checksum database
export GOSUMDB=sum.golang.org
```

### Development Practices

1. **Version Management**
   - Use semantic versioning for module versions
   - Tag releases with `git tag v1.0.0`
   - Follow Go module versioning best practices

2. **Dependency Updates**
   - Regular dependency updates with testing
   - Use `go get -u` to update to latest minor versions
   - Test thoroughly before committing updates

3. **Local Development**
   - Use `replace` directives for local module development
   - Remove replace directives before committing
   - Use `go work` for multi-module development

### CI/CD Integration

```bash
# Verify module integrity
go mod verify

# Ensure dependencies are available
go mod download

# Run tests with module verification
go test -mod=readonly ./...

# Build with specific module mode
go build -mod=readonly ./cmd/api
```

### Security Considerations

```bash
# Check for known vulnerabilities
go list -json -deps ./... | nancy sleuth

# Use Go's built-in vulnerability checking (Go 1.18+)
go list -json -deps ./... | go version -m

# Verify module checksums
go mod verify
```

## Consequences

### Positive
- **Official Support**: Maintained by Go team with guaranteed compatibility
- **Simplicity**: Minimal configuration and learning curve
- **Reproducibility**: Deterministic builds with cryptographic verification
- **Integration**: Seamless integration with Go toolchain
- **Performance**: Fast dependency resolution and builds
- **Security**: Built-in checksum verification and proxy support

### Negative
- **Go Version Requirement**: Requires Go 1.11+ (not an issue with Go 1.21)
- **Learning Curve**: Teams familiar with older Go dependency management need adaptation
- **Proxy Dependency**: Default configuration requires internet access for public modules

### Neutral
- **Module Boundaries**: Requires thinking about module organization and API design
- **Versioning**: Need to follow semantic versioning practices for module releases

## Future Considerations

1. **Multi-Module Repository**
   - Evaluate need for multiple modules in single repository
   - Consider Go workspaces for multi-module development

2. **Private Module Registry**
   - Set up private module proxy if needed for proprietary dependencies
   - Configure authentication for private module access

3. **Vendoring**
   - Consider `go mod vendor` for air-gapped deployments
   - Evaluate pros/cons of committing vendor directory

4. **Dependency Security**
   - Implement automated dependency vulnerability scanning
   - Regular dependency updates and security audits

## References

- [Go Modules Documentation](https://golang.org/ref/mod)
- [Go Modules Tutorial](https://golang.org/doc/tutorial/create-module)
- [Module Release and Versioning Workflow](https://golang.org/doc/modules/release-workflow)
- [Module Proxy Protocol](https://golang.org/ref/mod#module-proxy)
- [Go Modules Best Practices](https://golang.org/wiki/Modules)
