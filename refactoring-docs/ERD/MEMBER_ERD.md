# MEMBER ERD

## 요구사항 매핑
- `[MY-001]` 마이페이지: `member` 기본 프로필/식별 정보 참조
- 인증/인가 정책: `member.auth_provider`, `member_role.role`로 반영

## 설계 원칙
- 회원 계정은 소프트 상태값(`status`)으로 관리한다.
- 소셜/로컬 로그인을 단일 테이블에서 통합 관리한다.
- 권한은 다대다 확장 가능하도록 `member_role`로 분리한다.

## Role enum
- `MEMBER`
- `MANAGER`
- `ADMIN`

## member
- `id` BIGINT PRIMARY KEY AUTO_INCREMENT
- `email` VARCHAR(100) NOT NULL
- `password_hash` VARCHAR(255) NULL
- `auth_provider` VARCHAR(20) NOT NULL
- `provider_user_id` VARCHAR(100) NULL
- `custom_id` VARCHAR(50) NOT NULL
- `status` VARCHAR(20) NOT NULL
- `birth_date` DATE NULL
- `created_at` DATETIME NOT NULL
- `updated_at` DATETIME NOT NULL
- `last_login_at` DATETIME NULL

제약/인덱스:
- UNIQUE: `email`
- UNIQUE: `custom_id`
- UNIQUE: `(auth_provider, provider_user_id)`
- CHECK: `auth_provider IN ('LOCAL','GOOGLE','KAKAO','NAVER')`
- CHECK: `status IN ('ACTIVE','SUSPENDED','DELETED')`
- INDEX: `idx_member_status_created_at (status, created_at)`

간략 DDL:
```sql
CREATE TABLE member (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(100) NOT NULL,
  password_hash VARCHAR(255) NULL,
  auth_provider VARCHAR(20) NOT NULL,
  provider_user_id VARCHAR(100) NULL,
  custom_id VARCHAR(50) NOT NULL,
  status VARCHAR(20) NOT NULL,
  birth_date DATE NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  last_login_at DATETIME NULL,
  CONSTRAINT uq_member_email UNIQUE (email),
  CONSTRAINT uq_member_custom_id UNIQUE (custom_id),
  CONSTRAINT uq_member_provider UNIQUE (auth_provider, provider_user_id),
  CONSTRAINT chk_member_provider CHECK (auth_provider IN ('LOCAL','GOOGLE','KAKAO','NAVER')),
  CONSTRAINT chk_member_status CHECK (status IN ('ACTIVE','SUSPENDED','DELETED'))
);

CREATE INDEX idx_member_status_created_at
  ON member(status, created_at);
```

## member_role
- `id` BIGINT PRIMARY KEY AUTO_INCREMENT
- `member_id` BIGINT NOT NULL
- `role` VARCHAR(20) NOT NULL
- `assigned_at` DATETIME NOT NULL
- `assigned_by` BIGINT NULL
- `is_primary` BOOLEAN NOT NULL DEFAULT FALSE

제약/인덱스:
- FK: `member_id` -> `member.id`
- FK: `assigned_by` -> `member.id`
- UNIQUE: `(member_id, role)`
- CHECK: `role IN ('MEMBER','MANAGER','ADMIN')`
- INDEX: `idx_member_role_member_primary (member_id, is_primary)`

간략 DDL:
```sql
CREATE TABLE member_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  member_id BIGINT NOT NULL,
  role VARCHAR(20) NOT NULL,
  assigned_at DATETIME NOT NULL,
  assigned_by BIGINT NULL,
  is_primary BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT fk_member_role_member FOREIGN KEY (member_id) REFERENCES member(id),
  CONSTRAINT fk_member_role_assigned_by FOREIGN KEY (assigned_by) REFERENCES member(id),
  CONSTRAINT uq_member_role UNIQUE (member_id, role),
  CONSTRAINT chk_member_role CHECK (role IN ('MEMBER','MANAGER','ADMIN'))
);

CREATE INDEX idx_member_role_member_primary
  ON member_role(member_id, is_primary);
```

## 운영 규칙
- 사용자 1명은 복수 권한을 가질 수 있다.
- 신규 가입 시 `MEMBER` 권한 1건을 기본 부여한다.
- 마지막 권한 제거는 금지한다.

## member FK 연동 대상 (예정)
1. 장바구니
2. 구매/주문
3. 인바디 기록
4. 체형분석
5. 설문
6. 추천기록
7. 커뮤니티(게시글/댓글/신고)
8. 평점
