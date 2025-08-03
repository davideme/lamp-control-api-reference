---
applyTo: "src/typescript/**/*"
---

This TypeScript implementation of the lamp control API uses modern Node.js and TypeScript patterns.

Key frameworks and tools:
- Express.js for the HTTP server
- TypeScript for type safety
- Jest for testing
- ESLint and Prettier for code quality
- OpenAPI specification for API documentation

When working on this codebase:
- Follow the existing TypeScript configuration in tsconfig.json
- Maintain strict type safety and use proper TypeScript types
- Follow the repository pattern established in src/infrastructure/repositories/
- Use the service layer pattern in src/infrastructure/services/
- Write comprehensive tests using Jest, following the existing test patterns
- Follow the OpenAPI specification for any API changes
- Use async/await for asynchronous operations
- Follow the existing error handling patterns
- Maintain consistency with the lamp domain model (id, isOn properties)
- Use proper HTTP status codes for REST endpoints