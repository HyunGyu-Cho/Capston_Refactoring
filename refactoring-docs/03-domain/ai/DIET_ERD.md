# DIET ERD

AI 식단 추천 데이터 모델입니다.

## 주요 테이블
- `diet_recommendation`: 추천 헤더
- `diet_recommendation_detail`: 요일/끼니별 상세 항목
- `diet_recommendation_execution_log`: API 실행 로그

## 설계 의도
- 헤더/상세 분리로 확장성 확보
- 운영 로그를 기능 데이터와 분리
