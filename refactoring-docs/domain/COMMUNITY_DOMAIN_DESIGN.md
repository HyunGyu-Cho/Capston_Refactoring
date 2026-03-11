# COMMUNITY DOMAIN DESIGN

## 1. 이 문서의 목적
- 커뮤니티 도메인에서 무엇을 저장하고 어떤 규칙으로 동작하는지 빠르게 이해하기 위한 설계 문서
- 구현 전에 정책/경계/불변조건을 고정하기 위한 기준 문서

## 2. 범위
- 포함: 게시글, 댓글/대댓글, 좋아요/싫어요, 신고, 관리자 조치, 프로그램 평가
- 제외: 인증 자체, 결제/주문, 추천 생성

## 3. 한눈에 보는 핵심 정책
1. 게시글/댓글 삭제는 소프트 삭제만 허용한다.
2. 게시글 `updatedAt`은 미수정이면 `null`이다.
3. 댓글은 depth 2(댓글, 대댓글)까지만 허용한다.
4. 반응은 동일 사용자-동일 대상에 1건만 허용한다.
5. 신고는 동일 사용자-동일 대상에 1회만 허용한다.
6. 평가는 사용자 1회만 허용하고 별점은 1~5 범위만 허용한다.

## 4. Aggregate 설계

### 4.1 CommunityPost (Aggregate Root)
- Entity:
1. `CommunityPost`
2. `CommunityComment` (post 하위 모델)
- Value Object:
1. `PostCategory` (`FREE|WORKOUT|DIET|REVIEW|PRODUCT`)
2. `AnonymousFlag`
- Invariants:
1. 삭제된 게시글은 수정 불가
2. `updatedAt`은 최초 작성 시 `null`, 수정 시 갱신
3. 댓글 최대 depth 2
4. 삭제는 `isDeleted=true`로 처리

### 4.2 CommunityReaction (Aggregate Root)
- Entity:
1. `CommunityReaction`
- Value Object:
1. `ReactionTarget` (`POST|COMMENT` + `targetId`)
2. `ReactionType` (`LIKE|DISLIKE`)
- Invariants:
1. 동일 사용자-동일 대상 반응 1건 유지
2. 반응 변경은 upsert(덮어쓰기) 허용

### 4.3 CommunityReport (Aggregate Root)
- Entity:
1. `CommunityReport`
2. `ModerationAction`
- Value Object:
1. `ReportTarget` (`POST|COMMENT` + `targetId`)
2. `ReportStatus` (`PENDING|REVIEWED|ACTION_TAKEN|REJECTED`)
3. `ModerationActionType` (`DELETE|BLIND|RESTORE|WARN`)
- Invariants:
1. 동일 사용자-동일 대상 신고 1회만 허용
2. 신고 상태 전이는 정의된 규칙만 허용
3. 관리자 조치는 항상 이력 생성

### 4.4 Evaluation (Aggregate Root)
- Entity:
1. `Evaluation`
- Invariants:
1. 사용자 1회만 평가 가능 (`memberId` unique)
2. `rate`는 1~5 범위
3. 단일 프로그램 전제(현재 `programId` 없음)

## 5. Domain Service

### 5.1 ViewCountPolicyService
1. 로그인 사용자만 조회수 증가
2. 중복 조회 쿨다운(기본 10분)
3. Redis 실패 시 DB 직접 증가 fallback

### 5.2 ReportThresholdService
1. 대상별 누적 신고 건수 계산
2. 누적 5건 이상 시 관리자 알림 이벤트 발행

## 6. 상태 전이 규칙

### 6.1 신고 상태 전이
- 허용:
1. `PENDING -> REVIEWED`
2. `PENDING -> ACTION_TAKEN`
3. `PENDING -> REJECTED`
4. `REVIEWED -> ACTION_TAKEN`
5. `REVIEWED -> REJECTED`
- 금지:
1. `ACTION_TAKEN` 또는 `REJECTED` 이후 재오픈

### 6.2 게시글/댓글 상태 전이
- 허용:
1. `ACTIVE -> DELETED` (soft delete)
2. 관리자 `RESTORE` 조치로 복구

## 7. Repository Interface (Domain 기준)

### 7.1 PostRepository
1. `save(post)`
2. `findById(postId)`
3. `findActiveById(postId)`
4. `findPage(category, pageable)`

### 7.2 CommentRepository
1. `save(comment)`
2. `findByPostId(postId)`
3. `findById(commentId)`

### 7.3 ReactionRepository
1. `findByMemberAndTarget(memberId, targetType, targetId)`
2. `upsert(reaction)`
3. `delete(memberId, targetType, targetId)`
4. `countByTargetAndType(targetType, targetId, reactionType)`

### 7.4 ReportRepository
1. `save(report)`
2. `existsByReporterAndTarget(reporterId, targetType, targetId)`
3. `countByTarget(targetType, targetId)`
4. `findById(reportId)`

### 7.5 ModerationActionRepository
1. `save(action)`
2. `findByTarget(targetType, targetId)`

### 7.6 EvaluationRepository
1. `save(evaluation)`
2. `findByMemberId(memberId)`
3. `existsByMemberId(memberId)`

## 8. Use Case 목록
1. 게시글 작성/수정/삭제
2. 게시글 목록/상세 조회
3. 댓글/대댓글 작성/수정/삭제
4. 반응 등록/변경/취소
5. 신고 등록
6. 관리자 조치 등록
7. 평가 등록/내 평가 조회

## 9. 도메인 이벤트
1. `PostCreated`
2. `PostUpdated`
3. `PostDeleted`
4. `ReactionChanged`
5. `ReportCreated`
6. `ReportThresholdReached`
7. `ModerationActionCreated`
8. `EvaluationSubmitted`

## 10. 검증 체크리스트
1. 카테고리 enum 검증
2. 반응 중복 검증(unique)
3. 신고 중복 검증(unique)
4. 평가 중복 검증(unique)
5. 별점 범위 검증(1~5)
6. 수정/삭제 권한 검증(작성자 또는 관리자)
