# Requirements

## [MY-001] 마이페이지 프로필카드 조회
- 사용자는 마이페이지에서 최신 인바디, 최신 체형분석, 최신 운동/식단 추천, 캘린더 체크리스트, 구매목록, 장바구니, 과거 기록, 변화 추이를 조회할 수 있어야 한다.
- 각 요약 카드에는 상세보기 링크가 제공되어야 한다.
- "최신"은 데이터 유형별 `createdAt` 기준 최근 1건으로 정의한다.
- 데이터가 없으면 빈 상태를 표시해야 한다.

## [ANALYSIS-001] 인바디 기반 체형분석
- 사용자는 인바디 입력 후 체형분석을 요청할 수 있어야 한다.
- 시스템은 최신 인바디를 기준으로 분석을 수행하고 결과를 저장/조회 가능해야 한다.
- 필수 인바디 값 누락 또는 데이터 없음 시 요청을 거부해야 한다.

## [ANALYSIS-002] 체형분석 결과 저장/추적 정책
- 체형분석 결과는 `inbody_id` 기준으로 저장하며 `member_id`는 중복 저장하지 않는다.
- 분석 결과에는 `status(PENDING/SUCCESS/FAILED)`, `summary_text`, `recommendation_text`를 포함해야 한다.
- 분석 생성 이력에는 `model`, `prompt_version`, `created_at`을 저장해야 한다.
- 재현 가능성을 위해 `request_hash`, `temperature`, `input_snapshot_json`, `raw_response_json`을 저장해야 한다.
- 운영 메트릭(`latency_ms`, `token_usage`, `cost`, `error`)은 본 분석 테이블과 분리된 실행 로그 테이블에 저장해야 한다.

## [SURVEY-001] 체형분석 기반 설문 및 추천 요청
- 사용자는 체형분석 결과 페이지에서 설문작성 페이지로 이동할 수 있어야 한다.
- 설문 항목:
1. 운동 가능 일자(`workout_available_days_json`)
2. 식단 가능 일자(`diet_available_days_json`)
3. 식단 가능 끼니(`diet_available_meals_json`)
4. 평균 활동 수준(`activity_level_code`: `LOW`, `MODERATE`, `HIGH`, `VERY_HIGH`)
5. 운동 분할 루틴 선호(`workout_split_preference_code`: `STRENGTH_ONLY`, `STRENGTH_PLUS_CARDIO`, `CARDIO_ONLY`)
6. 선호 체형 목표(`body_goal_code`: `FAT_LOSS`, `MUSCLE_GAIN`, `RECOMPOSITION`, `MAINTENANCE`, `POSTURE_CORRECTION`)
7. 선호 식단 성향(`diet_preference_code`: `BALANCED`, `HIGH_PROTEIN`, `LOW_CARB`, `VEGETARIAN`, `FLEXITARIAN`, `KETO`)
8. 세부 추가사항(`additional_notes`)
- 추천 요청 시 ChatGPT API를 호출해 운동/식단 추천을 생성해야 한다.
- 추천 결과와 설문 응답은 이력으로 저장되어야 한다.

## [WORKOUT-001] 운동 추천 생성/저장/조회
- 시스템은 설문(`survey_id`) 기반으로 운동 추천을 생성해야 한다.
- 운동 추천 헤더에는 `model`, `prompt_version`, `status(PENDING/SUCCESS/FAILED)`, `summary_text`, `request_hash`, `created_at`을 저장해야 한다.
- 운동 추천 상세는 요일별/운동별 항목(`day_code`, `exercise_name`, `sets`, `reps`, `duration_min`, `rest_sec`, `intensity_code`, `sort_order`)으로 저장해야 한다.
- GPT 원문은 `raw_response_json`으로 저장해 포맷 변경 시 재파싱 가능해야 한다.
- 운영 메트릭(`latency_ms`, `token_usage`, `cost`, `error`)은 추천 본테이블과 분리된 실행 로그 테이블에 저장해야 한다.
- 사용자는 본인의 최신 운동 추천(`status=SUCCESS`, `created_at` 기준 최근 1건)을 조회할 수 있어야 한다.

## [DIET-001] 식단 추천 생성/저장/조회
- 시스템은 설문(`survey_id`) 기반으로 식단 추천을 생성해야 한다.
- 식단 추천 헤더에는 `model`, `prompt_version`, `status(PENDING/SUCCESS/FAILED)`, `summary_text`, `request_hash`, `created_at`을 저장해야 한다.
- 식단 추천 상세는 요일/끼니별 항목(`day_code`, `meal_code`, `menu_name`, `amount_text`, `kcal`, `protein_g`, `carb_g`, `fat_g`, `sort_order`)으로 저장해야 한다.
- GPT 원문은 `raw_response_json`으로 저장해 포맷 변경 시 재파싱 가능해야 한다.
- 운영 메트릭(`latency_ms`, `token_usage`, `cost`, `error`)은 추천 본테이블과 분리된 실행 로그 테이블에 저장해야 한다.
- 사용자는 본인의 최신 식단 추천(`status=SUCCESS`, `created_at` 기준 최근 1건)을 조회할 수 있어야 한다.

## [COMM-001] 커뮤니티 카테고리
- 카테고리: 자유게시판, 운동게시판, 식단게시판, 사용후기, 상품게시판

## [COMM-002] 게시글/댓글/상호작용
- 게시글은 카테고리, 작성시간, 수정시간(미수정 시 null), custom_id, 제목, 내용, 익명 여부, 좋아요/싫어요, 조회수를 가진다.
- 댓글/대댓글(depth) 지원
- 게시글/댓글 신고 기능 지원
- 익명 작성 시 화면에만 익명 노출, 내부적으로는 작성자 식별 유지

## [COMM-003] 게시글 수정/삭제
- 작성자 본인은 게시글 수정/삭제 가능
- 댓글이 달려 있어도 삭제 가능
- 삭제는 소프트 삭제
- 소프트 삭제된 원문은 관리자 조회 가능

## [COMM-004] 신고 누적 기반 운영
- 댓글 누적 신고 수 5건 이상 시 관리자 알림 발생
- 관리자는 사용자 동의 없이 강제 삭제/블라인드 가능
- 관리자 조치 이력 저장

## [COMM-005] 상호작용 카운트 캐싱/동기화 정책
- 게시글 좋아요/싫어요/조회수는 Redis 캐시를 사용해 실시간 집계할 수 있어야 한다.
- 카운트의 최종 원장은 MySQL에 저장되어야 하며, Redis 단독 저장에 의존해서는 안 된다.
- Redis 카운트는 배치 또는 이벤트 소비 방식으로 MySQL(`community_post.like_count`, `community_post.dislike_count`, `community_post.view_count`)에 주기 반영되어야 한다.
- 좋아요/싫어요 사용자 단위 원장은 MySQL `community_reaction`으로 관리되어야 한다.
- 장애 복구 시 MySQL 원장 기준으로 Redis 카운트를 재구성할 수 있어야 한다.

## [EVAL-001] 별점 평가
- 사용자는 별점 1~5를 선택해 평가할 수 있어야 한다.
- 세부 의견 텍스트를 함께 작성할 수 있어야 한다.

## [INBODY-001] 인바디 입력 경로
- 사용자는 아래 3가지 방법 중 하나로 인바디를 입력할 수 있어야 한다.
1. 수동 입력
2. 인바디 결과지 사진 업로드
3. 인바디 앱/기기 연동 데이터 입력

## [INBODY-002] 수동 입력 필수/선택 항목
- 수동 입력 시 필수 입력 항목을 최소화해야 한다.
- 필수 항목:
1. 성별(`gender`)
2. 키(`height_cm`, cm)
3. 몸무게(`weight_kg`, kg)
4. 골격근량(`skeletal_muscle_mass_kg`, kg)
5. 체지방량(`body_fat_mass_kg`, kg)
6. 기초대사량(`basal_metabolic_rate_kcal`, kcal)
- 선택 항목:
1. 내장지방레벨(`visceral_fat_level`)
2. 부위별 근육량/지방량(`inbody_segment`: 왼팔, 오른팔, 왼다리, 오른다리, 복부)

## [INBODY-003] 입력 검증 및 후속 입력
- 필수 항목 누락 시 저장/분석 요청을 거부하고 누락 항목을 사용자에게 안내해야 한다.
- 사진 업로드 방식은 자동 추출 실패 또는 부정확한 값이 있는 경우 사용자 수정 단계를 제공해야 한다.
- 인바디는 수정보다 신규 업로드 중심으로 관리하며, 기존 측정값 수정 이력(`updated_at`)은 두지 않는다.
- 분석요청 이후에도 사용자는 새로운 측정일의 인바디 데이터를 추가 업로드할 수 있어야 한다.
- 계산값은 저장하지 않고 조회 시 계산해야 한다:
1. 나이(`age`): 회원 `birth_date`와 조회 기준 시점으로 계산
2. 체지방률(`body_fat_percent`): `body_fat_mass_kg / weight_kg * 100`

## [SHOP-000] 구매/장바구니 ERD 보류 메모
- 구매/장바구니 도메인은 세부 정책 미확정으로 현재 ERD 설계를 보류한다.
- 추후 `member`, `program/product`, `order`, `cart` 연관으로 확장한다.
- 확정 필요 항목(TODO):
1. 결제수단/결제상태 정책
2. 주문 상태 전이(생성/결제완료/취소/환불)
3. 환불/취소 가능 조건 및 시점
4. 재고 차감/복구 시점
5. 장바구니 중복/만료 정책
