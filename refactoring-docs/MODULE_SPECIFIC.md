# MODULE SPECIFIC

## 문서 목적
- `refactoring-docs/requirements/REQUIREMENTS.md` 및 ERD 문서를 기준으로 모듈 경계를 확정한다.
- 각 모듈의 책임과 대략적인 API 엔드포인트를 정의한다.
- 상세 요청/응답 스키마는 추후 `API_CONTRACT.md`에서 확정한다.

## 공통 규칙
- Base Path: `/api/v1`
- 인증: JWT Access Token (30분), Refresh Token (7일)
- 권한: `USER`, `ADMIN`
- AI 생성 계열 API는 `Idempotency-Key` 헤더 사용 권장
- 오류 응답은 공통 포맷(`code`, `message`, `traceId`)을 사용

---

## 1) auth (identity-access)
### 책임
- 회원가입/로그인/토큰 재발급/로그아웃
- 인가 기본 정책 적용

### 주요 연관 데이터
- `member`, `member_role`

### 엔드포인트(초안)
- `POST /auth/signup`
- `POST /auth/login`
- `POST /auth/refresh`
- `POST /auth/logout`
- `GET /auth/me`

---

## 2) inbodyInput
### 책임
- 인바디 입력(수동/이미지/연동)
- 필수/선택 항목 검증
- 신규 업로드 이력 관리

### 주요 연관 데이터
- `inbody`, `inbody_segment`

### 엔드포인트(초안)
- `POST /inbody`
- `POST /inbody/image`
- `POST /inbody/device`
- `GET /inbody/latest`
- `GET /inbody`
- `GET /inbody/{inbodyId}`

---

## 3) inbodyAnalysis
### 책임
- 최신 인바디 기반 체형분석 생성/조회
- 분석 상태 및 결과 이력 관리

### 주요 연관 데이터
- `inbody_analysis`, `inbody_analysis_execution_log`

### 엔드포인트(초안)
- `POST /analysis/body`
- `GET /analysis/body/latest`
- `GET /analysis/body`
- `GET /analysis/body/{analysisId}`

---

## 4) survey
### 책임
- 설문 응답 저장/조회
- 추천 생성 입력 컨텍스트 제공

### 주요 연관 데이터
- `survey`

### 엔드포인트(초안)
- `POST /surveys`
- `GET /surveys/latest`
- `GET /surveys`
- `GET /surveys/{surveyId}`

---

## 5) recommendation-workout
### 책임
- 설문 기반 운동 추천 생성/저장/조회

### 주요 연관 데이터
- `workout_recommendation`, `workout_recommendation_detail`

### 엔드포인트(초안)
- `POST /recommendations/workout`
- `GET /recommendations/workout/latest`
- `GET /recommendations/workout`
- `GET /recommendations/workout/{recommendationId}`

---

## 6) recommendation-diet
### 책임
- 설문 기반 식단 추천 생성/저장/조회

### 주요 연관 데이터
- `diet_recommendation`, `diet_recommendation_detail`

### 엔드포인트(초안)
- `POST /recommendations/diet`
- `GET /recommendations/diet/latest`
- `GET /recommendations/diet`
- `GET /recommendations/diet/{recommendationId}`

---

## 7) community
### 책임
- 게시글/댓글/대댓글
- 좋아요/싫어요, 조회수, 인기글, 신고
- 소프트 삭제 및 관리자 조치 이력

### 주요 연관 데이터
- `community_post`, `community_comment`, `community_reaction`, `community_report`, `moderation_action`

### 엔드포인트(초안)
- `POST /community/posts`
- `GET /community/posts`
- `GET /community/posts/{postId}`
- `PATCH /community/posts/{postId}`
- `DELETE /community/posts/{postId}`
- `POST /community/posts/{postId}/comments`
- `PATCH /community/comments/{commentId}`
- `DELETE /community/comments/{commentId}`
- `POST /community/posts/{postId}/reactions`
- `DELETE /community/posts/{postId}/reactions`
- `POST /community/posts/{postId}/views`
- `GET /community/posts/popular`
- `POST /community/reports`
- `POST /admin/community/moderation-actions`

---

## 8) evaluation
### 책임
- 별점(1~5) 및 의견 등록/조회

### 주요 연관 데이터
- `evaluation`

### 엔드포인트(초안)
- `POST /evaluations`
- `GET /evaluations/me`
- `GET /admin/evaluations`

---

## 9) product
### 책임
- 상품/프로그램 카탈로그 조회
- 판매 상태/가격/재고 관리(정책 확정 후)

### 주요 연관 데이터(예정)
- `product`, `product_option`, `product_price`, `inventory`

### 엔드포인트(초안)
- `GET /products`
- `GET /products/{productId}`
- `GET /products/categories`
- `POST /admin/products`
- `PATCH /admin/products/{productId}`
- `PATCH /admin/products/{productId}/status`

---

## 10) cart (장바구니)
### 책임
- 장바구니 담기/수정/삭제/조회

### 주요 연관 데이터(예정)
- `cart`, `cart_item`

### 엔드포인트(초안)
- `GET /cart`
- `POST /cart/items`
- `PATCH /cart/items/{cartItemId}`
- `DELETE /cart/items/{cartItemId}`
- `DELETE /cart`

---

## 11) purchase (구매기능)
### 책임
- 주문 생성/결제 처리/상태 전이

### 주요 연관 데이터(예정)
- `order`, `order_item`, `payment`, `refund`

### 엔드포인트(초안)
- `POST /purchases`
- `POST /purchases/{purchaseId}/pay`
- `POST /purchases/{purchaseId}/cancel`
- `POST /purchases/{purchaseId}/refund`
- `GET /purchases/{purchaseId}`

---

## 12) purchase_history (구매목록 조회)
### 책임
- 사용자 구매 이력 조회

### 주요 연관 데이터(예정)
- `order`, `payment`, `refund`

### 엔드포인트(초안)
- `GET /purchase-history`
- `GET /purchase-history/{purchaseId}`
- `GET /purchase-history/summary`

---

## 13) userInfo (mypage 및 히스토리 조회)
### 책임
- 마이페이지 집계 조회
- 최신 카드/변화 추이/통합 히스토리 제공

### 주요 연관 데이터
- `member` + `inbody` + `analysis` + `recommendation` + `survey` + `purchase` + `cart`

### 엔드포인트(초안)
- `GET /users/me/profile`
- `PATCH /users/me/profile`
- `GET /users/me/dashboard`
- `GET /users/me/history`
- `GET /users/me/trends`

---

## 14) common
### 책임
- 공통 응답/예외/로깅/보안 유틸
- 공통 설정(페이징, 정렬, 검증, 트레이싱)

### 공통 API(최소)
- `GET /health`
- `GET /ready`

---

## 15) ai-client
### 책임
- OpenAI(ChatGPT) API 연동 추상화
- 프롬프트 버전 관리, 요청 해시, 실행 메트릭/에러 처리

### 운영 엔드포인트(선택)
- `GET /admin/ai/prompts`
- `POST /admin/ai/prompts`
- `GET /admin/ai/executions`

---

## 모듈 간 의존 권장
- `auth -> member`
- `inbodyAnalysis -> inbodyInput, ai-client`
- `recommendation-workout -> survey, ai-client`
- `recommendation-diet -> survey, ai-client`
- `community -> auth, common`
- `evaluation -> auth, common`
- `userInfo -> 다수 모듈(read only 집계)`
- 순환 의존 금지, 쓰기 소유권은 각 도메인 모듈에 둔다.
