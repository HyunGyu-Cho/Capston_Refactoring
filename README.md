# Smart Healthcare Refactoring

AI 기반 개인 맞춤 헬스케어 플랫폼의 리팩토링 프로젝트입니다.
이 문서는 현재 설계안을 업무(도메인) 기준으로 재정렬하고, 실제 개발에 바로 적용 가능한 기준을 제공합니다.

## 1. 설계안 평가

### 잘 설계된 점
- 화면이 아닌 업무 기준으로 기능을 분리해 책임 경계가 명확합니다.
- `inbody`, `survey`, `ai`, `plan`의 핵심 추천 흐름이 분리되어 유지보수성이 높습니다.
- 추천/상품/주문/결제를 분리해 커머스 확장 시 결합도를 낮췄습니다.
- 커뮤니티(`community`)와 품질 피드백(`feedback`) 분리가 데이터 활용 관점에서 적절합니다.
- 실패 지점과 운영 관측(메트릭/로그/알림)을 초기에 정의한 점이 실무적으로 강합니다.

### 보완이 필요한 점
- 성능 목표가 정성적입니다. SLA/SLO 수치가 필요합니다.
- 모듈 간 통신 규칙은 좋지만, 트랜잭션 경계와 이벤트 재처리 정책이 추가로 필요합니다.
- AI 응답 파싱 실패에 대한 폴백(재시도, 부분 저장, 사용자 안내) 정책을 명확히 해야 합니다.
- 민감정보 보호를 위해 저장/전송/접근 제어 기준(암호화, 마스킹, 감사로그)을 구체화해야 합니다.
- 관리자 기능 범위가 넓어 MVP/Phase 2를 나누는 우선순위가 필요합니다.

## 2. 서비스 목표

- 목표: 인바디 + 설문 기반 AI 분석으로 주간 운동/식단 계획을 제공하고, 관련 상품 구매와 피드백 수집까지 연결
- 아키텍처: `Spring Boot + Modular Monolith`
- 원칙: 기능 중복 최소화, 도메인 책임 명확화, 이벤트 기반 부가처리

## 3. 사용자와 시스템 역할

### 일반 사용자
- 회원가입/로그인
- 프로필/목표 관리
- 인바디 입력, 설문 응답
- AI 분석/추천 결과 조회
- 상품 조회/구매/결제
- 커뮤니티 작성/참여
- 추천 피드백 작성

### 관리자
- 사용자/상품/신고/피드백 관리
- 추천 실패/오류 모니터링
- 결제/주문 현황 관리
- 공지/운영 통계 관리

### 시스템 내부 역할
- AI 추천 처리
- 추천 결과 구조화/저장
- 추천-상품 매핑
- 결제 처리 연동
- 알림 발송

## 4. 핵심 유스케이스

### 4.1 추천 생성
1. 사용자 로그인
2. 인바디 입력
3. 설문 입력
4. AI 체형 분석
5. 주간 운동/식단 계획 생성
6. 결과 저장 및 조회 제공
7. 관련 상품 추천 노출

### 4.2 상품 구매
1. 추천 상품 클릭
2. 상품 상세 조회
3. 장바구니 또는 바로구매
4. 결제 요청
5. 주문/결제 결과 저장

### 4.3 커뮤니티
1. 게시글 작성
2. 댓글/반응
3. 신고 접수
4. 관리자 조치

### 4.4 관리자 운영
1. 관리자 로그인
2. 사용자/상품/신고 관리
3. 추천/결제 통계 확인

## 5. 도메인 구조 (업무 기준)

- `auth`: 회원가입/로그인/JWT/권한
- `member`: 회원 기본정보(User 확장 엔티티 포함), 프로필, 목표, 선호 기본정보
- `inbody`: 인바디 저장/최신값 판별/이력
- `survey`: 운동/식단/목표 설문
- `ai`: 프롬프트 구성, 외부 AI API 호출, 응답 파싱/정규화
- `plan`: 요일별 운동/식단 계획 저장, 버전관리, 채택 플랜
- `product`: 상품/카테고리/추천 매핑
- `order`: 장바구니/주문/주문상태
- `payment`: 결제 요청/성공/실패/이력
- `community`: 게시글/댓글/좋아요/신고
- `feedback`: 추천 만족도/후기/개선 데이터
- `admin`: 운영 관리 및 통계
- `notification`: 추천 완료/주문/공지 알림
- `common`: 공통 예외/응답/보안/유틸


## 5.1 모듈 명명 결정

- `recommendation` 모듈은 `ai`로 변경
: 외부 AI API 호출과 파싱/후처리 작업이 중심 책임이므로 범용 추천명보다 `ai`가 책임을 더 직접적으로 표현합니다.
- `body` 모듈은 `inbody`로 변경
: 도메인 용어를 구체화해 데이터 출처/범위를 명확히 합니다.
- `user` 모듈은 `member`로 변경
: `Member` 엔티티를 중심으로 사용자 확장 모델을 관리하고, 네이밍을 코드와 일치시킵니다.

## 5.2 인증 모델 분리 전략

- `Member`는 JPA 엔티티로 유지 (도메인/DB 모델)
- Spring Security 연동은 `MemberUserDetails implements UserDetails`로 분리 (인증 모델)
- 원칙
: 프레임워크 제공 `UserDetails` 모델을 도메인 엔티티의 상속 부모로 사용하지 않습니다.
- 이유
: 도메인 모델과 인증 프레임워크 모델의 결합을 줄여 유지보수성과 테스트 용이성을 확보합니다.

## 6. 모듈 책임 규칙

- 다른 모듈 DB 테이블 직접 접근 금지
- 다른 모듈 `Repository` 직접 호출 금지
- 공개된 `Application Service`로만 연동
- 부가 기능은 도메인 이벤트로 비동기 처리

예시:
- `inbody`: 인바디 저장/조회만 담당, AI 생성 금지
- `ai`: AI 호출/파싱만 담당, 결제 처리 금지

## 7. 추천 핵심 데이터 흐름

1. 인바디/설문 입력
2. 데이터 검증(누락/비정상 수치)
3. 추천 모듈이 최신 데이터 조합
4. AI 호출 및 응답 수신
5. 구조화 파싱(JSON/DTO)
6. `plan`에 요일별 계획 저장
7. 상품 매핑 및 알림 이벤트 발행
8. 사용자 조회

## 8. 데이터 소유권

- `users`, `member_profiles` -> `member`
- `inbody_records` -> `inbody`
- `survey_responses` -> `survey`
- `recommendations` -> `ai`
- `weekly_plans`, `plan_workouts`, `plan_meals` -> `plan`
- `products` -> `product`
- `orders` -> `order`
- `payments` -> `payment`
- `posts`, `comments` -> `community`
- `recommendation_feedbacks` -> `feedback`

## 9. 비기능 요구사항

### 성능(권장 목표)
- 추천 API p95 응답시간: 5초 이내
- 추천 API p99 응답시간: 8초 이내
- 동시 사용자: 1차 500 CCU 기준 무중단

### 보안
- JWT 기반 인증/인가(RBAC)
- 민감 건강데이터 암호화(저장 시) + 마스킹(조회/로그)
- 개인정보 최소수집 및 접근 감사로그

### 운영
- AI 실패 재시도(지수 백오프) + 실패 사유 코드화
- Dead Letter Queue 또는 실패 테이블로 재처리
- 관리자 페이지에서 실패건/신고/피드백 운영

### 확장성
- 운동 영상 추천
- 웨어러블 연동
- 챗봇 상담

## 10. 에러 코드 표준

- `BODY_DATA_INVALID`
- `SURVEY_NOT_COMPLETED`
- `RECOMMENDATION_GENERATION_FAILED`
- `RECOMMENDATION_PARSE_FAILED`
- `PRODUCT_NOT_FOUND`
- `PAYMENT_FAILED`

## 11. 관측/모니터링

- Spring Actuator
- Metrics(Prometheus)
- Dashboard(Grafana)
- Structured Logging(JSON)
- Error Alert(Slack/Email)
- Slow Query 감시

핵심 메트릭:
- 추천 성공률/실패율/평균 생성시간
- AI API 응답시간
- 상품 클릭률/결제 성공률
- 커뮤니티 신고율

## 12. 개발 로드맵

### Phase 1. 설계 확정
1. 요구사항 명세
2. 도메인 경계 확정
3. DB/API 초안

### Phase 2. 프로젝트 골격
1. 모듈 패키지 생성
2. 공통 예외/응답/보안
3. `auth`, `member` 기본 기능

### Phase 3. MVP
1. 회원가입/로그인
2. 인바디 입력
3. 설문 입력
4. AI 연동
5. 추천 저장/조회

### Phase 4. 비즈니스 확장
1. 상품 추천 매핑
2. 주문/결제
3. 커뮤니티/피드백

### Phase 5. 운영 고도화
1. 관리자 기능 확장
2. 모니터링/알림
3. 배포 파이프라인

## 13. 현재 코드베이스와 목표 구조

현재 패키지는 `com.example.smart_healthcare` 단일 구조를 사용합니다.
목표는 점진적으로 `도메인별 api/application/domain/infrastructure` 구조로 정리하는 것입니다.

예시:

```text
com.healthcare.ai
├─ api
├─ application
├─ domain
└─ infrastructure
```

## 14. 실행 가이드 (현 저장소 기준)

### Backend
```bash
cd backend/smart-healthcare
./gradlew bootRun
```

### Frontend
```bash
cd frontend/Smart-Healthcare-main
npm install
npm start
```

### Docker
```bash
docker-compose up -d
```

## 15. 다음 작업 우선순위 (권장)

1. 추천 도메인(`inbody/survey/ai/plan`) 경계부터 코드 레벨로 분리
2. 추천 응답 구조화 스키마(JSON Schema or DTO) 확정
3. 에러 코드/예외 처리 공통화
4. 추천 실패 재처리 배치 또는 큐 도입
5. 관리자 실패 모니터링 화면 최소 기능 구현






## 수정사항


1. 모듈명 변경
- `recommendation` -> `ai`
- `body` -> `inbody`
- `user` -> `member`

2. member 인증 모델 구조 변경
- `Member extends User` 제거
- `Member`를 JPA 엔티티로 유지
- `MemberUserDetails implements UserDetails` 도입
- `MemberUserDetailsService` 도입

3. 저장소 계층 명칭 정리
- `UserRepository` 계열 제거
- `MemberRepository`, `MemberJpaRepository`, `MemberRepositoryImpl`로 통일

4. 의존성 추가
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-security`
