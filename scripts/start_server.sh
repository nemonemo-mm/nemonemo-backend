#!/bin/bash

set -e

APP_DIR="/home/ubuntu/app"
BUILD_DIR="$APP_DIR/build/libs"
LOG_DIR="$APP_DIR/logs"

mkdir -p "$LOG_DIR"

echo "Moving to app directory: $APP_DIR"
cd "$APP_DIR"

echo "Looking for JAR in: $BUILD_DIR"
JAR_FILE=$(ls "$BUILD_DIR"/*.jar | head -n 1)

if [ -z "$JAR_FILE" ]; then
  echo "No JAR file found in $BUILD_DIR"
  exit 1
fi

echo "Using JAR: $JAR_FILE"

echo "Stopping existing application (if any)..."
PID=$(pgrep -f "$JAR_FILE" || true)
if [ -n "$PID" ]; then
  echo "Found running process with PID: $PID, killing..."
  kill -15 "$PID" || true
  sleep 5
fi

echo "Starting application..."
nohup java -jar "$JAR_FILE" \
  --spring.profiles.active=prod \
  > "$LOG_DIR/app.log" 2>&1 &

echo "Application started with new JAR."


