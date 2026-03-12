# API Contract (Draft)

이 문서는 각 모듈 API의 "엔드포인트 목록"을 빠르게 확인하는 용도입니다.
상세 요청/응답 필드는 모듈 문서에서 확정합니다.

## 공통
- Base URL: `/api/v1`
- 인증: Bearer JWT
- Content-Type: `application/json`

## 공통 응답 형태
```json
{
  "success": true,
  "data": {},
  "error": null,
  "timestamp": "2026-03-12T10:00:00Z"
}
```

## Auth
- `POST /auth/signup`
- `POST /auth/login`
- `POST /auth/refresh`
- `POST /auth/logout`
- `GET /auth/me`

## Inbody
- `POST /inbody`
- `POST /inbody/image`
- `POST /inbody/device`
- `GET /inbody/latest`
- `GET /inbody`
- `GET /inbody/{inbodyId}`

## Analysis
- `POST /analysis/body`
- `GET /analysis/body/latest`
- `GET /analysis/body`
- `GET /analysis/body/{analysisId}`

## Survey
- `POST /surveys`
- `GET /surveys/latest`
- `GET /surveys`
- `GET /surveys/{surveyId}`

## Recommendation
- `POST /recommendations/workout`
- `GET /recommendations/workout/latest`
- `POST /recommendations/diet`
- `GET /recommendations/diet/latest`

## Community
- `POST /community/posts`
- `GET /community/posts`
- `POST /community/reports`

## Health
- `GET /health`
- `GET /ready`
