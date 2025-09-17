#!/bin/bash
# Docker validation script for TypeScript implementation

set -e

echo "🐳 Building Docker image..."
docker build -t lamp-control-api-typescript:test .

echo "🚀 Starting container..."
docker run -d --name test-lamp-api -p 8080:8080 lamp-control-api-typescript:test

echo "⏳ Waiting for application to start..."
sleep 10

echo "🏥 Testing health endpoint..."
curl -f http://localhost:8080/health || {
    echo "❌ Health check failed"
    docker logs test-lamp-api
    docker stop test-lamp-api
    docker rm test-lamp-api
    exit 1
}

echo "🔦 Testing lamps API endpoint..."
curl -f http://localhost:8080/v1/lamps || {
    echo "❌ API test failed"
    docker logs test-lamp-api
    docker stop test-lamp-api
    docker rm test-lamp-api
    exit 1
}

echo "🧹 Cleaning up..."
docker stop test-lamp-api
docker rm test-lamp-api

echo "✅ Docker configuration validation passed!"