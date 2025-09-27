# Multi-stage Docker configuration for TypeScript implementation
# Stage 1: Build environment
FROM node:22 AS build-env

WORKDIR /app

# Copy package files for dependency installation
COPY package*.json ./

# Install all dependencies using npm ci for reproducible builds
RUN npm ci

# Copy all source files
COPY src ./src
COPY tsconfig.json ./

# Create the directory structure and copy OpenAPI spec from context root
RUN mkdir -p /docs/api
COPY docs/api/openapi.yaml /docs/api/openapi.yaml

# Build the TypeScript application
RUN npm run build

# Remove development dependencies to reduce final image size
RUN npm prune --omit=dev

# Stage 2: Production runtime  
FROM gcr.io/distroless/nodejs22-debian12

WORKDIR /app

# Copy built application and production dependencies from build stage
COPY --from=build-env /app/dist ./dist
COPY --from=build-env /app/node_modules ./node_modules
COPY --from=build-env /app/package*.json ./

# Copy the OpenAPI spec from build stage to the expected location
COPY --from=build-env /docs/api/openapi.yaml /docs/api/openapi.yaml

# Expose the application port
EXPOSE 8080

# Set the command to run the application
CMD ["dist/src/index.js"]