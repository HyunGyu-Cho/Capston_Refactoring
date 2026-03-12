# WORKOUT ERD

AI 운동 추천 데이터 모델입니다.

## 주요 테이블
- `workout_recommendation`: 추천 헤더
- `workout_recommendation_detail`: 운동 상세 항목
- `workout_recommendation_execution_log`: API 실행 로그

## 설계 의도
- 운동 처방 데이터를 구조적으로 저장
- 운영 로그를 분리해 장애/비용 분석 가능하도록 구성
