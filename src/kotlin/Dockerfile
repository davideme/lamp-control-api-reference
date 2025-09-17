# Multi-stage Dockerfile for Kotlin Ktor Application
# This expects the JAR to be built outside Docker (CI/CD pipeline)

# Runtime stage using Eclipse Temurin JRE 21
FROM eclipse-temurin:21-jre

# Install curl for health check
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create non-root user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Set working directory
WORKDIR /app

# Copy the built JAR (expects it to be built by CI/CD)
COPY build/libs/kotlin-server-1.0.0.jar /app/kotlin-server.jar

# Change ownership to non-root user
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose the port that Ktor uses
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

# Run the Ktor application with optimized JVM settings
CMD ["java", \
     "-server", \
     "-Xms512m", \
     "-Xmx1g", \
     "-XX:+UseG1GC", \
     "-XX:MaxGCPauseMillis=100", \
     "-XX:+UseStringDeduplication", \
     "-XX:+UseContainerSupport", \
     "-Djava.security.egd=file:/dev/./urandom", \
     "-jar", \
     "kotlin-server.jar"]