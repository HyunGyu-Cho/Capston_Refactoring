# Smart Healthcare Refactoring

이 저장소는 AI 기반 헬스케어 서비스를 안정적으로 운영하기 위해 코드를 모듈 구조로 재정리하는 프로젝트입니다.

## 이 프로젝트의 한 줄 목표
복잡한 기능을 "작은 모듈"로 나누고, 모듈 사이 규칙을 지켜서 유지보수하기 쉬운 백엔드를 만든다.

## 핵심 원칙
- 각 모듈은 자기 데이터만 책임진다.
- 다른 모듈의 DB/Repository를 직접 호출하지 않는다.
- 모듈 간 협력은 Application Service(공개 API)로만 한다.
- 인증 도메인 모델(`Member`)과 Spring Security 모델(`UserDetails`)을 분리한다.

## 현재 모듈
- 인증/회원: `auth`, `member`
- 추천 흐름: `inbody`, `survey`, `ai`, `plan`
- 커머스/커뮤니티: `product`, `order`, `payment`, `community`, `evaluation`
- 운영/기타: `notification`, `admin`, `userinfo`, `common`

## 모듈 내부 표준 구조
```text
com.example.smart_healthcare.<module>
├─ api
├─ application
├─ domain
└─ infrastructure
```

## 인증(Auth) 현재 정책
- Access Token: `Authorization: Bearer ...`
- Refresh Token: HttpOnly Cookie
- Cookie: `Secure`, `SameSite=Strict`, `Path=/api/v1/auth`
- Refresh: 회전(rotation) + 재사용 감지(reuse detection)

## 실행 방법
### Backend
```bash
cd backend/smart-healthcare
gradle bootRun
```

### Frontend
```bash
cd frontend/Smart-Healthcare-main
npm install
npm start
```

## 문서 위치
- 문서 인덱스: `refactoring-docs/00-index/README.md`
- 요구사항: `refactoring-docs/01-product/requirements/`
- 구조/규칙: `refactoring-docs/02-architecture/`
- 모듈별 상세: `refactoring-docs/03-domain/`
- DB 전략: `refactoring-docs/04-data/`
- 운영 문서: `refactoring-docs/05-operations/`
