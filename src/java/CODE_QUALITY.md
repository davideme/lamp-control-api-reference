# Java Code Quality Tools

This document describes the code quality tools configured for the Java Spring Boot project as outlined in [ADR 006: Java Linter and Formatter Selection](adr/006-java-linter-and-formatter.md).

## Tools Overview

### 🎨 Code Formatter: Spotless with Google Java Format
- **Purpose**: Automatically format Java code according to Google Java Style Guide
- **Configuration**: Google Java Format style with automatic import organization
- **Exclusions**: Generated OpenAPI code (`org.openapitools.api` and `org.openapitools.model` packages)

### 🔍 Static Analysis Tools

1. **SpotBugs**: Detects bugs and security vulnerabilities
2. **PMD**: Analyzes code quality and enforces best practices  
3. **Checkstyle**: Enforces coding style and conventions

## Quick Start

### Format Code
```bash
# Auto-format all Java code
make format

# Or using Maven directly
mvn spotless:apply
```

### Check Code Quality
```bash
# Check formatting only
make check-format

# Run all static analysis
make lint

# Run all quality checks (format + lint)
make quality-check
```

### Maven Commands

#### Formatting
```bash
# Apply formatting
mvn spotless:apply

# Check if code is formatted correctly
mvn spotless:check
```

#### Static Analysis
```bash
# Run all static analysis tools
mvn compile spotbugs:check pmd:check checkstyle:check

# Run individual tools
mvn spotbugs:check    # Bug detection
mvn pmd:check         # Code quality
mvn checkstyle:check  # Style checking
```

#### Generate Reports
```bash
# Generate HTML reports (in target/ directory)
mvn spotbugs:spotbugs pmd:pmd checkstyle:checkstyle
```

## IDE Integration

### IntelliJ IDEA
1. Install **Google Java Format** plugin
2. Go to Settings → Other Settings → Google Java Format
3. Enable "Enable google-java-format"
4. Check "Reformat code when saving"

### VS Code
1. Install **Language Support for Java** extension pack
2. The formatter will be automatically configured via Maven settings
3. Enable format on save in VS Code settings

### Eclipse
1. Install **Google Java Format** plugin from Eclipse Marketplace
2. Go to Window → Preferences → Google Java Format
3. Enable the formatter and set it as default

## Configuration Files

- **`pom.xml`**: Maven plugin configurations
- **`spotbugs-exclude.xml`**: SpotBugs exclusion rules
- **`checkstyle.xml`**: Custom Checkstyle configuration
- **`.git/hooks/pre-commit`**: Git pre-commit hook for automatic quality checks

## CI/CD Integration

The GitHub Actions workflow `.github/workflows/java-code-quality.yml` automatically:
- Checks code formatting
- Runs all static analysis tools
- Generates and uploads reports as artifacts
- Fails builds on quality violations

## Excluding Generated Code

All tools are configured to exclude generated OpenAPI code:
- `src/main/java/org/openapitools/api/**`
- `src/main/java/org/openapitools/model/**`

## Suppressing Violations

### Spotless (Formatting)
Spotless formatting cannot be suppressed for specific lines. Use `spotless:apply` to fix formatting issues.

### SpotBugs
```java
@SuppressFBWarnings("RULE_NAME")
public void myMethod() {
    // code here
}
```

### PMD
```java
@SuppressWarnings("PMD.RuleName")
public void myMethod() {
    // code here
}
```

### Checkstyle
```java
// CHECKSTYLE.OFF: RuleName
public void myMethod() {
    // code here
}
// CHECKSTYLE.ON: RuleName
```

## Troubleshooting

### Common Issues

1. **Build fails due to formatting**: Run `make format` or `mvn spotless:apply`
2. **SpotBugs false positives**: Add exclusions to `spotbugs-exclude.xml`
3. **Checkstyle violations**: Follow the style guide or add suppressions as needed
4. **PMD violations**: Refactor code or add appropriate suppressions

### Performance
- Initial run may be slow as Maven downloads plugins
- Subsequent runs are faster due to caching
- Use `mvn compile` before static analysis for faster execution

### Getting Help
- Check the ADR: [006-java-linter-and-formatter.md](adr/006-java-linter-and-formatter.md)
- Tool documentation:
  - [Spotless](https://github.com/diffplug/spotless)
  - [SpotBugs](https://spotbugs.github.io/)
  - [PMD](https://pmd.github.io/)
  - [Checkstyle](https://checkstyle.sourceforge.io/)
