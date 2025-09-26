# Multi-stage Docker configuration for TypeScript implementation
# Stage 1: Build environment
FROM node:22 AS build-env

WORKDIR /app

# Copy package files and node_modules for dependencies
COPY package*.json ./
COPY node_modules ./node_modules

# Copy all source files and OpenAPI spec
COPY src ./src
COPY tsconfig.json ./
COPY docs ./docs

# Build the TypeScript application
RUN npm run build

# Stage 2: Production runtime
FROM gcr.io/distroless/nodejs22-debian12

WORKDIR /app

# Copy built application and production dependencies from build stage
COPY --from=build-env /app/dist ./dist
COPY --from=build-env /app/node_modules ./node_modules
COPY --from=build-env /app/package*.json ./
COPY --from=build-env /app/docs ./docs

# Expose the application port
EXPOSE 8080

# Set the command to run the application
CMD ["dist/src/index.js"]