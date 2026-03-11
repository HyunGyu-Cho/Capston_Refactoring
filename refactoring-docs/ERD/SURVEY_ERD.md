# SURVEY ERD

## 요구사항 매핑
- `[SURVEY-001]` 체형분석 기반 설문: 설문 응답 저장 및 이력 관리
- `[SURVEY-001]` 추천 요청 입력값: 운동/식단 추천 생성의 입력 컨텍스트 제공

## 설계 원칙
- 설문은 이력 데이터로 관리하며 `updated_at`은 두지 않는다.
- 다중 선택 응답(가능 일자/끼니)은 JSON 배열로 저장한다.
- 분석/추천 활용이 필요한 항목은 ENUM/코드값으로 표준화한다.

## survey ([SURVEY-001])
- `survey_id` BIGINT PRIMARY KEY AUTO_INCREMENT
- `member_id` BIGINT NOT NULL
- `created_at` DATETIME NOT NULL
- `workout_available_days_json` JSON NOT NULL
- `diet_available_days_json` JSON NOT NULL
- `diet_available_meals_json` JSON NOT NULL
- `activity_level_code` VARCHAR(20) NOT NULL
- `workout_split_preference_code` VARCHAR(20) NOT NULL
- `body_goal_code` VARCHAR(20) NOT NULL
- `diet_preference_code` VARCHAR(20) NOT NULL
- `additional_notes` TEXT NULL

제약/인덱스:
- FK: `member_id` -> `member.id`
- CHECK: `activity_level_code IN ('LOW','MODERATE','HIGH','VERY_HIGH')`
- CHECK: `workout_split_preference_code IN ('STRENGTH_ONLY','STRENGTH_PLUS_CARDIO','CARDIO_ONLY')`
- CHECK: `body_goal_code IN ('FAT_LOSS','MUSCLE_GAIN','RECOMPOSITION','MAINTENANCE','POSTURE_CORRECTION')`
- CHECK: `diet_preference_code IN ('BALANCED','HIGH_PROTEIN','LOW_CARB','VEGETARIAN','FLEXITARIAN','KETO')`
- INDEX: `idx_survey_member_created_at (member_id, created_at)`

간략 DDL:
```sql
CREATE TABLE survey (
  survey_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  member_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL,

  workout_available_days_json JSON NOT NULL,
  diet_available_days_json JSON NOT NULL,
  diet_available_meals_json JSON NOT NULL,

  activity_level_code VARCHAR(20) NOT NULL,
  workout_split_preference_code VARCHAR(20) NOT NULL,
  body_goal_code VARCHAR(20) NOT NULL,
  diet_preference_code VARCHAR(20) NOT NULL,

  additional_notes TEXT NULL,

  CONSTRAINT fk_survey_member FOREIGN KEY (member_id) REFERENCES member(id),
  CONSTRAINT chk_survey_activity_level CHECK (activity_level_code IN ('LOW','MODERATE','HIGH','VERY_HIGH')),
  CONSTRAINT chk_survey_workout_split CHECK (workout_split_preference_code IN ('STRENGTH_ONLY','STRENGTH_PLUS_CARDIO','CARDIO_ONLY')),
  CONSTRAINT chk_survey_body_goal CHECK (body_goal_code IN ('FAT_LOSS','MUSCLE_GAIN','RECOMPOSITION','MAINTENANCE','POSTURE_CORRECTION')),
  CONSTRAINT chk_survey_diet_pref CHECK (diet_preference_code IN ('BALANCED','HIGH_PROTEIN','LOW_CARB','VEGETARIAN','FLEXITARIAN','KETO'))
);

CREATE INDEX idx_survey_member_created_at
  ON survey(member_id, created_at);
```

## JSON 값 예시
- `workout_available_days_json`: `["MON","WED","FRI"]`
- `diet_available_days_json`: `["MON","TUE","WED","THU","FRI"]`
- `diet_available_meals_json`: `["BREAKFAST","LUNCH","DINNER"]`

## 코드값 정의
- `activity_level_code`: `LOW`, `MODERATE`, `HIGH`, `VERY_HIGH`
- `workout_split_preference_code`: `STRENGTH_ONLY`(근력만), `STRENGTH_PLUS_CARDIO`(근력+유산소), `CARDIO_ONLY`(유산소만)
- `body_goal_code`: `FAT_LOSS`, `MUSCLE_GAIN`, `RECOMPOSITION`, `MAINTENANCE`, `POSTURE_CORRECTION`
- `diet_preference_code`: `BALANCED`, `HIGH_PROTEIN`, `LOW_CARB`, `VEGETARIAN`, `FLEXITARIAN`, `KETO`
