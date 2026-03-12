# COMMUNITY DOMAIN DESIGN

이 문서는 커뮤니티 기능이 어떤 규칙으로 동작하는지 쉽게 설명합니다.

## 포함 범위
- 게시글, 댓글/대댓글
- 좋아요/싫어요
- 신고, 관리자 조치
- 평가

## 핵심 정책
1. 게시글/댓글 삭제는 소프트 삭제
2. 대댓글은 depth 2까지
3. 동일 사용자의 동일 대상 반응은 1개만 허용
4. 동일 사용자의 동일 대상 신고는 1회만 허용
5. 평점은 1~5, 사용자당 1회

## 주요 이벤트
- PostCreated, PostDeleted
- ReactionChanged
- ReportCreated, ReportThresholdReached
- ModerationActionCreated
