# WORKOUT RECOMMENDATION ERD

## 요구사항 매핑
- `[SURVEY-001]` 추천 요청 시 ChatGPT API를 호출해 운동 추천을 생성해야 한다.
- `[SURVEY-001]` 추천 결과와 설문 응답은 이력으로 저장되어야 한다.
- `[MY-001]` 마이페이지에서 최신 운동 추천 조회가 가능해야 한다.

## 설계 원칙
- 추천 헤더와 상세 운동 항목을 분리해 확장성을 확보한다.
- GPT 호출 운영 메트릭은 기능 데이터와 분리된 로그 테이블에 저장한다.
- 추천 이력은 수정보다 신규 생성 중심으로 관리하며 `updated_at`은 두지 않는다.

## RecommendationStatus enum
- `PENDING`
- `SUCCESS`
- `FAILED`

## workout_recommendation
- `workout_recommendation_id` BIGINT PRIMARY KEY AUTO_INCREMENT
- `survey_id` BIGINT NOT NULL
- `model` VARCHAR(50) NOT NULL
- `prompt_version` VARCHAR(30) NOT NULL
- `status` VARCHAR(20) NOT NULL
- `summary_text` TEXT NULL
- `raw_response_json` JSON NULL
- `request_hash` CHAR(64) NOT NULL
- `created_at` DATETIME NOT NULL

제약/인덱스:
- FK: `survey_id` -> `survey.survey_id`
- CHECK: `status IN ('PENDING','SUCCESS','FAILED')`
- INDEX: `idx_workout_rec_survey_created_at (survey_id, created_at)`
- INDEX: `idx_workout_rec_status_created_at (status, created_at)`
- INDEX: `idx_workout_rec_request_hash (request_hash)`

간략 DDL:
```sql
CREATE TABLE workout_recommendation (
  workout_recommendation_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  survey_id BIGINT NOT NULL,
  model VARCHAR(50) NOT NULL,
  prompt_version VARCHAR(30) NOT NULL,
  status VARCHAR(20) NOT NULL,
  summary_text TEXT NULL,
  raw_response_json JSON NULL,
  request_hash CHAR(64) NOT NULL,
  created_at DATETIME NOT NULL,
  CONSTRAINT fk_workout_rec_survey FOREIGN KEY (survey_id) REFERENCES survey(survey_id),
  CONSTRAINT chk_workout_rec_status CHECK (status IN ('PENDING','SUCCESS','FAILED'))
);

CREATE INDEX idx_workout_rec_survey_created_at
  ON workout_recommendation(survey_id, created_at);

CREATE INDEX idx_workout_rec_status_created_at
  ON workout_recommendation(status, created_at);

CREATE INDEX idx_workout_rec_request_hash
  ON workout_recommendation(request_hash);
```

## workout_recommendation_detail
- `id` BIGINT PRIMARY KEY AUTO_INCREMENT
- `workout_recommendation_id` BIGINT NOT NULL
- `day_code` VARCHAR(10) NOT NULL
- `exercise_name` VARCHAR(100) NOT NULL
- `sets` INT NULL
- `reps` INT NULL
- `duration_min` INT NULL
- `rest_sec` INT NULL
- `intensity_code` VARCHAR(20) NULL
- `memo` TEXT NULL
- `sort_order` INT NOT NULL

제약/인덱스:
- FK: `workout_recommendation_id` -> `workout_recommendation.workout_recommendation_id`
- CHECK: `day_code IN ('MON','TUE','WED','THU','FRI','SAT','SUN')`
- CHECK: `sets IS NULL OR sets > 0`
- CHECK: `reps IS NULL OR reps > 0`
- CHECK: `duration_min IS NULL OR duration_min > 0`
- CHECK: `rest_sec IS NULL OR rest_sec >= 0`
- CHECK: `sort_order >= 0`
- INDEX: `idx_workout_detail_rec_day (workout_recommendation_id, day_code)`

간략 DDL:
```sql
CREATE TABLE workout_recommendation_detail (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  workout_recommendation_id BIGINT NOT NULL,
  day_code VARCHAR(10) NOT NULL,
  exercise_name VARCHAR(100) NOT NULL,
  sets INT NULL,
  reps INT NULL,
  duration_min INT NULL,
  rest_sec INT NULL,
  intensity_code VARCHAR(20) NULL,
  memo TEXT NULL,
  sort_order INT NOT NULL,
  CONSTRAINT fk_workout_detail_rec FOREIGN KEY (workout_recommendation_id) REFERENCES workout_recommendation(workout_recommendation_id),
  CONSTRAINT chk_workout_detail_day CHECK (day_code IN ('MON','TUE','WED','THU','FRI','SAT','SUN')),
  CONSTRAINT chk_workout_detail_sets CHECK (sets IS NULL OR sets > 0),
  CONSTRAINT chk_workout_detail_reps CHECK (reps IS NULL OR reps > 0),
  CONSTRAINT chk_workout_detail_duration CHECK (duration_min IS NULL OR duration_min > 0),
  CONSTRAINT chk_workout_detail_rest CHECK (rest_sec IS NULL OR rest_sec >= 0),
  CONSTRAINT chk_workout_detail_sort CHECK (sort_order >= 0)
);

CREATE INDEX idx_workout_detail_rec_day
  ON workout_recommendation_detail(workout_recommendation_id, day_code);
```

## workout_recommendation_execution_log
- `id` BIGINT PRIMARY KEY AUTO_INCREMENT
- `workout_recommendation_id` BIGINT NOT NULL
- `provider` VARCHAR(30) NOT NULL
- `provider_request_id` VARCHAR(100) NULL
- `latency_ms` INT NULL
- `token_usage_input` INT NULL
- `token_usage_output` INT NULL
- `token_usage_total` INT NULL
- `cost_usd` DECIMAL(12,6) NULL
- `error_code` VARCHAR(50) NULL
- `error_message` TEXT NULL
- `logged_at` DATETIME NOT NULL

제약/인덱스:
- FK: `workout_recommendation_id` -> `workout_recommendation.workout_recommendation_id`
- CHECK: `latency_ms IS NULL OR latency_ms >= 0`
- CHECK: `token_usage_input IS NULL OR token_usage_input >= 0`
- CHECK: `token_usage_output IS NULL OR token_usage_output >= 0`
- CHECK: `token_usage_total IS NULL OR token_usage_total >= 0`
- CHECK: `cost_usd IS NULL OR cost_usd >= 0`
- INDEX: `idx_workout_log_rec_id (workout_recommendation_id)`
- INDEX: `idx_workout_log_logged_at (logged_at)`

간략 DDL:
```sql
CREATE TABLE workout_recommendation_execution_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  workout_recommendation_id BIGINT NOT NULL,
  provider VARCHAR(30) NOT NULL,
  provider_request_id VARCHAR(100) NULL,
  latency_ms INT NULL,
  token_usage_input INT NULL,
  token_usage_output INT NULL,
  token_usage_total INT NULL,
  cost_usd DECIMAL(12,6) NULL,
  error_code VARCHAR(50) NULL,
  error_message TEXT NULL,
  logged_at DATETIME NOT NULL,
  CONSTRAINT fk_workout_log_rec FOREIGN KEY (workout_recommendation_id) REFERENCES workout_recommendation(workout_recommendation_id),
  CONSTRAINT chk_workout_log_latency CHECK (latency_ms IS NULL OR latency_ms >= 0),
  CONSTRAINT chk_workout_log_token_in CHECK (token_usage_input IS NULL OR token_usage_input >= 0),
  CONSTRAINT chk_workout_log_token_out CHECK (token_usage_output IS NULL OR token_usage_output >= 0),
  CONSTRAINT chk_workout_log_token_total CHECK (token_usage_total IS NULL OR token_usage_total >= 0),
  CONSTRAINT chk_workout_log_cost CHECK (cost_usd IS NULL OR cost_usd >= 0)
);

CREATE INDEX idx_workout_log_rec_id
  ON workout_recommendation_execution_log(workout_recommendation_id);

CREATE INDEX idx_workout_log_logged_at
  ON workout_recommendation_execution_log(logged_at);
```

## 조회 예시
- 회원 최신 운동 추천 1건:
```sql
SELECT wr.*
FROM workout_recommendation wr
JOIN survey s ON s.survey_id = wr.survey_id
WHERE s.member_id = ?
  AND wr.status = 'SUCCESS'
ORDER BY wr.created_at DESC
LIMIT 1;
```
