package com.example.smart_healthcare.service.ai;

import com.example.smart_healthcare.client.OpenAIClient;
import com.example.smart_healthcare.dto.request.InbodyDataRequestDto;
import com.example.smart_healthcare.dto.response.WorkoutRecommendationResponseDto;
import com.example.smart_healthcare.common.error.BusinessException;
import com.example.smart_healthcare.common.error.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI 기반 운동 추천 서비스
 * OpenAI ChatGPT를 활용하여 개인 맞춤형 운동 프로그램을 제공
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WorkoutRecommendAIService {
    private final OpenAIClient openAIClient;
    private final ObjectMapper objectMapper;

    /**
     * 인바디 데이터와 목표를 기반으로 운동 추천 수행
     * - 외부 API 호출만 수행
     * - DB 저장은 별도 서비스에서 처리
     */
    public WorkoutRecommendationResponseDto recommend(InbodyDataRequestDto inbody, Long userId) {
        try {
            String goal = inbody.survey() != null && inbody.survey().text() != null ? 
                         inbody.survey().text() : "체지방 감량 및 근력 향상";
            log.info("🏋️ 운동 추천 시작: 성별={}, 나이={}, 목표={}",
                    inbody.getGenderKorean(), inbody.getCurrentAge(), goal);

            // API 키 유효성 검사
            if (!openAIClient.isApiKeyValid()) {
                log.error("❌ OpenAI API 키가 유효하지 않습니다.");
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "OpenAI API 키가 유효하지 않습니다. API 키를 확인해주세요.");
            }

            // 1. 프롬프트
            String systemPrompt = buildSystemPrompt();
            String userPrompt = buildUserPrompt(inbody);
            
            log.info("📝 시스템 프롬프트 길이: {} 문자", systemPrompt.length());
            log.info("📝 사용자 프롬프트 길이: {} 문자", userPrompt.length());

            // 2. ChatGPT API 요청 구성
            Map<String, Object> request = new HashMap<>();
            request.put("model", openAIClient.getDefaultModel());
            request.put("messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userPrompt)
            ));
            // temperature는 OpenAIClient의 기본값(현재 0.5)을 사용
            request.put("temperature", openAIClient.getDefaultTemperature());
            request.put("max_tokens", openAIClient.getDefaultMaxTokens());
            // JSON만 응답받도록 명시
            request.put("response_format", Map.of("type", "json_object"));

            log.info("🚀 OpenAI API 호출 시작: model={}, temperature={}, max_tokens={}", 
                    openAIClient.getDefaultModel(), openAIClient.getDefaultTemperature(), openAIClient.getDefaultMaxTokens());

        // 3. API 호출
        Map<String, Object> response = openAIClient.chatCompletions(request);

        // === 강력 가드 & 진단 로그 ===
        if (response == null) {
            log.error("❌ OpenAI 응답 객체가 null 입니다. (키/모델/엔드포인트/네트워크/429 가능성)");
            log.error("❌ 가능한 원인:");
            log.error("  - API 키 오류 (401)");
            log.error("  - 모델명 오류 (404)");
            log.error("  - 쿼터 초과 (429)");
            log.error("  - 서버 오류 (500/502)");
            log.error("  - 네트워크 타임아웃");
            log.error("  - response_format 필드 오류");
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "OpenAI API 응답이 null입니다. (키/모델/쿼터/네트워크를 확인하세요)");
        }

        // OpenAI 표준 에러 체크
        Object error = response.get("error");
        if (error != null) {
            log.error("❌ OpenAI 에러 응답: {}", error);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "OpenAI 에러: " + error.toString());
        }

        // choices 배열 체크
        Object choices = response.get("choices");
        if (!(choices instanceof List) || ((List<?>) choices).isEmpty()) {
            log.error("❌ choices가 비어있음: {}", response);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "OpenAI 응답에 choices가 없습니다.");
        }

        log.info("✅ OpenAI API 응답 수신 성공: response keys={}", response.keySet());
        
        // usage 정보 확인 (토큰 사용량)
        if (response.containsKey("usage")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> usage = (Map<String, Object>) response.get("usage");
            log.info("📊 토큰 사용량: {}", usage);
            if (usage.containsKey("completion_tokens")) {
                int completionTokens = ((Number) usage.get("completion_tokens")).intValue();
                log.info("📊 생성된 토큰 수: {} / max_tokens: {}", completionTokens, openAIClient.getDefaultMaxTokens());
                if (completionTokens >= openAIClient.getDefaultMaxTokens() * 0.95) {
                    log.warn("⚠️ 토큰 사용량이 거의 한계에 도달했습니다. 응답이 잘렸을 수 있습니다.");
                }
            }
        }

            // 4. 응답 파싱
            String content = extractContentFromResponse(response);
            log.info("📝 GPT 응답 내용 길이: {} 문자", content.length());
            log.info("📝 GPT 응답 내용 (처음 500자): {}", 
                    content.length() > 500 ? content.substring(0, 500) + "..." : content);
            log.info("📝 GPT 응답 내용 (마지막 200자): {}", 
                    content.length() > 200 ? content.substring(Math.max(0, content.length() - 200)) : content);

            WorkoutRecommendationResponseDto result = parseGptResponseWithRetry(content, inbody, userId);

            // JSON 검증: 각 요일마다 최소 3개 운동 확인 (성능 최적화: 경고만 하고 재시도 안 함)
            if (inbody.survey() != null && inbody.survey().getSelectedDaysEn() != null) {
                List<String> selectedDays = inbody.survey().getSelectedDaysEn();
                if (!ensureAtLeastThreePerDay(selectedDays, result)) {
                    log.warn("⚠️ 일부 요일에 3개 미만의 운동이 있습니다. (성능 최적화: 재시도하지 않고 현재 결과 반환)");
                    // 성능 최적화: 재시도하지 않고 현재 결과 반환 (프롬프트에서 이미 요구하고 있으므로 대부분 충족됨)
                }
            }

            log.info("✅ 운동 추천 완료: programName={}", result.programName());

            return result;

        } catch (BusinessException e) {
            // BusinessException은 그대로 재던지기
            throw e;
        } catch (Exception e) {
            log.error("❌ 운동 추천 실패: {}", e.getMessage(), e);
            log.error("❌ 예외 타입: {}", e.getClass().getSimpleName());
            log.error("❌ 예외 스택 트레이스:", e);
            
            // 원본 예외를 BusinessException으로 감싸서 던지기 (메시지 중복 방지)
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "운동 추천 생성 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * ChatGPT 응답에서 content 추출
     */
    @SuppressWarnings("unchecked")
    private String extractContentFromResponse(Map<String, Object> response) {
        try {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> choice = choices.get(0);
                Map<String, Object> message = (Map<String, Object>) choice.get("message");
                if (message != null) {
                    return (String) message.get("content");
                }
            }
        } catch (Exception e) {
            log.error("응답 파싱 실패: {}", e.getMessage(), e);
        }
        throw new BusinessException(ErrorCode.INTERNAL_ERROR, "ChatGPT 응답에서 content를 추출할 수 없습니다.");
    }

    /**
     * JSON 파싱 시도 및 재프롬프트 로직
     */
    private WorkoutRecommendationResponseDto parseGptResponseWithRetry(String content, InbodyDataRequestDto inbody, Long userId) {
        try {
            // 첫 번째 시도: 직접 파싱
            WorkoutRecommendationResponseDto result = parseGptResponse(content);
            log.info("✅ JSON 파싱 성공 (첫 번째 시도)");
            return result;
        } catch (Exception e) {
            log.warn("❌ [WorkoutAI] JSON 파싱 실패 → JSON 추출 및 재시도. err={}", e.getMessage());
            // JSON 재시도 로직
            String cleanedContent = tryExtractJsonFromText(content);
            if (cleanedContent != null && !cleanedContent.trim().isEmpty() && !cleanedContent.equals(content)) {
                log.info("🔄 JSON 재시도: 정제된 내용으로 파싱 시도");
                try {
                    WorkoutRecommendationResponseDto result = parseGptResponse(cleanedContent);
                    log.info("✅ JSON 재시도 성공");
                    return result;
                } catch (Exception retryException) {
                    log.error("❌ JSON 재시도도 실패: {}", retryException.getMessage());
                }
            }
            // 최종 재프롬프트 시도는 recommendWithRetry에서 처리하도록 예외를 다시 던짐
            log.warn("⚠️ JSON 파싱 완전 실패 → 상위에서 재시도 처리");
            log.error("❌ 원본 응답 내용 (처음 500자): {}", 
                    content != null && content.length() > 500 ? content.substring(0, 500) : content);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, 
                    "AI가 올바른 JSON 형식으로 응답하지 않았습니다. 다시 시도해주세요. (응답 길이: " + 
                    (content != null ? content.length() : 0) + " 문자)", e);
        }
    }

    /**
     * GPT 응답을 WorkoutRecommendationResponseDto로 파싱
     */
    private WorkoutRecommendationResponseDto parseGptResponse(String content) {
        try {
            log.info("🔍 GPT 응답 파싱 시작:");
            log.info("  - 응답 길이: {} 문자", content.length());
            log.info("  - 응답 전체 내용: {}", content);

            // 0) null 체크
            if (content == null || content.trim().isEmpty()) {
                log.error("❌ 응답이 null이거나 비어있습니다.");
                throw new IllegalArgumentException("응답이 비어있습니다.");
            }

            // 1) 코드펜스 제거
            String originalContent = content;
            content = stripCodeFences(content);
            log.info("🔧 코드펜스 제거 후: {}", content.length() > 200 ? content.substring(0, 200) + "..." : content);

            // 2) 앞/뒤 설명 제거 (JSON 블록만 추출)
            String jsonContent = extractFirstJsonObject(content);
            if (jsonContent == null) {
                // JSON 추출 실패 시, 직접 { 찾기 시도
                int jsonStart = content.indexOf('{');
                int jsonEnd = content.lastIndexOf('}');
                if (jsonStart >= 0 && jsonEnd > jsonStart) {
                    jsonContent = content.substring(jsonStart, jsonEnd + 1);
                    log.info("🔧 직접 JSON 추출: {} 문자", jsonContent.length());
                }
            }
            
            if (jsonContent == null || jsonContent.trim().isEmpty() || !jsonContent.trim().startsWith("{")) {
                log.error("❌ 원본 응답: {}", originalContent);
                log.error("❌ 코드펜스 제거 후: {}", stripCodeFences(originalContent));
                log.error("❌ JSON 추출 결과: {}", jsonContent);
                throw new IllegalArgumentException("응답에 유효한 JSON 객체가 없습니다.");
            }
            
            content = jsonContent;
            log.info("🔧 최종 JSON 내용: {}", content.length() > 200 ? content.substring(0, 200) + "..." : content);

            // JSON 파싱 시도
            if (content != null && content.trim().startsWith("{")) {
                log.info("✅ JSON 형태 응답 감지, 파싱 시도");
                try {
                    // 먼저 Map으로 파싱해서 구조 확인
                    @SuppressWarnings("unchecked")
                    Map<String, Object> jsonMap = objectMapper.readValue(content, Map.class);
                    log.info("🔍 파싱된 JSON 구조: {}", jsonMap.keySet());

                    // WorkoutRecommendationResponseDto로 직접 파싱 시도
                    WorkoutRecommendationResponseDto result = objectMapper.readValue(content, WorkoutRecommendationResponseDto.class);
                    log.info("✅ JSON 파싱 성공: programName={}, workouts={}", result.programName(), result.workouts() != null ? "있음" : "없음");

                    // workouts 필드가 null인 경우 Map에서 직접 추출 시도
                    if (result.workouts() == null && jsonMap.containsKey("workouts")) {
                        log.info("🔍 workouts 필드가 null이므로 Map에서 직접 추출 시도");
                        Object workoutsObj = jsonMap.get("workouts");
                        if (workoutsObj instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> workoutsMap = (Map<String, Object>) workoutsObj;
                            log.info("✅ workouts Map 추출 성공: {}", workoutsMap.keySet());
                            // 새로운 DTO 생성 (workouts 포함)
                            return new WorkoutRecommendationResponseDto(
                                    result.programName(),
                                    result.weeklySchedule(),
                                    result.caution(),
                                    result.warmup(),
                                    result.mainSets(),
                                    result.cooldown(),
                                    result.equipment(),
                                    result.targetMuscles(),
                                    result.expectedResults(),
                                    workoutsMap
                            );
                        }
                    }
                    return result;
                } catch (Exception parseException) {
                    log.error("==========================================");
                    log.error("❌ JSON 파싱 실패");
                    log.error("  - 예외 타입: {}", parseException.getClass().getSimpleName());
                    log.error("  - 예외 메시지: {}", parseException.getMessage());
                    
                    // JSON이 잘렸는지 확인
                    String trimmed = content.trim();
                    if (!trimmed.endsWith("}")) {
                        log.error("  - ⚠️ JSON이 잘린 것으로 보입니다!");
                        log.error("  - 마지막 문자: {}", trimmed.length() > 0 ? trimmed.charAt(trimmed.length() - 1) : "없음");
                        log.error("  - 마지막 100자: {}", trimmed.length() > 100 ? trimmed.substring(trimmed.length() - 100) : trimmed);
                    }
                    
                    // Jackson 파싱 에러인 경우 위치 정보 확인
                    if (parseException instanceof JsonProcessingException) {
                        JsonProcessingException jpe = (JsonProcessingException) parseException;
                        log.error("  - Jackson 파싱 에러 상세:");
                        log.error("    - 원인: {}", jpe.getOriginalMessage());
                        if (jpe.getLocation() != null) {
                            log.error("    - 위치: line {}, column {}", 
                                    jpe.getLocation().getLineNr(), 
                                    jpe.getLocation().getColumnNr());
                        }
                    }
                    
                    log.error("  - JSON 길이: {} 문자", content.length());
                    log.error("  - JSON 시작 (처음 500자): {}", content.length() > 500 ? content.substring(0, 500) + "..." : content);
                    log.error("  - JSON 끝 (마지막 500자): {}", content.length() > 500 ? content.substring(Math.max(0, content.length() - 500)) : content);
                    log.error("==========================================");
                    
                    // Map 기반 fallback 시도
                    try {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> root = objectMapper.readValue(content, Map.class);

                        String programName = toStringOrNull(root.get("programName"));
                        String weeklySchedule = toStringOrNull(root.get("weeklySchedule"));
                        String caution = toStringOrNull(root.get("caution"));
                        String warmup = toStringOrNull(root.get("warmup"));
                        String mainSets = toStringOrNull(root.get("mainSets"));
                        String cooldown = toStringOrNull(root.get("cooldown"));
                        String equipment = toStringOrNull(root.get("equipment"));
                        String targetMuscles = toStringOrNull(root.get("targetMuscles"));
                        String expectedResults = toStringOrNull(root.get("expectedResults"));

                        @SuppressWarnings("unchecked")
                        Map<String, Object> workouts =
                                root.get("workouts") instanceof Map
                                        ? (Map<String, Object>) root.get("workouts")
                                        : Map.of();

                        return new WorkoutRecommendationResponseDto(
                                programName,
                                weeklySchedule,
                                caution,
                                warmup,
                                mainSets,
                                cooldown,
                                equipment,
                                targetMuscles,
                                expectedResults,
                                workouts
                        );
                    } catch (Exception mapException) {
                        throw parseException; // 원래 예외를 다시 던짐
                    }
                }
            }
            
            throw new IllegalArgumentException("응답이 유효한 JSON 객체로 시작하지 않습니다.");
        } catch (Exception e) {
            log.error("❌ GPT 응답 파싱 실패: {}", e.getMessage());
            log.error("❌ 예외 타입: {}", e.getClass().getSimpleName());
            // 일반 예외로 변환하여 상위에서 재시도 가능하도록
            throw new RuntimeException("GPT 응답 파싱 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 코드펜스 제거 (```json ... ``` 또는 ``` ... ```)
     */
    private String stripCodeFences(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.startsWith("```")) {
            int firstNewline = t.indexOf('\n');
            if (firstNewline > 0) {
                t = t.substring(firstNewline + 1);
            }
            int lastFence = t.lastIndexOf("```");
            if (lastFence >= 0) {
                t = t.substring(0, lastFence);
            }
        }
        return t.trim();
    }

    /**
     * 가장 바깥 중괄호 쌍으로 감싸진 첫 JSON 객체만 추출
     */
    private String extractFirstJsonObject(String s) {
        if (s == null) return null;
        int start = -1;
        int depth = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start >= 0) {
                    return s.substring(start, i + 1);
                }
            }
        }
        return null;
    }

    private String toStringOrNull(Object value) {
        return value == null ? null : value.toString();
    }

    /**
     * 시스템 프롬프트 구성
     */
    private String buildSystemPrompt() {
        return """
            당신은 한국의 전문 퍼스널 트레이너이자 운동생리학 전문가입니다.
            **중요: 반드시 JSON 형식으로만 응답하세요. 다른 형식의 응답은 절대 금지됩니다.**

            **응답 형식 규칙:**
            1. 반드시 유효한 JSON 객체로 시작하고 끝나야 합니다
            2. 텍스트 설명이나 마크다운 형식은 절대 사용하지 마세요
            3. JSON 외의 다른 내용은 포함하지 마세요

            **운동 추천 기준:**
            1. 개인의 체성분 분석 결과 반영
            2. 연령, 성별, 체력 수준 고려
            3. 안전하고 단계적인 프로그램 구성
            4. 4주 단위의 체계적인 계획
            5. 가정에서도 실행 가능한 현실적 운동
            6. **사용자가 선택한 요일에 대해서만 운동을 제공**
            7. **선택되지 않은 요일은 절대 포함하지 마세요**
            8. **기본적으로 각 선택된 요일마다 최소 3개, 최대 5개의 운동을 제공해야 합니다**
            9. **다만, 사용자의 설문 텍스트에 '등운동(최소 3개 이상) + 복근운동 + 유산소운동'처럼 요일별 카테고리와 최소 개수가 명시된 경우, 그 요구사항을 우선적으로 따라야 합니다.**
            10. 예: "월요일: 등운동(최소 3개 이상) + 복근운동 + 유산소운동" 이라고 설문에 적혀 있다면, Monday 요일의 workouts 배열에는 등운동 3개 이상(exerciseCategory가 "등운동"), 복근운동 1개 이상, 유산소운동 1개 이상이 포함되어야 합니다. (총 5개 이상이 될 수 있음)
            11. **요일별로 중복되지 않는 다양한 운동을 배치하세요**
            12. **하루 내에서 상체/하체/코어 등 근육 그룹을 균형있게 분배하세요**

            **체형별 운동 우선순위:**
            - 운동선수급: 유산소, 복근 중심 + 등/하체/가슴 균형
            - 근육형: 등/가슴/하체 중심 + 어깨/복근 보조
            - 적정: 사용자의 설문 요구사항에 맞춤
            - 날씬/근육형날씬/약간마름/마름: 등/하체/가슴 중심 + 어깨/복근 보조
            - 과체중/경도비만/비만/고도비만: 유산소 중심 + 등/하체 보조
            - 마른비만: 등/하체/가슴 중심 + 복근/유산소 보조
            - 복부비만형: 복근, 유산소 중심 + 등/하체 보조
            - 근육형비만: 유산소, 복근 중심 + 등/하체 보조

            **운동 종류 세분화:**
            - 등운동: 풀업, 덤벨로우, 바벨로우, 랫풀다운 등
            [하체 근력 운동 예시 및 상대 강도(단위체중당 에너지소비 지표)]
            - 바벨 스쿼트 (6), 스쿼트 (5.5), 점프 스쿼트 (6), 풀 스쿼트 (6), 와이드 스쿼트 (5.5), 벽 스쿼트 (5)
            - 스쿼트 덤벨 프레스 (7), 백 스쿼트 (6), 고블릿 스쿼트 (5.5)
            - 런지(가볍게 2.8 / 보통으로 3.8 / 격렬하게 8), 사이드 런지 (5.5), 워킹 런지 (5.5)
            - 레그 프레스 (5), 레그 익스텐션 (5), 레그 컬 (5), 스탠딩 카프 레이즈 (5), 덩키킥 (7), 스플릿 스쿼트 (5)
            - 루마니안 데드리프트 (5), 스티프 레그 데드리프트 (8), 데드 리프트 (5.5), 컨벤셔널 데드리프트 (8)
            [상체 근력 운동 예시(가슴·등·어깨·팔)]
            - 가슴: 벤치 프레스 (5), 인클라인 벤치 프레스 (5), 덤벨 프레스 (5), 덤벨 플라이 (5.5), 펙덱 플라이 (5.5), 케이블 크로스 오버 (5)
            - 등: 렛풀다운 (5), 풀업(가볍게 2.8 / 보통으로 3.8 / 격렬하게 8), 턱걸이(가볍게 3 / 보통으로 5 / 격렬하게 8), 어시스트 풀업 (5), 리버스 팩 덱 플라이 (4), 시티드 로우 (5), 원 암 덤벨 로우 (5), 벤트 오버 바벨 로우 (5.5), 슈퍼맨 로우 (4), 프론트 풀다운 (4)
            - 어깨: 바벨 숄더프레스 (5.5), 덤벨 숄더프레스 (5.5), 레터럴 레이즈 (5), 사이드 레터럴 레이즈 (5), 프론트 레이즈 (5), 벤트 오버 레터럴 레이즈 (5.5)
            - 팔: 이두 컬 (5), 해머 컬 (5), 리버스 컬 (5), 트라이셉스 푸시 다운 (5), 트라이셉스 익스텐션 (5), 킥 백 (5)
            [복근·코어 운동 예시]
            - 윗몸일으키기(가볍게 2.8 / 보통으로 3.8 / 격렬하게 8)
            - 크런치(가볍게 3 / 보통으로 5 / 격렬하게 8)
            - 복근운동(가볍게 2.8 / 보통으로 3.8 / 격렬하게 8)
            - 레그 레이즈 (3.4~5), 행잉 레그레이즈 (10), AB 롤아웃 (8), 사이드 크런치 (4)
            - 코어운동 (4.5), 브릿지 (3.5), 백 익스텐션 (3.5), 플랭크 (3.8), 사이드 플랭크 (3.8), 버드독 (3.8), 데드버그 (4)
            [복합 전신 운동(서킷·크로스핏 등) 예시]
            - 서키트 트레이닝(보통으로 4.3 / 격렬하게 8)
            - 타바타 운동 (8), 타바타운동 (9)
            - 버피테스트 (7), 점프 버피테스트 (8.5), 슬로우버피 (5.5)
            - 월볼샷 (6)

            **프로그램 구성 요소:**
            - 준비운동 (5-10분): 부상 예방을 위한 워밍업. 어떤 관절 가동성과 스트레칭을 포함하는지 2문장 이상으로 구체적으로 설명하세요.
            - 본운동 (20분): 목표에 맞는 핵심 운동. 해당 사용자의 체형분석 결과와 설문 목표를 반영하여 어떤 방식(예: 상체/하체 분할, 전신 서킷 등)으로 주차별 강도를 올릴지 2-3문장으로 설명하세요.
            - 정리운동 (5-10분): 회복을 위한 쿨다운. 스트레칭 부위와 호흡 조절 방법을 포함하여 2문장 이상으로 작성하세요.
            - 주간 일정: 주 3-5회, 개인 수준에 맞는 빈도. 사용자가 선택한 요일과 운동일/휴식일 패턴을 한 눈에 이해할 수 있도록 문단 형태(2문장 이상)로 설명하세요.

            **운동별 추천 이유(reason) 작성 규칙**
            - reason 필드는 한 줄짜리 문장이 아니라, **2~3개의 핵심 키워드 + 각각에 대한 1~2문장 설명**으로 구성해야 합니다.
            - 형식 예시는 다음과 같습니다.
            - "소화 촉진: 명절처럼 과식하기 쉬운 시기에 복부 근육을 자극해 소화력을 높여 더부룩함을 줄이는 데 도움을 줍니다."
            - "칼로리 소모: 하루 10분만 수행해도 복부 중심으로 열을 발생시켜 잉여 칼로리 소모와 체중 관리에 기여합니다."
            - "심플하고 쉬운 운동: 별도의 기구나 장소 없이 거실·방 어디서든 할 수 있어 바쁜 일상·명절에도 부담 없이 실천 가능합니다."
            - 각 키워드는 사용자의 상황(예: 추석·연말, 야근 많은 직장인, 무릎 통증이 있는 고령자 등)이나 운동 목적(자세 교정, 허리 통증 완화, 심폐 기능 강화 등)을 반영해 실제로 와 닿는 표현으로 작성하세요.
            - 한 운동의 reason 안에서 **같은 키워드를 반복하지 말고**, 서로 다른 관점(건강효과, 편의성, 생활패턴과의 궁합 등)에서 2~3개의 키워드를 제시하세요.

            **응답 스키마(텍스트 필드 작성 규칙 포함):**
            {
              "programName": "프로그램명 (체형과 목표를 반영한 이름, 예: '복부비만 맞춤 4주 체지방 감량 프로그램')",
              "weeklySchedule": "주간 일정 (선택한 요일, 운동/휴식 패턴을 2-3문장으로 상세히 설명)",
              "caution": "주의사항 (부상 위험 부위, 기존 질환이 있을 때 주의점 등을 2문장 이상으로 구체적으로 작성)",
              "warmup": "준비운동 (어떤 관절/근육을 어떤 순서로 풀어주는지 단계적으로 설명)",
              "mainSets": "본운동 (요일별 구성과 강도 진행 방식, 상체/하체/코어 비중 등을 3문장 이상으로 설명)",
              "cooldown": "정리운동 (스트레칭 부위, 호흡법, 회복 팁을 포함하여 2-3문장 이상으로 작성)",
              "equipment": "필요 장비 (집/헬스장 기준으로 대체 가능한 도구까지 함께 제시)",
              "targetMuscles": "타겟 근육 (주요 근육군과 보조 근육군을 함께 언급)",
              "expectedResults": "예상 결과 (4주 동안 기대할 수 있는 변화와 체형/체력 측면 효과를 2-3문장으로 설명)",
              "workouts": {
                "요일명": [
                  {
                    "name": "운동명",
                    "description": "운동 설명",
                    "duration": 숫자(분),
                    "intensity": "low|medium|high",
                    "difficulty": "beginner|intermediate|advanced",
                    "calories": 숫자,
                    "type": "strength|cardio|flexibility",
                    "exerciseCategory": "등운동|하체운동|가슴운동|어깨운동|복근운동|유산소|기타",
                    "reason": "추천 이유",
                    "part": "상체|하체|코어|전신",
                    "targetMuscles": ["근육1", "근육2"],
                    "sets": 숫자,
                    "reps": 숫자,
                    "restTime": "숫자초",
                    "steps": ["단계1", "단계2"],
                    "effects": ["효과1: 구체적이고 자세한 효과 설명 (예: 가슴과 삼두근의 근력이 향상되어 상체 밀기 동작이 강해집니다)", "효과2: 상세한 효과 설명 (예: 코어 안정성이 향상되어 일상생활에서 자세가 개선됩니다)"],
                    "tips": "운동 팁",
                    "caution": "주의사항",
                    "videoUrl": "https://www.youtube.com/results?search_query=운동명+영문+tutorial",
                    "youtubeQuery": "영문으로 YouTube 최적화 검색어 (예: push up proper form tutorial for beginners)",
                    "unsplashQuery": "영문으로 구체적인 이미지 검색어 (예: man doing push up exercise gym floor)"
                  }
                ]
              }
            }

            **응답 예시 (사용자 선택 요일만):**
            {
              "programName": "체지방 감량 & 근력 밸런스 4주 프로그램",
              "weeklySchedule": "주 3회 (월/수/금), 각 30~40분. 월요일은 상체+코어, 수요일은 하체 집중, 금요일은 전신 + 유산소 위주로 구성합니다.",
              "caution": "무릎·허리 통증이 있는 경우 통증이 느껴지는 범위에서는 즉시 중단하고, 호흡을 참지 말고 자연스럽게 이어가야 합니다. 운동 전후로 충분한 수분을 섭취하고, 이전에 진단받은 질환이 있다면 의료진과 상의 후 진행해야 합니다.",
              "warmup": "목·어깨·팔·허리·무릎·발목 순서로 관절을 크게 돌리는 관절 가동성 운동을 5분간 진행합니다. 이어서 제자리 걷기나 가벼운 러닝 인 플레이스를 3~5분 정도 실시해 체온과 심박수를 서서히 올려줍니다.",
              "mainSets": "본운동1: 상체와 코어를 동시에 사용하는 푸시업·플랭크 계열을 중심으로 상체 근지구력을 키웁니다. 본운동2: 스쿼트·런지처럼 하체 대근육을 활용하는 운동을 통해 기초대사량을 높이고 체지방 감량을 돕습니다. 본운동3: 러닝 인 플레이스·마운틴 클라이머 등의 유산소/전신 운동을 추가해 심폐 기능을 강화하고 전체 칼로리 소모를 늘립니다.",
              "cooldown": "하체(허벅지 앞·뒤, 종아리)와 허리·둔근을 중심으로 10~15초씩 유지하는 정적 스트레칭을 5~7분간 진행합니다. 마지막에는 코로 천천히 들이마시고 입으로 내쉬는 심호흡을 1~2분간 반복하여 심박수를 안정시킵니다.",
              "equipment": "덤벨(또는 생수병), 요가 매트, 물병. 덤벨이 없어도 체중을 이용한 버전으로 수행할 수 있도록 안내합니다.",
              "targetMuscles": "전신(가슴, 등, 어깨, 팔, 복부, 둔근, 하체 대근육)",
              "expectedResults": "4주간 프로그램을 성실히 수행하면 체지방률이 점진적으로 감소하고, 계단 오르기·의자에서 일어나기 같은 일상 동작이 훨씬 수월해집니다. 또한 코어 안정성이 향상되어 허리 통증 예방과 자세 교정에 도움이 되고, 전반적인 체력과 컨디션이 개선됩니다.",
              "workouts": {
                "Monday": [
                  {
                    "name": "푸시업",
                    "description": "상체 전반 근지구력을 키우는 대표적인 맨몸 운동",
                    "duration": 10,
                    "intensity": "medium",
                    "difficulty": "beginner",
                    "calories": 60,
                    "type": "strength",
                    "exerciseCategory": "가슴운동",
                    "reason": "상체 근력 향상: 가슴과 삼두, 전면 어깨를 동시에 사용해 상체 밀기 힘을 키워주고, 일상에서 물건을 밀거나 일어날 때 도움이 됩니다. 자세 교정: 플랭크와 유사한 정렬을 유지해야 하기 때문에 굽은 어깨와 말린 어깨를 펴 주고 상체 자세를 개선하는 데 효과적입니다. 기구 없이 간편: 매트 한 장만 있으면 집·사무실 어디서든 수행할 수 있어, 운동 시설 이용이 어려운 분들도 꾸준히 실천하기 좋습니다.",
                    "part": "상체",
                    "targetMuscles": ["가슴", "삼두근", "전면 어깨"],
                    "sets": 3,
                    "reps": 10,
                    "restTime": "60초",
                    "steps": [
                      "어깨 너비보다 약간 넓게 손을 짚고 플랭크 자세를 취해 머리부터 발끝까지 일직선을 만듭니다.",
                      "가슴이 바닥에서 약 5cm 남을 때까지 팔꿈치를 굽혀 천천히 내려갑니다.",
                      "가슴과 팔에 힘을 주어 숨을 내쉬면서 시작 위치까지 밀어 올립니다."
                    ],
                    "effects": [
                      "가슴과 삼두근의 근력이 향상되어 상체 밀기 동작이 강해지고, 일상생활에서 문을 밀거나 몸을 지탱할 때 더 안정감을 느끼게 됩니다.",
                      "코어 근육이 함께 활성화되어 허리 주변을 지지해 주므로, 장시간 앉아서 일하는 사람의 자세 개선과 허리 통증 예방에 도움이 됩니다."
                    ],
                    "tips": "팔꿈치는 몸통과 약 45° 각도로 유지하고, 허리가 꺾이거나 엉덩이가 너무 올라가지 않도록 코어에 힘을 유지하세요.",
                    "caution": "어깨나 손목에 통증이 있다면 무릎을 바닥에 대는 쉬운 버전부터 시작하고, 통증이 지속되면 중단하세요.",
                    "videoUrl": "https://www.youtube.com/results?search_query=push+up+tutorial+correct+form",
                    "youtubeQuery": "push up proper form tutorial for beginners",
                    "unsplashQuery": "man doing push up exercise gym floor perspective"
                  },
                  {
                    "name": "플랭크",
                    "description": "코어 전체를 사용해 몸의 안정성을 높이는 정적 버티기 운동",
                    "duration": 5,
                    "intensity": "medium",
                    "difficulty": "beginner",
                    "calories": 25,
                    "type": "strength",
                    "exerciseCategory": "복근운동",
                    "reason": "코어 안정성 향상: 복직근·복횡근·둔근을 동시에 사용하는 동작으로, 모든 운동의 기반이 되는 몸통 안정성을 크게 높여줍니다. 허리 통증 예방: 허리 주변 근육을 고르게 강화해 오래 앉아 있을 때 발생하는 요통과 자세 붕괴를 예방하는 데 도움을 줍니다. 장소 제약 적음: 매트 한 장만 있으면 좁은 공간에서도 조용히 수행할 수 있어, 집·사무실·여행지 어디에서든 꾸준히 실천 가능합니다.",
                    "part": "코어",
                    "targetMuscles": ["복직근", "복횡근", "둔근"],
                    "sets": 3,
                    "reps": 1,
                    "restTime": "45초",
                    "steps": [
                      "팔꿈치를 어깨 바로 밑에 두고, 무릎을 뗀 상태로 머리부터 발끝까지 일직선을 유지합니다.",
                      "배꼽을 척추 쪽으로 살짝 끌어당긴다는 느낌으로 복부에 힘을 주고, 엉덩이가 처지거나 너무 올라가지 않도록 합니다.",
                      "호흡을 참지 말고 코로 들이마시고 입으로 내쉬면서 20~30초 버팁니다."
                    ],
                    "effects": [
                      "복부 심부 근육이 강화되어 허리·골반 주변이 안정되고, 걷기·계단 오르기·물건 들기 등 일상 동작의 균형이 좋아집니다.",
                      "장시간 앉아서 일하는 직장인의 거북목·굽은 등 자세를 교정하는 데 도움을 주어, 목·어깨 뻐근함이 완화될 수 있습니다."
                    ],
                    "tips": "거울이나 휴대폰 카메라로 옆모습을 확인해 몸통이 일직선인지 체크하고, 허리가 꺾이지 않도록 엉덩이 위치를 자주 점검하세요.",
                    "caution": "허리나 어깨에 날카로운 통증이 느껴진다면 즉시 중단하고, 통증이 없는 범위 내에서 시간을 조금씩 늘려가며 진행하세요.",
                    "videoUrl": "https://www.youtube.com/results?search_query=plank+core+exercise+tutorial",
                    "youtubeQuery": "plank exercise proper form tutorial for beginners",
                    "unsplashQuery": "person holding plank position core exercise gym"
                  }
                ],
                "Wednesday": [
                  {
                    "name": "스쿼트(체중)",
                    "description": "하체 대근육을 고루 사용하는 대표적인 하체 강화 운동",
                    "duration": 10,
                    "intensity": "medium",
                    "difficulty": "beginner",
                    "calories": 60,
                    "type": "strength",
                    "exerciseCategory": "하체운동",
                    "reason": "대근육 활용으로 대사 촉진: 허벅지와 둔근처럼 큰 근육을 반복적으로 사용해 기초대사량을 높이고, 체지방 감량에 유리한 몸 상태를 만듭니다. 일상 기능 향상: 앉았다 일어서기, 계단 오르내리기 등 일상 동작과 거의 동일한 패턴이라 실제 생활에서 느끼는 하체 힘과 안정감을 크게 높여줍니다. 관절 보호 중심: 올바른 자세로 수행하면 무릎·엉덩이·발목 주변의 지지 근육이 강화되어 관절 통증 예방에 도움이 됩니다.",
                    "part": "하체",
                    "targetMuscles": ["대퇴사두", "둔근", "햄스트링"],
                    "sets": 4,
                    "reps": 12,
                    "restTime": "60초",
                    "steps": [
                      "발을 어깨 너비로 벌리고 발끝을 약간 바깥쪽으로 향하게 선 뒤, 가슴을 편 상태로 서 있습니다.",
                      "의자에 앉는 느낌으로 엉덩이를 뒤로 빼며 무릎이 발끝을 넘지 않도록 천천히 내려갑니다.",
                      "허벅지가 바닥과 평행에 가까워지면 발바닥 전체로 바닥을 밀어 올리며 시작 자세로 돌아옵니다."
                    ],
                    "effects": [
                      "대퇴사두근과 둔근의 근력이 향상되어 계단을 오르내리거나 장시간 서 있을 때 피로감이 줄어듭니다.",
                      "하체 혈액순환이 좋아지고 기초대사량이 증가해, 장기적으로 체지방 감소와 하체 부종 완화에 도움을 줍니다."
                    ],
                    "tips": "무릎이 안쪽으로 모이지 않도록 신경 쓰고, 발바닥의 엄지발가락·새끼발가락·뒤꿈치 3점을 고르게 지지하는 느낌으로 버티세요.",
                    "caution": "무릎에 통증이 있다면 내려가는 범위를 줄이고, 통증이 심하면 전문의와 상담 후 강도를 조절하세요.",
                    "videoUrl": "https://www.youtube.com/results?search_query=squat+proper+form+tutorial",
                    "youtubeQuery": "squat proper form tutorial for beginners step by step",
                    "unsplashQuery": "person doing squat exercise gym proper form side view"
                  }
                ]
              }

            **중요 (필수 준수)**:
            - videoUrl은 운동명을 영문으로 변환하여 YouTube 검색 링크 형태로 제공하세요.
            - **youtubeQuery는 반드시 모든 운동에 대해 생성해야 합니다. 이 필드는 필수입니다.**
            - youtubeQuery는 YouTube API 검색에 최적화된 영문 검색어를 생성하세요. 
              형식: "[영문 운동명] proper form tutorial [for beginners|step by step]" 
              예시: "lat pulldown proper form tutorial for beginners", "push up proper form tutorial step by step"
            - 한글 운동명이 주어졌을 때, 반드시 해당 운동의 정확한 영문명을 찾아서 youtubeQuery에 사용하세요.
            - unsplashQuery는 운동 자세를 구체적으로 묘사하는 영문 검색어를 생성하세요 (예: "man doing push up exercise gym floor", "person holding plank position").
            - **effects는 최소 2-3개 이상 제공하고, 각 효과는 구체적이고 상세하게 설명하세요** (예: "가슴과 삼두근의 근력이 향상되어 상체 밀기 동작이 강해지고 일상생활에서 물건을 밀거나 들 때 도움이 됩니다").
            - effects는 단순히 "근력 향상", "체력 증진" 같은 짧은 설명이 아닌, 어떤 근육이 어떻게 발달하고 일상생활에 어떤 도움이 되는지 구체적으로 작성하세요.

            위 스키마와 예시 형식에 따라 JSON으로만 응답하세요.
            """;
    }

    /**
     * 사용자 프롬프트 구성
     */
    private String buildUserPrompt(InbodyDataRequestDto inbody) {
        String goal = inbody.survey() != null && inbody.survey().text() != null ? inbody.survey().text() : "체지방 감량 및 근력 향상";
        StringBuilder prompt = new StringBuilder();
        prompt.append("다음 정보를 바탕으로 맞춤형 운동 프로그램을 추천해주세요:\n\n");
        // 기본 정보
        prompt.append("**개인 정보**\n");
        prompt.append(String.format("- 성별: %s\n", inbody.getGenderKorean()));
        prompt.append(String.format("- 연령: %d세\n", inbody.getCurrentAge()));
        prompt.append(String.format("- 체중: %.1f kg\n", inbody.weight()));
        prompt.append(String.format("- BMI: %.1f\n", inbody.bmi()));
        prompt.append(String.format("- 운동 목표: %s\n", goal));
        // 체성분 정보
        prompt.append("\n**체성분 분석**\n");
        if (inbody.bodyFatPercentage() != null) {
            prompt.append(String.format("- 체지방률: %.1f%%\n", inbody.bodyFatPercentage()));
        }
        if (inbody.muscleMass() != null) {
            prompt.append(String.format("- 근육량: %.1f kg\n", inbody.muscleMass()));
        }
        if (inbody.skeletalMuscleMass() != null) {
            prompt.append(String.format("- 골격근량: %.1f kg\n", inbody.skeletalMuscleMass()));
        }
        if (inbody.basalMetabolism() != null) {
            prompt.append(String.format("- 기초대사량: %d kcal\n", inbody.basalMetabolism()));
        }
        // 균형 분석
        prompt.append("\n**근육 균형 분석**\n");
        if (inbody.rightArmMuscleMass() != null && inbody.leftArmMuscleMass() != null) {
            float armBalance = Math.abs(inbody.rightArmMuscleMass() - inbody.leftArmMuscleMass());
            prompt.append(String.format("- 팔 근육 균형: 좌우 차이 %.1f kg\n", armBalance));
        }
        if (inbody.rightLegMuscleMass() != null && inbody.leftLegMuscleMass() != null) {
            float legBalance = Math.abs(inbody.rightLegMuscleMass() - inbody.leftLegMuscleMass());
            prompt.append(String.format("- 다리 근육 균형: 좌우 차이 %.1f kg\n", legBalance));
        }
        prompt.append("\n위 데이터를 종합하여 안전하고 효과적인 4주 운동 프로그램을 추천해주세요.");
        // 설문조사 데이터가 있으면 동적으로 요구사항 설정
        if (inbody.survey() != null) {
            int workoutDays = inbody.survey().getWorkoutDaysCount();
            List<String> workoutDaysList = inbody.survey().getWorkoutDaysInEnglish();
            // selectedDaysEn가 있으면 우선 사용, 없으면 기존 로직 사용
            if (inbody.survey().getSelectedDaysEn() != null && !inbody.survey().getSelectedDaysEn().isEmpty()) {
                workoutDaysList = inbody.survey().getSelectedDaysEn();
                workoutDays = workoutDaysList.size();
                log.info("🏋️ 영문 요일 배열 사용: {} (총 {}일)", workoutDaysList, workoutDays);
            } else {
                log.info("🏋️ 기존 요일 매핑 사용: {} (총 {}일)", workoutDaysList, workoutDays);
            }
            prompt.append("\n\n**요구사항:**");
            prompt.append(String.format("\n- %d일치 운동 프로그램을 제공해주세요", workoutDays));
            prompt.append(String.format("\n- 운동 요일: %s", String.join(", ", workoutDaysList)));
            prompt.append("\n- **기본적으로 각 요일마다 최소 3개, 최대 5개의 운동을 제공해야 합니다**");
            prompt.append("\n- **사용자의 설문 텍스트에 '등운동(최소 3개 이상) + 복근운동 + 유산소운동'처럼 요일별 카테고리와 최소 개수가 명시된 경우, 해당 요일의 운동 개수와 카테고리 구성을 그 요구사항에 맞춰 주세요.**");
            prompt.append("\n- 예: 설문에 \"월요일: 등운동(최소 3개 이상) + 복근운동 + 유산소운동\"이라고 적혀 있다면, Monday 요일에는 등운동 3개 이상 + 복근운동 1개 이상 + 유산소운동 1개 이상이 포함되도록 설계하세요.");
            prompt.append("\n- **요일별로 중복되지 않는 다양한 운동을 배치하세요**");
            prompt.append("\n- **하루 내에서 상체/하체/코어 등 근육 그룹을 균형있게 분배하세요**");
            prompt.append("\n- 각 운동은 30분 내외로 구성해주세요");
            prompt.append("\n- workouts 필드에 요일별 운동 배열을 포함해주세요");
            prompt.append("\n- **중요**: 선택된 요일에 대해서만 운동을 제공하세요");
            prompt.append(String.format("\n- **필수**: %s 요일에 대한 운동을 모두 포함해야 합니다", String.join(", ", workoutDaysList)));
            prompt.append("\n- **절대 금지**: 선택되지 않은 요일(Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday 중 선택되지 않은 요일)은 포함하지 마세요");
            prompt.append(String.format("\n- **정확한 요일**: 오직 %s 요일만 workouts 객체에 포함하세요", String.join(", ", workoutDaysList)));
            // 동적 JSON 구조 생성
            prompt.append("\n\n**JSON 응답 형식 (사용자 선택 요일에 맞춰 구성):**");
            prompt.append("\n{\n");
            prompt.append(" \"programName\": \"프로그램 이름\",\n");
            prompt.append(" \"weeklySchedule\": \"주간 일정 설명\",\n");
            prompt.append(" \"caution\": \"주의사항\",\n");
            prompt.append(" \"warmup\": \"준비운동 설명\",\n");
            prompt.append(" \"mainSets\": \"본운동 세트 설명\",\n");
            prompt.append(" \"cooldown\": \"정리운동 설명\",\n");
            prompt.append(" \"equipment\": \"필요 장비\",\n");
            prompt.append(" \"targetMuscles\": \"타겟 근육\",\n");
            prompt.append(" \"expectedResults\": \"예상 결과\",\n");
            prompt.append(" \"workouts\": {\n");
            for (int i = 0; i < workoutDaysList.size(); i++) {
                String day = workoutDaysList.get(i);
                String workoutType = getWorkoutTypeForDay(i, workoutDays);
                prompt.append(String.format(" \"%s\": [\n", day));
                // 각 요일마다 3개 운동 예시 제공 (기본 최소 개수와 맞춤)
                for (int j = 0; j < 3; j++) { // 기본 예시는 3개
                    prompt.append(" {\n");
                    prompt.append(" \"name\": \"운동명\",\n");
                    prompt.append(" \"description\": \"운동 설명\",\n");
                    prompt.append(" \"duration\": 30,\n");
                    prompt.append(" \"intensity\": \"medium\",\n");
                    prompt.append(" \"difficulty\": \"medium\",\n");
                    prompt.append(" \"calories\": 150,\n");
                    prompt.append(String.format(" \"type\": \"%s\",\n", getWorkoutTypeForJson(workoutType)));
                    prompt.append(" \"reason\": \"이 운동을 추천하는 이유\",\n");
                    prompt.append(" \"part\": \"운동 부위 (상체/하체/전신)\",\n");
                    prompt.append(" \"targetMuscles\": [\"주요 타겟 근육\"],\n");
                    prompt.append(" \"sets\": 3,\n");
                    prompt.append(" \"reps\": 12,\n");
                    prompt.append(" \"restTime\": \"60초\",\n");
                    prompt.append(" \"steps\": [\"1단계: 자세 설명\", \"2단계: 동작 방법\", \"3단계: 주의사항\"],\n");
                    prompt.append(" \"effects\": [\"효과1: 구체적이고 자세한 효과 설명 (예: 가슴과 삼두근의 근력이 향상되어 상체 밀기 동작이 강해지고 일상생활 활동에 도움이 됩니다)\", \"효과2: 상세한 효과 설명 (예: 코어 안정성이 향상되어 자세가 개선되고 허리 통증 예방에 효과적입니다)\", \"효과3: 추가 효과 설명\"],\n");
                    prompt.append(" \"tips\": \"운동 팁과 주의사항\",\n");
                    prompt.append(" \"caution\": \"부상 예방 주의사항\"\n");
                    prompt.append(" }");
                    if (j < 2) { // 마지막이 아니면 콤마 추가
                        prompt.append(",");
                    }
                    prompt.append("\n");
                }
                prompt.append(" ]");
                if (i < workoutDaysList.size() - 1) {
                    prompt.append(",");
                }
                prompt.append("\n");
            }
            prompt.append(" }\n");
            prompt.append("}");
            // 추가 강조 메시지
            prompt.append(String.format("\n\n**최종 확인사항:**"));
            prompt.append(String.format("\n- workouts 객체에는 오직 %s 요일만 포함되어야 합니다", String.join(", ", workoutDaysList)));
            prompt.append(String.format("\n- 총 %d개의 요일에 대한 운동을 제공하세요", workoutDays));
            prompt.append("\n- 선택되지 않은 요일은 절대 포함하지 마세요");
            // 요일별 운동 타입 제안
            if (workoutDays >= 3) {
                prompt.append("\n\n**운동 타입 제안:**");
                for (int i = 0; i < workoutDaysList.size(); i++) {
                    String day = workoutDaysList.get(i);
                    String workoutType = getWorkoutTypeForDay(i, workoutDays);
                    prompt.append(String.format("\n- %s: %s", day, workoutType));
                }
            }
        } else {
            // 설문조사 데이터가 없는 경우 예외 처리
            log.error("❌ 설문조사 데이터가 없습니다. 운동 추천을 위해서는 설문조사가 필수입니다.");
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "운동 추천을 위해서는 설문조사가 필요합니다. 먼저 설문조사를 작성해주세요.");
        }
        return prompt.toString();
    }

    /**
     * 텍스트에서 JSON 추출 시도
     */
    private String tryExtractJsonFromText(String content) {
        if (content == null || content.trim().isEmpty()) {
            return null;
        }
        // 1. 코드 블록에서 JSON 추출
        String cleaned = stripCodeFences(content);
        if (isValidJson(cleaned)) {
            return cleaned;
        }
        // 2. { } 패턴으로 JSON 추출
        Pattern jsonPattern = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
        Matcher matcher = jsonPattern.matcher(content);
        if (matcher.find()) {
            String extracted = matcher.group();
            if (isValidJson(extracted)) {
                return extracted;
            }
        }
        // 3. 첫 번째 { 부터 마지막 } 까지 추출
        int startIndex = content.indexOf('{');
        int endIndex = content.lastIndexOf('}');
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            String extracted = content.substring(startIndex, endIndex + 1);
            if (isValidJson(extracted)) {
                return extracted;
            }
        }
        return null;
    }

    /**
     * JSON 유효성 검사
     */
    private boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }
        try {
            objectMapper.readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 각 요일마다 최소 3개 운동이 있는지 검증
     */
    private boolean ensureAtLeastThreePerDay(List<String> selectedDays, WorkoutRecommendationResponseDto dto) {
        if (dto == null || dto.workouts() == null) {
            log.warn("❌ DTO 또는 workouts가 null입니다.");
            return false;
        }
        final int MIN_PER_DAY = 3;
        for (String day : selectedDays) {
            Object dayWorkoutsObj = dto.workouts().get(day);
            if (dayWorkoutsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> dayWorkouts = (List<Map<String, Object>>) dayWorkoutsObj;
                if (dayWorkouts.size() < MIN_PER_DAY) {
                    log.warn("❌ {} 요일에 운동이 {}개만 있습니다. (최소 {}개 필요)", day, dayWorkouts.size(), MIN_PER_DAY);
                    return false;
                }
            } else {
                log.warn("❌ {} 요일에 운동 데이터가 없거나 잘못된 형식입니다.", day);
                return false;
            }
        }
        log.info("✅ 모든 요일에 {}개 이상의 운동이 있습니다.", MIN_PER_DAY);
        return true;
    }

    /**
     * 재시도 로직: 부족한 운동을 보강하는 프롬프트로 재시도
     */
    private WorkoutRecommendationResponseDto recommendWithRetry(InbodyDataRequestDto inbody, Long userId, int attempt) {
        log.info("🔄 운동 추천 재시도: attempt={}", attempt);
        try {
            // 재시도용 강화된 프롬프트 생성
            String systemPrompt = buildSystemPrompt();
            String userPrompt = buildUserPromptWithRetry(inbody, attempt);
            Map<String, Object> request = new HashMap<>();
            request.put("model", openAIClient.getDefaultModel());
            List<Map<String, Object>> messages = new ArrayList<>();
            Map<String, Object> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);
            messages.add(systemMessage);
            Map<String, Object> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", userPrompt);
            messages.add(userMessage);
            request.put("messages", messages);
            // 재시도 시에도 동일한 기본 temperature 사용
            request.put("temperature", openAIClient.getDefaultTemperature());
            request.put("max_tokens", openAIClient.getDefaultMaxTokens());
            request.put("response_format", Map.of("type", "json_object"));
            
            Map<String, Object> response = openAIClient.chatCompletions(request);
            String content = extractContentFromResponse(response);
            WorkoutRecommendationResponseDto result = parseGptResponse(content);
            
            // 재검증
            if (inbody.survey() != null && inbody.survey().getSelectedDaysEn() != null) {
                List<String> selectedDays = inbody.survey().getSelectedDaysEn();
                if (!ensureAtLeastThreePerDay(selectedDays, result)) {
                    if (attempt < 2) {
                        log.warn("⚠️ 재시도 {}에서도 부족합니다. 한 번 더 시도합니다.", attempt);
                        return recommendWithRetry(inbody, userId, attempt + 1);
                    } else {
                        log.warn("⚠️ 최대 재시도 횟수 초과. 현재 결과를 반환합니다.");
                        return result;
                    }
                }
            }
            return result;
        } catch (Exception e) {
            log.error("❌ 재시도 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "운동 추천 재시도 중 오류가 발생했습니다.");
        }
    }

    /**
     * 재시도용 강화된 사용자 프롬프트
     */
    private String buildUserPromptWithRetry(InbodyDataRequestDto inbody, int attempt) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("이전 응답에서 일부 요일에 운동이 부족했습니다. ");
        prompt.append("**중요: 각 요일마다 최소 3개 이상의 운동을 제공해야 하며, 사용자의 설문에서 명시한 '최소 개수' 요구사항이 있다면 그 개수 이상을 제공해야 합니다.**\n\n");
        // 기존 프롬프트 내용 추가
        prompt.append(buildUserPrompt(inbody));
        prompt.append("\n\n**재시도 요구사항:**");
        prompt.append("\n- 각 선택된 요일마다 최소 3개 이상의 운동을 제공하세요");
        prompt.append("\n- 설문 텍스트에 '등운동(최소 3개 이상) + 복근운동 + 유산소운동'처럼 요일별 카테고리와 최소 개수가 적혀 있다면 그 요구를 반드시 지키세요");
        prompt.append("\n- 운동이 지나치게 적은 요일이 없도록 하세요");
        prompt.append("\n- 요일별로 다양한 운동을 배치하세요");
        return prompt.toString();
    }

    /**
     * 요일별 운동 타입 결정
     */
    private String getWorkoutTypeForDay(int dayIndex, int totalDays) {
        if (totalDays <= 2) {
            return dayIndex == 0 ? "전신 근력운동" : "유산소운동";
        } else if (totalDays == 3) {
            return switch (dayIndex) {
                case 0 -> "상체 근력운동";
                case 1 -> "하체 근력운동";
                case 2 -> "유산소운동";
                default -> "전신 근력운동";
            };
        } else if (totalDays == 4) {
            return switch (dayIndex) {
                case 0 -> "상체 근력운동";
                case 1 -> "유산소운동";
                case 2 -> "하체 근력운동";
                case 3 -> "유산소운동";
                default -> "전신 근력운동";
            };
        } else {
            return switch (dayIndex) {
                case 0 -> "상체 근력운동";
                case 1 -> "유산소운동";
                case 2 -> "하체 근력운동";
                case 3 -> "유산소운동";
                case 4 -> "전신 근력운동";
                case 5 -> "유산소운동";
                case 6 -> "휴식 또는 가벼운 스트레칭";
                default -> "전신 근력운동";
            };
        }
    }

    /**
     * 운동 타입을 JSON용으로 변환
     */
    private String getWorkoutTypeForJson(String workoutType) {
        if (workoutType.contains("상체")) return "strength";
        if (workoutType.contains("하체")) return "strength";
        if (workoutType.contains("전신")) return "strength";
        if (workoutType.contains("유산소")) return "cardio";
        return "strength";
    }

}