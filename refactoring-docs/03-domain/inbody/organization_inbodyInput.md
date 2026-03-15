## InbodyInput 기능
- inbody entity {
    inbody_id pk auto_increment
    member_id fk not null
    bigint height
    bigint weight
    ... 기타 각종 인바디 데이터 정보들
    LocalDate createdAt
    updatedAt은 필요없음. 인바디 데이터는 최초 생성만이 중요함. 
    
}
- login 된 사용자만 이용 가능
- 구현해야 할 기능은 크게 2가지
    -인바디 데이터 입력 기능
    -인바디 데이터 저장 기능
- login 된 사용자의 id나 accessToken 기반으로 사용자의 age, gender, birthDate 같은 정보를 자동으로 가져온다
- DTO
    -InbodyInputRequest.java: frontend로부터 인바디 데이터 입력받아 backend로 전송. JSON 타입. 아래는 예시
        POST /api/v1/inbodyInput  { "height" : 173(단위: cm) , 
        "weight" : 73(단위: kg), 
        "체지방량" : 12.5(단위: kg),
        "골격근량" : 33.4(단위: kg),
        "체수분": 46.8(단위: L),
        "체지방률": 24.4(단위: %),
        "BMI": 24.7 (단위: kg/m^2),
        "복부지방률": 0.81
        "내장지방레벨": 5   
        }
    -InbodyInputResponse.java: 인바디 데이터 정상 저장됐다고 frontend로 보내는 메세지정도..
    ApiResponse.java 구조로 맞춤 {
        "success" : "true",
        "data" : {"height" : 173(단위: cm) , 
        "weight" : 73(단위: kg), 
        "체지방량" : 12.5(단위: kg),
        "골격근량" : 33.4(단위: kg),
        "체수분": 46.8(단위: L),
        "체지방률": 24.4(단위: %),
        "BMI": 24.7 (단위: kg/m^2),
        "복부지방률": 0.81
        "내장지방레벨": 5   
        }, 
        "error" : ??,
        "timestamp": "YYYYMMDD HHMMSS"
    }

    -InbodyRepository.java
        (1) 데이터 조회: InbodyRepository.findByMemberId(memberId)
        (2) 데이터 저장: InbodyRepository.save(Member member)
        (3) 사용자별 데이터 최신순 조회: InbodyRepository.findDataByASC(member 정보)
            - 인덱스 활용(member_id, created_at) 이 좋을래나? 
            - created-at 이 snowflake 이용해서 적용되는게 중복을 최소화하는 방법일까..?
        (4) 인바디 세부정보 확인(이건 mypage에서의 기능이긴 함. 예를들어 일부 카드 보여주고 상세기록 페이지로 가면 더 상세하게 보여준다던가, 예전기록 중 하나 클릭하면 최신 정보와의 변화 추이를 표로 나타낸다건가..)

    -InbodyService.java
        (1) request dto를 받아 정보 읽은 뒤
        (2) 로그인된 사용자 정보를 바탕으로 member 엔티티 참조하는 inbody 테이블에 신규 컬럼 생성
        (3) /api/v1/inbody/{inbody_id} 컨트롤러 요청으로 들어온 GetMapping: inbody_id 에 해당하는 인바디데이터 세부정보 보여줌
        (4) /api/v1/inbody: 컨트롤러 요청으로 들어온 PostMapping: 사용자가 입력한 데이터 저장 후 설문 페이지로 리다이렉트
        (5) /api/v1/inbody/{member_id} 컨트롤러 요청으로 들어온 GetMapping: 사용자의 최신 인바디 데이터 5건 보여주기 (5건 이하면 해당 갯수만큼). 5건 이상인 경우에 한해 5, 10, 20, 50, 100 건 으로 페이지별 보여질 데이터 개수 조정이 가능하도록 할거임.

        
------ 수정사항(초안)

## 개발 명세 (확정안)

### 1. 목표 및 원칙
- 사용자 편의성을 최우선으로 한다.
- 입력은 간단하게, 조회는 빠르고 명확하게 제공한다.
- 단순성을 유지하되 핵심 분석 정보는 누락 없이 저장한다.
- 원본 측정값 + 핵심 계산값을 함께 저장한다.

### 2. 인증/권한
- 인바디 기능은 로그인 사용자만 접근 가능하다.
- member_id는 클라이언트가 전달하지 않고 서버가 access token에서 추출한다.
- 상세/목록 조회 시 본인 데이터만 조회 가능해야 한다.

### 3. 데이터 모델 명세
#### 3.1 inbody 테이블
- inbody_id: PK, bigint, auto increment
- member_id: FK, not null
- measured_at: datetime, not null (실제 측정 시각)
- height_cm: decimal(5,2), not null
- weight_kg: decimal(5,2), not null
- body_fat_mass_kg: decimal(5,2), null
- skeletal_muscle_mass_kg: decimal(5,2), null
- body_water_l: decimal(5,2), null
- waist_hip_ratio: decimal(4,2), null
- visceral_fat_level: tinyint, null
- bmi: decimal(5,2), not null (계산값 저장)
- body_fat_percent: decimal(5,2), null (계산값 저장)
- age_at_measurement: tinyint, not null (계산값 저장)
- calculation_version: varchar(20), not null (예: v1)

#### 3.2 시간 컬럼 정책
- 단순성 우선 원칙에 따라 created_at은 두지 않는다.
- 최신/정렬/검색 기준은 measured_at으로 통일한다.

#### 3.3 인덱스/제약
- INDEX idx_inbody_member_measured_at (member_id, measured_at DESC)
- 중복 정책(택1)
1. UNIQUE(member_id, measured_at)로 동일 시각 중복 저장 방지
2. 중복 허용 시 조회 정렬에 inbody_id DESC 보조 적용

### 4. 계산 로직 명세
- BMI = weight_kg / ((height_cm / 100) ^ 2)
- body_fat_percent = (body_fat_mass_kg / weight_kg) * 100
- age_at_measurement = measured_at 기준 만나이
- 소수점 처리 기준은 round half up, 소수점 2자리로 통일
- 계산식 변경 시 calculation_version을 증가시킨다.

### 5. API 명세
#### 5.1 저장
- POST /api/v1/inbody
- 요청 본문: InbodyInputRequest
- 처리
1. 토큰에서 member_id 추출
2. 요청값 검증
3. 계산값 산출(bmi, body_fat_percent, age_at_measurement)
4. inbody 저장
5. ApiResponse 반환

#### 5.2 상세 조회
- GET /api/v1/inbody/{inbodyId}
- 본인 데이터 검증 후 반환

#### 5.3 내 목록 조회
- GET /api/v1/inbody/me?page=0&size=5
- 기본 size=5
- 허용 size: 5,10,20,50,100
- 정렬: measured_at DESC

### 6. DTO 명세
#### 6.1 InbodyInputRequest
- JSON 키는 영문으로 통일한다.
- 권장 필드
- measuredAt
- heightCm
- weightKg
- bodyFatMassKg
- skeletalMuscleMassKg
- bodyWaterL
- waistHipRatio
- visceralFatLevel

#### 6.2 InbodyInputResponse (ApiResponse.data)
- inbodyId
- measuredAt
- member 정보 요약
- memberId
- gender
- birthDate
- ageAtMeasurement
- 인바디 측정/계산 값
- heightCm
- weightKg
- bmi
- bodyFatMassKg
- bodyFatPercent
- skeletalMuscleMassKg
- bodyWaterL
- waistHipRatio
- visceralFatLevel

#### 6.3 ApiResponse 공통
- success: boolean
- data: object or null
- error: object or null
- timestamp: ISO-8601 문자열

### 7. 입력 검증 규칙
- 필수값: measuredAt, heightCm, weightKg
- heightCm, weightKg는 0보다 커야 한다.
- bodyFatMassKg, skeletalMuscleMassKg, bodyWaterL, waistHipRatio, visceralFatLevel은 음수 불가
- 범위 검증 실패 시 필드별 에러 메시지를 반환한다.

### 8. 에러 처리 규칙
- 인증 실패: 401
- 권한 없음(타인 데이터 접근): 403
- 데이터 없음: 404
- 유효성 실패: 400
- 서버 오류: 500
- 모든 오류는 ApiResponse.error 형식으로 통일한다.

### 9. 개발 단계
1. ERD/마이그레이션 확정
2. Entity/Repository 구현
3. Service 계산/검증 로직 구현
4. Controller/API 응답 스펙 구현
5. 단위/통합 테스트 작성
6. 목록 조회 성능 확인(인덱스 점검)

### 10. 테스트 명세
- 계산 정확도 테스트(BMI, 체지방률, 나이)
- 경계값 테스트(0, 음수, 과대값)
- 인증/권한 테스트
- 정렬/페이징 테스트
- 응답 DTO에 member 요약정보 포함 여부 테스트

### 11. 추후 확장
- OCR 기반 자동 입력(사진 업로드 -> 추출 -> 사용자 확인 -> 저장)
- 외부 인바디 기기/플랫폼 API 연동

### 12. 대규모 트래픽 대응 명세
#### 12.1 Snowflake 적용 대상
- Snowflake 알고리즘은 inbody_id(PK) 생성에 적용한다.
- measured_at에는 Snowflake를 적용하지 않는다.
- measured_at은 사용자가 실제 측정한 비즈니스 시간 데이터다.

#### 12.2 정렬 기준(최신 조회)
- 최신 조회는 measured_at DESC를 1차 기준으로 사용한다.
- measured_at이 동일한 경우 inbody_id DESC를 2차 기준으로 사용한다.
- 표준 정렬: ORDER BY measured_at DESC, inbody_id DESC

#### 12.3 캐시 적용 원칙
- 최신 1건/5건 조회는 캐시 우선(Cache-Aside) 전략을 적용한다.
- 캐시 키 예시
- inbody:latest:{memberId}
- inbody:latest5:{memberId}
- 저장 성공 시 해당 member 캐시를 즉시 갱신 또는 무효화한다.
- 캐시 장애 시 DB 조회로 폴백한다.

#### 12.4 DB 인덱스 보강
- idx_inbody_member_measured_at_id (member_id, measured_at DESC, inbody_id DESC)
- 캐시 미스 상황에서도 최신 조회 성능을 보장한다.

### 13. 상세 주석(한글)
- 본 문서는 "사용자 편의성 우선"을 기준으로 작성한다.
- 사용자 입력 부담을 줄이기 위해 필수값은 최소화하고, 계산 가능한 값은 서버에서 산출한다.
- 단, 서비스 운영과 분석에 필요한 핵심 계산값(BMI, 체지방률, 측정 시점 나이)은 저장한다.
- `inbody_id`는 시스템 식별자이므로 Snowflake를 적용한다.
- `measured_at`은 사용자가 실제 측정한 시각이며, 비즈니스 의미를 가지는 컬럼이다.
- 최신 조회 정렬은 `measured_at DESC, inbody_id DESC`를 사용한다.
- 응답 DTO의 member 정보는 최소 범위로 제한하여 개인정보 과노출을 방지한다.
- 최신 조회 캐시는 성능 최적화를 위한 계층이며, 장애 시 DB로 폴백한다.
- 중복 저장 정책은 운영 정책에 따라 `UNIQUE(member_id, measured_at)` 적용 여부를 선택한다.
- 계산식이 변경되면 `calculation_version`을 올려 과거 데이터 해석 기준을 분리한다.
