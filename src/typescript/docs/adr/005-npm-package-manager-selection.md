# ADR 005: npm as Package Manager and Build Tool

## Status

Accepted

## Context

The TypeScript Lamp Control API implementation requires a robust package manager and build orchestration system to handle:

- Dependency installation and version management
- Script execution and build automation
- Development vs production dependency separation
- Lock file management for reproducible builds
- Integration with CI/CD pipelines
- Compatibility with Node.js ecosystem tools

Available package management options for Node.js/TypeScript projects include:

1. **npm** - Default Node.js package manager
2. **Yarn** - Facebook's alternative package manager
3. **pnpm** - Performant npm alternative with efficient storage
4. **Bun** - Modern JavaScript runtime with built-in package manager

## Decision

We will use **npm** as our package manager and build tool orchestrator for the TypeScript implementation.

## Rationale

### Why npm?

1. **Default and Universal**
   - Ships with Node.js by default (version 10.x+ required)
   - No additional installation or setup required
   - Universal compatibility across all environments
   - Standard choice for most Node.js projects

2. **Dependency Management**
   - Semantic versioning support with flexible version ranges
   - Automatic lock file generation (`package-lock.json`)
   - Reliable dependency resolution algorithm
   - Support for scoped packages and private registries

3. **Build Script Integration**
   - Built-in script execution via `package.json` scripts
   - Environment variable support
   - Cross-platform script compatibility
   - Integration with popular build tools (TypeScript, Jest, ESLint)

4. **Ecosystem Compatibility**
   - Works with all TypeScript and Node.js tools
   - Extensive registry with millions of packages
   - Security audit capabilities (`npm audit`)
   - Workspaces support for monorepo scenarios

5. **Development Workflow**
   - Separate development and production dependencies
   - Global and local package installation
   - Built-in package linking for development
   - Version management and publishing capabilities

6. **CI/CD Integration**
   - `npm ci` for deterministic, reproducible builds
   - Cache-friendly for CI environments
   - Security scanning integration
   - Artifact publishing capabilities

## Project Configuration

### Package.json Structure
```json
{
  "name": "lamp-control-api",
  "version": "1.0.0",
  "type": "module",
  "engines": {
    "node": ">=22.x",
    "npm": ">=10.x"
  },
  "scripts": {
    "build": "tsc",
    "dev": "tsx src/index.ts",
    "start": "node dist/index.js",
    "test": "NODE_OPTIONS=--experimental-vm-modules jest",
    "lint": "eslint . --ext .ts",
    "format": "prettier --write \"src/**/*.ts\""
  }
}
```

### Dependency Categories

1. **Production Dependencies**
   - Core application libraries (Express, etc.)
   - Runtime utilities and frameworks
   - Database drivers and ORMs

2. **Development Dependencies**
   - TypeScript compiler and type definitions
   - Testing frameworks (Jest) and utilities
   - Code quality tools (ESLint, Prettier)
   - Build and development tools (tsx, nodemon)

### Lock File Management
- `package-lock.json` ensures reproducible builds
- Committed to version control
- Automatically updated when dependencies change
- Used by `npm ci` in CI/CD for exact dependency installation

## Alternatives Considered

### Yarn
**Pros:**
- Faster dependency installation with parallel downloads
- Better workspace support for monorepos
- Yarn Berry (v2+) offers Plug'n'Play and zero-installs
- More deterministic dependency resolution

**Cons:**
- Additional tool installation required
- Different CLI commands and workflow
- Yarn Berry has breaking changes from v1
- Less universal adoption than npm

### pnpm
**Pros:**
- Significant disk space savings through content-addressable storage
- Faster installation times
- Strict dependency resolution (no phantom dependencies)
- Good monorepo support

**Cons:**
- Additional installation step required
- Different CLI and workflow
- Potential compatibility issues with some packages
- Smaller ecosystem and community

### Bun
**Pros:**
- Extremely fast package installation and script execution
- Built-in bundler and test runner
- Native TypeScript support
- Modern JavaScript runtime with better performance

**Cons:**
- Very new tool with potential stability issues
- Smaller ecosystem and community support
- May have compatibility issues with existing Node.js packages
- Still maturing for production use

## Implementation Details

### Development Workflow

```bash
# Install dependencies
npm install

# Development with hot reload
npm run dev

# Build for production
npm run build

# Run tests
npm run test

# Code quality checks
npm run lint
npm run format

# Production start
npm start
```

### CI/CD Integration

```bash
# Clean installation for CI
npm ci

# Run full test suite
npm run test:coverage

# Build and verify
npm run build
npm run lint
```

### Security and Maintenance

```bash
# Audit dependencies for vulnerabilities
npm audit

# Fix automatically resolvable vulnerabilities
npm audit fix

# Check for outdated packages
npm outdated

# Update dependencies
npm update
```

## Consequences

### Positive
- **Zero Setup**: Works out of the box with Node.js installation
- **Universal Compatibility**: Works in all environments without additional configuration
- **Ecosystem Access**: Full access to npm registry and package ecosystem
- **Standard Workflow**: Familiar commands and processes for most developers
- **CI/CD Ready**: Excellent support for automated builds and deployments

### Negative
- **Performance**: Slower than some alternatives (pnpm, Yarn) for large projects
- **Disk Usage**: Less efficient storage compared to pnpm
- **Phantom Dependencies**: Allows access to indirect dependencies (less strict than pnpm)

### Neutral
- **Maturity**: Well-established tool with predictable behavior
- **Learning Curve**: Minimal for developers familiar with Node.js ecosystem

## Future Considerations

1. **Performance Optimization**
   - Consider `.npmrc` configuration for faster installs
   - Evaluate npm workspaces if project grows to monorepo

2. **Security**
   - Regular dependency audits
   - Consider tools like Snyk for enhanced security scanning
   - Implement automated dependency updates with Dependabot

3. **Migration Path**
   - If performance becomes critical, evaluate migration to pnpm
   - Monitor Bun development for future consideration
   - Maintain compatibility with standard npm workflow

## References

- [npm Documentation](https://docs.npmjs.com/)
- [Node.js Package Manager Comparison](https://nodejs.dev/learn/an-introduction-to-the-npm-package-manager)
- [package.json Specification](https://docs.npmjs.com/cli/v8/configuring-npm/package-json)
- [npm CLI Commands](https://docs.npmjs.com/cli/v8/commands)
- [npm Security Best Practices](https://docs.npmjs.com/security)
