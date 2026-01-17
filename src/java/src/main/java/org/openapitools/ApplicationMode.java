package org.openapitools;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.sql.DataSource;

/**
 * Handles different application startup modes: serve, migrate, and serve-only.
 */
public class ApplicationMode {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationMode.class);

    public enum Mode {
        SERVE,       // Default: run migrations then start server
        MIGRATE,     // Run migrations only and exit
        SERVE_ONLY   // Start server without running migrations
    }

    /**
     * Run migrations only and exit
     */
    public static void runMigrationsOnly(String[] args) {
        logger.info("Running migrations only...");

        // Start Spring context to get DataSource
        System.setProperty("server.port", "0"); // Don't start HTTP server
        System.setProperty("spring.flyway.enabled", "true");

        try {
            ConfigurableApplicationContext context = SpringApplication.run(
                OpenApiGeneratorApplication.class, args
            );

            DataSource dataSource = context.getBean(DataSource.class);

            // Run Flyway migrations manually
            Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .validateOnMigrate(true)
                .load();

            int migrationsExecuted = flyway.migrate().migrationsExecuted;

            if (migrationsExecuted > 0) {
                logger.info("Successfully executed {} migration(s)", migrationsExecuted);
            } else {
                logger.info("Database schema is up to date");
            }

            context.close();
            logger.info("Migrations completed successfully");

        } catch (Exception e) {
            logger.error("Migration failed", e);
            System.exit(1);
        }
    }

    /**
     * Determine the operation mode from command line arguments
     */
    public static Mode parseMode(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("--mode=")) {
                String mode = arg.substring(7).toLowerCase();
                switch (mode) {
                    case "migrate":
                        return Mode.MIGRATE;
                    case "serve-only":
                        return Mode.SERVE_ONLY;
                    case "serve":
                        return Mode.SERVE;
                    default:
                        logger.error("Invalid mode: {}. Valid modes are: serve, migrate, serve-only", mode);
                        System.exit(1);
                }
            }
        }
        return Mode.SERVE; // Default mode
    }

    /**
     * Configure Spring properties based on the mode
     */
    public static void configureMode(Mode mode) {
        switch (mode) {
            case SERVE:
                logger.info("Starting server with automatic migrations...");
                System.setProperty("spring.flyway.enabled", "true");
                break;
            case SERVE_ONLY:
                logger.info("Starting server without running migrations...");
                System.setProperty("spring.flyway.enabled", "false");
                break;
            case MIGRATE:
                // Handled separately in runMigrationsOnly()
                break;
        }
    }
}
