# auth_modify.md

## 문서 목적
auth 모듈에서 실제로 수정한 내용을 "무엇을 왜 바꿨는지" 기준으로 기록합니다.

## 적용 날짜
- 2026-03-12

## 핵심 수정 요약
1. 로그아웃에도 refresh 쿠키가 전달되도록 쿠키 Path를 `/api/v1/auth`로 변경
2. 쿠키 만료시간을 설정값(`refresh-expire-sec`)과 일치시킴
3. 인증 실패 시 세부 에러코드가 유지되도록 EntryPoint 처리 개선
4. InMemory 세션 인덱스 동시성 개선
5. Redis 인덱스 TTL을 하드코딩 7일에서 설정값 기반으로 변경
6. JWT secret/cookie secure를 환경변수 기반으로 관리

## 주요 변경 파일
- `backend/smart-healthcare/src/main/java/com/example/smart_healthcare/auth/api/AuthController.java`
- `backend/smart-healthcare/src/main/java/com/example/smart_healthcare/auth/security/JwtAuthenticationFilter.java`
- `backend/smart-healthcare/src/main/java/com/example/smart_healthcare/auth/security/JsonAuthenticationEntryPoint.java`
- `backend/smart-healthcare/src/main/java/com/example/smart_healthcare/auth/session/InMemoryRefreshSessionRepository.java`
- `backend/smart-healthcare/src/main/java/com/example/smart_healthcare/auth/session/RedisRefreshSessionRepository.java`
- `backend/smart-healthcare/src/main/resources/application.yml`

## 검증 상태
- 현재 기준 `gradle test` 통과
