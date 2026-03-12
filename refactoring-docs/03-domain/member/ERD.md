# MEMBER ERD

회원/권한 모델의 핵심 구조를 설명합니다.

## 주요 테이블
- `member`: 회원 계정 정보
- `member_role`: 회원 권한 정보

## 핵심 규칙
- 이메일은 unique
- custom_id는 unique
- 신규 가입 시 `USER` 권한 자동 부여
- 사용자 1명은 여러 권한을 가질 수 있음

## 상태/제공자 예시
- status: `ACTIVE`, `SUSPENDED`, `DELETED`
- auth_provider: `LOCAL`, `GOOGLE`, `KAKAO`, `NAVER`
