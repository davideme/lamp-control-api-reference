# ADR 001: Choose PHP Version for New Project

## Status

Accepted

## Date

2025-05-25

## Context

We are starting a new PHP project and must select the PHP version to ensure maximum support, compatibility, and access to recent features. The project aims for stability, maintainability, and community support over the next several years.

## Decision

We will use **PHP 8.3** as the runtime version for this project.

## Consequences

* **Security & Support:** PHP 8.3 is the latest stable release, actively maintained, with security updates until November 2026.
* **Modern Features:** Enables usage of the latest language features and performance enhancements.
* **Community & Ecosystem:** All major PHP frameworks (e.g., Laravel, Symfony) and libraries are compatible.
* **Deployment:** Most major cloud providers and hosts support PHP 8.3.
* **Future-Proofing:** Early adoption of PHP 8.4 is not recommended as it is not yet released/stable.

## Alternatives Considered

* **PHP 8.2:** Support ends December 2025; shorter maintenance window.
* **Older Versions (8.1, 8.0, 7.x):** End of life; no security or active support.
* **PHP 8.4:** Not yet released/stable as of this decision.

## Risks

* Some legacy or poorly maintained libraries may not yet be fully compatible with PHP 8.3, though this risk is minor given the maturity of the ecosystem.

## References

* [PHP Supported Versions](https://www.php.net/supported-versions.php)
* [PHP 8.3 Release Announcement](https://www.php.net/releases/8.3/)

---

Let me know if you need this customized for a specific project, or if you want a Markdown file or a Notion-ready version.
