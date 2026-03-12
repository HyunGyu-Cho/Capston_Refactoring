# MODULE SPECIFIC

이 문서는 모듈 경계와 의존 방향을 쉽게 이해하기 위한 안내서입니다.

## 현재 채택 모듈
- `auth`, `member`, `inbody`, `survey`, `ai`, `plan`
- `product`, `order`, `payment`, `community`, `evaluation`, `notification`, `admin`, `userinfo`, `common`

## 모듈 내부 표준 구조
```text
com.example.smart_healthcare.<module>
├─ api
├─ application
├─ domain
└─ infrastructure
```

## 반드시 지킬 규칙
- 모듈 밖 테이블 직접 접근 금지
- 모듈 간 Repository 직접 호출 금지
- 순환 의존 금지

## 의존 방향(요약)
- `auth -> member, common`
- `ai -> inbody, survey, common`
- `plan -> ai, member, common`
- `order -> product, member, common`
- `payment -> order, common`
- `community -> member, auth, common`
- `userinfo -> 여러 모듈(read only)`

## Auth API 목록
- `POST /auth/signup`
- `POST /auth/login`
- `POST /auth/refresh`
- `POST /auth/logout`
- `GET /auth/me`
