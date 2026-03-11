# INBODY Input ERD

## 요구사항 매핑
- [INBODY-001] 입력 경로: 수동 입력, 사진 업로드, 앱/기기 연동
- [INBODY-002] 수동 입력 필수/선택 항목: inbody, inbody_segment 컬럼으로 반영
- [INBODY-003] 입력 검증/후속 입력/계산값 정책: CHECK 제약, 신규 업로드 이력, 계산값 비저장

## 설계 원칙
- 인바디는 수정보다 신규 업로드 중심 이력 데이터로 관리한다.
- 따라서 `updated_at`은 두지 않고, `created_at`으로 측정 시점을 관리한다.
- 계산값(`age`, `body_fat_percent`)은 저장하지 않고 조회 시 계산한다.
- 단위는 컬럼명에 명시한다 (`cm`, `kg`, `kcal`).

## inbody ([INBODY-002], [INBODY-003])
- `inbody_id` BIGINT PRIMARY KEY AUTO_INCREMENT
- `member_id` BIGINT NOT NULL 
- `gender` VARCHAR(10) NOT NULL
- `height_cm` DECIMAL(5,2) NOT NULL
- `weight_kg` DECIMAL(5,2) NOT NULL
- `skeletal_muscle_mass_kg` DECIMAL(5,2) NOT NULL
- `body_fat_mass_kg` DECIMAL(5,2) NOT NULL
- `basal_metabolic_rate_kcal` DECIMAL(7,2) NOT NULL
- `visceral_fat_level` DECIMAL(5,2) NULL
- `created_at` DATETIME NOT NULL

제약/인덱스:
- FK: `member_id` -> `member.id`
- CHECK: `height_cm > 0`, `weight_kg > 0`
- CHECK: `skeletal_muscle_mass_kg >= 0`, `body_fat_mass_kg >= 0`, `basal_metabolic_rate_kcal > 0`
- CHECK: `gender IN ('MALE','FEMALE','OTHER')`
- INDEX: `idx_inbody_member_created_at (member_id, created_at)`

간략 DDL:
```sql
CREATE TABLE inbody (
  inbody_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  member_id BIGINT NOT NULL,
  gender VARCHAR(10) NOT NULL,
  height_cm DECIMAL(5,2) NOT NULL,
  weight_kg DECIMAL(5,2) NOT NULL,
  skeletal_muscle_mass_kg DECIMAL(5,2) NOT NULL,
  body_fat_mass_kg DECIMAL(5,2) NOT NULL,
  basal_metabolic_rate_kcal DECIMAL(7,2) NOT NULL,
  visceral_fat_level DECIMAL(5,2) NULL,
  created_at DATETIME NOT NULL,
  CONSTRAINT fk_inbody_member FOREIGN KEY (member_id) REFERENCES member(id),
  CONSTRAINT chk_inbody_gender CHECK (gender IN ('MALE','FEMALE','OTHER')),
  CONSTRAINT chk_inbody_height CHECK (height_cm > 0),
  CONSTRAINT chk_inbody_weight CHECK (weight_kg > 0),
  CONSTRAINT chk_inbody_smm CHECK (skeletal_muscle_mass_kg >= 0),
  CONSTRAINT chk_inbody_bfm CHECK (body_fat_mass_kg >= 0),
  CONSTRAINT chk_inbody_bmr CHECK (basal_metabolic_rate_kcal > 0)
);

CREATE INDEX idx_inbody_member_created_at
  ON inbody(member_id, created_at);
```

## inbody_segment (부위별 근육량/지방량) ([INBODY-002])
- `id` BIGINT PRIMARY KEY AUTO_INCREMENT
- `inbody_id` BIGINT NOT NULL
- `segment` VARCHAR(20) NOT NULL
- `muscle_mass_kg` DECIMAL(5,2) NULL
- `fat_mass_kg` DECIMAL(5,2) NULL

제약/인덱스:
- FK: `inbody_id` -> `inbody.inbody_id`
- CHECK: `segment IN ('LEFT_ARM','RIGHT_ARM','LEFT_LEG','RIGHT_LEG','ABDOMEN')`
- UNIQUE: `(inbody_id, segment)`
- CHECK: `muscle_mass_kg IS NULL OR muscle_mass_kg >= 0`
- CHECK: `fat_mass_kg IS NULL OR fat_mass_kg >= 0`

간략 DDL:
```sql
CREATE TABLE inbody_segment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  inbody_id BIGINT NOT NULL,
  segment VARCHAR(20) NOT NULL,
  muscle_mass_kg DECIMAL(5,2) NULL,
  fat_mass_kg DECIMAL(5,2) NULL,
  CONSTRAINT fk_inbody_segment_inbody FOREIGN KEY (inbody_id) REFERENCES inbody(inbody_id),
  CONSTRAINT uq_inbody_segment UNIQUE (inbody_id, segment),
  CONSTRAINT chk_inbody_segment_name CHECK (segment IN ('LEFT_ARM','RIGHT_ARM','LEFT_LEG','RIGHT_LEG','ABDOMEN')),
  CONSTRAINT chk_inbody_segment_muscle CHECK (muscle_mass_kg IS NULL OR muscle_mass_kg >= 0),
  CONSTRAINT chk_inbody_segment_fat CHECK (fat_mass_kg IS NULL OR fat_mass_kg >= 0)
);
```

## 계산값 처리 (저장 안 함) ([INBODY-003])
- `age`: 회원의 `birth_date`와 조회 시점 기준 계산
- `body_fat_percent`: `body_fat_mass_kg / weight_kg * 100` 계산

예시 조회식:
```sql
SELECT
  i.inbody_id,
  i.member_id,
  TIMESTAMPDIFF(YEAR, m.birth_date, i.created_at) AS age,
  ROUND((i.body_fat_mass_kg / i.weight_kg) * 100, 2) AS body_fat_percent
FROM inbody i
JOIN member m ON m.id = i.member_id;
```

