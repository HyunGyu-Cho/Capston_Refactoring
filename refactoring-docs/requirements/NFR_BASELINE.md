# NFR Baseline

## 1. 트래픽 규모
- DAU: 1,000
- 피크 동시 접속자(CCU): 300
- 1인당 평균 요청: 10 req/day
- 평균 QPS: 10,000 / 86,400 ~= 0.12
- 피크 QPS: 운영 측정 후 확정

## 2. 성능
- AI 제외 API: p95 < 300ms
- 페이지 이동 시간: 2초 이내
- AI API Latency: 운영 데이터 측정 후 목표 확정

## 3. 가용성
- 전체 서비스 업타임: 99.9%

## 4. 동시성 제어
- Redis 기반 제어
- DB Optimistic Lock

## 5. 일관성 정책
- 핵심 데이터(결제/주문/권한/추천 저장)는 즉시 정합성 보장
- 게시글 좋아요/조회수는 eventual consistency 허용

## 6. 보안
- JWT 기반 인증/인가
- OAuth(google/kakao/naver)는 추후 확장

## 7. 모니터링
- Latency, QPS, Error Rate, CPU/Memory

## 8. 장애 대응 (AI 호출)
- timeout
- retry 최대 1회
- circuit breaker
- fallback 안내 메시지

## 9. 로깅
- JSON 구조화 로그
- 필수 필드: timestamp, traceId, endpoint, method, status, latencyMs, errorCode, maskedUserId
- 민감정보 로그 금지

## 10. AI Prompt Management
- 프롬프트 버전 관리
- 모델/파라미터 이력 기록

## 11. AI 응답 일관성
- JSON 스키마 강제
- 스키마 불일치 시 저장 금지

## 12. Hallucination 제어
- 허용 필드/범위 검증
- 이상치 검증 실패 시 재요청 1회 후 실패 처리

## 13. AI 호출 최소화
- 동일 입력 캐시 재사용
- idempotency key로 중복 호출 방지

## 14. Rate Limiting
- 사용자 단위 제한
- 초과 시 queue 처리 또는 429

## 15. AI Observability
- 성공률/실패율
- p50/p95/p99
- retry율/회로차단율
