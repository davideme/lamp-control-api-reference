# Multi-stage Dockerfile for Java Spring Boot Application
# Build stage using Maven with OpenJDK 21
FROM maven:3.9-eclipse-temurin-21 AS build-env

# Set working directory for build
WORKDIR /app

# Copy Maven configuration files first for better layer caching
COPY pom.xml .
COPY checkstyle.xml .
COPY pmd-rules.xml .
COPY spotbugs-exclude.xml .

# Copy source code
COPY src/ src/

# Build the application (skip tests for Docker build speed)
RUN mvn clean package -DskipTests -q

# Runtime stage using distroless Java 21
FROM gcr.io/distroless/java21-debian12:nonroot

# Copy the built JAR from build stage
COPY --from=build-env /app/target/openapi-spring-1.0.0.jar /app/main.jar

# Set working directory
WORKDIR /app

# Expose the port
EXPOSE 8080

# Run the Spring Boot application
CMD ["main.jar"]