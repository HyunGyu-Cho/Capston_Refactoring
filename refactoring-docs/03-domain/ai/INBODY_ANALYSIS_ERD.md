# INBODY ANALYSIS ERD

인바디 기반 AI 분석 데이터 저장 구조입니다.

## 주요 테이블
- `inbody_analysis`: 사용자 기능용 결과 본문
- `inbody_analysis_execution_log`: 운영/관측 로그

## 설계 의도
- 사용자에게 보여줄 데이터와 운영 메트릭을 분리
- 재현성을 위해 입력 스냅샷/원문 응답 저장
- 상태는 `PENDING/SUCCESS/FAILED`
