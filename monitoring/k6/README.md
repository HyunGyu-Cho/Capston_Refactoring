# k6 Load Test

## 실행 순서
1. 백엔드 실행
- backend/smart-healthcare 디렉터리에서 실행
- gradle bootRun

2. 모니터링/Redis 실행
- 프로젝트 루트에서 실행
- docker compose up -d redis prometheus grafana

3. k6 부하 테스트 실행
- 프로젝트 루트에서 실행
- docker compose --profile loadtest run --rm k6

## 확인 URL
- Grafana: http://localhost:3000 (admin/admin)
- Prometheus: http://localhost:9090
- Prometheus 메트릭 직접 확인: http://localhost:8080/actuator/prometheus

## 참고
- k6 스크립트는 로그인 후 access token을 받아 인바디 저장/목록 조회를 반복 호출한다.
- 테스트 중 Grafana 대시보드에서 캐시 hit/miss, p95, 에러율을 확인하면 된다.
