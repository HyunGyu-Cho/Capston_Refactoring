# auth_modify.md

## 적용 일자
- 2026-03-12

## 목적
기존 auth 구현 분석에서 확인된 보안/안정성 이슈를 패치하고, 모듈 구조 기준을 문서/코드에 반영한다.

## 코드 수정 사항

1. 로그아웃 쿠키 전송 문제 수정
- 파일: `backend/smart-healthcare/src/main/java/com/example/smart_healthcare/auth/api/AuthController.java`
- 변경:
  - Refresh 쿠키 Path를 `/api/v1/auth/refresh` -> `/api/v1/auth`로 변경
  - SameSite를 `Lax` -> `Strict`로 변경
- 효과:
  - `/auth/logout` 요청에도 브라우저가 refresh 쿠키를 정상 전송

2. Refresh 만료시간 하드코딩 제거
- 파일: `AuthController.java`
- 변경:
  - 쿠키 maxAge를 고정 7일 대신 `app.auth.jwt.refresh-expire-sec` 설정값 사용
- 효과:
  - JWT refresh 만료와 쿠키 만료 정책 일치

3. 인증 실패 상세 오류코드 보존
- 파일:
  - `backend/smart-healthcare/src/main/java/com/example/smart_healthcare/auth/security/JwtAuthenticationFilter.java`
  - `backend/smart-healthcare/src/main/java/com/example/smart_healthcare/auth/security/JsonAuthenticationEntryPoint.java`
- 변경:
  - 필터에서 `AppException`의 에러코드/메시지를 request attribute로 전달
  - EntryPoint가 고정코드(`AUTH-401-002`) 대신 전달받은 코드/메시지 사용
- 효과:
  - 만료/무효 등 인증 실패 원인을 정확히 응답

4. InMemory 세션 저장소 동시성 개선
- 파일: `backend/smart-healthcare/src/main/java/com/example/smart_healthcare/auth/session/InMemoryRefreshSessionRepository.java`
- 변경:
  - `HashSet` -> `ConcurrentHashMap.newKeySet()`
- 효과:
  - 동시 접근 시 멤버별 JTI 인덱스 안전성 향상

5. Redis 인덱스 TTL 하드코딩 제거
- 파일: `backend/smart-healthcare/src/main/java/com/example/smart_healthcare/auth/session/RedisRefreshSessionRepository.java`
- 변경:
  - 멤버 인덱스 TTL을 `Duration.ofDays(7)` 대신 `app.auth.jwt.refresh-expire-sec` 사용
- 효과:
  - refresh 토큰 정책과 Redis 인덱스 TTL 일관성 확보

6. 운영 보안 기본값 강화
- 파일: `backend/smart-healthcare/src/main/resources/application.yml`
- 변경:
  - JWT secret을 환경변수 우선 사용: `${APP_AUTH_JWT_SECRET:...}`
  - 쿠키 secure 기본값을 환경변수 기반으로 설정: `${APP_AUTH_COOKIE_SECURE:true}`
- 효과:
  - 시크릿/전송보안 운영 환경 제어 강화

## 모듈 구조 반영 사항

1. 문서 구조 정리
- 파일: `refactoring-docs/02-architecture/module-map/MODULE_SPECIFIC.md`
- 변경:
  - 깨진 인코딩 문서를 UTF-8 기준으로 재작성
  - 채택 모듈 목록, 의존 방향, 내부 계층 표준(`api/application/domain/infrastructure`) 명시

2. 백엔드 README 삭제
- 파일: `backend/smart-healthcare/README.md`
- 변경:
  - auth 보안 동작(쿠키 정책, 회전/재사용 감지) 반영
  - 패키지 구조 표준 명시

3. 루트 README 구조 섹션 갱신
- 파일: `README.md`
- 변경:
  - 목표 구조 표현을 채택 구조로 업데이트
  - `userinfo` 모듈을 read model 전용으로 명시

4. 패키지 네이밍 정규화
- 변경:
  - `userInfo` -> `userinfo`
  - 파일: `backend/smart-healthcare/src/main/java/com/example/smart_healthcare/userinfo/package-info.java`
- 효과:
  - Java 패키지 네이밍 일관성 개선

## 검증 메모
- 로컬 환경에서 `gradle`/`gradlew` 실행 파일이 없어 테스트를 실행하지 못함.

