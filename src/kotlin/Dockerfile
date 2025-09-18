# Multi-stage Dockerfile for Kotlin Ktor Application
# This uses a pre-built JAR for reliability in CI/CD environments

# Runtime stage using distroless Java 21 with non-root user
FROM gcr.io/distroless/java21-debian12:nonroot

# Set working directory
WORKDIR /app

# Copy the pre-built shadow JAR (built by CI/CD)
COPY build/libs/kotlin-server-1.0.0.jar /app/kotlin-server.jar

# Expose the port that Ktor uses
EXPOSE 8080

# Run the Ktor application with optimized JVM settings
CMD ["-server", "-Xms512m", "-Xmx1g", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-XX:+UseStringDeduplication", "-XX:+UseContainerSupport", "-Djava.security.egd=file:/dev/./urandom", "-jar", "kotlin-server.jar"]