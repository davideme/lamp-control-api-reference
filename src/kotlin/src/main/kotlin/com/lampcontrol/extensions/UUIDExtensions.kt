package com.lampcontrol.extensions

import java.util.UUID

/**
 * Attempts to parse this String as a UUID.
 * Returns the parsed UUID, or null if the string is not a valid UUID format.
 */
fun String.toUuidOrNull(): UUID? =
    runCatching { UUID.fromString(this) }.getOrNull()
