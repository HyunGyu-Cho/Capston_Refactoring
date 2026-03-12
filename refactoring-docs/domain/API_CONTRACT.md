# API_CONTRACT (Draft)

## 1. 공통
- Base URL: `/api/v1`
- 인증: Bearer JWT
- 토큰 정책: Access 30분, Refresh 7일
- Content-Type: `application/json` (파일 업로드 API 제외)

### 공통 응답 포맷
```json
{
  "success": true,
  "data": {},
  "error": null,
  "timestamp": "2026-03-12T10:00:00Z"
}
```

### 공통 에러 포맷
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "COMMON-400-001",
    "message": "Invalid request"
  },
  "timestamp": "2026-03-12T10:00:00Z"
}
```

---

## 2. auth (identity-access)
- `POST /auth/signup` : 회원가입
- `POST /auth/login` : 로그인(Access/Refresh 발급)
- `POST /auth/refresh` : 토큰 재발급
- `POST /auth/logout` : 로그아웃(Refresh 무효화)
- `GET /auth/me` : 내 인증 주체 조회

---

## 3. inbodyInput
- `POST /inbody` : 인바디 수동 입력 저장
- `POST /inbody/image` : 인바디 이미지 업로드/OCR 추출
- `POST /inbody/device` : 앱/기기 연동 입력 저장
- `GET /inbody/latest` : 최신 인바디 조회
- `GET /inbody` : 인바디 이력 조회(페이징)
- `GET /inbody/{inbodyId}` : 인바디 상세 조회

---

## 4. inbodyAnalysis
- `POST /analysis/body` : 체형분석 생성 요청
- `GET /analysis/body/latest` : 최신 체형분석 조회
- `GET /analysis/body` : 체형분석 이력 조회
- `GET /analysis/body/{analysisId}` : 체형분석 상세 조회

---

## 5. survey
- `POST /surveys` : 설문 작성
- `GET /surveys/latest` : 최신 설문 조회
- `GET /surveys` : 설문 이력 조회
- `GET /surveys/{surveyId}` : 설문 상세 조회

---

## 6. recommendation-workout
- `POST /recommendations/workout` : 운동 추천 생성 요청
- `GET /recommendations/workout/latest` : 최신 운동 추천 조회
- `GET /recommendations/workout` : 운동 추천 이력 조회
- `GET /recommendations/workout/{recommendationId}` : 운동 추천 상세 조회

---

## 7. recommendation-diet
- `POST /recommendations/diet` : 식단 추천 생성 요청
- `GET /recommendations/diet/latest` : 최신 식단 추천 조회
- `GET /recommendations/diet` : 식단 추천 이력 조회
- `GET /recommendations/diet/{recommendationId}` : 식단 추천 상세 조회

---

## 8. product
- `GET /products` : 상품 목록 조회
- `GET /products/{productId}` : 상품 상세 조회
- `GET /products/categories` : 상품 카테고리 조회
- `POST /admin/products` : 상품 등록(관리자)
- `PATCH /admin/products/{productId}` : 상품 수정(관리자)
- `PATCH /admin/products/{productId}/status` : 판매 상태 변경(관리자)

---

## 9. cart (장바구니)
- `GET /cart` : 장바구니 조회
- `POST /cart/items` : 장바구니 항목 추가
- `PATCH /cart/items/{cartItemId}` : 장바구니 항목 수정
- `DELETE /cart/items/{cartItemId}` : 장바구니 항목 삭제
- `DELETE /cart` : 장바구니 전체 비우기

---

## 10. purchase (구매기능)
- `POST /purchases` : 주문 생성
- `POST /purchases/{purchaseId}/pay` : 결제 요청
- `POST /purchases/{purchaseId}/cancel` : 주문 취소
- `POST /purchases/{purchaseId}/refund` : 환불 요청
- `GET /purchases/{purchaseId}` : 주문 상세 조회

---

## 11. purchase_history (구매목록 조회)
- `GET /purchase-history` : 구매목록 조회
- `GET /purchase-history/{purchaseId}` : 구매 상세 조회
- `GET /purchase-history/summary` : 구매 요약 조회

---

## 12. userInfo (mypage/히스토리 조회)
- `GET /users/me/profile` : 내 프로필 조회
- `PATCH /users/me/profile` : 내 프로필 수정
- `GET /users/me/dashboard` : 마이페이지 대시보드 조회
- `GET /users/me/history` : 통합 히스토리 조회
- `GET /users/me/trends` : 변화 추이 조회

---

## 13. common
- `GET /health` : 헬스체크
- `GET /ready` : 레디니스 체크

---

## 14. ai-client (관리자/운영)
- `GET /admin/ai/prompts` : 프롬프트 버전 목록 조회
- `POST /admin/ai/prompts` : 프롬프트 버전 등록
- `GET /admin/ai/executions` : AI 실행 로그 조회

---

## 15. 참고
- 본 문서는 엔드포인트 레벨 초안이다.
- 요청/응답 상세 스키마, 에러코드, 권한 매트릭스는 모듈별 상세 계약 문서에서 확정한다.
