# Smart Healthcare Docker 실행 가이드

이 가이드는 Smart Healthcare 애플리케이션을 Docker를 사용하여 실행하는 방법을 설명합니다.

## 📋 목차

- [사전 요구사항](#사전-요구사항)
- [환경 설정](#환경-설정)
- [Docker 실행](#docker-실행)
- [서비스 접속](#서비스-접속)
- [유용한 명령어](#유용한-명령어)
- [문제 해결](#문제-해결)

## 🛠 사전 요구사항

다음 소프트웨어가 설치되어 있어야 합니다:

- **Docker**: 20.10 이상
- **Docker Compose**: 2.0 이상

### Docker 설치 확인

```bash
docker --version
docker-compose --version
```

### Docker Desktop 다운로드

- **Windows/Mac**: [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- **Linux**: [Docker Engine](https://docs.docker.com/engine/install/)

## ⚙️ 환경 설정

### 1. 환경 변수 파일 생성

프로젝트 루트 디렉토리에 `.env` 파일을 생성합니다:

```bash
cp .env.example .env
```

### 2. 환경 변수 설정

`.env` 파일을 열고 필요한 값을 설정합니다:

```env
# MySQL 데이터베이스 비밀번호
MYSQL_ROOT_PASSWORD=1234

# OpenAI API 키 (필수)
OPENAI_API_KEY=your-openai-api-key-here

# Unsplash API 키 (선택사항)
UNSPLASH_ACCESS_KEY=your-unsplash-key-here

# YouTube API 키 (선택사항)
YOUTUBE_API_KEY=your-youtube-key-here

# AI 기능 활성화
AI_FEATURES_ENABLED=true

# 개발 모드
DEV_MODE=false
```

**⚠️ 중요**: `OPENAI_API_KEY`는 필수입니다. [OpenAI 웹사이트](https://platform.openai.com/)에서 API 키를 발급받으세요.

## 🚀 Docker 실행

### 전체 시스템 시작 (권장)

모든 서비스(MySQL, 백엔드, 프론트엔드)를 한 번에 시작합니다:

```bash
docker-compose up -d
```

- `-d` 옵션: 백그라운드에서 실행

### 빌드 포함 시작

Docker 이미지를 다시 빌드하면서 시작합니다:

```bash
docker-compose up -d --build
```

### 개별 서비스 시작

특정 서비스만 시작할 수 있습니다:

```bash
# MySQL만 시작
docker-compose up -d mysql

# 백엔드만 시작 (MySQL이 먼저 실행되어야 함)
docker-compose up -d backend

# 프론트엔드만 시작
docker-compose up -d frontend
```

### 로그 확인

실행 중인 서비스의 로그를 확인합니다:

```bash
# 모든 서비스 로그
docker-compose logs -f

# 특정 서비스 로그
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f mysql
```

- `-f` 옵션: 실시간으로 로그 출력 (Ctrl+C로 종료)

## 🌐 서비스 접속

서비스가 정상적으로 시작되면 다음 주소로 접속할 수 있습니다:

| 서비스 | 주소 | 설명 |
|--------|------|------|
| **프론트엔드** | http://localhost | React 웹 애플리케이션 |
| **백엔드 API** | http://localhost:8080 | Spring Boot REST API |
| **API 문서** | http://localhost:8080/swagger-ui.html | Swagger UI |
| **MySQL** | localhost:3306 | 데이터베이스 (외부 클라이언트) |

### 서비스 상태 확인

모든 컨테이너의 상태를 확인합니다:

```bash
docker-compose ps
```

정상적으로 실행 중이면 `STATUS` 열에 `Up`이 표시됩니다.

### Health Check

서비스 헬스 체크 상태 확인:

```bash
# 백엔드 헬스 체크
curl http://localhost:8080/actuator/health

# 프론트엔드 헬스 체크
curl http://localhost
```

## 📝 유용한 명령어

### 서비스 중지

```bash
# 모든 서비스 중지
docker-compose stop

# 특정 서비스 중지
docker-compose stop backend
```

### 서비스 재시작

```bash
# 모든 서비스 재시작
docker-compose restart

# 특정 서비스 재시작
docker-compose restart backend
```

### 서비스 중지 및 제거

```bash
# 컨테이너 중지 및 제거
docker-compose down

# 컨테이너, 네트워크, 볼륨 모두 제거
docker-compose down -v
```

**⚠️ 주의**: `-v` 옵션을 사용하면 데이터베이스 데이터도 삭제됩니다!

### 컨테이너 내부 접속

```bash
# 백엔드 컨테이너 접속
docker-compose exec backend sh

# MySQL 컨테이너 접속
docker-compose exec mysql bash

# MySQL 데이터베이스 접속
docker-compose exec mysql mysql -u root -p
```

### 리소스 정리

사용하지 않는 Docker 리소스를 정리합니다:

```bash
# 중지된 컨테이너 제거
docker container prune

# 사용하지 않는 이미지 제거
docker image prune

# 사용하지 않는 볼륨 제거
docker volume prune

# 모든 미사용 리소스 제거
docker system prune -a
```

## 🔧 문제 해결

### 1. 포트 충돌

**증상**: "port is already allocated" 오류

**해결책**:
```bash
# 포트를 사용 중인 프로세스 확인 (Windows)
netstat -ano | findstr :8080
netstat -ano | findstr :3306
netstat -ano | findstr :80

# 포트를 사용 중인 프로세스 확인 (Linux/Mac)
lsof -i :8080
lsof -i :3306
lsof -i :80

# docker-compose.yml에서 포트 변경
# 예: "8081:8080" (호스트:컨테이너)
```

### 2. 데이터베이스 연결 실패

**증상**: 백엔드가 MySQL에 연결할 수 없음

**해결책**:
```bash
# MySQL 컨테이너 상태 확인
docker-compose ps mysql

# MySQL 로그 확인
docker-compose logs mysql

# MySQL이 완전히 시작될 때까지 대기 후 백엔드 재시작
docker-compose restart backend
```

### 3. 빌드 실패

**증상**: Docker 이미지 빌드 중 오류 발생

**해결책**:
```bash
# 캐시 없이 빌드
docker-compose build --no-cache

# 개별 서비스 빌드
docker-compose build backend
docker-compose build frontend
```

### 4. OpenAI API 키 오류

**증상**: AI 기능이 작동하지 않음

**해결책**:
1. `.env` 파일에서 `OPENAI_API_KEY`가 올바르게 설정되었는지 확인
2. API 키에 충분한 크레딧이 있는지 확인
3. 백엔드 재시작:
   ```bash
   docker-compose restart backend
   ```

### 5. 프론트엔드가 백엔드와 통신하지 못함

**증상**: API 요청 실패

**해결책**:
1. 백엔드가 정상적으로 실행 중인지 확인:
   ```bash
   docker-compose ps backend
   curl http://localhost:8080/actuator/health
   ```

2. 네트워크 연결 확인:
   ```bash
   docker network inspect practice_smart-healthcare-network
   ```

3. Nginx 설정 확인:
   ```bash
   docker-compose exec frontend cat /etc/nginx/conf.d/default.conf
   ```

### 6. 메모리 부족

**증상**: 컨테이너가 자주 재시작되거나 느림

**해결책**:
1. Docker Desktop 설정에서 메모리 할당량 증가 (최소 4GB 권장)
2. 불필요한 컨테이너/이미지 정리:
   ```bash
   docker system prune -a
   ```

### 7. 로그 확인

문제가 발생하면 먼저 로그를 확인하세요:

```bash
# 모든 서비스 로그
docker-compose logs

# 최근 로그만 보기
docker-compose logs --tail=100

# 특정 서비스의 실시간 로그
docker-compose logs -f backend
```

## 📊 시스템 리소스 모니터링

### 컨테이너 리소스 사용량 확인

```bash
docker stats
```

### 디스크 사용량 확인

```bash
docker system df
```

## 🔄 업데이트 및 재배포

코드 변경 후 재배포하는 방법:

```bash
# 1. 서비스 중지
docker-compose down

# 2. 최신 코드로 이미지 재빌드
docker-compose build

# 3. 서비스 재시작
docker-compose up -d
```

또는 한 번에:

```bash
docker-compose up -d --build
```

## 🔐 보안 권장사항

1. **환경 변수 보호**: `.env` 파일을 절대 Git에 커밋하지 마세요
2. **강력한 비밀번호**: 운영 환경에서는 강력한 MySQL 비밀번호 사용
3. **API 키 보호**: API 키를 코드에 직접 포함하지 마세요
4. **정기 업데이트**: Docker 이미지와 의존성을 정기적으로 업데이트하세요

## 📚 추가 자료

- [Docker 공식 문서](https://docs.docker.com/)
- [Docker Compose 공식 문서](https://docs.docker.com/compose/)
- [Spring Boot Docker 가이드](https://spring.io/guides/topicals/spring-boot-docker/)
- [React 프로덕션 빌드](https://react.dev/learn/start-a-new-react-project#building-for-production)

## 💡 팁

1. **개발 환경**: 로컬에서 개발할 때는 Docker를 사용하지 않고, 운영 배포 시에만 Docker를 사용하는 것도 좋은 방법입니다.

2. **빠른 재시작**: 코드 변경 후 전체를 재빌드하지 않고 특정 서비스만 재시작할 수 있습니다:
   ```bash
   docker-compose restart backend
   ```

3. **로그 저장**: 로그를 파일로 저장하려면:
   ```bash
   docker-compose logs > logs.txt
   ```

4. **백그라운드 실행**: 항상 `-d` 옵션을 사용하여 백그라운드에서 실행하세요.

## 🆘 도움이 필요하신가요?

문제가 해결되지 않으면 다음을 확인하세요:

1. Docker Desktop이 실행 중인지 확인
2. `.env` 파일이 올바르게 설정되었는지 확인
3. 로그를 확인하여 구체적인 오류 메시지 찾기
4. 필요한 포트(80, 8080, 3306)가 사용 가능한지 확인
5. 충분한 디스크 공간이 있는지 확인

---

**Happy Coding! 🚀**

