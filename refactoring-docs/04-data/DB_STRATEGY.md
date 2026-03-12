# DB Strategy

이 문서는 운영 환경에서 MySQL/Redis를 어떻게 역할 분리해 사용할지 설명합니다.

## 기본 원칙
- MySQL: 최종 원장(Source of Truth)
- Redis: 캐시/세션/고속 카운트 처리

## 읽기/쓰기 전략
- 쓰기(생성/수정/삭제): MySQL Primary
- 일반 조회: MySQL Replica 우선
- 강한 정합성이 필요한 조회: Primary 우선

## 캐시 전략
- 기본: Cache-Aside
- 조회수/카운트: Redis 집계 후 주기적으로 MySQL 반영 가능
- 장애 시: Redis 우회하고 MySQL 직접 조회

## 장애 대응
- Replica 지연 증가 시 읽기를 Primary로 임시 전환
- Redis 장애 시 캐시 없이 서비스 계속 동작

## 운영 체크 포인트
- DB 복제 지연
- Redis hit ratio
- API p95/p99 지연
- 에러율
