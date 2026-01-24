package com.lampcontrol

import com.lampcontrol.database.DatabaseConfig
import com.lampcontrol.database.FlywayConfig
import com.lampcontrol.plugins.configureHTTP
import com.lampcontrol.plugins.configureMonitoring
import com.lampcontrol.plugins.configureResources
import com.lampcontrol.plugins.configureRouting
import com.lampcontrol.plugins.configureSerialization
import com.lampcontrol.plugins.configureStatusPages
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

private val logger = LoggerFactory.getLogger("Application")
private const val DEFAULT_PORT = 8080

fun main(args: Array<String>) {
    // Parse command line arguments
    val mode = args.find { it.startsWith("--mode=") }?.substringAfter("=") ?: "serve-only"

    when (mode) {
        "migrate" -> runMigrationsOnly()
        "serve" -> startServer(runMigrations = true)
        "serve-only" -> startServer(runMigrations = false)
        else -> {
            logger.error("Invalid mode: $mode. Valid modes are: serve, migrate, serve-only")
            exitProcess(1)
        }
    }
}

/**
 * Run database migrations only and exit
 */
fun runMigrationsOnly() {
    logger.info("Running migrations only...")
    val config = DatabaseConfig.fromEnv()

    if (config == null) {
        logger.warn("No PostgreSQL configuration found, nothing to migrate")
        return
    }

    logger.info("Running migrations for database: ${config.host}:${config.port}/${config.database}")

    val success = FlywayConfig.runMigrations(config)
    if (!success) {
        logger.error("Migrations failed")
        exitProcess(1)
    }

    logger.info("Migrations completed successfully")
}

/**
 * Start the server with optional migrations
 */
fun startServer(runMigrations: Boolean) {
    if (runMigrations) {
        logger.info("Starting server with automatic migrations...")
    } else {
        logger.info("Starting server without running migrations...")
        // Set system property to skip migrations in DatabaseFactory
        System.setProperty("skip.migrations", "true")
    }

    val port = System.getenv("KTOR_PORT")?.toIntOrNull()
        ?: System.getenv("PORT")?.toIntOrNull()
        ?: DEFAULT_PORT

    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureResources()
    configureSerialization()
    configureHTTP()
    configureMonitoring()
    configureStatusPages()
    configureRouting()
}
