# AUTH GitHub Issues

이 문서는 auth 작업을 GitHub 이슈로 쉽게 나누기 위한 템플릿입니다.

## 권장 라벨
- `area:auth`
- `type:feature`
- `type:security`
- `type:test`
- `type:docs`
- `priority:p0|p1|p2`

## 우선순위 순서
### P0
1. 공통 에러 포맷/코드 정리
2. SecurityConfig, JWT 필터 정리
3. Signup/Login 안정화

### P1
4. Refresh 세션 저장소(Redis)
5. Rotation/Revoke/Reuse Detection
6. Logout 무효화 정책

### P2
7. Me API 고도화
8. 관측성/감사 로그
9. 테스트/문서 자동 동기화

## 이슈 템플릿
```md
## 배경
왜 이 작업이 필요한지 한 문장으로 설명

## 작업
- [ ] 구현 항목 1
- [ ] 구현 항목 2

## 완료 조건(AC)
- [ ] 사용자 관점 결과
- [ ] 테스트 통과 조건
```
