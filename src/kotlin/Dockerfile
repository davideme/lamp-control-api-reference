# Multi-stage Dockerfile for Kotlin Ktor Application
# Build stage using OpenJDK 21 with Gradle wrapper
FROM eclipse-temurin:21-jdk AS build-env

# Set working directory for build
WORKDIR /app

# Copy Gradle wrapper and configuration files first for better layer caching
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle.kts .
COPY settings.gradle .
COPY gradle.properties .

# Make gradlew executable
RUN chmod +x gradlew

# Copy source code
COPY src/ src/

# Build the shadow JAR using the project's Gradle wrapper (skip tests for Docker build speed)
RUN ./gradlew shadowJar --no-daemon -q

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
