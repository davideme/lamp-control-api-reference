# ADR 012: Python HTTP Frameworks Selection

## Status

Accepted

## Context

For the Python implementation of the Lamp Control API, we need to select an appropriate HTTP framework that provides the necessary capabilities for building a robust, maintainable, and high-performance API. The framework selection impacts development efficiency, API design consistency, performance characteristics, and long-term maintenance costs.

The Lamp Control API requires:

- OpenAPI/Swagger specification support
- Robust request validation
- Structured error handling
- Authentication and authorization mechanisms
- Middleware support for cross-cutting concerns
- Good integration with other Python libraries and databases
- Active community and long-term maintenance

## Decision

We will use **FastAPI** as the primary HTTP framework for the Python implementation of the Lamp Control API.

Key factors influencing this decision:

1. **API-first Development**

   - Native OpenAPI/Swagger integration
   - Automatic documentation generation
   - Schema-based request/response validation via Pydantic
   - Strong typing support

2. **Performance**

   - Built on Starlette and Uvicorn for high performance
   - Asynchronous request handling capabilities
   - Efficient request processing with minimal overhead
   - Benchmarks show performance comparable to Node.js and Go frameworks

3. **Developer Experience**

   - Intuitive API design with decorator-based routing
   - Extensive type annotations for better IDE support
   - Reduced boilerplate compared to alternatives
   - Good development feedback loop with auto-reloading

4. **Ecosystem**

   - Compatible with ASGI servers (Uvicorn, Hypercorn, Daphne)
   - Integrates well with SQLAlchemy, Prisma, MongoDB drivers
   - Active plugin ecosystem for auth, caching, etc.
   - Works well with deployment platforms (Docker, Kubernetes)

5. **Project Maturity**
   - Stable API with semantic versioning
   - Active development and maintenance
   - Good security track record
   - Used in production by many organizations

## Consequences

### Positive

1. **Development Speed**

   - Reduced boilerplate code
   - Automatic validation and documentation
   - Strong IDE support through type annotations
   - Clear separation of concerns in project structure

2. **API Quality**

   - Consistent OpenAPI documentation
   - Strong input validation
   - Clear error responses
   - Self-documenting API endpoints

3. **Performance**

   - Efficient handling of concurrent requests
   - Low latency for IoT device communication
   - Good resource utilization under load
   - Ability to scale horizontally

4. **Maintainability**
   - Clear project structure conventions
   - Type-safe code reducing runtime errors
   - Easy to understand request/response flow
   - Good separation of business logic from HTTP concerns

### Negative

1. **Learning Curve**

   - Developers need to understand FastAPI's approach to routing
   - Knowledge of Pydantic models required
   - Async programming concepts needed for advanced use cases
   - Some complexity in dependency injection patterns

2. **Ecosystem Maturity**

   - While growing rapidly, ecosystem is less mature than Flask
   - Fewer third-party extensions compared to Django
   - Some edge cases may require custom solutions

3. **Deployment Considerations**
   - Requires ASGI server configuration
   - More complex deployment than WSGI-based frameworks
   - Optimal performance requires proper server tuning

## Alternatives Considered

1. **Flask**

   - **Why Not Selected:**
     - Synchronous by default, requiring extra work for async
     - Requires multiple extensions for API documentation
     - Less integrated API validation capabilities
     - Lower performance under concurrent load
     - Requires more boilerplate for modern API features

2. **Django REST Framework**

   - **Why Not Selected:**
     - Heavyweight with many unused features for our use case
     - Higher learning curve for API-only applications
     - Slower request handling due to middleware stack
     - More configuration required for OpenAPI support
     - Less suited for lightweight microservice architecture

3. **Starlette**

   - **Why Not Selected:**
     - More low-level than FastAPI
     - No built-in request validation
     - Manual OpenAPI documentation required
     - Less developer productivity for API-focused projects
     - FastAPI provides needed abstractions on top of Starlette

4. **Falcon**

   - **Why Not Selected:**
     - Less integrated OpenAPI support
     - More manual validation required
     - Smaller community and ecosystem
     - Fewer learning resources available
     - Less intuitive for complex API patterns

5. **Sanic**
   - **Why Not Selected:**
     - Less mature API validation ecosystem
     - Less comprehensive documentation
     - Smaller community compared to FastAPI
     - Less integrated OpenAPI tooling
     - API patterns less aligned with our project needs

## Implementation Notes

1. **Project Structure:**

   ```
   lamp_control/
     api/
       app.py          # FastAPI application instance
       middleware.py   # Custom middleware components
       routes/         # API endpoint route modules
     schemas/          # Pydantic models for request/response
     services/         # Business logic services
     db/               # Database access layer
   ```

2. **Dependencies:**

   - Add to `pyproject.toml` or `requirements.txt`:
     ```
     fastapi>=0.103.0
     uvicorn>=0.23.0
     pydantic>=2.3.0
     ```

3. **Development Setup:**

   - Server with hot-reload: `uvicorn lamp_control.api.app:app --reload`
   - API documentation available at `/docs` and `/redoc` endpoints
   - Use pytest for testing API endpoints

4. **Production Deployment:**
   - Deploy with Uvicorn behind Nginx or similar
   - Consider Gunicorn with Uvicorn workers for multi-process deployment
   - Monitor performance and adjust worker configuration as needed

## References

1. [FastAPI Documentation](https://fastapi.tiangolo.com/)
2. [Starlette Project](https://www.starlette.io/)
3. [Pydantic Documentation](https://docs.pydantic.dev/)
4. [ASGI Specification](https://asgi.readthedocs.io/)
5. [Python Web Framework Benchmarks](https://www.techempower.com/benchmarks/#section=data-r21)
