# DIET RECOMMENDATION ERD

## 요구사항 매핑
- `[SURVEY-001]` 추천 요청 시 ChatGPT API를 호출해 식단 추천을 생성해야 한다.
- `[SURVEY-001]` 추천 결과와 설문 응답은 이력으로 저장되어야 한다.
- `[MY-001]` 마이페이지에서 최신 식단 추천 조회가 가능해야 한다.

## 설계 원칙
- 추천 헤더와 상세 식단 항목을 분리해 확장성을 확보한다.
- GPT 호출 운영 메트릭은 기능 데이터와 분리된 로그 테이블에 저장한다.
- 추천 이력은 수정보다 신규 생성 중심으로 관리하며 `updated_at`은 두지 않는다.

## RecommendationStatus enum
- `PENDING`
- `SUCCESS`
- `FAILED`

## diet_recommendation
- `diet_recommendation_id` BIGINT PRIMARY KEY AUTO_INCREMENT
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
- INDEX: `idx_diet_rec_survey_created_at (survey_id, created_at)`
- INDEX: `idx_diet_rec_status_created_at (status, created_at)`
- INDEX: `idx_diet_rec_request_hash (request_hash)`

간략 DDL:
```sql
CREATE TABLE diet_recommendation (
  diet_recommendation_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  survey_id BIGINT NOT NULL,
  model VARCHAR(50) NOT NULL,
  prompt_version VARCHAR(30) NOT NULL,
  status VARCHAR(20) NOT NULL,
  summary_text TEXT NULL,
  raw_response_json JSON NULL,
  request_hash CHAR(64) NOT NULL,
  created_at DATETIME NOT NULL,
  CONSTRAINT fk_diet_rec_survey FOREIGN KEY (survey_id) REFERENCES survey(survey_id),
  CONSTRAINT chk_diet_rec_status CHECK (status IN ('PENDING','SUCCESS','FAILED'))
);

CREATE INDEX idx_diet_rec_survey_created_at
  ON diet_recommendation(survey_id, created_at);

CREATE INDEX idx_diet_rec_status_created_at
  ON diet_recommendation(status, created_at);

CREATE INDEX idx_diet_rec_request_hash
  ON diet_recommendation(request_hash);
```

## diet_recommendation_detail
- `id` BIGINT PRIMARY KEY AUTO_INCREMENT
- `diet_recommendation_id` BIGINT NOT NULL
- `day_code` VARCHAR(10) NOT NULL
- `meal_code` VARCHAR(20) NOT NULL
- `menu_name` VARCHAR(100) NOT NULL
- `amount_text` VARCHAR(100) NULL
- `kcal` INT NULL
- `protein_g` DECIMAL(6,2) NULL
- `carb_g` DECIMAL(6,2) NULL
- `fat_g` DECIMAL(6,2) NULL
- `memo` TEXT NULL
- `sort_order` INT NOT NULL

제약/인덱스:
- FK: `diet_recommendation_id` -> `diet_recommendation.diet_recommendation_id`
- CHECK: `day_code IN ('MON','TUE','WED','THU','FRI','SAT','SUN')`
- CHECK: `meal_code IN ('BREAKFAST','LUNCH','DINNER','SNACK')`
- CHECK: `kcal IS NULL OR kcal >= 0`
- CHECK: `protein_g IS NULL OR protein_g >= 0`
- CHECK: `carb_g IS NULL OR carb_g >= 0`
- CHECK: `fat_g IS NULL OR fat_g >= 0`
- CHECK: `sort_order >= 0`
- INDEX: `idx_diet_detail_rec_day_meal (diet_recommendation_id, day_code, meal_code)`

간략 DDL:
```sql
CREATE TABLE diet_recommendation_detail (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  diet_recommendation_id BIGINT NOT NULL,
  day_code VARCHAR(10) NOT NULL,
  meal_code VARCHAR(20) NOT NULL,
  menu_name VARCHAR(100) NOT NULL,
  amount_text VARCHAR(100) NULL,
  kcal INT NULL,
  protein_g DECIMAL(6,2) NULL,
  carb_g DECIMAL(6,2) NULL,
  fat_g DECIMAL(6,2) NULL,
  memo TEXT NULL,
  sort_order INT NOT NULL,
  CONSTRAINT fk_diet_detail_rec FOREIGN KEY (diet_recommendation_id) REFERENCES diet_recommendation(diet_recommendation_id),
  CONSTRAINT chk_diet_detail_day CHECK (day_code IN ('MON','TUE','WED','THU','FRI','SAT','SUN')),
  CONSTRAINT chk_diet_detail_meal CHECK (meal_code IN ('BREAKFAST','LUNCH','DINNER','SNACK')),
  CONSTRAINT chk_diet_detail_kcal CHECK (kcal IS NULL OR kcal >= 0),
  CONSTRAINT chk_diet_detail_protein CHECK (protein_g IS NULL OR protein_g >= 0),
  CONSTRAINT chk_diet_detail_carb CHECK (carb_g IS NULL OR carb_g >= 0),
  CONSTRAINT chk_diet_detail_fat CHECK (fat_g IS NULL OR fat_g >= 0),
  CONSTRAINT chk_diet_detail_sort CHECK (sort_order >= 0)
);

CREATE INDEX idx_diet_detail_rec_day_meal
  ON diet_recommendation_detail(diet_recommendation_id, day_code, meal_code);
```

## diet_recommendation_execution_log
- `id` BIGINT PRIMARY KEY AUTO_INCREMENT
- `diet_recommendation_id` BIGINT NOT NULL
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
- FK: `diet_recommendation_id` -> `diet_recommendation.diet_recommendation_id`
- CHECK: `latency_ms IS NULL OR latency_ms >= 0`
- CHECK: `token_usage_input IS NULL OR token_usage_input >= 0`
- CHECK: `token_usage_output IS NULL OR token_usage_output >= 0`
- CHECK: `token_usage_total IS NULL OR token_usage_total >= 0`
- CHECK: `cost_usd IS NULL OR cost_usd >= 0`
- INDEX: `idx_diet_log_rec_id (diet_recommendation_id)`
- INDEX: `idx_diet_log_logged_at (logged_at)`

간략 DDL:
```sql
CREATE TABLE diet_recommendation_execution_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  diet_recommendation_id BIGINT NOT NULL,
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
  CONSTRAINT fk_diet_log_rec FOREIGN KEY (diet_recommendation_id) REFERENCES diet_recommendation(diet_recommendation_id),
  CONSTRAINT chk_diet_log_latency CHECK (latency_ms IS NULL OR latency_ms >= 0),
  CONSTRAINT chk_diet_log_token_in CHECK (token_usage_input IS NULL OR token_usage_input >= 0),
  CONSTRAINT chk_diet_log_token_out CHECK (token_usage_output IS NULL OR token_usage_output >= 0),
  CONSTRAINT chk_diet_log_token_total CHECK (token_usage_total IS NULL OR token_usage_total >= 0),
  CONSTRAINT chk_diet_log_cost CHECK (cost_usd IS NULL OR cost_usd >= 0)
);

CREATE INDEX idx_diet_log_rec_id
  ON diet_recommendation_execution_log(diet_recommendation_id);

CREATE INDEX idx_diet_log_logged_at
  ON diet_recommendation_execution_log(logged_at);
```

## 조회 예시
- 회원 최신 식단 추천 1건:
```sql
SELECT dr.*
FROM diet_recommendation dr
JOIN survey s ON s.survey_id = dr.survey_id
WHERE s.member_id = ?
  AND dr.status = 'SUCCESS'
ORDER BY dr.created_at DESC
LIMIT 1;
```
