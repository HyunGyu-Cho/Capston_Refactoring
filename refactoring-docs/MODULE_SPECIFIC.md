# MODULE SPECIFIC

## 문서 목적
- `refactoring-docs/requirements/REQUIREMENTS.md` 및 ERD 문서를 기준으로 모듈 경계를 확정한다.
- 각 모듈의 책임과 대략적인 API 엔드포인트를 정의한다.
- 상세 요청/응답 스키마는 추후 `API_CONTRACT.md`에서 확정한다.

## 공통 규칙
- Base Path: `/api/v1`
- 인증: JWT Access Token (30분), Refresh Token (7일)
- 권한:
- 일반 사용자: `USER`
- 관리자: `ADMIN`
- 멱등성/중복방지:
- AI 생성 계열 API는 `Idempotency-Key` 헤더 권장
- 오류 응답: 공통 에러 포맷(`code`, `message`, `traceId`) 사용

---

## 1) auth (identity-access)
### 책임
- 회원가입/로그인/토큰 재발급/로그아웃
- 인가 기본 정책 적용

### 주요 연관 데이터
- `member`, `member_role`

### 엔드포인트(초안)
- `POST /auth/signup` : 회원가입
- `POST /auth/login` : 로그인(Access/Refresh 발급)
- `POST /auth/refresh` : 토큰 재발급
- `POST /auth/logout` : 로그아웃(Refresh 무효화)
- `GET /auth/me` : 내 인증 주체 조회

---

## 2) inbodyInput
### 책임
- 인바디 입력(수동/이미지/연동)
- 필수/선택 항목 검증
- 신규 업로드 이력 관리

### 주요 연관 데이터
- `inbody`, `inbody_segment`

### 엔드포인트(초안)
- `POST /inbody` : 인바디 수동 입력 저장
- `POST /inbody/image` : 인바디 결과지 이미지 업로드/OCR 추출
- `POST /inbody/device` : 앱/기기 연동 입력 저장
- `GET /inbody/latest` : 내 최신 인바디 조회
- `GET /inbody` : 내 인바디 이력 조회(페이징)
- `GET /inbody/{inbodyId}` : 특정 인바디 상세 조회

---

## 3) inbodyAnalysis
### 책임
- 최신 인바디 기반 체형분석 생성/조회
- 분석 상태 및 결과 이력 관리

### 주요 연관 데이터
- `inbody_analysis`, `inbody_analysis_execution_log`

### 엔드포인트(초안)
- `POST /analysis/body` : 체형분석 생성 요청
- `GET /analysis/body/latest` : 최신 체형분석 조회
- `GET /analysis/body` : 체형분석 이력 조회
- `GET /analysis/body/{analysisId}` : 체형분석 상세 조회

---

## 4) survey
### 책임
- 설문 응답 저장/조회
- 추천 생성 입력 컨텍스트 제공

### 주요 연관 데이터
- `survey`

### 엔드포인트(초안)
- `POST /surveys` : 설문 작성
- `GET /surveys/latest` : 최신 설문 조회
- `GET /surveys` : 설문 이력 조회
- `GET /surveys/{surveyId}` : 설문 상세 조회

---

## 5) recommendation-workout
### 책임
- 설문 기반 운동 추천 생성/저장/조회

### 주요 연관 데이터
- `workout_recommendation`, `workout_recommendation_detail`, 실행 로그

### 엔드포인트(초안)
- `POST /recommendations/workout` : 운동 추천 생성 요청(`surveyId`)
- `GET /recommendations/workout/latest` : 최신 운동 추천 조회
- `GET /recommendations/workout` : 운동 추천 이력 조회
- `GET /recommendations/workout/{recommendationId}` : 운동 추천 상세 조회

---

## 6) recommendation-diet
### 책임
- 설문 기반 식단 추천 생성/저장/조회

### 주요 연관 데이터
- `diet_recommendation`, `diet_recommendation_detail`, 실행 로그

### 엔드포인트(초안)
- `POST /recommendations/diet` : 식단 추천 생성 요청(`surveyId`)
- `GET /recommendations/diet/latest` : 최신 식단 추천 조회
- `GET /recommendations/diet` : 식단 추천 이력 조회
- `GET /recommendations/diet/{recommendationId}` : 식단 추천 상세 조회

---

## 7) product
### 책임
- 상품/프로그램 카탈로그 조회
- 판매 상태/가격/재고(정책 확정 후) 관리

### 주요 연관 데이터(예정)
- `product`, `product_option`, `product_price`, `inventory`

### 엔드포인트(초안)
- `GET /products` : 상품 목록 조회
- `GET /products/{productId}` : 상품 상세 조회
- `GET /products/categories` : 상품 카테고리 조회
- `POST /admin/products` : 상품 등록(관리자)
- `PATCH /admin/products/{productId}` : 상품 수정(관리자)
- `PATCH /admin/products/{productId}/status` : 상품 판매상태 변경(관리자)

---

## 8) cart (장바구니)
### 책임
- 장바구니 담기/수정/삭제/조회
- 중복 정책 및 만료 정책(추후 확정)

### 주요 연관 데이터(예정)
- `cart`, `cart_item`

### 엔드포인트(초안)
- `GET /cart` : 내 장바구니 조회
- `POST /cart/items` : 장바구니 상품 추가
- `PATCH /cart/items/{cartItemId}` : 장바구니 수량/옵션 수정
- `DELETE /cart/items/{cartItemId}` : 장바구니 항목 삭제
- `DELETE /cart` : 장바구니 전체 비우기

---

## 9) purchase (구매기능)
### 책임
- 주문 생성/결제 처리/상태 전이

### 주요 연관 데이터(예정)
- `order`, `order_item`, `payment`, `refund`

### 엔드포인트(초안)
- `POST /purchases` : 주문 생성(장바구니 또는 단건)
- `POST /purchases/{purchaseId}/pay` : 결제 요청
- `POST /purchases/{purchaseId}/cancel` : 주문 취소
- `POST /purchases/{purchaseId}/refund` : 환불 요청
- `GET /purchases/{purchaseId}` : 주문 상세 조회

---

## 10) purchase_history (구매목록 조회)
### 책임
- 사용자 구매 이력 조회
- 기간/상태 필터 및 페이징 제공

### 주요 연관 데이터(예정)
- `order`, `payment`, `refund`

### 엔드포인트(초안)
- `GET /purchase-history` : 내 구매목록 조회
- `GET /purchase-history/{purchaseId}` : 구매 상세 조회
- `GET /purchase-history/summary` : 구매 요약(총건수/총금액 등)

---

## 11) userInfo (mypage 및 히스토리 조회)
### 책임
- 마이페이지 집계 조회
- 최신 카드/변화 추이/통합 히스토리 제공

### 주요 연관 데이터
- `member` + `inbody` + `analysis` + `recommendation` + `survey` + `purchase` + `cart`

### 엔드포인트(초안)
- `GET /users/me/profile` : 내 프로필 조회
- `PATCH /users/me/profile` : 내 프로필 수정
- `GET /users/me/dashboard` : 마이페이지 통합 카드 조회(`MY-001`)
- `GET /users/me/history` : 통합 히스토리 조회
- `GET /users/me/trends` : 변화 추이 조회(인바디/분석 기반)

---

## 12) common
### 책임
- 공통 응답/예외/로깅/보안 유틸
- 공통 설정(페이징, 정렬, 검증, 트레이싱)

### 공통 API(필요 최소)
- `GET /health` : 헬스체크
- `GET /ready` : 레디니스 체크

---

## 13) ai-client
### 책임
- OpenAI(ChatGPT) API 연동 추상화
- 프롬프트 버전 관리, 요청 해시, 실행 메트릭/에러 처리

### 내부 인터페이스 중심
- 외부 공개 API보다 내부 서비스 호출용 모듈로 운영
- 필요 시 관리자/운영 API만 제한적으로 노출

### 운영 엔드포인트(선택)
- `GET /admin/ai/prompts` : 프롬프트 버전 목록(관리자)
- `POST /admin/ai/prompts` : 프롬프트 버전 등록(관리자)
- `GET /admin/ai/executions` : AI 호출 실행 로그 조회(관리자)

---

## 모듈 간 의존 권장
- `auth -> member`
- `inbodyAnalysis -> inbodyInput, ai-client`
- `survey -> (독립)`
- `recommendation-workout -> survey, ai-client`
- `recommendation-diet -> survey, ai-client`
- `community(추가 시) -> auth, member, common, cache`
- `userInfo -> 다수 모듈(read only 집계)`
- 순환 의존 금지, 쓰기 소유권은 각 도메인 모듈에 둔다.
