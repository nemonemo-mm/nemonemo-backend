#!/bin/bash

set -e

APP_DIR="/home/ubuntu/app"
BUILD_DIR="$APP_DIR/build/libs"
LOG_DIR="$APP_DIR/logs"

mkdir -p "$LOG_DIR"

# Java 설치 확인 및 설치
if ! command -v java &> /dev/null; then
    echo "Java is not installed. Installing OpenJDK 17..."
    sudo apt-get update
    sudo apt-get install -y openjdk-17-jdk
    echo "Java installation completed."
fi

# Java 버전 확인
echo "Java version:"
java -version

# 데이터베이스 자동 생성 
if [ -f "$APP_DIR/scripts/setup_database.sh" ]; then
    echo "Setting up database..."
    # 환경 변수 로드 (EC2에 직접 생성해야 함)
    if [ -f "$APP_DIR/scripts/setup_env.sh" ]; then
        source "$APP_DIR/scripts/setup_env.sh"
    fi
    bash "$APP_DIR/scripts/setup_database.sh"
fi

echo "Moving to app directory: $APP_DIR"
cd "$APP_DIR"

echo "Looking for executable JAR in: $BUILD_DIR"
# -plain.jar를 제외하고 실행 가능한 JAR 찾기 (bootJar로 생성된 JAR)
JAR_FILE=$(ls "$BUILD_DIR"/*.jar | grep -v "plain.jar" | head -n 1)

if [ -z "$JAR_FILE" ]; then
  echo "No executable JAR file found in $BUILD_DIR"
  echo "Available JAR files:"
  ls -la "$BUILD_DIR"/*.jar || true
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
# 환경 변수 로드 (EC2에 직접 생성한 setup_env.sh 파일에서)
if [ -f "$APP_DIR/scripts/setup_env.sh" ]; then
    source "$APP_DIR/scripts/setup_env.sh"
    # 환경 변수를 export해서 Java 프로세스에 전달
    export DB_HOST DB_PORT DB_USER DB_PASSWORD DB_NAME
    export GOOGLE_CLIENT_ID_WEB GOOGLE_CLIENT_SECRET_WEB
    export JWT_SECRET
    export FIREBASE_SERVICE_ACCOUNT_KEY_JSON_BASE64
    export FIREBASE_STORAGE_BUCKET
    export WEBSOCKET_ALLOWED_ORIGINS
fi

nohup java -jar "$JAR_FILE" \
  --spring.profiles.active=prod \
  > "$LOG_DIR/app.log" 2>&1 &

echo "Application started with new JAR."


