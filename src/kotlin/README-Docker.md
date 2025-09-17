# Docker Configuration for Kotlin Lamp Control API

This directory contains a multi-stage Docker configuration for the Kotlin implementation of the Lamp Control API using Ktor framework.

## Docker Configuration

### Dockerfile Features
- **Multi-stage build approach**: Build stage for dependencies, runtime stage for execution
- **Security-focused**: Non-root user execution, minimal attack surface
- **Optimized**: Layer caching, health checks, optimized JVM settings
- **Production-ready**: Eclipse Temurin JRE 21, proper resource limits

### Build Process
1. **Build Stage**: Uses Eclipse Temurin JDK 21 for building
2. **Runtime Stage**: Uses Eclipse Temurin JRE 21 for execution
3. **Fat JAR**: Includes all dependencies using Shadow plugin
4. **Security**: Runs as non-root user `appuser`

## Usage

### Building the Application
```bash
# Build the shadow JAR
./gradlew shadowJar

# Build Docker image
docker build -t kotlin-lamp-api .

# Or build with specific tag
docker build -t kotlin-lamp-api:v1.0.0 .
```

### Running the Container
```bash
# Run with port mapping
docker run -p 8080:8080 kotlin-lamp-api

# Run in background
docker run -d -p 8080:8080 --name lamp-api kotlin-lamp-api

# Run with resource limits
docker run -p 8080:8080 \
  --memory=1g \
  --cpus=1 \
  kotlin-lamp-api
```

### Testing the API
```bash
# Health check
curl http://localhost:8080/health

# List lamps
curl http://localhost:8080/v1/lamps

# Get specific lamp
curl http://localhost:8080/v1/lamps/lamp-001
```

## Configuration

### Environment Variables
The application supports configuration through environment variables:

- `KTOR_ENV`: Environment (development, production)
- `KTOR_PORT`: Server port (default: 8080)
- `KTOR_HOST`: Server host (default: 0.0.0.0)

### JVM Settings
The Docker container uses optimized JVM settings:
- **Memory**: 512MB initial, 1GB maximum
- **GC**: G1 garbage collector with 100ms pause target
- **Container**: Container-aware JVM settings
- **Security**: Fast random number generation

## Health Checks

The Docker container includes built-in health checks:
- **Endpoint**: `/health`
- **Interval**: 30 seconds
- **Timeout**: 3 seconds
- **Retries**: 3 attempts

## CI/CD Integration

The Docker configuration is integrated with GitHub Actions:

1. **Build Job**: Builds shadow JAR
2. **Docker Job**: Creates and tests Docker image
3. **Testing**: Validates container startup and API endpoints
4. **Cleanup**: Removes test containers and images

### GitHub Actions Workflow
```yaml
- name: Build shadow JAR for Docker
  run: ./gradlew shadowJar

- name: Build Docker image
  run: docker build -t kotlin-lamp-api:${{ github.sha }} .

- name: Test Docker container
  run: |
    docker run -d --name test-container -p 8080:8080 kotlin-lamp-api:${{ github.sha }}
    sleep 10
    curl -f http://localhost:8080/health
    curl -f -X GET http://localhost:8080/v1/lamps
    docker stop test-container
    docker rm test-container
```

## File Structure

```
.
├── Dockerfile              # Main Docker configuration
├── Dockerfile.multistage   # Alternative full build version
├── .dockerignore           # Docker ignore patterns
├── build.gradle.kts        # Build configuration with Shadow plugin
└── README-Docker.md        # This documentation
```

## Troubleshooting

### Common Issues

1. **JAR not found**: Ensure `./gradlew shadowJar` has been run
2. **Port conflicts**: Change port mapping `-p 8081:8080`
3. **Memory issues**: Increase Docker memory limits
4. **Health check fails**: Check application logs

### Logs
```bash
# View container logs
docker logs <container-name>

# Follow logs
docker logs -f <container-name>
```

### Debug Mode
```bash
# Run with debug output
docker run -p 8080:8080 -e KTOR_ENV=development kotlin-lamp-api
```

## Security Considerations

- Uses non-root user for container execution
- Minimal base image (Eclipse Temurin JRE)
- No unnecessary packages installed
- Health checks for monitoring
- Resource limits configurable
- SSL/TLS termination handled by reverse proxy (recommended)

## Performance

- **Startup time**: ~3-5 seconds
- **Memory usage**: ~300-500MB under normal load
- **CPU usage**: Minimal for typical API workloads
- **Concurrent connections**: Hundreds with default settings

## Alternative Build

The `Dockerfile.multistage` provides a complete source-to-image build but may have SSL certificate issues in some Docker environments. The main `Dockerfile` follows the CI/CD pattern where the JAR is built outside Docker for better reliability and caching.