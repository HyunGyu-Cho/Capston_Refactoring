# k6 + Grafana 실행 가이드 (무료)

## 비용/라이선스
- 이 가이드는 무료 범위만 사용합니다.
- 사용 도구
1. k6 (OSS)
2. Prometheus (OSS)
3. Grafana OSS
4. Redis OSS
5. Docker Desktop (개인/소규모 사용은 무료 정책 범위에서 사용 가능)

주의:
- 조직 규모/매출 조건에 따라 Docker Desktop 정책이 달라질 수 있으니, 회사 정책과 Docker 공식 라이선스 조건은 별도 확인하세요.
- 이 구성 자체는 유료 클라우드 서비스(Managed Grafana/Cloud k6/Managed Redis)를 사용하지 않습니다.

## 사전 준비
1. 백엔드가 로컬에서 실행 가능해야 함
2. Docker Desktop 실행 중이어야 함
3. 포트 사용 가능
- 3000 (Grafana)
- 6379 (Redis)
- 8080 (Spring Boot)
- 9090 (Prometheus)

## 파일 위치
- docker compose: `docker-compose.yml`
- prometheus 설정: `monitoring/prometheus.yml`
- k6 스크립트: `monitoring/k6/inbody-load-test.js`
- grafana 대시보드: `monitoring/grafana/inbody-dashboard.json`

## 실행 순서
### 1) 백엔드 실행
`backend/smart-healthcare` 디렉터리에서:

```powershell
gradle bootRun
```

### 2) 모니터링/Redis 실행
프로젝트 루트에서:

```powershell
docker compose up -d redis prometheus grafana
```

### 3) k6 부하 테스트 실행
프로젝트 루트에서:

```powershell
docker compose --profile loadtest run --rm k6
```

## UI 접속
- Grafana: http://localhost:3000
- Prometheus: http://localhost:9090
- 앱 메트릭 원본: http://localhost:8080/actuator/prometheus

Grafana 기본 계정:
- ID: `admin`
- PW: `admin`

## Grafana 대시보드 Import
1. Grafana 접속
2. Dashboards -> Import
3. `monitoring/grafana/inbody-dashboard.json` 업로드
4. Prometheus 데이터소스 선택 후 Import

## 확인 가능한 지표
- 캐시 hit/miss
- 캐시 evict
- Inbody API p95 지연시간
- Inbody API 4xx/5xx 에러율

## 종료
```powershell
docker compose down
```

필요 시 볼륨까지 삭제:
```powershell
docker compose down -v
```

## 트러블슈팅
1. Grafana 접속 안 됨
- `docker ps`로 컨테이너 상태 확인
- 3000 포트 점유 확인

2. 메트릭이 안 보임
- 앱이 켜져 있는지 확인 (`bootRun`)
- `http://localhost:8080/actuator/prometheus` 응답 확인
- Prometheus Targets에서 `smart-healthcare`가 UP 상태인지 확인

3. k6 실패
- 로그인/회원가입 API가 정상인지 확인
- 앱 로그에서 인증/검증 실패 원인 확인

4. Redis 연결 문제
- `docker compose ps redis` 상태 확인
- 앱 설정의 redis host/port가 `localhost:6379`인지 확인
