# DB 전략

## 1. 목표
- 기본 영속 저장소로 MySQL을 사용한다.
- 캐시 및 고빈도 카운터 처리를 위해 Redis를 사용한다.
- MySQL은 Primary(쓰기) + Replica(읽기) 토폴로지로 운영한다.
- 쓰기 요청은 Primary, 기본 읽기 요청은 Replica로 라우팅한다.
- 강한 정합성이 필요한 경우 Primary 읽기 우회 규칙을 적용한다.

## 2. 아키텍처 원칙
- MySQL Primary:
- INSERT/UPDATE/DELETE 및 트랜잭션 커밋 처리
- MySQL Replica(1..N):
- SELECT 처리, Primary에서 비동기 복제
- Redis:
- 조회 캐시 계층
- 카운터 버퍼(예: 게시글 조회수)
- 토큰/세션 메타데이터(예: Refresh Token 상태)

## 3. 읽기/쓰기 라우팅
### 3.1 쓰기 경로
- 모든 쓰기 트랜잭션은 Primary로 보낸다.
- 예: 회원가입, 로그인 감사 로그, 게시글/댓글 생성·수정·삭제, 리액션 변경

### 3.2 읽기 경로
- 일반 목록/검색/상세 조회는 Replica로 보낸다.
- 허용 가능한 복제 지연 범위 내에서 성능을 우선한다.

### 3.3 강한 읽기 정합성(Read-After-Write)
- 아래 경우에는 Primary 읽기를 강제한다.
- 쓰기 직후 동일 엔터티를 즉시 재조회해야 하는 API
- 권한/상태 전이/결제 등 오판 위험이 큰 API
- 서비스 계층에서 일관성 플래그(예: `STRONG`)로 라우팅을 분기한다.

## 4. Redis 캐시 전략(통합)
### 4.1 캐시 패턴
- 기본 패턴은 Cache-Aside를 사용한다.
1. 캐시 미스: MySQL 조회 후 Redis에 TTL과 함께 저장
2. 캐시 히트: Redis 값 반환
- 쓰기 시에는 MySQL 반영 후 관련 캐시 키를 무효화한다.

### 4.2 캐시 대상
- 조회 빈도가 높고 쓰기 빈도가 상대적으로 낮은 데이터
- 예: 게시글 상세, 게시글 목록, 일부 집계 응답

### 4.3 커뮤니티 카운터 정책
- Redis 적용 대상: `community_post.view_count`
- MySQL 단독 관리 대상: `like_count`, `dislike_count`, `community_reaction`
- 근거:
1. 조회수는 상대적으로 완화된 정합성으로 운영 가능
2. 좋아요/싫어요는 랭킹/신뢰도에 직접 영향을 주므로 강한 정합성 필요

### 4.4 중복 조회 방지
- 인증 사용자에 한해 조회수 증가를 허용한다.
- 기본 쿨다운: 10분(600초)
- 키: `post:{postId}:viewer:{memberId}`
- 처리 흐름:
1. 중복 방지 키에 `SET NX EX 600` 수행
2. 성공한 경우에만 `INCR post:{postId}:view_count` 수행
- 기본은 Redis 원자 연산을 사용하고 분산락은 사용하지 않는다.
- 필요 시 Lua 스크립트로 체크+증가를 원자적으로 묶는다.

### 4.5 TTL 및 키 설계
- 카운터 키: `post:{postId}:view_count` (TTL 없음)
- 중복 방지 키: `post:{postId}:viewer:{memberId}` (TTL 600초)
- 선택 키(증분 배치용): `post:{postId}:view_delta`
- 일반 TTL 가이드:
- 상세 캐시: 60~300초
- 목록/검색 캐시: 30~120초

## 5. 동기화 및 원장 원칙
- 최종 원장(Source of Truth)은 MySQL이다.
- Redis는 성능 계층이며 유실 가능성을 전제로 운영한다.
- 조회수 동기화:
1. 기본: 1분 주기 배치(또는 컨슈머)로 Redis 증분을 MySQL에 반영
2. 복구: MySQL 기준으로 Redis 상태를 재구성

## 6. 장애 대응
### 6.1 Replica 지연/장애
- 복제 지연을 모니터링하고 임계치를 정의한다.
- 임계치 초과 시 읽기를 Primary 우선으로 전환한다.
- 비정상 Replica는 라우팅 풀에서 즉시 제외한다.
- Replica 전체 장애 시 Primary로 읽기를 폴백한다.

### 6.2 Redis 장애
- 캐시를 우회하고 MySQL 직접 조회로 폴백한다.
- 조회수는 임시로 MySQL에 직접 반영한다(성능 저하 허용).
- Redis 복구 후 MySQL 기준으로 재적재한다.

### 6.3 MySQL 장애
- 선택적으로 카운터 이벤트를 큐/로그에 임시 적재한다.
- 복구 후 적재 이벤트를 재반영한다.

## 7. 용량 및 운영
- Redis `maxmemory`를 명시적으로 설정한다(초기 권장: 128MB~256MB).
- 권장 eviction 정책: `volatile-ttl`
- 핵심 모니터링 지표:
- DB: 복제 지연, 커넥션 수, p95/p99 지연, 슬로우 쿼리 수
- Redis: hit ratio, used_memory/maxmemory, evicted_keys, 명령 지연
- App: Primary/Replica 읽기 비율, 캐시 hit/miss 비율, 강한 읽기 비율

## 8. 단계별 도입 계획
1. Phase 1: 단일 MySQL + Redis(Cache-Aside)
2. Phase 2: Replica 1대 추가 + 읽기/쓰기 분리
3. Phase 3: Replica 다중화 + 지연 기반 동적 라우팅
4. Phase 4: 카운터 동기화 파이프라인 고도화(배치 -> 이벤트 스트림)

## 9. 결론
- MySQL Primary/Replica + Redis 조합은 현재 요구사항에 적합하다.
- 다만 "모든 읽기를 Replica로 고정"하면 정합성 문제가 발생할 수 있다.
- 따라서 강한 읽기 정합성(Primary 우회) 규칙을 운영 표준으로 유지해야 한다.
