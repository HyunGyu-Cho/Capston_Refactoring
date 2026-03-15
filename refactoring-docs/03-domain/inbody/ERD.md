# INBODY ERD

인바디 입력 데이터를 저장하는 구조입니다.

## 설계 원칙
- 사용자 편의성 1순위
- 입력은 단순하게, 조회는 빠르게
- 원본 측정값 + 핵심 계산값을 함께 저장
- 대규모 트래픽을 고려한 식별자/조회 전략 적용

## 주요 테이블
- `inbody`: 인바디 측정 및 계산 결과 저장
- `inbody_segment`(선택): 부위별 상세 측정값 확장용

## inbody 테이블
- `inbody_id` PK, bigint (Snowflake ID)
- `member_id` FK, not null
- `measured_at` datetime, not null
- `height_cm` decimal(5,2), not null
- `weight_kg` decimal(5,2), not null
- `body_fat_mass_kg` decimal(5,2), null
- `skeletal_muscle_mass_kg` decimal(5,2), null
- `body_water_l` decimal(5,2), null
- `waist_hip_ratio` decimal(4,2), null
- `visceral_fat_level` tinyint, null
- `bmi` decimal(5,2), not null (계산값 저장)
- `body_fat_percent` decimal(5,2), null (계산값 저장)
- `age_at_measurement` tinyint, not null (계산값 저장)
- `calculation_version` varchar(20), not null

## 시간/정렬 규칙
- `measured_at`은 사용자가 실제 측정한 시간
- `created_at`은 단순성 우선 원칙에 따라 사용하지 않음
- 최신 조회 표준 정렬: `ORDER BY measured_at DESC, inbody_id DESC`

## 인덱스/제약
- `idx_inbody_member_measured_at_id (member_id, measured_at DESC, inbody_id DESC)`
- 중복 정책(택1)
1. `UNIQUE(member_id, measured_at)`로 동일 시각 중복 저장 방지
2. 중복 허용 후 정렬/조회에서 `inbody_id`로 tie-break

## 계산값 저장 정책
- 저장 시점에 서버가 계산 후 DB 저장
- 계산식
1. `bmi = weight_kg / ((height_cm / 100)^2)`
2. `body_fat_percent = (body_fat_mass_kg / weight_kg) * 100`
3. `age_at_measurement = measured_at 기준 만나이`
- 계산 로직 변경 시 `calculation_version` 증가

## 캐시 전략(조회 성능)
- 최신 1건/5건 조회는 Cache-Aside 전략 적용
- 캐시 키 예시
1. `inbody:latest:{memberId}`
2. `inbody:latest5:{memberId}`
- 저장 성공 시 해당 member 캐시 갱신 또는 무효화
- 캐시 장애 시 DB 조회로 폴백

## 단위 규칙
- 단위는 컬럼명에 포함
1. `cm`, `kg`, `l`
- 비율/레벨은 명확한 컬럼명으로 표현
1. `body_fat_percent`, `waist_hip_ratio`, `visceral_fat_level`

## 상세 주석(한글)
- `inbody_id`는 시스템 내부 고유 식별자이며 Snowflake로 생성한다.
- `measured_at`은 사용자 측정 시각으로, 정렬/최신 판단의 1차 기준이다.
- 동일 `measured_at`이 존재할 수 있으므로 tie-break로 `inbody_id DESC`를 사용한다.
- `bmi`, `body_fat_percent`, `age_at_measurement`는 저장 시점 계산값이다.
- 계산값 저장 목적은 조회 성능과 응답 일관성 확보이다.
- 계산식 개정 시 `calculation_version`을 증가시켜 버전 추적 가능하게 한다.
- 캐시는 조회 성능 보조 수단이며, 정합성은 DB를 기준으로 유지한다.
- 저장 성공 시 캐시를 갱신/무효화하여 stale 데이터 노출을 방지한다.
- 캐시 장애는 서비스 장애로 전파하지 않고 DB 폴백으로 흡수한다.
