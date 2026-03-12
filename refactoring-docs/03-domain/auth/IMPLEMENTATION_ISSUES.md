# AUTH Implementation Issues

이 문서는 auth 구현 시 자주 발생하는 문제와 해결 기준을 정리합니다.

## 주요 이슈와 해결 상태
1. 쿠키 Path가 `/refresh`로 고정되어 logout에 쿠키가 안 붙는 문제
- 해결: Path를 `/api/v1/auth`로 변경

2. EntryPoint가 항상 `AUTH-401-002`를 반환해 원인 손실
- 해결: 필터에서 코드/메시지 전달, EntryPoint가 그대로 응답

3. InMemory 세션 인덱스 동시성 위험
- 해결: `HashSet` -> `ConcurrentHashMap.newKeySet()`

4. Redis 인덱스 TTL 하드코딩
- 해결: `refresh-expire-sec` 설정값 사용

5. logout가 permitAll 누락되어 테스트 실패
- 해결: SecurityConfig에 `/api/v1/auth/logout` 추가

## 재발 방지 체크리스트
- 쿠키 정책 변경 시 logout/refresh 브라우저 동작 함께 확인
- 보안 에러코드는 필터 -> EntryPoint 전달 체인으로 점검
- 세션 저장소 자료구조는 동시성 전제에서 선택
- 설정값 하드코딩 금지
