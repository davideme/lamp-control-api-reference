# ADR 008: TypeScript Security Middleware

**Status:** Proposed

**Date:** 2025-04-18

## Context

Web application security is paramount. We need to incorporate baseline security measures into the TypeScript REST API implementation (primarily Express.js) to mitigate common web vulnerabilities and prevent abuse.

## Decision

We will utilize the following security-focused middleware for the Express.js application:

-   **Helmet:**
    -   _Rationale:_ Helmet helps secure Express apps by setting various HTTP headers. It bundles multiple smaller middleware functions that set headers like `Strict-Transport-Security`, `X-Frame-Options`, `X-Content-Type-Options`, `Content-Security-Policy` (configurable), etc., providing protection against common attacks like cross-site scripting (XSS) and clickjacking with minimal configuration.
-   **`express-rate-limit`:**
    -   _Rationale:_ Provides basic rate limiting to protect API endpoints against brute-force attacks or denial-of-service attempts by limiting repeated requests from the same IP address.

## Consequences

-   **Pros:**
    -   Provides a good baseline security posture with minimal effort.
    -   Mitigates several common web security vulnerabilities.
    -   Protects against simple denial-of-service or brute-force attacks via rate limiting.
    -   Both libraries are widely used and maintained.
-   **Cons:**
    -   These libraries provide baseline protection; they are not a complete security solution. Input validation (covered by Zod in ADR-006), proper authentication/authorization, and secure coding practices are still essential.
    -   Helmet's `Content-Security-Policy` might require careful configuration depending on the application's needs (especially if serving front-end assets).
    -   Basic IP-based rate limiting can be bypassed (e.g., using distributed attacks) and might affect users behind shared IPs/proxies if not configured carefully.
    -   Adds dependencies.

## Alternatives Considered

-   **Manual Header Setting:** Manually setting all relevant security headers is error-prone and hard to maintain.
-   **Other Rate Limiting Libraries:** Various other rate limiting libraries exist, but `express-rate-limit` is popular and straightforward for basic use cases.
-   **Web Application Firewall (WAF):** A WAF (like AWS WAF, Cloudflare) provides broader protection at the infrastructure level but doesn't replace application-level security measures like setting correct headers.
-   **No Security Middleware:** Unacceptable due to the high risk of common vulnerabilities.

## References

-   [Helmet](https://helmetjs.github.io/)
-   [`express-rate-limit`](https://github.com/express-rate-limit/express-rate-limit)
-   [OWASP Top Ten Project](https://owasp.org/www-project-top-ten/) 