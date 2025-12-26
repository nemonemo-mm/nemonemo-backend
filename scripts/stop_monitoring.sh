#!/bin/bash

# 모니터링 스택 중지 스크립트

set -e

MONITORING_DIR="/home/ubuntu/app/monitoring"

if [ -d "$MONITORING_DIR" ]; then
    cd "$MONITORING_DIR"
    echo "모니터링 스택 중지 중..."
    docker-compose down
    echo "모니터링 스택 중지 완료"
else
    echo "모니터링 디렉토리를 찾을 수 없습니다: $MONITORING_DIR"
fi


