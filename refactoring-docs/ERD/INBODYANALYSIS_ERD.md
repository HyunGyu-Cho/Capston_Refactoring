# INBODY ANALYSIS ERD

## 요구사항 매핑
- `[ANALYSIS-001]` 인바디 기반 체형분석: 최신 인바디 기준 분석 생성/저장/조회
- `[MY-001]` 마이페이지 프로필카드 조회: 최신 체형분석 1건 조회

## 설계 원칙
- 체형분석은 `inbody_id`를 기준으로 관리하고, `member_id`는 중복 저장하지 않는다.
- 사용자 기능에 필요한 도메인 상태(`status`, `summary_text`)는 본테이블에 저장한다.
- 운영/관측 메트릭(`latency_ms`, `token_usage`, `cost`)은 로그 테이블로 분리한다.
- GPT 응답 포맷 변경에 대비해 `raw_response_json`과 `input_snapshot_json`을 저장한다.

## AnalysisStatus enum
- `PENDING`
- `SUCCESS`
- `FAILED`

## inbody_analysis ([ANALYSIS-001], [MY-001])
- `analysis_id` BIGINT PRIMARY KEY AUTO_INCREMENT
- `inbody_id` BIGINT NOT NULL
- `model` VARCHAR(50) NOT NULL
- `prompt_version` VARCHAR(30) NOT NULL
- `status` VARCHAR(20) NOT NULL
- `summary_text` TEXT NULL
- `recommendation_text` TEXT NULL
- `raw_response_json` JSON NULL
- `request_hash` CHAR(64) NOT NULL
- `temperature` DECIMAL(3,2) NOT NULL
- `input_snapshot_json` JSON NOT NULL
- `created_at` DATETIME NOT NULL

제약/인덱스:
- FK: `inbody_id` -> `inbody.inbody_id`
- CHECK: `status IN ('PENDING','SUCCESS','FAILED')`
- CHECK: `temperature >= 0 AND temperature <= 2`
- INDEX: `idx_analysis_inbody_created_at (inbody_id, created_at)`
- INDEX: `idx_analysis_status_created_at (status, created_at)`
- INDEX: `idx_analysis_request_hash (request_hash)`

간략 DDL:
```sql
CREATE TABLE inbody_analysis (
  analysis_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  inbody_id BIGINT NOT NULL,
  model VARCHAR(50) NOT NULL,
  prompt_version VARCHAR(30) NOT NULL,
  status VARCHAR(20) NOT NULL,
  summary_text TEXT NULL,
  recommendation_text TEXT NULL,
  raw_response_json JSON NULL,
  request_hash CHAR(64) NOT NULL,
  temperature DECIMAL(3,2) NOT NULL,
  input_snapshot_json JSON NOT NULL,
  created_at DATETIME NOT NULL,
  CONSTRAINT fk_analysis_inbody FOREIGN KEY (inbody_id) REFERENCES inbody(inbody_id),
  CONSTRAINT chk_analysis_status CHECK (status IN ('PENDING','SUCCESS','FAILED')),
  CONSTRAINT chk_analysis_temp CHECK (temperature >= 0 AND temperature <= 2)
);

CREATE INDEX idx_analysis_inbody_created_at
  ON inbody_analysis(inbody_id, created_at);

CREATE INDEX idx_analysis_status_created_at
  ON inbody_analysis(status, created_at);

CREATE INDEX idx_analysis_request_hash
  ON inbody_analysis(request_hash);
```

## inbody_analysis_execution_log (운영/관측)
- `id` BIGINT PRIMARY KEY AUTO_INCREMENT
- `analysis_id` BIGINT NOT NULL
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
- FK: `analysis_id` -> `inbody_analysis.analysis_id`
- CHECK: `latency_ms IS NULL OR latency_ms >= 0`
- CHECK: `token_usage_input IS NULL OR token_usage_input >= 0`
- CHECK: `token_usage_output IS NULL OR token_usage_output >= 0`
- CHECK: `token_usage_total IS NULL OR token_usage_total >= 0`
- CHECK: `cost_usd IS NULL OR cost_usd >= 0`
- INDEX: `idx_analysis_log_analysis_id (analysis_id)`
- INDEX: `idx_analysis_log_logged_at (logged_at)`

간략 DDL:
```sql
CREATE TABLE inbody_analysis_execution_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  analysis_id BIGINT NOT NULL,
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
  CONSTRAINT fk_analysis_log_analysis FOREIGN KEY (analysis_id) REFERENCES inbody_analysis(analysis_id),
  CONSTRAINT chk_analysis_log_latency CHECK (latency_ms IS NULL OR latency_ms >= 0),
  CONSTRAINT chk_analysis_log_token_in CHECK (token_usage_input IS NULL OR token_usage_input >= 0),
  CONSTRAINT chk_analysis_log_token_out CHECK (token_usage_output IS NULL OR token_usage_output >= 0),
  CONSTRAINT chk_analysis_log_token_total CHECK (token_usage_total IS NULL OR token_usage_total >= 0),
  CONSTRAINT chk_analysis_log_cost CHECK (cost_usd IS NULL OR cost_usd >= 0)
);

CREATE INDEX idx_analysis_log_analysis_id
  ON inbody_analysis_execution_log(analysis_id);

CREATE INDEX idx_analysis_log_logged_at
  ON inbody_analysis_execution_log(logged_at);
```

## 조회 기준 예시
- 사용자 최신 체형분석 1건:
```sql
SELECT a.*
FROM inbody_analysis a
JOIN inbody i ON i.inbody_id = a.inbody_id
WHERE i.member_id = ?
  AND a.status = 'SUCCESS'
ORDER BY a.created_at DESC
LIMIT 1;
```
