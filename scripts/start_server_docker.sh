#!/bin/bash

set -e

APP_DIR="/home/ubuntu/app"
cd "$APP_DIR"

echo "Stopping existing containers (if any)..."
docker-compose -f docker-compose.prod.yml down || true

echo "Building Docker image..."
docker build -f Dockerfile.prod -t nemonemo-backend:latest .

echo "Starting application with Docker Compose..."
docker-compose -f docker-compose.prod.yml up -d

echo "Checking container status..."
docker-compose -f docker-compose.prod.yml ps

echo "Application started with Docker."

