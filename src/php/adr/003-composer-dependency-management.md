# ADR 003: Composer as Dependency Manager and Build Tool

## Status

Accepted

## Context

The PHP implementation of the Lamp Control API requires a robust dependency management and build automation system to handle:

- Package installation and version management
- Autoloading and namespace management
- Development vs production dependency separation
- Reproducible builds across environments
- Script automation and build tasks
- Integration with CI/CD pipelines
- Security vulnerability tracking

Available dependency management options for PHP projects include:

1. **Composer** - Standard PHP dependency manager
2. **PEAR** - Legacy PHP package manager
3. **Manual dependency management** - Direct file inclusion
4. **Git submodules** - Version control-based dependencies
5. **Custom package management** - Framework-specific solutions

## Decision

We will use **Composer** as our dependency manager and build automation tool for the PHP implementation.

## Rationale

### Why Composer?

1. **PHP Standard**
   - De facto standard for modern PHP dependency management
   - Adopted by virtually all modern PHP frameworks and libraries
   - Actively maintained by the PHP community
   - Cross-platform compatibility (Windows, macOS, Linux)

2. **Package Management**
   - Packagist.org as central repository with extensive PHP library ecosystem
   - Semantic versioning support with flexible constraints
   - Transitive dependency resolution with conflict detection
   - Support for multiple package repositories

3. **Autoloading**
   - PSR-4 and PSR-0 autoloading standards support
   - Automatic class loading without manual includes
   - Optimized autoloader generation for production
   - Custom autoloading rules support

4. **Development Workflow**
   - Simple JSON-based configuration
   - Built-in script execution capabilities
   - Development vs production dependency separation
   - Lock file generation for reproducible builds

5. **Build Automation**
   - Custom script definitions for build tasks
   - Pre and post-install/update hooks
   - Environment-specific configurations
   - Integration with popular PHP tools

6. **Security and Maintenance**
   - Security advisory integration
   - Vulnerability scanning capabilities
   - Package verification and integrity checking
   - Regular updates and maintenance

## Project Configuration

### composer.json Structure
```json
{
    "name": "lamp-control-api/php-implementation",
    "description": "PHP implementation of the Lamp Control API",
    "type": "project",
    "license": "MIT",
    "require": {
        "php": "^8.2",
        "slim/slim": "^4.12",
        "slim/psr7": "^1.6",
        "monolog/monolog": "^3.4",
        "vlucas/phpdotenv": "^5.5",
        "doctrine/dbal": "^3.6"
    },
    "require-dev": {
        "phpunit/phpunit": "^10.3",
        "squizlabs/php_codesniffer": "^3.7",
        "phpstan/phpstan": "^1.10",
        "friendsofphp/php-cs-fixer": "^3.22",
        "roave/security-advisories": "dev-latest"
    },
    "autoload": {
        "psr-4": {
            "LampControlApi\\": "src/"
        }
    },
    "autoload-dev": {
        "psr-4": {
            "LampControlApi\\Tests\\": "tests/"
        }
    },
    "scripts": {
        "start": "php -S localhost:8080 public/index.php",
        "test": "phpunit",
        "test-coverage": "phpunit --coverage-html coverage",
        "lint": "phpcs --standard=PSR12 src tests",
        "lint-fix": "phpcbf --standard=PSR12 src tests",
        "analyze": "phpstan analyse src tests --level=8",
        "format": "php-cs-fixer fix",
        "format-check": "php-cs-fixer fix --dry-run --diff",
        "security-check": "composer audit",
        "post-install-cmd": [
            "@composer dump-autoload --optimize"
        ],
        "post-update-cmd": [
            "@composer dump-autoload --optimize"
        ]
    },
    "config": {
        "optimize-autoloader": true,
        "sort-packages": true,
        "allow-plugins": {
            "composer/package-versions-deprecated": true
        }
    },
    "minimum-stability": "stable",
    "prefer-stable": true
}
```

### Development Workflow

```bash
# Install dependencies
composer install

# Install without dev dependencies (production)
composer install --no-dev --optimize-autoloader

# Add a package
composer require slim/slim

# Add a development package
composer require --dev phpunit/phpunit

# Remove a package
composer remove package/name

# Update all packages
composer update

# Update specific package
composer update slim/slim

# Show package information
composer show package/name

# Validate composer.json
composer validate

# Generate optimized autoloader
composer dump-autoload --optimize
```

### Script Execution
```bash
# Run built-in scripts
composer start                 # Start development server
composer test                  # Run tests
composer lint                  # Check code style
composer analyze               # Run static analysis
composer format                # Fix code formatting
composer security-check        # Check for vulnerabilities

# Run with custom arguments
composer test -- --filter=LampTest
composer lint -- src/
```

## Dependency Categories

### Production Dependencies
```json
{
    "require": {
        "php": "^8.2",
        "slim/slim": "^4.12",           // Web framework
        "slim/psr7": "^1.6",            // PSR-7 implementation
        "monolog/monolog": "^3.4",      // Logging
        "vlucas/phpdotenv": "^5.5",     // Environment configuration
        "doctrine/dbal": "^3.6",        // Database abstraction
        "ramsey/uuid": "^4.7",          // UUID generation
        "guzzlehttp/guzzle": "^7.8"     // HTTP client
    }
}
```

### Development Dependencies
```json
{
    "require-dev": {
        "phpunit/phpunit": "^10.3",                    // Testing framework
        "squizlabs/php_codesniffer": "^3.7",           // Code style checker
        "phpstan/phpstan": "^1.10",                    // Static analysis
        "friendsofphp/php-cs-fixer": "^3.22",          // Code formatter
        "roave/security-advisories": "dev-latest",     // Security advisories
        "symfony/var-dumper": "^6.3",                  // Debugging utilities
        "fakerphp/faker": "^1.23"                      // Test data generation
    }
}
```

## Directory Structure

```
lamp-control-api-php/
├── composer.json              # Dependency configuration
├── composer.lock              # Lock file (auto-generated)
├── vendor/                    # Installed packages (git-ignored)
│   ├── autoload.php           # Composer autoloader
│   └── ...                    # Package files
├── src/                       # Application source code
│   ├── Controllers/
│   ├── Models/
│   └── Services/
├── tests/                     # Test files
├── public/                    # Web-accessible files
│   └── index.php              # Application entry point
├── config/                    # Configuration files
└── storage/                   # Application storage
    └── logs/
```

## Autoloading Configuration

### PSR-4 Autoloading
```json
{
    "autoload": {
        "psr-4": {
            "LampControlApi\\": "src/",
            "LampControlApi\\Controllers\\": "src/Controllers/",
            "LampControlApi\\Models\\": "src/Models/"
        },
        "files": [
            "src/helpers.php"
        ]
    },
    "autoload-dev": {
        "psr-4": {
            "LampControlApi\\Tests\\": "tests/"
        }
    }
}
```

### Usage in Code
```php
<?php
// Require Composer autoloader
require_once __DIR__ . '/../vendor/autoload.php';

// Classes are automatically loaded
use LampControlApi\Controllers\LampController;
use LampControlApi\Models\Lamp;

$controller = new LampController();
$lamp = new Lamp();
```

## Alternatives Considered

### PEAR (PHP Extension and Application Repository)
**Pros:**
- Mature package management system
- System-wide package installation
- PECL extension management

**Cons:**
- Legacy system with declining adoption
- Global installation conflicts
- Limited modern PHP framework support
- Complex dependency resolution

### Manual Dependency Management
**Pros:**
- Complete control over dependencies
- No external tools required
- Simple for very basic projects

**Cons:**
- Manual version management overhead
- No automatic dependency resolution
- Security vulnerabilities difficult to track
- No autoloading standardization

### Git Submodules
**Pros:**
- Version control integration
- Direct source code access
- No external package manager

**Cons:**
- Complex dependency updates
- No semantic versioning support
- Manual autoloading configuration
- Difficult conflict resolution

### Framework-Specific Solutions
**Pros:**
- Tight integration with specific frameworks
- Framework-optimized features

**Cons:**
- Vendor lock-in to specific framework
- Limited ecosystem compared to Composer
- Migration complexity between frameworks

## Security Management

### Security Advisories
```bash
# Check for known vulnerabilities
composer audit

# Install security advisories package
composer require --dev roave/security-advisories:dev-latest

# Update to get latest security information
composer update roave/security-advisories
```

### Best Practices
1. **Regular Updates**
   - Update packages regularly with testing
   - Monitor security advisories
   - Use `composer outdated` to check for updates

2. **Version Constraints**
   - Use caret (`^`) for compatible updates
   - Use tilde (`~`) for more restrictive updates
   - Pin specific versions for critical dependencies

3. **Lock File Management**
   - Always commit `composer.lock` to version control
   - Use `composer install` in production (not `update`)
   - Regular lock file updates in controlled manner

## CI/CD Integration

### Installation Commands
```bash
# Production installation
composer install --no-dev --optimize-autoloader --no-interaction

# Development installation
composer install --optimize-autoloader

# Verify installation
composer validate --strict
```

### CI Configuration Example
```yaml
# .github/workflows/php.yml
name: PHP CI
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - uses: shivammathur/setup-php@v2
      with:
        php-version: '8.2'
        extensions: mbstring, xml, ctype, iconv, intl, pdo_sqlite
        tools: composer:v2
    - run: composer validate --strict
    - run: composer install --prefer-dist --no-progress
    - run: composer test
    - run: composer lint
    - run: composer analyze
    - run: composer security-check
```

## Performance Optimization

### Production Optimizations
```bash
# Optimize autoloader for production
composer dump-autoload --optimize --classmap-authoritative

# Install without dev dependencies
composer install --no-dev --optimize-autoloader

# Use APCu cache for autoloader (if available)
composer config cache-files-ttl 86400
```

### Development Optimizations
```json
{
    "config": {
        "optimize-autoloader": true,
        "classmap-authoritative": false,
        "apcu-autoloader": false,
        "cache-files-ttl": 86400
    }
}
```

## Consequences

### Positive
- **Industry Standard**: Universal adoption in modern PHP development
- **Rich Ecosystem**: Access to extensive package repository (Packagist)
- **Autoloading**: Standardized PSR-4 autoloading eliminates manual includes
- **Security**: Built-in vulnerability scanning and advisory system
- **Build Automation**: Custom scripts for development and deployment tasks
- **Reproducible Builds**: Lock files ensure consistent environments

### Negative
- **Internet Dependency**: Requires internet access for package installation
- **Vendor Directory Size**: Can grow large with many dependencies
- **Lock File Conflicts**: Potential merge conflicts in team development

### Neutral
- **Learning Curve**: Minimal for modern PHP developers
- **Configuration**: JSON-based configuration is straightforward

## Future Considerations

1. **Private Repositories**
   - Set up private Packagist for proprietary packages
   - Configure authentication for private repositories

2. **Package Development**
   - Create internal packages for shared code
   - Implement package versioning and release automation

3. **Performance Monitoring**
   - Monitor package update impact on application performance
   - Optimize autoloader configuration for production workloads

4. **Security Automation**
   - Implement automated security scanning in CI/CD
   - Set up dependency update automation with testing

## References

- [Composer Documentation](https://getcomposer.org/doc/)
- [Packagist Repository](https://packagist.org/)
- [PSR-4 Autoloading Standard](https://www.php-fig.org/psr/psr-4/)
- [PHP Security Advisories](https://github.com/FriendsOfPHP/security-advisories)
- [Composer Best Practices](https://blog.martinhujer.cz/17-tips-for-using-composer-efficiently/)
