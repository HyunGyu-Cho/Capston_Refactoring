# COMMUNITY ERD

## 요구사항 매핑
- `[COMM-001]` 커뮤니티 카테고리
- `[COMM-002]` 게시글/댓글/대댓글/익명/신고/상호작용
- `[COMM-003]` 게시글 수정/삭제(소프트 삭제)
- `[COMM-004]` 신고 누적 5건 이상 관리자 알림/조치 이력

## 설계 원칙
- 삭제는 물리 삭제가 아닌 `is_deleted` 기반 소프트 삭제를 사용한다.
- 좋아요/싫어요/조회수는 Redis 캐시를 사용하되, MySQL을 원장(Source of Truth)으로 유지한다.
- 대댓글은 `parent_comment_id`로 표현한다.

## Category enum
- `FREE` (자유)
- `WORKOUT` (운동)
- `DIET` (식단)
- `REVIEW` (사용후기)
- `PRODUCT` (상품)

## community_post
- `post_id` BIGINT PRIMARY KEY AUTO_INCREMENT
- `member_id` BIGINT NOT NULL
- `custom_id` VARCHAR(50) NOT NULL
- `category` VARCHAR(20) NOT NULL
- `title` VARCHAR(200) NOT NULL
- `content` TEXT NOT NULL
- `is_anonymous` BOOLEAN NOT NULL DEFAULT FALSE
- `like_count` INT NOT NULL DEFAULT 0
- `dislike_count` INT NOT NULL DEFAULT 0
- `view_count` INT NOT NULL DEFAULT 0
- `created_at` DATETIME NOT NULL
- `updated_at` DATETIME NULL
- `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE

제약/인덱스:
- FK: `member_id` -> `member.id`
- UNIQUE: `custom_id`
- CHECK: `category IN ('FREE','WORKOUT','DIET','REVIEW','PRODUCT')`
- CHECK: `like_count >= 0 AND dislike_count >= 0 AND view_count >= 0`
- INDEX: `idx_post_category_created_at (category, created_at)`
- INDEX: `idx_post_member_created_at (member_id, created_at)`
- INDEX: `idx_post_is_deleted_created_at (is_deleted, created_at)`

간략 DDL:
```sql
CREATE TABLE community_post (
  post_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  member_id BIGINT NOT NULL,
  custom_id VARCHAR(50) NOT NULL,
  category VARCHAR(20) NOT NULL,
  title VARCHAR(200) NOT NULL,
  content TEXT NOT NULL,
  is_anonymous BOOLEAN NOT NULL DEFAULT FALSE,
  like_count INT NOT NULL DEFAULT 0,
  dislike_count INT NOT NULL DEFAULT 0,
  view_count INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NULL,
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT fk_post_member FOREIGN KEY (member_id) REFERENCES member(id),
  CONSTRAINT uq_post_custom_id UNIQUE (custom_id),
  CONSTRAINT chk_post_category CHECK (category IN ('FREE','WORKOUT','DIET','REVIEW','PRODUCT')),
  CONSTRAINT chk_post_counts CHECK (like_count >= 0 AND dislike_count >= 0 AND view_count >= 0)
);
```

## community_comment
- `comment_id` BIGINT PRIMARY KEY AUTO_INCREMENT
- `post_id` BIGINT NOT NULL
- `member_id` BIGINT NOT NULL
- `parent_comment_id` BIGINT NULL
- `content` TEXT NOT NULL
- `is_anonymous` BOOLEAN NOT NULL DEFAULT FALSE
- `created_at` DATETIME NOT NULL
- `modified_at` DATETIME NOT NULL
- `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE

제약/인덱스:
- FK: `post_id` -> `community_post.post_id`
- FK: `member_id` -> `member.id`
- FK: `parent_comment_id` -> `community_comment.comment_id`
- INDEX: `idx_comment_post_created_at (post_id, created_at)`
- INDEX: `idx_comment_parent_created_at (parent_comment_id, created_at)`
- INDEX: `idx_comment_is_deleted_created_at (is_deleted, created_at)`

간략 DDL:
```sql
CREATE TABLE community_comment (
  comment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  member_id BIGINT NOT NULL,
  parent_comment_id BIGINT NULL,
  content TEXT NOT NULL,
  is_anonymous BOOLEAN NOT NULL DEFAULT FALSE,
  created_at DATETIME NOT NULL,
  modified_at DATETIME NOT NULL,
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT fk_comment_post FOREIGN KEY (post_id) REFERENCES community_post(post_id),
  CONSTRAINT fk_comment_member FOREIGN KEY (member_id) REFERENCES member(id),
  CONSTRAINT fk_comment_parent FOREIGN KEY (parent_comment_id) REFERENCES community_comment(comment_id)
);
```

## community_reaction (좋아요/싫어요 원장)
- `id` BIGINT PRIMARY KEY AUTO_INCREMENT
- `target_type` VARCHAR(20) NOT NULL
- `target_id` BIGINT NOT NULL
- `member_id` BIGINT NOT NULL
- `reaction_type` VARCHAR(20) NOT NULL
- `created_at` DATETIME NOT NULL

제약/인덱스:
- CHECK: `target_type IN ('POST','COMMENT')`
- CHECK: `reaction_type IN ('LIKE','DISLIKE')`
- UNIQUE: `(target_type, target_id, member_id)` (사용자 1명은 동일 대상에 좋아요/싫어요 포함 1회만 반응 가능)
- INDEX: `idx_reaction_target (target_type, target_id)`
- INDEX: `idx_reaction_member_created_at (member_id, created_at)`

간략 DDL:
```sql
CREATE TABLE community_reaction (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  target_type VARCHAR(20) NOT NULL,
  target_id BIGINT NOT NULL,
  member_id BIGINT NOT NULL,
  reaction_type VARCHAR(20) NOT NULL,
  created_at DATETIME NOT NULL,
  CONSTRAINT fk_reaction_member FOREIGN KEY (member_id) REFERENCES member(id),
  CONSTRAINT chk_reaction_target_type CHECK (target_type IN ('POST','COMMENT')),
  CONSTRAINT chk_reaction_type CHECK (reaction_type IN ('LIKE','DISLIKE')),
  CONSTRAINT uq_reaction_user_target UNIQUE (target_type, target_id, member_id)
);

CREATE INDEX idx_reaction_target
  ON community_reaction(target_type, target_id);
```

## community_report (신고)
- `report_id` BIGINT PRIMARY KEY AUTO_INCREMENT
- `target_type` VARCHAR(20) NOT NULL
- `target_id` BIGINT NOT NULL
- `reporter_member_id` BIGINT NOT NULL
- `reason_code` VARCHAR(30) NOT NULL
- `reason_detail` TEXT NULL
- `status` VARCHAR(20) NOT NULL
- `created_at` DATETIME NOT NULL

제약/인덱스:
- CHECK: `target_type IN ('POST','COMMENT')`
- CHECK: `status IN ('PENDING','REVIEWED','ACTION_TAKEN','REJECTED')`
- UNIQUE: `(target_type, target_id, reporter_member_id)`
- INDEX: `idx_report_target_status (target_type, target_id, status)`
- INDEX: `idx_report_status_created_at (status, created_at)`

간략 DDL:
```sql
CREATE TABLE community_report (
  report_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  target_type VARCHAR(20) NOT NULL,
  target_id BIGINT NOT NULL,
  reporter_member_id BIGINT NOT NULL,
  reason_code VARCHAR(30) NOT NULL,
  reason_detail TEXT NULL,
  status VARCHAR(20) NOT NULL,
  created_at DATETIME NOT NULL,
  CONSTRAINT fk_report_reporter FOREIGN KEY (reporter_member_id) REFERENCES member(id),
  CONSTRAINT chk_report_target_type CHECK (target_type IN ('POST','COMMENT')),
  CONSTRAINT chk_report_status CHECK (status IN ('PENDING','REVIEWED','ACTION_TAKEN','REJECTED')),
  CONSTRAINT uq_report_once UNIQUE (target_type, target_id, reporter_member_id)
);
```

## community_moderation_action (관리자 조치 이력)
- `action_id` BIGINT PRIMARY KEY AUTO_INCREMENT
- `report_id` BIGINT NULL
- `target_type` VARCHAR(20) NOT NULL
- `target_id` BIGINT NOT NULL
- `action_type` VARCHAR(30) NOT NULL
- `action_reason` TEXT NULL
- `admin_member_id` BIGINT NOT NULL
- `created_at` DATETIME NOT NULL

제약/인덱스:
- FK: `report_id` -> `community_report.report_id`
- FK: `admin_member_id` -> `member.id`
- CHECK: `target_type IN ('POST','COMMENT')`
- CHECK: `action_type IN ('DELETE','BLIND','RESTORE','WARN')`
- INDEX: `idx_mod_target_created_at (target_type, target_id, created_at)`

간략 DDL:
```sql
CREATE TABLE community_moderation_action (
  action_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  report_id BIGINT NULL,
  target_type VARCHAR(20) NOT NULL,
  target_id BIGINT NOT NULL,
  action_type VARCHAR(30) NOT NULL,
  action_reason TEXT NULL,
  admin_member_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL,
  CONSTRAINT fk_mod_report FOREIGN KEY (report_id) REFERENCES community_report(report_id),
  CONSTRAINT fk_mod_admin FOREIGN KEY (admin_member_id) REFERENCES member(id),
  CONSTRAINT chk_mod_target_type CHECK (target_type IN ('POST','COMMENT')),
  CONSTRAINT chk_mod_action_type CHECK (action_type IN ('DELETE','BLIND','RESTORE','WARN'))
);
```

## evaluation (프로그램 평가)
- 전제: 현재 서비스는 단일 프로그램 평가만 지원하므로 `program_id`는 두지 않는다.
- `evaluation_id` BIGINT PRIMARY KEY AUTO_INCREMENT
- `member_id` BIGINT NOT NULL
- `rate` TINYINT NOT NULL
- `feedback_detail` TEXT NULL
- `created_at` DATETIME NOT NULL

제약/인덱스:
- FK: `member_id` -> `member.id`
- CHECK: `rate BETWEEN 1 AND 5`
- UNIQUE: `(member_id)` 1인 1회 평가 정책
- INDEX: `idx_evaluation_rate_created_at (rate, created_at)`

간략 DDL:
```sql
CREATE TABLE evaluation (
  evaluation_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  member_id BIGINT NOT NULL,
  rate TINYINT NOT NULL,
  feedback_detail TEXT NULL,
  created_at DATETIME NOT NULL,
  CONSTRAINT fk_evaluation_member FOREIGN KEY (member_id) REFERENCES member(id),
  CONSTRAINT chk_evaluation_rate CHECK (rate BETWEEN 1 AND 5),
  CONSTRAINT uq_evaluation_member UNIQUE (member_id)
);

CREATE INDEX idx_evaluation_rate_created_at
  ON evaluation(rate, created_at);
```

## Redis 캐싱 전략 (요약)
- `post:{postId}:view_count`, `post:{postId}:like_count`, `post:{postId}:dislike_count` 키로 캐시/실시간 집계
- 반응 원장/신고/조치 이력은 MySQL 저장
- 배치/이벤트 소비로 Redis 카운트를 MySQL `community_post`에 주기 반영
