# Docker를 사용한 로컬 개발 환경 설정


### Docker Desktop 설치 및 실행

1. **Docker Desktop 설치 확인**
   - Windows에서 Docker Desktop이 설치되어 있는지 확인
   - 설치되어 있지 않다면: [Docker Desktop 다운로드](https://www.docker.com/products/docker-desktop/)

2. **Docker Desktop 실행**
   - 시작 메뉴에서 "Docker Desktop" 검색 후 실행
   - 시스템 트레이에 Docker 아이콘이 보이면 실행 중입니다
   - Docker Desktop이 완전히 시작될 때까지 기다리세요 (1-2분 소요)

3. **설치 확인**
   ```powershell
   docker --version
   docker compose version
   ```
   위 명령어들이 작동하면 설치 및 실행이 완료된 것입니다.

## 시작하기기

### 1. PostgreSQL 컨테이너 시작

**방법 1: Docker Compose V2 (권장, 최신 Docker Desktop)**
```powershell
docker compose up -d
```

**방법 2: Docker Compose V1 (구버전)**
```powershell
docker-compose up -d
```

### 2. 컨테이너 상태 확인

**Docker Compose V2:**
```powershell
docker compose ps
```

**Docker Compose V1:**
```powershell
docker-compose ps
```

### 3. 애플리케이션 실행

**옵션 1: IDE에서 실행**
- Run Configuration에서 `--spring.profiles.active=local` 추가
- 또는 환경 변수: `SPRING_PROFILES_ACTIVE=local`

**옵션 2: 터미널에서 실행**
```powershell
.\gradlew.bat bootRun --args='--spring.profiles.active=local'
```

### 4. 데이터베이스 연결 확인

애플리케이션이 시작되면 Flyway가 자동으로 마이그레이션을 실행하여 테이블을 생성한다.

## Docker 명령어

> **참고**: 최신 Docker Desktop에서는 `docker compose` (하이픈 없음)를 사용합니다.
> 만약 `docker compose`가 안 되면 `docker-compose` (하이픈 있음)를 시도해보세요.

### 컨테이너 시작
```powershell
docker compose up -d
# 또는
docker-compose up -d
```

### 컨테이너 중지
```powershell
docker compose down
# 또는
docker-compose down
```

### 컨테이너 중지 + 데이터 삭제
```powershell
docker compose down -v
# 또는
docker-compose down -v
```

### 로그 확인
```powershell
docker compose logs -f postgres
# 또는
docker-compose logs -f postgres
```

### 컨테이너 접속 (psql)
```powershell
docker compose exec postgres psql -U postgres -d nemonemo
# 또는
docker-compose exec postgres psql -U postgres -d nemonemo
```

## 설정 정보

### PostgreSQL 접속 정보
- **호스트**: localhost
- **포트**: 5432
- **데이터베이스**: nemonemo
- **사용자명**: postgres
- **비밀번호**: postgres

### 데이터 저장
- Docker 볼륨(`postgres_data`)에 데이터가 저장됩니다
- 컨테이너를 삭제해도 데이터는 유지됩니다 (볼륨 삭제 전까지)

## 문제 해결

### 포트 충돌
만약 5432 포트가 이미 사용 중이라면:
1. `docker-compose.yml`에서 포트 번호 변경 (예: `"5433:5432"`)
2. `application-local.properties`의 포트 번호도 함께 변경

### 컨테이너가 시작되지 않을 때
```powershell
docker compose logs postgres
# 또는
docker-compose logs postgres
```
로그를 확인하여 문제를 파악하세요.

### 데이터 초기화
데이터를 완전히 삭제하고 새로 시작하려면:
```powershell
docker compose down -v
docker compose up -d
# 또는
docker-compose down -v
docker-compose up -d
```

## 참고

- 로컬 개발: `application-local.properties` 사용
- 운영 환경: `application-prod.properties` 파일 생성 후 AWS RDS 설정
- 테스트: `application-test.properties` 사용 (현재 DB 없이 실행)

