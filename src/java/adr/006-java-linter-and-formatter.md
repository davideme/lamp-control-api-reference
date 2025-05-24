# ADR 006: Java Linter and Formatter Selection

**Status:** Proposed
**Date:** 2025-05-24

**Context:**
The Java Spring Boot project requires consistent code formatting and static analysis to ensure code quality, maintainability, and team productivity. Currently, the project lacks automated code formatting and linting tools, which can lead to:
- Inconsistent code style across team members
- Potential bugs and code smells going undetected
- Merge conflicts due to formatting differences
- Reduced code readability and maintainability

**Decision:**
The project will adopt **Spotless** for code formatting and **SpotBugs + PMD + Checkstyle** for static analysis (linting).

**Tools Selected:**

### Code Formatter: Spotless
- **Primary formatter:** Google Java Format
- **Integration:** Maven plugin for automated formatting
- **Enforcement:** Format checking in CI/CD pipeline

### Static Analysis (Linting):
1. **SpotBugs:** Bug pattern detection and security vulnerability identification
2. **PMD:** Code quality analysis and best practices enforcement
3. **Checkstyle:** Code style and convention compliance

**Implementation Approach:**

### Maven Plugin Configuration:
- Spotless Maven plugin for automated formatting
- SpotBugs Maven plugin for bug detection
- PMD Maven plugin for code quality analysis
- Checkstyle Maven plugin for style compliance

### IDE Integration:
- IntelliJ IDEA: Google Java Format plugin
- VS Code: Java formatting and extension support
- Eclipse: Google Java Format plugin

### CI/CD Integration:
- Format verification in build pipeline
- Static analysis reports generation
- Build failure on critical violations

**Consequences:**

**Positive:**
- Consistent code formatting across the entire codebase
- Early detection of potential bugs and security issues
- Improved code readability and maintainability
- Reduced time spent on code review discussions about formatting
- Better team productivity and collaboration
- Integration with existing Maven build system

**Negative:**
- Initial setup time and learning curve for team members
- Possible friction during transition period as existing code gets reformatted
- Additional build time for static analysis
- Need to configure IDE plugins for each developer

**Alternatives Considered:**

### Formatters:
- **Prettier (Java plugin):** Less mature for Java, primarily focused on web technologies
- **Eclipse Code Formatter:** Good option but less standardized than Google Java Format
- **IntelliJ IDEA formatter:** IDE-specific, not easily portable across different development environments

### Linting Tools:
- **SonarQube:** Comprehensive but requires separate server setup and may be overkill for this project size
- **Error Prone:** Google's static analysis tool, good complement but more focused on compile-time checks
- **Detekt:** Kotlin-focused, not applicable for Java projects

**Rationale:**

### Spotless with Google Java Format:
- Industry-standard formatting rules widely adopted in the Java community
- Excellent Maven integration with minimal configuration
- Zero-configuration approach reduces team debates about formatting preferences
- Strong IDE support across all major Java development environments
- Automatic code formatting on save and pre-commit hooks support

### SpotBugs + PMD + Checkstyle Combination:
- **SpotBugs:** Proven track record for bug detection, successor to FindBugs
- **PMD:** Excellent for detecting code smells and enforcing best practices
- **Checkstyle:** De facto standard for Java style checking
- All three tools have mature Maven plugins and excellent documentation
- Comprehensive coverage of different aspects of code quality
- Can be gradually introduced with configurable severity levels

**Configuration Strategy:**
- Start with lenient rules and gradually increase strictness
- Exclude generated OpenAPI code from formatting and linting
- Create team-specific rule configurations based on project needs
- Document exceptions and custom rules in project documentation

**References:**
- [Spotless Maven Plugin Documentation](https://github.com/diffplug/spotless/tree/main/plugin-maven)
- [Google Java Format](https://github.com/google/google-java-format)
- [SpotBugs Maven Plugin](https://spotbugs.github.io/spotbugs-maven-plugin/)
- [PMD Maven Plugin](https://maven.apache.org/plugins/maven-pmd-plugin/)
- [Checkstyle Maven Plugin](https://maven.apache.org/plugins/maven-checkstyle-plugin/)
