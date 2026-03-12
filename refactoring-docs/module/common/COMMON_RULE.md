# COMMON RULE

## 1. 목적
- 모든 모듈에서 공통으로 따라야 하는 API/예외/인증/로깅 규칙을 정의한다.
- 모듈별 구현 편차를 줄이고 프론트엔드 연동 안정성을 확보한다.

## 2. 범위
- 적용 대상: `auth`, `inbodyInput`, `inbodyAnalysis`, `survey`, `recommendation-*`, `community`, `evaluation`, `product`, `cart`, `purchase`, `purchase_history`, `userInfo`
- Base Path: `/api/v1`

## 3. 공통 응답 규격
```json
{
  "success": true,
  "data": {},
  "error": null,
  "timestamp": "2026-03-12T10:00:00Z"
}
```

### 규칙
- 성공 시 `success=true`, `data` 필수, `error=null`
- 실패 시 `success=false`, `data=null`, `error` 필수
- `timestamp`는 ISO-8601 UTC 문자열

## 4. 공통 에러 규격
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "COMMON-400-001",
    "message": "Invalid request",
    "traceId": "f2b6a4d1c9a04c2f"
  },
  "timestamp": "2026-03-12T10:00:00Z"
}
```

### Validation 에러(필드 단위)
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "COMMON-400-VAL",
    "message": "Validation failed",
    "traceId": "f2b6a4d1c9a04c2f",
    "fieldErrors": [
      { "field": "email", "reason": "must be email format" }
    ]
  },
  "timestamp": "2026-03-12T10:00:00Z"
}
```

## 5. 전역 예외 처리 규칙
- Validation 예외: `400`
- 인증 실패: `401`
- 권한 부족: `403`
- 리소스 없음: `404`
- 비즈니스 충돌(중복/상태 충돌): `409`
- 서버 내부 오류: `500`

## 6. 인증/인가 규칙
- 인증 방식: Bearer JWT
- Access Token 만료: 30분
- Refresh Token 만료: 7일
- 보호 API는 인증 필수
- 작성/수정/삭제는 소유자 또는 관리자만 허용
- 관리자 기능은 `ADMIN` 권한으로 제한

## 7. 현재 사용자 컨텍스트 규칙
- 인증 성공 시 서버는 `currentUserId`, `roles`를 요청 컨텍스트에 주입
- 컨트롤러/서비스는 토큰 파싱 직접 구현 금지(공통 컴포넌트만 사용)

## 8. 페이지네이션/정렬 규격

### 8.1 페이지 번호 기반(기본)
- 요청 쿼리: `page`, `size`, `sort`
- 기본값: `page=0`, `size=20`, `sort=id,desc`
- 허용 `size`: `10`, `20`, `50`, `100`
- 최대 `size`: `100`
- ID 정책: Snowflake 기반 고유 ID 사용
- 정렬/조회 성능 확보를 위해 `id` 또는 `(created_at, id)` 인덱스를 활용한다.

#### 페이지 응답 DTO
```json
{
  "items": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0
}
```

### 8.2 무한 스크롤 기반(커서)
- 요청 쿼리: `cursor`, `size`
- 기본값: `size=20`
- 허용 `size`: `10`, `20`, `50`, `100`
- 정렬 기준: 최신순(`id desc` 또는 `created_at desc, id desc`)
- 다음 페이지는 마지막 항목의 정렬 키를 `cursor`로 전달한다.

#### 커서 응답 DTO
```json
{
  "items": [],
  "nextCursor": "18402039812039123",
  "hasNext": true,
  "size": 20
}
```

## 9. 로깅/트레이싱 규칙
- 모든 요청에 `traceId` 생성/전파
- 구조화 로그(JSON) 사용
- 필수 로그 필드: `timestamp`, `traceId`, `endpoint`, `method`, `status`, `latencyMs`
- 민감정보(비밀번호, 토큰, 주민정보 등) 로그 출력 금지

## 10. 시간/직렬화 규칙
- 시간 기준: UTC
- 응답 날짜시간 포맷: ISO-8601
- Enum은 문자열로 직렬화

## 11. 헬스체크 규칙
- `GET /api/v1/health`: 프로세스 생존 여부
- `GET /api/v1/ready`: 의존성(DB/Redis 등) 준비 여부

## 12. 버전 관리 규칙
- 브레이킹 변경은 `/api/v2` 등 새 버전으로 분리
- 비브레이킹 변경은 기존 버전 내 확장(필드 추가) 우선

## 13. 구현 우선순위(최소 공통 골격)
1. 공통 응답/에러 DTO
2. 전역 예외 핸들러
3. JWT 인증 필터 및 현재 사용자 추출
4. 페이지네이션 공통 객체
5. traceId 기반 로깅 필터
