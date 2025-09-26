# ADR 007: Node.js Type Stripping Compatibility

**Status:** Accepted

**Date:** 2025-01-26

## Context

Node.js introduced Type Stripping as a native feature in v22.6.0, allowing TypeScript files to run directly without compilation. This feature enables lightweight TypeScript execution by stripping inline types and replacing them with whitespace, eliminating the need for source maps and complex build processes.

The Type Stripping feature is designed to be lightweight and only supports erasable TypeScript syntax. Features requiring JavaScript code generation (like enums, parameter properties, namespaces with runtime code) need the `--experimental-transform-types` flag.

## Decision

We will update the TypeScript implementation to be fully compatible with Node.js Type Stripping, enabling the project to run TypeScript files directly without compilation while maintaining TypeScript's type safety benefits during development.

### Key Configuration Changes

1. **Updated tsconfig.json** with Type Stripping compatible settings:
   ```json
   {
     "compilerOptions": {
       "target": "esnext",
       "module": "nodenext", 
       "moduleResolution": "nodenext",
       "rewriteRelativeImportExtensions": true,
       "erasableSyntaxOnly": true,
       "verbatimModuleSyntax": true
     }
   }
   ```

2. **Updated import statements**:
   - Use explicit `type` keyword for type-only imports to comply with `verbatimModuleSyntax`
   - Include `.ts` file extensions in relative imports as required by `nodenext` module resolution
   - Convert parameter properties to explicit constructor assignments (incompatible with erasable-only syntax)

3. **Added npm scripts** for native TypeScript execution:
   - `start:native`: Run production using `node --experimental-strip-types`
   - `dev:native`: Run development using `node --experimental-strip-types`

## Rationale

- **Performance**: Eliminates compilation step for development and can improve startup times
- **Simplicity**: Reduces build complexity by leveraging native Node.js capabilities
- **Future-ready**: Aligns with Node.js's direction toward native TypeScript support
- **Backward compatibility**: Maintains full compatibility with traditional compilation workflows
- **Type safety**: Preserves all TypeScript type checking benefits during development

## Implementation Details

### Import Statement Updates
Before (incompatible):
```typescript
import { LampEntity } from '../entities/LampEntity';
import { FastifyReply, FastifyRequest } from 'fastify';
```

After (compatible):
```typescript  
import type { LampEntity } from '../entities/LampEntity.ts';
import type { FastifyReply, FastifyRequest } from 'fastify';
```

### Constructor Pattern Updates
Before (parameter properties, requires transformation):
```typescript
constructor(private readonly repository: LampRepository) {}
```

After (explicit assignment, erasable-only):
```typescript
private readonly repository: LampRepository;

constructor(repository: LampRepository) {
  this.repository = repository;
}
```

### Supported Features
- Interface and type definitions ✅
- Type annotations ✅  
- Generic types ✅
- Union and intersection types ✅
- Type assertions ✅
- Import type statements ✅

### Unsupported Features (require `--experimental-transform-types`)
- Enum declarations ❌
- Parameter properties ❌ (converted to explicit assignments)
- Namespaces with runtime code ❌
- Legacy module syntax with runtime code ❌

## Consequences

### Positive
- **Faster development workflow**: No compilation step needed for running TypeScript
- **Simplified tooling**: Can run TypeScript directly with Node.js
- **Reduced dependencies**: Less reliance on TypeScript compilation tools for basic execution
- **Better debugging**: Direct execution without source maps
- **Future compatibility**: Aligned with Node.js native TypeScript direction

### Negative
- **Node.js version requirement**: Requires Node.js 22.6.0+ for type stripping support
- **Syntax restrictions**: Limited to erasable TypeScript syntax only
- **Feature limitations**: Some TypeScript features still require compilation
- **Learning curve**: Developers need to understand type stripping limitations

## Alternatives Considered

1. **Continue using tsx/ts-node only**: Would miss the benefits of native Node.js type support
2. **Abandon TypeScript**: Would lose type safety benefits 
3. **Use only compiled JavaScript**: Would increase build complexity and lose development benefits

## Migration Impact

- **Zero breaking changes**: All existing npm scripts and workflows continue to work
- **Additive enhancement**: New type stripping scripts added alongside existing ones
- **Progressive adoption**: Teams can gradually migrate to native type stripping workflows

## References

- [Node.js Type Stripping Documentation](https://nodejs.org/docs/latest/api/typescript.html)
- [TypeScript 5.8+ Compatibility Requirements](https://www.typescriptlang.org/)
- [Node.js --experimental-strip-types flag](https://nodejs.org/docs/latest/api/cli.html#--experimental-strip-types)