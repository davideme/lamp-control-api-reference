package com.lampcontrol.config

/**
 * Defines the operation modes supported by the application.
 *
 * - [MIGRATE]: Run database migrations only, then exit.
 * - [SERVE]: Run migrations and start the HTTP server (local development convenience).
 * - [SERVE_ONLY]: Start the HTTP server without running migrations (production default).
 */
enum class OperationMode(val cliValue: String) {
    MIGRATE("migrate"),
    SERVE("serve"),
    SERVE_ONLY("serve-only"),
    ;

    companion object {
        fun fromString(value: String): OperationMode? = entries.find { it.cliValue == value }
    }
}
