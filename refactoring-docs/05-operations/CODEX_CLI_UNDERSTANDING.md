# CODEX CLI Understanding

이 문서는 Codex CLI 작업에서 얻은 설계 인사이트를 정리한 회고 문서입니다.

## 이번 작업에서 확인한 것
1. 문서와 코드가 조금만 어긋나도 테스트에서 바로 문제가 드러난다.
2. auth에서는 쿠키 경로/보안속성처럼 "작은 설정"이 실제 동작에 큰 영향을 준다.
3. 에러코드는 단순히 숫자가 아니라 운영 관측에 핵심이다.

## auth 관련 최종 정합성
- 쿠키 정책: `HttpOnly`, `Secure`, `SameSite=Strict`, `Path=/api/v1/auth`
- 공개 경로: signup/login/refresh/logout/health/ready
- Refresh: rotation + revoke + reuse detection
- 테스트: `gradle test` 통과

## 다음 개선 포인트
1. 문서-코드 자동 동기화 체크(예: CI 문서 검증)
2. 운영 프로파일별 쿠키 secure 정책 가이드 강화
3. Gradle 경고(Deprecated) 정리
