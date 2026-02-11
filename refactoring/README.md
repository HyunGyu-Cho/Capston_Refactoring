# 🏥 Smart Healthcare - AI 기반 개인 맞춤형 헬스케어 웹 서비스

<div align="center">

![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen?style=for-the-badge&logo=spring)
![React](https://img.shields.io/badge/React-19.1.0-blue?style=for-the-badge&logo=react)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=for-the-badge&logo=mysql)
![OpenAI](https://img.shields.io/badge/OpenAI-GPT--4o-412991?style=for-the-badge&logo=openai)

**AI 기반 체형 분석 및 맞춤형 운동/식단 추천 서비스**

[기능 소개](#-주요-기능) • [기술 스택](#-기술-스택) • [설치 방법](#-설치-및-실행) • [프로젝트 구조](#-프로젝트-구조)

</div>

---

## 📋 목차

- [프로젝트 소개](#-프로젝트-소개)
- [주요 기능](#-주요-기능)
- [기술 스택](#-기술-스택)
- [시스템 아키텍처](#-시스템-아키텍처)
- [설치 및 실행](#-설치-및-실행)
- [프로젝트 구조](#-프로젝트-구조)
- [API 문서](#-api-문서)
- [환경 변수 설정](#-환경-변수-설정)
- [사용 방법](#-사용-방법)
- [개발 가이드](#-개발-가이드)
- [기여하기](#-기여하기)
- [라이선스](#-라이선스)

---

## 🎯 프로젝트 소개

**Smart Healthcare**는 사용자의 인바디 데이터를 기반으로 AI(GPT-4o)를 활용하여 체형을 분석하고, 개인 맞춤형 운동 및 식단을 추천하는 종합 헬스케어 웹 서비스입니다.

### 핵심 가치

- 🤖 **AI 기반 분석**: OpenAI GPT-4o를 활용한 정확한 체형 분석
- 🎯 **맞춤형 추천**: 사용자별 인바디 데이터와 설문조사를 기반으로 한 개인화된 추천
- 📊 **데이터 시각화**: 인바디 기록 히스토리 및 통계 제공
- 👥 **커뮤니티**: 사용자 간 정보 공유 및 소통 플랫폼

---

## ✨ 주요 기능

### 1. 인증 및 사용자 관리
- ✅ 이메일/비밀번호 회원가입 및 로그인
- ✅ JWT 기반 토큰 인증 시스템
- ✅ 사용자 프로필 관리

### 2. 인바디 데이터 관리
- 📝 인바디 측정 데이터 입력 및 저장
- 📈 인바디 기록 히스토리 조회
- 📊 인바디 데이터 기반 통계 및 그래프 제공
- 📅 캘린더 기반 일정 관리

### 3. AI 기반 체형 분석
- 🤖 OpenAI GPT-4o 모델을 활용한 체형 분석
- 🏋️ 체형 분류 (운동선수급, 근육형, 적정, 날씬형, 과체중, 비만 등)
- 💡 체형별 맞춤 건강 조언 제공
- 📋 체형 분석 결과 히스토리 관리

### 4. AI 기반 운동 추천
- 🏃 사용자 체형, 목표, 설문조사 기반 맞춤 운동 프로그램 생성
- 📅 요일별 운동 스케줄 제공
- 📝 운동별 상세 설명, 자세, 효과, 주의사항 제공
- 🎥 YouTube 영상 링크 연동
- 📚 운동 프로그램 히스토리 관리

### 5. AI 기반 식단 추천
- 🍽️ 사용자 체형, 목표, 설문조사 기반 맞춤 식단 생성
- 📅 요일별(월~금) 끼니별 식단 제공
- 🥗 식단별 영양소 정보, 조리법, 재료, 추천 이유 제공
- 📝 상세한 조리 방법 제공 (5-7단계)
- 📚 식단 프로그램 히스토리 관리

### 6. 설문조사 관리
- 📋 운동 목표, 선호 요일, 끼니 수 등 사용자 선호도 수집
- 📊 설문조사 히스토리 조회 및 관리

### 7. 커뮤니티 기능
- ✍️ 게시글 작성, 수정, 삭제
- 💬 댓글 작성 및 관리 (대댓글 지원)
- 👍 게시글 좋아요/싫어요 반응 기능
- 🚨 게시글 신고 기능
- 🏷️ 카테고리별 게시글 조회 (운동, 식단, 질문, 자유게시판, 팁, 후기, 성공후기)

### 8. 평가 및 리뷰
- ⭐ 운동/식단 추천에 대한 평가 및 리뷰 작성
- 📊 별점 평가 시스템
- 📝 평가 히스토리 조회

### 9. 관리자 기능
- 👥 사용자 관리 (조회, 수정, 삭제)
- 📝 게시글 관리 (조회, 삭제, 신고 처리)
- 📊 시스템 통계 및 모니터링
- 📋 로그 조회 및 관리

---

## 🛠 기술 스택

### Backend
- **언어**: Java 17
- **프레임워크**: Spring Boot 3.4.5
- **데이터베이스**: MySQL 8.0
- **ORM**: Spring Data JPA / Hibernate
- **인증**: Spring Security + JWT
- **API 문서화**: SpringDoc OpenAPI (Swagger)
- **빌드 도구**: Gradle
- **로깅**: Logback + Logstash Encoder

### Frontend
- **언어**: JavaScript (ES6+)
- **프레임워크**: React 19.1.0
- **라우팅**: React Router DOM 7.6.0
- **스타일링**: Tailwind CSS 3.4.3
- **UI 컴포넌트**: Headless UI, Heroicons
- **애니메이션**: Framer Motion, AOS
- **차트**: Recharts
- **빌드 도구**: Create React App

### AI & External APIs
- **AI 모델**: OpenAI GPT-4o
- **YouTube API**: YouTube Data API v3

### DevOps
- **컨테이너화**: Docker, Docker Compose
- **웹 서버**: Nginx (프론트엔드)
- **환경 변수**: dotenv-java

---

## 🏗 시스템 아키텍처

```
┌─────────────────┐
│   Frontend      │  React + Tailwind CSS
│   (React App)   │
└────────┬────────┘
         │ HTTP/REST API
         │ JWT Authentication
┌────────▼────────┐
│   Backend       │  Spring Boot + Spring Security
│  (Spring Boot)  │
└────────┬────────┘
         │
    ┌────┴────┬──────────────┬──────────────┐
    │         │              │              │
┌───▼───┐ ┌──▼──┐    ┌──────▼──────┐ ┌─────▼─────┐
│ MySQL │ │OpenAI│    │  YouTube   │ │  OAuth    │
│Database│ │ GPT-4o│    │   API     │ │ Providers │
└───────┘ └──────┘    └────────────┘ └───────────┘
```

### 아키텍처 패턴
- **Backend**: Layered Architecture (Controller → Service → Repository)
- **Frontend**: Component-Based Architecture
- **AI 서비스**: Facade Pattern (WorkoutRecommendationFacade, DietRecommendationFacade)

---

## 🚀 설치 및 실행

### 사전 요구사항
- Java 17 이상
- Node.js 18 이상
- MySQL 8.0 이상
- Docker & Docker Compose (선택사항)
- OpenAI API Key
- YouTube Data API Key (선택사항)

### 1. 저장소 클론

```bash
git clone https://github.com/your-username/smart-healthcare.git
cd smart-healthcare
```

### 2. 환경 변수 설정

#### Backend 환경 변수
`backend/smart-healthcare/.env` 파일 생성:

```env
# 데이터베이스 설정
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/finaldbsmarthealthcare?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your_password

# JWT 설정
JWT_SECRET=your_jwt_secret_key_minimum_256_bits
JWT_EXPIRATION=86400000

# OpenAI API
OPENAI_API_KEY=your_openai_api_key

# YouTube API
YOUTUBE_API_KEY=your_youtube_api_key
```

#### Frontend 환경 변수
`frontend/Smart-Healthcare-main/.env` 파일 생성:

### 3. 데이터베이스 설정

```bash
# MySQL 데이터베이스 생성
mysql -u root -p < setup_mysql.sql
```

### 4. Docker Compose로 실행 (권장)

```bash
# 전체 시스템 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 시스템 중지
docker-compose down
```

### 5. 수동 실행

#### Backend 실행

```bash
cd backend/smart-healthcare

# Gradle로 실행
./gradlew bootRun

# 또는 빌드 후 실행
./gradlew build
java -jar build/libs/smart-healthcare-0.0.1-SNAPSHOT.jar
```

Backend는 `http://localhost:8080`에서 실행됩니다.

#### Frontend 실행

```bash
cd frontend/Smart-Healthcare-main

# 의존성 설치
npm install

# 개발 서버 실행
npm start
```

Frontend는 `http://localhost:3000`에서 실행됩니다.

### 6. API 문서 확인

Backend 실행 후 다음 URL에서 API 문서를 확인할 수 있습니다:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

---

## 📁 프로젝트 구조

```
smart-healthcare/
├── backend/
│   └── smart-healthcare/
│       ├── src/main/java/com/example/smart_healthcare/
│       │   ├── client/              # 외부 API 클라이언트
│       │   │   └── OpenAIClient.java
│       │   ├── common/              # 공통 유틸리티
│       │   │   ├── converter/
│       │   │   │   └── JsonAttributeConverter.java
│       │   │   ├── entity/
│       │   │   │   └── BaseEntity.java
│       │   │   ├── error/
│       │   │   │   ├── BusinessException.java
│       │   │   │   └── ErrorCode.java
│       │   │   └── dto/
│       │   │       ├── ApiResponseDto.java
│       │   │       └── PaginationDto.java
│       │   ├── config/              # 설정 클래스
│       │   │   ├── DataInitializer.java
│       │   │   ├── DotenvEnvironmentPostProcessor.java
│       │   │   ├── JacksonConfig.java
│       │   │   ├── JpaConfig.java
│       │   │   ├── JwtAuthenticationFilter.java
│       │   │   ├── SecurityConfig.java
│       │   │   ├── WebConfig.java
│       │   │   └── openapi/
│       │   │       └── OpenApiConfig.java
│       │   ├── controller/          # REST API 컨트롤러
│       │   │   ├── AdminController.java
│       │   │   ├── AuthController.java
│       │   │   ├── BodyAnalysisController.java
│       │   │   ├── CommunityController.java
│       │   │   ├── DietRecommendationController.java
│       │   │   ├── EvaluationController.java
│       │   │   ├── HealthSummaryController.java
│       │   │   ├── ImageController.java
│       │   │   ├── InbodyRecordController.java
│       │   │   ├── SurveyController.java
│       │   │   ├── UserHistoryController.java
│       │   │   └── WorkoutRecommendationController.java
│       │   ├── service/             # 비즈니스 로직
│       │   │   ├── ai/              # AI 서비스
│       │   │   │   ├── ChatGPTBodyAnalysisService.java
│       │   │   │   ├── DietRecommendAIService.java
│       │   │   │   └── WorkoutRecommendAIService.java
│       │   │   ├── facade/          # Facade 패턴
│       │   │   │   ├── BodyAnalysisFacade.java
│       │   │   │   ├── DietRecommendationFacade.java
│       │   │   │   └── WorkoutRecommendationFacade.java
│       │   │   ├── AdminService.java
│       │   │   ├── AuthService.java
│       │   │   ├── BodyAnalysisResultService.java
│       │   │   ├── CommentService.java
│       │   │   ├── CommunityService.java
│       │   │   ├── CustomUserDetailsService.java
│       │   │   ├── DietRecommendationService.java
│       │   │   ├── EvaluationService.java
│       │   │   ├── ImageService.java
│       │   │   ├── InbodyRecordService.java
│       │   │   ├── ReactionService.java
│       │   │   ├── SurveyService.java
│       │   │   ├── UserHistoryService.java
│       │   │   ├── UserService.java
│       │   │   ├── ValidationService.java
│       │   │   ├── WorkoutRecommendationService.java
│       │   │   └── YoutubeService.java
│       │   ├── repository/          # 데이터 접근 계층
│       │   │   ├── AIBodyAnalysisResultRepository.java
│       │   │   ├── AIDietRecommendationRepository.java
│       │   │   ├── AIWorkoutRecommendationRepository.java
│       │   │   ├── CommentRepository.java
│       │   │   ├── CommunityPostRepository.java
│       │   │   ├── EvaluationRepository.java
│       │   │   ├── FoodImageCacheRepository.java
│       │   │   ├── InbodyRecordRepository.java
│       │   │   ├── NotificationRepository.java
│       │   │   ├── PostReactionRepository.java
│       │   │   ├── PostReportRepository.java
│       │   │   ├── SurveyRepository.java
│       │   │   ├── UserHistoryRepository.java
│       │   │   └── UserRepository.java
│       │   ├── entity/              # 엔티티 클래스
│       │   │   ├── AIBodyAnalysisResult.java
│       │   │   ├── AIDietRecommendation.java
│       │   │   ├── AIWorkoutRecommendation.java
│       │   │   ├── Comment.java
│       │   │   ├── CommunityPost.java
│       │   │   ├── Evaluation.java
│       │   │   ├── FoodImageCache.java
│       │   │   ├── HealthReport.java
│       │   │   ├── InbodyRecord.java
│       │   │   ├── Notification.java
│       │   │   ├── PostReaction.java
│       │   │   ├── PostReport.java
│       │   │   ├── RecommendationRecord.java
│       │   │   ├── Survey.java
│       │   │   ├── User.java
│       │   │   └── UserHistory.java
│       │   ├── dto/                 # 데이터 전송 객체
│       │   │   ├── request/         # 요청 DTO
│       │   │   │   ├── AddReactionRequestDto.java
│       │   │   │   ├── DietRecommendationRequestDto.java
│       │   │   │   ├── EvaluationRequestDto.java
│       │   │   │   ├── InbodyDataRequestDto.java
│       │   │   │   ├── LoginRequestDto.java
│       │   │   │   ├── PostRequestDto.java
│       │   │   │   ├── SignupRequestDto.java
│       │   │   │   ├── SocialLoginRequestDto.java
│       │   │   │   ├── SurveyDataRequestDto.java
│       │   │   │   ├── SurveyRequestDto.java
│       │   │   │   ├── UpdatePostRequestDto.java
│       │   │   │   ├── UpdateUserRequestDto.java
│       │   │   │   └── WorkoutRecommendationRequestDto.java
│       │   │   └── response/        # 응답 DTO
│       │   │       ├── AuthResponseDto.java
│       │   │       ├── BodyAnalysisResponseDto.java
│       │   │       ├── CommentResponseDto.java
│       │   │       ├── DietRecommendationResponseDto.java
│       │   │       ├── EvaluationResponseDto.java
│       │   │       ├── InbodyRecordResponseDto.java
│       │   │       ├── PostResponseDto.java
│       │   │       ├── ReactionCheckResponseDto.java
│       │   │       ├── ReactionResponseDto.java
│       │   │       ├── SurveyResponseDto.java
│       │   │       ├── UserHistoryResponseDto.java
│       │   │       ├── UserResponseDto.java
│       │   │       └── WorkoutRecommendationResponseDto.java
│       │   ├── exception/           # 예외 처리
│       │   │   ├── BaseException.java
│       │   │   ├── BusinessLogicException.java
│       │   │   ├── GlobalExceptionHandler.java
│       │   │   ├── OpenAIException.java
│       │   │   ├── ResourceNotFoundException.java
│       │   │   ├── UnauthorizedException.java
│       │   │   └── ValidationException.java
│       │   ├── interceptor/         # 인터셉터
│       │   │   ├── AdminAuthInterceptor.java
│       │   │   └── LoggingInterceptor.java
│       │   ├── event/               # 이벤트 처리
│       │   │   ├── ReactionAddedEvent.java
│       │   │   ├── ReactionChangedEvent.java
│       │   │   ├── ReactionEvent.java
│       │   │   ├── ReactionRemovedEvent.java
│       │   │   └── ReviewCreatedEvent.java
│       │   └── util/                # 유틸리티
│       │       └── JwtUtil.java
│       ├── src/main/resources/
│       │   ├── application.properties
│       │   ├── application-prod.properties
│       │   └── logback-spring.xml
│       ├── build.gradle
│       ├── settings.gradle
│       ├── gradle.properties
│       └── Dockerfile
│
├── frontend/
│   └── Smart-Healthcare-main/
│       ├── src/
│       │   ├── api/                # API 모듈
│       │   │   ├── auth.js
│       │   │   ├── bodyAnalysis.js
│       │   │   ├── config.js
│       │   │   ├── dietRecommendation.js
│       │   │   ├── inbody.js
│       │   │   ├── survey.js
│       │   │   ├── unsplash.js
│       │   │   └── workoutRecommendation.js
│       │   ├── components/          # 재사용 컴포넌트
│       │   │   ├── AdminHeader.jsx
│       │   │   ├── AdminLayout.jsx
│       │   │   ├── BackButton.jsx
│       │   │   ├── Button.jsx
│       │   │   ├── Card.jsx
│       │   │   ├── DietIllustration.jsx
│       │   │   ├── EvaluationForm.jsx
│       │   │   ├── EvaluationList.jsx
│       │   │   ├── EvaluationWithReviewForm.jsx
│       │   │   ├── Footer.jsx
│       │   │   ├── Header.jsx
│       │   │   ├── Hero.jsx
│       │   │   ├── HeroWithBg.jsx
│       │   │   ├── InputField.jsx
│       │   │   ├── Layout.jsx
│       │   │   ├── PasswordScreen.jsx
│       │   │   ├── ProtectedRoute.jsx
│       │   │   ├── SectionWithWave.jsx
│       │   │   ├── StarRating.jsx
│       │   │   └── WorkoutIllustration.jsx
│       │   ├── pages/               # 페이지 컴포넌트
│       │   │   ├── AboutPage.jsx
│       │   │   ├── AdminCommunityPage.jsx
│       │   │   ├── AdminContentPage.jsx
│       │   │   ├── AdminDashboardPage.jsx
│       │   │   ├── AdminLoginPage.jsx
│       │   │   ├── AdminLogsPage.jsx
│       │   │   ├── AdminStatsPage.jsx
│       │   │   ├── AdminSystemPage.jsx
│       │   │   ├── AdminUsersPage.jsx
│       │   │   ├── BodyAnalysisPage.jsx
│       │   │   ├── CalendarPage.jsx
│       │   │   ├── CommunityPage.jsx
│       │   │   ├── CommunityPostDetailPage.jsx
│       │   │   ├── CommunityPostEditPage.jsx
│       │   │   ├── DietDetailPage.jsx
│       │   │   ├── EvaluationPage.jsx
│       │   │   ├── HealthHistoryPage.jsx
│       │   │   ├── InbodyHistoryPage.jsx
│       │   │   ├── InbodyInputPage.jsx
│       │   │   ├── LoginPage.jsx
│       │   │   ├── MainPage.jsx
│       │   │   ├── MyPage.jsx
│       │   │   ├── RecommendationsPage.jsx
│       │   │   ├── RecommendedDietListPage.jsx
│       │   │   ├── RecommendedWorkoutListPage.jsx
│       │   │   ├── SignupPage.jsx
│       │   │   ├── SurveyDetailPage.jsx
│       │   │   ├── SurveyHistoryPage.jsx
│       │   │   ├── SurveyPage.jsx
│       │   │   └── WorkoutDetailPage.jsx
│       │   ├── utils/               # 유틸리티 함수
│       │   │   ├── authManager.js
│       │   │   ├── bodyTypeUtils.js
│       │   │   ├── categoryUtils.js
│       │   │   ├── dataMapper.js
│       │   │   ├── dietUtils.js
│       │   │   ├── exerciseUtils.js
│       │   │   ├── imageUtils.js
│       │   │   └── storageManager.js
│       │   ├── data/                # 데이터 파일
│       │   │   └── inbody.js
│       │   ├── App.jsx              # 라우팅 설정
│       │   ├── App.css
│       │   ├── App.test.js
│       │   ├── index.js             # 엔트리 포인트
│       │   ├── index.css
│       │   ├── logo.svg
│       │   ├── reportWebVitals.js
│       │   └── setupTests.js
│       ├── public/                  # 정적 자산
│       │   ├── assets/
│       │   ├── favicon.ico
│       │   ├── index.html
│       │   ├── logo192.png
│       │   ├── logo512.png
│       │   ├── manifest.json
│       │   └── robots.txt
│       ├── package.json
│       ├── package-lock.json
│       ├── tailwind.config.js
│       ├── postcss.config.js
│       ├── nginx.conf
│       └── Dockerfile
│
├── docker-compose.yml              # Docker Compose 설정
├── setup_mysql.sql                 # 데이터베이스 초기화 스크립트
├── env.example.txt                 # 환경 변수 예시
├── DOCKER_GUIDE.md                 # Docker 가이드
└── README.md                       # 프로젝트 문서
```

### 주요 디렉토리 설명

#### Backend
- **controller/**: REST API 엔드포인트 정의
- **service/**: 비즈니스 로직 구현
  - **ai/**: AI 추천 서비스 (체형 분석, 운동 추천, 식단 추천)
  - **facade/**: 복잡한 서비스 조율
- **repository/**: 데이터베이스 접근 계층
- **entity/**: JPA 엔티티 클래스
- **dto/**: 요청/응답 데이터 전송 객체

#### Frontend
- **api/**: 백엔드 API 호출 모듈
- **components/**: 재사용 가능한 UI 컴포넌트
- **pages/**: 페이지 컴포넌트
- **utils/**: 유틸리티 함수

---

## 📚 API 문서

### 주요 API 엔드포인트

#### 인증
- `POST /api/auth/signup` - 회원가입
- `POST /api/auth/login` - 로그인
- `GET /api/auth/me` - 현재 사용자 정보

#### 인바디 관리
- `POST /api/inbody` - 인바디 데이터 입력
- `GET /api/inbody` - 인바디 기록 조회
- `GET /api/inbody/history` - 인바디 히스토리

#### 체형 분석
- `POST /api/body-analysis` - AI 체형 분석 요청
- `GET /api/body-analysis/{id}` - 체형 분석 결과 조회

#### 운동 추천
- `POST /api/workout-recommendation` - 운동 추천 요청
- `GET /api/workout-recommendation/{id}` - 운동 추천 결과 조회

#### 식단 추천
- `POST /api/diet-recommendation` - 식단 추천 요청
- `GET /api/diet-recommendation/{id}` - 식단 추천 결과 조회

#### 커뮤니티
- `GET /api/community` - 게시글 목록 조회
- `POST /api/community` - 게시글 작성
- `GET /api/community/{id}` - 게시글 상세 조회
- `PUT /api/community/{id}` - 게시글 수정
- `DELETE /api/community/{id}` - 게시글 삭제
- `POST /api/community/{id}/comments` - 댓글 작성
- `POST /api/community/{id}/reactions` - 반응 추가

자세한 API 문서는 Swagger UI에서 확인할 수 있습니다.

---

## 🔧 환경 변수 설정

### 필수 환경 변수

| 변수명 | 설명 | 예시 |
|--------|------|------|
| `SPRING_DATASOURCE_URL` | MySQL 데이터베이스 URL | `jdbc:mysql://localhost:3306/finaldbsmarthealthcare` |
| `SPRING_DATASOURCE_USERNAME` | 데이터베이스 사용자명 | `root` |
| `SPRING_DATASOURCE_PASSWORD` | 데이터베이스 비밀번호 | `your_password` |
| `JWT_SECRET` | JWT 토큰 서명 키 (최소 256비트) | `your_jwt_secret_key` |
| `OPENAI_API_KEY` | OpenAI API 키 | `sk-...` |

### 선택 환경 변수

| 변수명 | 설명 |
|--------|------|
| `YOUTUBE_API_KEY` | YouTube Data API 키 (운동 영상 검색용) |
| `GOOGLE_CLIENT_ID` | Google OAuth 클라이언트 ID |
| `KAKAO_CLIENT_ID` | Kakao OAuth 클라이언트 ID |
| `NAVER_CLIENT_ID` | Naver OAuth 클라이언트 ID |

---

## 💻 사용 방법

### 1. 회원가입 및 로그인
1. 메인 페이지에서 "회원가입" 클릭
2. 이메일/비밀번호 입력 
3. 회원가입 완료 후 로그인

### 2. 인바디 데이터 입력
1. "인바디 입력" 메뉴 선택
2. 인바디 측정 데이터 입력 (체중, 체지방률, 근육량 등)
3. 데이터 저장

### 3. 체형 분석
1. "체형 분석" 메뉴 선택
2. 인바디 데이터 기반 AI 체형 분석 실행
3. 분석 결과 확인 (체형 분류, 건강 조언 등)

### 4. 운동/식단 추천
1. "설문조사" 메뉴에서 운동 목표, 선호 요일 등 입력
2. "추천 받기" 버튼 클릭
3. AI 기반 맞춤형 운동 및 식단 추천 확인
4. 각 운동/식단의 상세 정보 및 YouTube 영상 확인

### 5. 커뮤니티 이용
1. "커뮤니티" 메뉴 선택
2. 게시글 작성, 댓글 작성, 좋아요/싫어요 반응
3. 카테고리별 게시글 조회

---

## 👨‍💻 개발 가이드

### 코드 스타일
- **Backend**: Java 코딩 컨벤션 준수, Lombok 사용
- **Frontend**: ESLint 규칙 준수, 함수형 컴포넌트 사용


### 테스트
```bash
# Backend 테스트
cd backend/smart-healthcare
./gradlew test

# Frontend 테스트
cd frontend/Smart-Healthcare-main
npm test
```

---


## 🙏 감사의 말

이 프로젝트는 다음 오픈소스 프로젝트들을 사용합니다:
- [Spring Boot](https://spring.io/projects/spring-boot)
- [React](https://reactjs.org/)
- [OpenAI](https://openai.com/)
- [Tailwind CSS](https://tailwindcss.com/)

---

<div align="center">

**⭐ 이 프로젝트가 도움이 되었다면 Star를 눌러주세요! ⭐**

Made with ❤️ by Smart Healthcare Team

</div>
