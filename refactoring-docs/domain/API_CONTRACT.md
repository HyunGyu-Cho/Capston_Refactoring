# API CONTRACT (Draft)

## 공통 규칙
- Base URL: `/api`
- 인증: Bearer JWT
- Content-Type: `application/json`
- 시간 포맷: ISO-8601 (UTC)

## 공통 응답 포맷
```json
{
  "success": true,
  "data": {},
  "error": null,
  "timestamp": "2026-03-12T10:00:00Z"
}
```

에러 응답:
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "COMM-404-001",
    "message": "Post not found"
  },
  "timestamp": "2026-03-12T10:00:00Z"
}
```

## 커뮤니티

### 1) 게시글 작성
- `POST /api/community/posts`
- 권한: 로그인 사용자
- request:
```json
{
  "category": "FREE",
  "title": "제목",
  "content": "내용",
  "isAnonymous": false
}
```
- response(data):
```json
{
  "postId": 1,
  "customId": "post_abc123",
  "createdAt": "2026-03-12T10:00:00Z"
}
```

### 2) 게시글 목록 조회
- `GET /api/community/posts?category=FREE&page=0&size=20&sort=createdAt,desc`
- 권한: 로그인 사용자
- response(data):
```json
{
  "items": [
    {
      "postId": 1,
      "customId": "post_abc123",
      "category": "FREE",
      "title": "제목",
      "authorName": "익명",
      "isAnonymous": true,
      "likeCount": 3,
      "dislikeCount": 0,
      "viewCount": 21,
      "createdAt": "2026-03-12T10:00:00Z",
      "updatedAt": null
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

### 3) 게시글 상세 조회
- `GET /api/community/posts/{postId}`
- 권한: 로그인 사용자만 가능 (조회수 증가는 로그인 사용자만)
- response(data): 게시글 + 댓글 트리

### 4) 게시글 수정
- `PATCH /api/community/posts/{postId}`
- 권한: 작성자 또는 관리자
- request:
```json
{
  "title": "수정 제목",
  "content": "수정 내용",
  "category": "WORKOUT"
}
```
- response(data):
```json
{
  "postId": 1,
  "updatedAt": "2026-03-12T10:10:00Z"
}
```

### 5) 게시글 삭제(소프트)
- `DELETE /api/community/posts/{postId}`
- 권한: 작성자 또는 관리자
- response(data):
```json
{
  "postId": 1,
  "isDeleted": true
}
```

### 6) 댓글/대댓글 작성
- `POST /api/community/posts/{postId}/comments`
- 권한: 로그인 사용자
- request:
```json
{
  "content": "댓글 내용",
  "isAnonymous": false,
  "parentCommentId": null
}
```
- response(data):
```json
{
  "commentId": 10,
  "postId": 1,
  "parentCommentId": null,
  "createdAt": "2026-03-12T10:00:00Z"
}
```

### 7) 댓글 수정
- `PATCH /api/community/comments/{commentId}`
- 권한: 작성자 또는 관리자

### 8) 댓글 삭제(소프트)
- `DELETE /api/community/comments/{commentId}`
- 권한: 작성자 또는 관리자

### 9) 반응 등록/변경(좋아요/싫어요)
- `PUT /api/community/reactions`
- 권한: 로그인 사용자
- request:
```json
{
  "targetType": "POST",
  "targetId": 1,
  "reactionType": "LIKE"
}
```
- 정책: 동일 사용자-동일 대상 1건 유지, 요청 시 upsert

### 10) 반응 취소
- `DELETE /api/community/reactions`
- 권한: 로그인 사용자
- request:
```json
{
  "targetType": "POST",
  "targetId": 1
}
```

### 11) 신고 등록
- `POST /api/community/reports`
- 권한: 로그인 사용자
- request:
```json
{
  "targetType": "COMMENT",
  "targetId": 10,
  "reasonCode": "ABUSE",
  "reasonDetail": "욕설"
}
```
- 정책: 동일 사용자-동일 대상 1회만

### 12) 관리자 조치 등록
- `POST /api/community/moderation-actions`
- 권한: 관리자
- request:
```json
{
  "reportId": 100,
  "targetType": "COMMENT",
  "targetId": 10,
  "actionType": "BLIND",
  "actionReason": "정책 위반"
}
```

### 13) 프로그램 평가 등록
- `POST /api/evaluations`
- 권한: 로그인 사용자
- request:
```json
{
  "rate": 5,
  "feedbackDetail": "좋았습니다"
}
```
- 정책: 사용자 1회만 가능

### 14) 내 평가 조회
- `GET /api/evaluations/me`
- 권한: 로그인 사용자

## 에러 코드
- `COMMON-400-001`: 잘못된 요청 값
- `COMMON-401-001`: 인증 실패
- `COMMON-403-001`: 권한 없음
- `COMM-404-001`: 게시글 없음
- `COMM-404-002`: 댓글 없음
- `COMM-409-001`: 이미 신고한 대상
- `EVAL-409-001`: 이미 평가한 사용자

## 권한 매트릭스 (요약)
- 비로그인: 평가 조회
- 로그인 사용자: 게시글/댓글 작성, 반응, 신고, 평가
- 작성자 또는 관리자: 게시글/댓글 수정/삭제
- 관리자: 조치 등록
