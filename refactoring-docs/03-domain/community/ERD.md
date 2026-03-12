# COMMUNITY ERD

커뮤니티 데이터 모델을 이해하기 쉬운 형태로 정리한 문서입니다.

## 주요 테이블
- `community_post`: 게시글 원장
- `community_comment`: 댓글/대댓글
- `community_reaction`: 좋아요/싫어요 원장
- `community_report`: 신고
- `community_moderation_action`: 관리자 조치 이력
- `evaluation`: 별점/의견

## 핵심 제약
- 반응 중복 방지: `(target_type, target_id, member_id)` unique
- 신고 중복 방지: `(target_type, target_id, reporter_member_id)` unique
- 평가 중복 방지: `member_id` unique
- 평점 범위: 1~5

## 운영 포인트
- 조회수/좋아요/싫어요는 Redis 집계 가능
- 최종 원장은 MySQL 유지
