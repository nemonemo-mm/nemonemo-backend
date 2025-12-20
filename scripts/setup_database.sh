#!/bin/bash

# 데이터베이스 자동 생성 스크립트
# RDS에 nemonemo 데이터베이스가 없으면 생성
# 환경 변수에서 데이터베이스 정보를 읽습니다

set -e

# 환경 변수에서 데이터베이스 설정 읽기 
DB_HOST="${DB_HOST:-mm-db.c7oqe62yydsa.ap-southeast-2.rds.amazonaws.com}"
DB_PORT="${DB_PORT:-5432}"
DB_USER="${DB_USER:-postgres}"
DB_PASSWORD="${DB_PASSWORD:-}"
DB_NAME="${DB_NAME:-nemonemo}"

# 비밀번호가 없으면 에러
if [ -z "$DB_PASSWORD" ]; then
    echo "ERROR: DB_PASSWORD environment variable is not set!"
    echo "Please set it before running this script:"
    echo "  export DB_PASSWORD='your-password'"
    exit 1
fi

echo "Checking if database '$DB_NAME' exists..."

# PostgreSQL 클라이언트 설치 확인
if ! command -v psql &> /dev/null; then
    echo "PostgreSQL client not found. Installing..."
    sudo apt-get update
    sudo apt-get install -y postgresql-client
fi

# 데이터베이스 존재 여부 확인
DB_EXISTS=$(PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='$DB_NAME'" 2>/dev/null || echo "0")

if [ "$DB_EXISTS" = "1" ]; then
    echo "Database '$DB_NAME' already exists. Skipping creation."
else
    echo "Database '$DB_NAME' does not exist. Creating..."
    PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d postgres -c "CREATE DATABASE $DB_NAME;" 2>&1
    
    if [ $? -eq 0 ]; then
        echo "Database '$DB_NAME' created successfully."
    else
        echo "Failed to create database. It might already exist or there's a permission issue."
    fi
fi

echo "Database setup completed."

