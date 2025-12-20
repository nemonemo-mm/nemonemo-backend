#!/bin/bash

# 모니터링 스택 (Prometheus + Grafana) 설치 및 시작 스크립트
# EC2 인스턴스에서 실행

set -e

APP_DIR="/home/ubuntu/app"
MONITORING_DIR="$APP_DIR/monitoring"

echo "=========================================="
echo "모니터링 스택 설정 시작"
echo "=========================================="

# Docker 설치 확인
if ! command -v docker &> /dev/null; then
    echo "Docker가 설치되어 있지 않습니다. 설치 중..."
    sudo apt-get update
    sudo apt-get install -y docker.io docker-compose
    sudo systemctl start docker
    sudo systemctl enable docker
    sudo usermod -aG docker ubuntu
    echo "Docker 설치 완료. 재로그인 후 다시 실행하세요."
    exit 1
fi

# Docker Compose 설치 확인
if ! command -v docker-compose &> /dev/null; then
    echo "Docker Compose가 설치되어 있지 않습니다. 설치 중..."
    sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
    echo "Docker Compose 설치 완료"
fi

# 모니터링 디렉토리 생성
mkdir -p "$MONITORING_DIR"
cd "$MONITORING_DIR"

# 필요한 파일 복사
if [ -f "$APP_DIR/docker-compose.monitoring.prod.yml" ]; then
    cp "$APP_DIR/docker-compose.monitoring.prod.yml" "$MONITORING_DIR/docker-compose.yml"
fi

if [ -f "$APP_DIR/prometheus.prod.yml" ]; then
    cp "$APP_DIR/prometheus.prod.yml" "$MONITORING_DIR/prometheus.yml"
fi

# Grafana provisioning 디렉토리 복사
if [ -d "$APP_DIR/grafana" ]; then
    cp -r "$APP_DIR/grafana" "$MONITORING_DIR/"
fi

# Grafana 비밀번호 설정 (환경 변수에서 읽기)
if [ -z "$GRAFANA_ADMIN_PASSWORD" ]; then
    echo "경고: GRAFANA_ADMIN_PASSWORD 환경 변수가 설정되지 않았습니다."
    echo "기본 비밀번호를 사용합니다. 보안을 위해 환경 변수를 설정하세요."
    export GRAFANA_ADMIN_PASSWORD="admin"
fi

# Grafana 도메인 자동 설정 (EC2 IP 또는 localhost)
if [ -z "$GRAFANA_DOMAIN" ]; then
    # EC2인지 확인 (AWS 메타데이터 서버 접근 가능한지)
    EC2_IP=$(curl -s --max-time 2 http://169.254.169.254/latest/meta-data/public-ipv4 2>/dev/null)
    if [ -n "$EC2_IP" ]; then
        # EC2 환경
        export GRAFANA_DOMAIN="${EC2_IP}:3000"
        echo "EC2 환경 감지: Grafana 도메인을 ${GRAFANA_DOMAIN}로 설정합니다."
    else
        # 로컬 환경
        export GRAFANA_DOMAIN="localhost:3000"
        echo "로컬 환경: Grafana 도메인을 ${GRAFANA_DOMAIN}로 설정합니다."
    fi
else
    echo "환경 변수에서 Grafana 도메인을 읽었습니다: ${GRAFANA_DOMAIN}"
fi

# Grafana Admin User 설정 (없으면 기본값)
if [ -z "$GRAFANA_ADMIN_USER" ]; then
    export GRAFANA_ADMIN_USER="admin"
fi

# 모니터링 스택 시작
echo "모니터링 스택 시작 중..."
docker-compose up -d

# 상태 확인
echo ""
echo "=========================================="
echo "모니터링 스택 상태 확인"
echo "=========================================="
docker-compose ps

# EC2 공인 IP 확인 (표시용)
EC2_IP=$(curl -s --max-time 2 http://169.254.169.254/latest/meta-data/public-ipv4 2>/dev/null || echo "localhost")

echo ""
echo "=========================================="
echo "모니터링 스택 설정 완료"
echo "=========================================="
echo "Prometheus: http://${EC2_IP}:9090"
echo "Grafana: http://${GRAFANA_DOMAIN}"
echo ""
echo "Grafana 로그인:"
echo "  사용자명: ${GRAFANA_ADMIN_USER}"
echo "  비밀번호: ${GRAFANA_ADMIN_PASSWORD}"
echo ""
echo "⚠️  보안 주의사항:"
echo "   - 방화벽에서 9090, 3000 포트를 외부에 직접 노출하지 마세요."
echo "   - Nginx 리버스 프록시 + 인증 설정을 강력히 권장합니다."
echo "   - 또는 VPN/SSH 터널링을 통해 접속하세요."

