package com.lampcontrol.domain

/**
 * Sealed exception hierarchy for domain-level errors.
 * These exceptions are mapped to HTTP responses by Ktor's StatusPages plugin.
 */
sealed class DomainException(message: String) : Exception(message) {
    /** The requested resource was not found. */
    data class NotFound(val resourceId: String) : DomainException("Resource not found: $resourceId")

    /** The provided ID is not a valid format (e.g., not a valid UUID). */
    data class InvalidId(val id: String) : DomainException("Invalid ID format: $id")
}
