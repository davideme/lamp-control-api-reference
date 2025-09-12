# Code Coverage Setup for PHP Lamp Control API

This document explains the code coverage configuration and requirements for the PHP implementation of the Lamp Control API.

## Overview

The project enforces a **minimum 80% code coverage** requirement that is checked in both CI/CD pipeline and local development.

## Coverage Configuration

### What's Included
- All business logic in the `lib/Api` package
- Model classes in `lib/Model`
- Base model functionality in `lib/BaseModel.php`
- Unit tests and integration tests
- Error handling paths

### What's Excluded
- Generated files (auto-generated OpenAPI code)
- Vendor dependencies
- Public directory assets

## Tools Used

### PHPUnit
PHPUnit is configured to generate coverage reports in multiple formats:
- **Clover XML**: For CI/CD integration and automated parsing
- **HTML Report**: For detailed line-by-line coverage visualization
- **Text Report**: For quick console output during development

### Xdebug
Coverage collection requires Xdebug extension, which is automatically configured in the CI environment.

## CI/CD Integration

### GitHub Actions Workflow
The coverage check is integrated into the CI pipeline in `.github/workflows/php-ci.yml`:

```yaml
- name: Run PHPUnit tests
  run: vendor/bin/phpunit --coverage-text --coverage-clover=coverage.xml

- name: Check code coverage
  run: |
    # ... extract coverage from clover XML ...
    if [ "$MEETS_THRESHOLD" -eq 0 ]; then
      echo "❌ Code coverage is ${COVERAGE}%, below required 80%"
      exit 1
    fi
```

### Coverage Artifacts
- Coverage profile: `coverage.xml` (Clover format)
- HTML report: `coverage-report/` directory (uploaded as artifact)
- Summary displayed in CI logs

## Local Development

### Running Tests with Coverage

```bash
# Run tests with coverage (basic)
composer test-coverage

# Or using vendor/bin directly
vendor/bin/phpunit --coverage-text --coverage-clover=coverage.xml

# Generate HTML report for detailed analysis
vendor/bin/phpunit --coverage-html coverage-report --coverage-clover=coverage.xml
```

### Using the Coverage Check Script

```bash
# Use default 80% threshold
./scripts/check-coverage.sh

# Use custom threshold
./scripts/check-coverage.sh 75

# Use custom coverage file
./scripts/check-coverage.sh 80 my-coverage.xml
```

## Coverage Script

The `scripts/check-coverage.sh` script provides automated coverage checking:

### Script Features
- ✅ Automatically runs tests if coverage file missing
- ✅ Extracts coverage from Clover XML format
- ✅ Provides detailed coverage breakdown
- ✅ Configurable thresholds
- ✅ Clear pass/fail indication
- ✅ Helpful guidance for improving coverage

## Current Coverage Status

As of the latest implementation:
- **Current Coverage**: 47.22%
- **Required Coverage**: 80%
- **Status**: ❌ Below threshold (needs improvement)

### Coverage Breakdown
- **Total Statements**: 288
- **Covered Statements**: 136
- **Uncovered Statements**: 152

## Improving Coverage

### Focus Areas
1. **API Endpoints**: Add tests for all CRUD operations
2. **Error Handling**: Test error scenarios and edge cases
3. **Model Validation**: Test data validation and serialization
4. **Business Logic**: Cover all conditional paths

### Testing Strategy
```php
// Example test structure
class DefaultApiTest extends TestCase
{
    public function testCreateLamp()
    {
        // Test successful lamp creation
    }
    
    public function testCreateLampWithInvalidData()
    {
        // Test error handling
    }
    
    public function testGetNonExistentLamp()
    {
        // Test 404 scenarios
    }
}
```

## Viewing Coverage Reports

### HTML Report
After running tests with `--coverage-html coverage-report`, open:
```
coverage-report/index.html
```

The HTML report provides:
- 🟢 Green: Covered lines
- 🔴 Red: Uncovered lines
- 📊 Coverage percentages by class and method
- 📈 Coverage trends and statistics

### Command Line
Use `--coverage-text` for immediate feedback:
```bash
vendor/bin/phpunit --coverage-text
```

## Troubleshooting

### Common Issues

1. **"Xdebug not available"**: Install/enable Xdebug extension
2. **"No coverage driver available"**: Check PHP extensions
3. **Low coverage**: Review HTML report to identify uncovered code
4. **Slow test execution**: Coverage collection adds overhead

### Performance Tips
- Use `--coverage-text` for quick checks
- Generate HTML reports only when needed
- Consider using `--filter` to test specific classes during development

### Configuration Files
- `phpunit.xml`: Main PHPUnit configuration
- `phpunit.xml.dist`: Distributed configuration template
- Coverage includes: `lib/Api`, `lib/Model`, `lib/BaseModel.php`

This comprehensive coverage setup ensures high code quality and catches regressions early in the development process.