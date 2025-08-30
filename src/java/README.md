
# Lamp Control API - Java

## Overview
This is a Java Spring Boot implementation of the Lamp Control API using the [OpenAPI Generator](https://openapi-generator.tech) project.
By using the [OpenAPI-Spec](https://openapis.org), you can easily generate an API stub.
This is an example of building API stub interfaces in Java using the Spring framework.

The stubs generated can be used in your existing Spring-MVC or Spring-Boot application to create controller endpoints
by adding ```@Controller``` classes that implement the interface. Eg:
```java
@Controller
public class LampController implements LampsApi {
// implement all LampsApi methods
}
```

You can also use the interface to create [Spring-Cloud Feign clients](http://projects.spring.io/spring-cloud/spring-cloud.html#spring-cloud-feign-inheritance).Eg:
```java
@FeignClient(name="lamps", url="http://lamp-service.example.com/v1")
public interface LampClient extends LampsApi {

}
```

## Building and Running

### Prerequisites
- Java 21 (Eclipse Temurin recommended)
- Maven 3.6+

### Local Development
```bash
# Compile and run tests
mvn clean test

# Run the application
mvn spring-boot:run

# Package the application
mvn clean package
```

The application will be available at `http://localhost:8080/v1/lamps`

### Docker Multi-Stage Build

This project includes a multi-stage Dockerfile that uses OpenJDK 21 for building and Google's distroless image for runtime, following security best practices.

#### Build Docker Image
```bash
docker build -t lamp-control-api .
```

#### Run Docker Container
```bash
docker run -p 8080:8080 lamp-control-api
```

The Docker image uses:
- **Build stage**: `openjdk:21-jdk-slim` - Contains Maven and full JDK for building the application
- **Runtime stage**: `gcr.io/distroless/java21-debian12` - Minimal, secure base image with only Java runtime

#### Multi-Stage Benefits
- **Security**: Distroless images have minimal attack surface
- **Size**: Smaller runtime image (no build tools, package managers)
- **Performance**: Faster container startup and reduced resource usage

## API Documentation

Once running, the API documentation is available at:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
