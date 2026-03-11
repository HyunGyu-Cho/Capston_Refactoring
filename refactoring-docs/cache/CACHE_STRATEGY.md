# CACHE STRATEGY

## 목적
- 커뮤니티 트래픽에서 가장 빈번한 조회수 증가 연산을 Redis로 분리해 DB 부하를 줄인다.
- 정렬/추천에 직접 영향이 큰 좋아요/싫어요는 MySQL 원장만 사용해 강한 정합성을 유지한다.

## 범위
- Redis 적용 대상: `community_post.view_count`만 해당
- Redis 미적용 대상: `like_count`, `dislike_count`, `community_reaction`

## 결정 사항
1. 조회수만 Redis 적용
- 이유:
1. 조회수는 상대적으로 덜 민감한 지표다.
2. 읽기/증가 요청이 매우 많아 캐시 효과가 크다.
3. 좋아요/싫어요는 정렬/추천/신뢰도 로직에 사용될 가능성이 있어 원장(MySQL) 직접 관리가 안전하다.

2. 로그인 사용자만 조회수 증가
- 비로그인 사용자는 조회수 증가 대상에서 제외한다.
- 인증된 사용자(`member_id` 존재) 요청만 카운트한다.

3. 어뷰징 방지 정책
- 동일 사용자-동일 게시글에 대해 최소 증가 간격(`cooldown`) 적용
- 기본값: 10분(600초)
- 키: `post:{postId}:viewer:{memberId}`
- 동작:
1. 키가 없으면 조회수 +1 후 키 생성(`SET NX EX 600`)
2. 키가 있으면 증가하지 않음
- 확장 옵션:
1. 시간당 사용자별 증가 상한(예: 100회)
2. 비정상 패턴(IP/UA 급변, 순차 스캔) 탐지 후 보안 로그 적재

4. TTL 정책
- 조회수 본 카운터 키(`post:{postId}:view_count`)는 TTL 없음(영구)
- 이유: TTL 만료 시 카운터 유실/재구성 비용이 커지고 데이터 왜곡 가능성 증가
- 어뷰징 방지 키(`post:{postId}:viewer:{memberId}`)만 TTL 적용(기본 600초)

5. 동시성/락 전략
- 기본 원칙: Redis 원자 연산으로 처리하고 분산락은 사용하지 않는다.
- 증가 연산:
1. `SET key value NX EX 600` 성공 시에만
2. `INCR post:{postId}:view_count` 수행
- 이유: `SET NX` + `INCR` 조합으로 대부분 경합을 안전하게 처리 가능, 락 오버헤드 제거
- 정합성 강화가 필요하면 Lua 스크립트로 원자적 묶음 처리:
1. 중복뷰 키 체크
2. 없을 때만 카운트 증가
3. 중복뷰 키 TTL 설정

6. 백업/복구(질문 2)
- 원장(Source of Truth): MySQL `community_post.view_count`
- Redis는 성능 계층으로 간주
- 동기화 방식:
1. 배치(기본): 1분 주기로 Redis 증분을 MySQL에 반영
2. 장애 시: MySQL 기준 서비스 지속, Redis 복구 후 백필
- 백필 절차:
1. 배치 중단
2. MySQL 값을 Redis에 재적재
3. 배치 재시작
- 운영 백업:
1. Redis RDB/AOF는 운영 편의 목적(복구 시간 단축)
2. 최종 정합성 기준은 MySQL

## 키 설계
- 조회수 카운터: `post:{postId}:view_count`
- 중복 조회 방지: `post:{postId}:viewer:{memberId}`
- 선택(배치 증분용): `post:{postId}:view_delta`

## 요청 처리 플로우
1. 게시글 상세 조회 요청 수신
2. 로그인 사용자 여부 확인
3. 로그인 사용자면 `SET NX EX 600` 시도
4. 성공 시 Redis 조회수 증가
5. 게시글 본문/기타 데이터는 MySQL 조회
6. 응답 시 조회수는 Redis 우선, 없으면 MySQL fallback

## 장애 시 동작
- Redis 장애:
1. 조회수 증가는 임시로 MySQL 직접 반영(성능 저하 허용)
2. Redis 복구 후 MySQL 기준으로 재적재
- MySQL 장애:
1. 조회수 증가를 임시 큐/로그로 보관 가능(선택)
2. MySQL 복구 후 재반영

## 모니터링 지표
- 조회수 증가 QPS
- Redis 명령 실패율
- 배치 지연 시간
- Redis 조회수와 MySQL 조회수 차이
- 어뷰징 차단 건수(중복 조회 차단 수)

## 정책 기본값(초기)
- 조회 중복 방지 TTL: 600초(10분)
- 동기화 주기: 1분
- 조회수 카운터 TTL: 없음

## 메모리 예산 및 용량 관리
- 전제 트래픽: DAU 1,000 / 피크 동시접속 300
- 운영 원칙: 캐시는 "유실 가능 성능 계층"으로 운영하고, 메모리 한도를 초과하면 제거(Eviction)되어도 서비스는 MySQL fallback으로 정상 동작해야 한다.

1. 메모리 예산
- Redis `maxmemory`를 명시적으로 설정한다. (초기 권장: 128MB~256MB)
- 캐시 외 목적(세션/락/큐)과 인스턴스 분리 또는 메모리 quota 분리를 권장한다.

2. 키 크기 최소화
- 게시글 조회수는 숫자 카운터만 저장한다.
- 중복 조회 방지 키는 값 없이 짧은 토큰(예: `1`)로 저장한다.
- JSON/대형 객체를 본 문서 범위 키에 저장하지 않는다.

3. TTL/키 수 관리
- 본 카운터 키(`post:{postId}:view_count`)는 무TTL 유지
- 중복 방지 키(`post:{postId}:viewer:{memberId}`)는 TTL 600초 유지
- 트래픽 증가 시 우선 조정 순서:
1. 중복 방지 TTL 축소(600 -> 300초)
2. 비정상 요청 차단 정책 강화
3. Redis 메모리 상향 또는 샤딩

4. Eviction 정책
- 기본 권장: `volatile-ttl` (TTL 있는 키부터 제거)
- 대안: `allkeys-lru` (TTL 없는 키까지 제거 가능하므로 신중히 선택)
- 본 전략은 `view_count` 무TTL 키 보호를 위해 `volatile-ttl`을 우선 권장한다.

5. 용량 모니터링
- 필수 지표:
1. used_memory / maxmemory 비율
2. evicted_keys
3. keyspace hit ratio
4. command latency(p95)
- 임계치 예시:
1. 메모리 사용률 80% 초과 경고
2. `evicted_keys` 지속 증가 시 즉시 대응

## 오픈 이슈
- 10분 쿨다운이 서비스 성격에 맞는지 A/B 검증 필요
- 인기글 정렬에서 조회수 반영 시점(실시간 Redis vs 동기화 후 MySQL) 확정 필요
- IP 기반 보조 제한 정책 도입 여부 결정 필요
