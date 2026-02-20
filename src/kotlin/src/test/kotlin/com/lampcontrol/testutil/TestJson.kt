package com.lampcontrol.testutil

import com.lampcontrol.serialization.UUIDSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import java.util.UUID

/**
 * Shared JSON configuration for tests, matching the production serialization settings.
 * Use [instance] instead of defining per-test Json instances.
 */
object TestJson {
    val instance: Json =
        Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            serializersModule =
                SerializersModule {
                    contextual(UUID::class, UUIDSerializer)
                }
        }
}
