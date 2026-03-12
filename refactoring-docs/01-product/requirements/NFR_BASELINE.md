# NFR Baseline

이 문서는 "성능/가용성/보안/운영" 같은 비기능 목표를 정의합니다.

## 트래픽 가정
- DAU: 1,000
- 피크 동시 접속: 300 CCU

## 성능 목표
- 일반 API p95: 300ms 이내 목표
- AI 추천 API는 별도 측정 후 p95/p99 목표를 확정

## 가용성
- 서비스 목표 가용성: 99.9%

## 데이터 일관성
- 결제/주문/권한/추천 저장: 강한 정합성
- 조회수/좋아요 집계: 필요 시 eventual consistency 허용

## 보안
- JWT 기반 인증/인가
- 민감정보 로그 출력 금지(비밀번호, 토큰, 개인정보 원문)

## 장애 대응
- timeout, retry, circuit breaker 사용
- AI 장애 시 사용자 안내 가능한 fallback 메시지 제공

## 관측성
- 필수 지표: Latency, Error Rate, QPS, CPU/Memory
- 필수 로그 필드: `timestamp`, `traceId`, `endpoint`, `status`, `latencyMs`

## AI 운영 원칙
- Prompt 버전 관리
- 응답 스키마 검증
- 중복 호출 방지를 위한 idempotency 정책
