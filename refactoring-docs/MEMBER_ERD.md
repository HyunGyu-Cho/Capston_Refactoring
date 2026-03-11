# Member ERD (ENUM + member_role)

## member
- `id` BIGINT PK AUTO_INCREMENT
- `email` VARCHAR(100) NOT NULL UNIQUE
- `password_hash` VARCHAR(255) NULL
- `auth_provider` VARCHAR(20) NOT NULL (`LOCAL`, `GOOGLE`, `KAKAO`, `NAVER`)
- `provider_user_id` VARCHAR(100) NULL
- `custom_id` VARCHAR(50) NOT NULL UNIQUE
- `status` VARCHAR(20) NOT NULL (`ACTIVE`, `SUSPENDED`, `DELETED`)
- `created_at` DATETIME NOT NULL
- `updated_at` DATETIME NOT NULL
- `last_login_at` DATETIME NULL

제약:
- UNIQUE(`auth_provider`, `provider_user_id`)

## Role enum
- `MEMBER`
- `MANAGER`
- `ADMIN`

## member_role
- `id` BIGINT PK AUTO_INCREMENT
- `member_id` BIGINT NOT NULL FK -> `member.id`
- `role` VARCHAR(20) NOT NULL (Role enum string)
- `assigned_at` DATETIME NOT NULL
- `assigned_by` BIGINT NULL FK -> `member.id`
- `is_primary` BOOLEAN NOT NULL DEFAULT FALSE

제약:
- UNIQUE(`member_id`, `role`)

정책:
- 사용자 1명은 복수 권한 가능
- 신규 가입 시 `MEMBER` 1건 기본 부여
- 마지막 권한 삭제 금지

## member FK 연동 대상(예정)
1. 장바구니
2. 구매/주문
3. 인바디 레코드
4. 체형분석
5. 설문
6. 추천기록
7. 커뮤니티(게시글/댓글/신고)
8. 평가
