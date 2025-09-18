FROM node:22 AS build-env
COPY . /app
WORKDIR /app

# Install all dependencies to build TypeScript
RUN npm ci

# Build the application
RUN npm run build

# Install only production dependencies
RUN npm ci --omit=dev

FROM gcr.io/distroless/nodejs22-debian12
COPY --from=build-env /app /app
WORKDIR /app
CMD ["dist/src/index.js"]