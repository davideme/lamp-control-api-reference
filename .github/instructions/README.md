# GitHub Copilot Instructions Configuration

This directory contains GitHub Copilot coding agent instructions for the Lamp Control API Reference repository. These instructions help GitHub Copilot understand the specific requirements, patterns, and best practices for each language implementation in this multi-language reference project.

## File Structure

```
.github/instructions/
├── README.md                    # This file - explains the instructions setup
├── instructions.md              # Main repository-wide instructions
├── typescript.instructions.md  # TypeScript-specific guidance
├── python.instructions.md      # Python-specific guidance
├── java.instructions.md        # Java-specific guidance
├── csharp.instructions.md      # C#-specific guidance
├── php.instructions.md         # PHP-specific guidance
├── go.instructions.md          # Go-specific guidance
├── kotlin.instructions.md      # Kotlin-specific guidance
└── ruby.instructions.md        # Ruby-specific guidance
```

## How It Works

GitHub Copilot coding agents use these instruction files to understand:
- **Repository context**: What this project is and its purpose
- **Language-specific requirements**: Framework choices, patterns, and conventions
- **Quality standards**: Testing, linting, and code coverage requirements
- **Architecture patterns**: How each implementation should be structured
- **Development workflow**: How to contribute and maintain consistency

## File Format

Each instruction file follows this format:

```yaml
---
applyTo: "src/language/**/*"
---

Description and guidelines for the specific language implementation.
```

### YAML Frontmatter
- `applyTo`: Specifies which files this instruction applies to using glob patterns
- The pattern `src/language/**/*` matches all files in the language directory

### Content Guidelines
Each language-specific instruction file includes:
- **Technology stack description**: Key frameworks and tools used
- **Development practices**: Coding standards, patterns, and conventions
- **Testing requirements**: Testing frameworks, coverage expectations, and test types
- **Build and tooling**: Package managers, linters, and development workflow
- **Architecture guidance**: How to structure code and follow established patterns

## Updating Instructions

When modifying these instructions:

### Main Instructions (`instructions.md`)
Update for repository-wide changes affecting all implementations:
- API specification changes
- New quality requirements
- Updated development practices
- Architecture pattern changes

### Language-Specific Instructions
Update individual language files when:
- Framework versions change
- New tools are adopted for a language
- Language-specific patterns evolve
- Testing or linting requirements change

### Best Practices for Updates
1. **Consistency**: Ensure changes maintain consistency across implementations
2. **Specificity**: Be specific about requirements and expectations
3. **Examples**: Include examples where helpful
4. **Testing**: Verify instructions work with actual development scenarios
5. **Documentation**: Update related documentation in the main repository

## Integration with Development Workflow

These instructions integrate with:

### CI/CD Pipelines
- Each language has specific CI workflows in `.github/workflows/`
- Instructions should align with CI requirements and quality gates
- Testing and coverage expectations match CI configuration

### Repository Structure
- Instructions reference the established project structure
- Code organization patterns are documented and enforced
- Dependencies and build tools are clearly specified

### Quality Metrics
- Coverage requirements are specified and tracked
- Code quality standards are defined and measurable
- Performance and build metrics are considered

## Maintenance

### Regular Reviews
- Review instructions quarterly or when major changes occur
- Ensure instructions remain current with evolving best practices
- Update framework versions and tool recommendations
- Verify consistency across all language implementations

### Validation
- Test instructions with actual development scenarios
- Verify Copilot follows the guidance effectively
- Collect feedback from contributors and update accordingly
- Monitor code quality metrics to ensure instructions are effective

### Version Control
- All instruction changes should be reviewed through pull requests
- Document reasoning for instruction changes in commit messages
- Consider impact on existing implementations when making changes
- Maintain backward compatibility where possible

## Contributing to Instructions

When contributing changes to these instruction files:

1. **Understand the Impact**: Changes affect how Copilot assists with code generation
2. **Test Thoroughly**: Verify instructions work in practice
3. **Maintain Consistency**: Ensure changes align with other language instructions
4. **Document Changes**: Clearly explain why changes are needed
5. **Review Process**: Follow the standard PR review process for changes

## Troubleshooting

### Common Issues

**Copilot Not Following Instructions**
- Verify the `applyTo` pattern matches the files you're working on
- Ensure instructions are clear and specific
- Check for conflicts between different instruction files

**Inconsistent Behavior Across Languages**
- Review main instructions vs language-specific instructions
- Ensure consistent terminology and requirements
- Verify all language instructions follow the same patterns

**Outdated Guidance**
- Regularly review and update framework versions
- Keep best practices current with ecosystem evolution
- Monitor community standards and update accordingly

For more information about this repository's development practices, see:
- `CONTRIBUTING.md` - DORA-based development guidelines
- `docs/COMPARISON.md` - Implementation metrics and analysis
- Individual language README files in `src/[language]/`