# Multi-stage Dockerfile for Kotlin Ktor Application
# Build stage using Gradle with OpenJDK 21
FROM gradle:8.5-jdk21 AS build-env

# Set working directory for build
WORKDIR /app

# Copy Gradle configuration files first for better layer caching
COPY build.gradle.kts .
COPY settings.gradle .
COPY gradle.properties .

# Copy source code
COPY src/ src/

# Build the shadow JAR (skip tests for Docker build speed)
RUN gradle shadowJar --no-daemon -q

# Runtime stage using distroless Java 21
FROM gcr.io/distroless/java21-debian12:nonroot

# Copy the built JAR from build stage
COPY --from=build-env /app/build/libs/kotlin-server.jar /app/main.jar

# Set working directory
WORKDIR /app

# Expose the port
EXPOSE 8080

# Run the Ktor application
CMD ["main.jar"]
