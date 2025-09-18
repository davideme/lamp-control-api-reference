# Docker Configuration for Kotlin Lamp Control API

This directory contains a Docker configuration for the Kotlin implementation of the Lamp Control API using Ktor framework.

## Docker Configuration

### Dockerfile Features
- **Distroless base image**: Uses `gcr.io/distroless/java21-debian12:nonroot` for maximum security
- **Pre-built JAR approach**: Uses shadow JAR built by CI/CD for reliability
- **Non-root user**: Runs as non-root user for enhanced security
- **Minimal attack surface**: Distroless image contains only JRE and application
- **Optimized JVM settings**: G1GC with container-aware configuration

### Build Process
1. **CI/CD builds shadow JAR**: Gradle builds fat JAR with all dependencies
2. **Docker packages JAR**: Simple Docker layer with pre-built artifact
3. **Distroless runtime**: Secure, minimal runtime environment

## Usage

### Building the Application
```bash
# Build the shadow JAR first
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
# Test that container started successfully (no curl in distroless)
docker ps | grep kotlin-lamp-api

# Check application logs
docker logs <container-name>
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

## Security Features

### Distroless Base Image
- **No shell**: No bash, sh, or other shells available
- **No package manager**: No apt, yum, or other package managers
- **Minimal OS**: Only essential libraries for Java runtime
- **Non-root user**: Runs as user ID 65532 (nonroot)
- **Read-only filesystem**: Application layer is read-only

### Benefits
- **Reduced attack surface**: Minimal components reduce vulnerability exposure
- **No shell access**: Prevents shell-based attacks
- **Reproducible builds**: Distroless images are immutable and reproducible

## CI/CD Integration

The Docker configuration is integrated with GitHub Actions:

1. **Build Job**: Builds shadow JAR outside Docker for reliability
2. **Docker Job**: Creates Docker image with pre-built JAR
3. **Testing**: Validates container startup and process health
4. **No health checks**: Distroless doesn't support curl-based health checks

### GitHub Actions Workflow
```yaml
- name: Build shadow JAR for Docker
  run: ./gradlew shadowJar

- name: Build Docker image
  run: docker build -t kotlin-lamp-api:${{ github.sha }} .

- name: Test Docker container startup
  run: |
    docker run -d --name test-container -p 8080:8080 kotlin-lamp-api:${{ github.sha }}
    sleep 15
    
    # Check if container is still running
    if ! docker ps | grep test-container; then
      echo "Container failed to start or crashed"
      docker logs test-container
      exit 1
    fi
    
    docker stop test-container
    docker rm test-container
```

## File Structure

```
.
├── Dockerfile           # Docker configuration using distroless
├── .dockerignore        # Docker ignore patterns
├── build.gradle.kts     # Build configuration with Shadow plugin
└── README-Docker.md     # This documentation
```

## Troubleshooting

### Common Issues

1. **JAR not found**: Ensure `./gradlew shadowJar` has been run
2. **Port conflicts**: Change port mapping `-p 8081:8080`
3. **Memory issues**: Increase Docker memory limits
4. **No shell access**: Distroless images don't have shells (by design)

### Logs
```bash
# View container logs (primary debugging method)
docker logs <container-name>

# Follow logs in real-time
docker logs -f <container-name>
```

### Debug Mode
```bash
# Run with debug output
docker run -p 8080:8080 -e KTOR_ENV=development kotlin-lamp-api
```

## Performance

- **Startup time**: ~3-5 seconds
- **Image size**: ~150MB (much smaller than full JDK images)
- **Memory usage**: ~300-500MB under normal load
- **CPU usage**: Minimal for typical API workloads
- **Security**: Maximum with distroless approach

## Migration Notes

This configuration uses a **distroless** approach which differs from traditional images:

- **No health checks**: Cannot use curl or other tools
- **No debugging tools**: No shell, ps, ls, etc.
- **Container monitoring**: Use `docker logs` and `docker ps` from host
- **Security first**: Trade convenience for security and minimal attack surface