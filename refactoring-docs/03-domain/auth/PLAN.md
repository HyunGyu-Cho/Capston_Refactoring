# AUTH Plan

이 문서는 auth 모듈을 어떤 순서로 구현하고, 완료 기준을 어떻게 확인할지 설명합니다.

## 구현 범위 (MVP)
- `POST /api/v1/auth/signup`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `GET /api/v1/auth/me`

## 설계 핵심
1. Access는 Stateless JWT
2. Refresh는 Stateful 저장소(기본 Redis)
3. Refresh 원문 저장 금지, 해시로 비교
4. Rotation/Revoke/Reuse Detection 적용

## 현재 운영 정책(코드 기준)
- Access 만료: 30분
- Refresh 만료: 7일
- Refresh Cookie: HttpOnly + Secure + SameSite=Strict + Path=/api/v1/auth
- permitAll: signup/login/refresh/logout/health/ready

## 구현 단계
1. 공통 에러코드/예외 처리
2. SecurityConfig + JWT Filter
3. Signup/Login
4. Refresh/Rotation/Reuse Detection
5. Logout/Me
6. 통합 테스트

## 완료 기준
- auth 5개 API 동작
- 실패 케이스 에러코드 일관성 유지
- `gradle test` 통과
- 문서와 코드 경로/정책 불일치 없음
