# COMMON RULE

이 문서는 모든 모듈이 같이 지켜야 하는 공통 규칙입니다.

## 1) 응답 규칙
- 성공: `success=true`, `error=null`
- 실패: `success=false`, `data=null`, `error` 필수
- `timestamp`는 UTC ISO-8601 형식 사용

## 2) 에러 규칙
- 에러는 `code`, `message`, `traceId`를 포함
- 검증 실패는 필드 단위 상세(`errors[]`)를 제공

## 3) 인증/인가
- 인증 방식: Bearer JWT
- 기본 정책: 보호 API는 인증 필수
- 관리자 API는 `ADMIN` 권한 필요

## 4) 페이지네이션
- 기본값: `page=0`, `size=20`
- 최대 `size=100`
- 정렬 기본: `id,desc`

## 5) 로깅/추적
- 모든 요청에 `traceId`를 부여
- 민감정보(비밀번호/토큰 원문)는 로그 금지

## 6) 헬스체크
- `GET /api/v1/health`: 프로세스 생존
- `GET /api/v1/ready`: 의존성 준비 상태(DB/Redis)
