package com.example.smart_healthcare.service.ai;

import com.example.smart_healthcare.client.OpenAIClient;
import com.example.smart_healthcare.dto.response.DietRecommendationResponseDto;
import com.example.smart_healthcare.dto.request.InbodyDataRequestDto;
import com.example.smart_healthcare.common.error.BusinessException;
import com.example.smart_healthcare.common.error.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 기반 식단 추천 서비스
 * OpenAI ChatGPT를 활용하여 개인 맞춤형 식단 가이드를 제공
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DietRecommendAIService {
    private final OpenAIClient openAIClient;
    private final ObjectMapper objectMapper;

    /**
     * 인바디 데이터와 식습관 선호도를 기반으로 식단 추천 수행
     * - 외부 API 호출만 수행
     * - DB 저장은 별도 서비스에서 처리
     */
    public DietRecommendationResponseDto recommend(InbodyDataRequestDto inbody, Long userId) {
        try {
            String dietaryPreference = inbody.survey() != null && inbody.survey().text() != null ? 
                                     inbody.survey().text() : "고단백 선호, 한식 위주, 특별한 제약 없음";
            log.info("🍽️ 식단 추천 시작: 성별={}, 나이={}, 선호도={}",
                    inbody.getGenderKorean(), inbody.getCurrentAge(), dietaryPreference);
            
            // 🔍 설정 검증 로그 (운동과 동일한지 확인)
            log.info("🔍 [DietAI] 설정 검증: model={}, baseUrl={}, hasKey={}, apiKeyValid={}",
                    openAIClient.getDefaultModel(), 
                    openAIClient.getClass().getSimpleName(), // baseUrl은 private이라 클래스명으로
                    openAIClient.isApiKeyValid() ? "있음" : "없음",
                    openAIClient.isApiKeyValid());

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
            log.info("🍽️ 식단 추천 API 호출 시작");
            Map<String, Object> response = openAIClient.chatCompletions(request);

            // === 강력 가드 & 진단 로그 ===
            if (response == null) {
                log.error("❌ [DietAI] Null or empty choices. req={}", 
                         "model=" + openAIClient.getDefaultModel() + ", temp=0.3, max_tokens=4096");
                log.error("❌ 가능한 원인:");
                log.error("  - API 키 오류 (401)");
                log.error("  - 모델명 오류 (404)");
                log.error("  - 쿼터 초과 (429)");
                log.error("  - 서버 오류 (500/502)");
                log.error("  - 네트워크 타임아웃");
                log.error("  - response_format 필드 오류");
                log.error("❌ API 키 유효성: {}", openAIClient.isApiKeyValid());
                       log.error("❌ 요청 정보: model={}, temperature={}, max_tokens={}", 
                                openAIClient.getDefaultModel(), 0.3, openAIClient.getDefaultMaxTokens());
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
            
            // 🔍 응답 본문 검증
            if (content == null || content.isBlank()) {
                log.error("❌ [DietAI] Empty content. model={}, req={}", 
                         openAIClient.getDefaultModel(), "diet_recommendation");
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "OpenAI 응답 본문이 비었습니다.");
            }

            // JSON 파싱 시도 및 재프롬프트 로직
            DietRecommendationResponseDto result = parseGptResponseWithRetry(content, inbody, userId);
            log.info("✅ 식단 추천 완료: mealStyle={}", result.mealStyle());

            return result;

        } catch (BusinessException e) {
            // BusinessException은 그대로 재던지기
            throw e;
        } catch (Exception e) {
            log.error("❌ 식단 추천 실패: {}", e.getMessage(), e);
            log.error("❌ 예외 타입: {}", e.getClass().getSimpleName());
            log.error("❌ 예외 스택 트레이스:", e);
            
            // 원본 예외를 BusinessException으로 감싸서 던지기
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "식단 추천 생성 중 오류가 발생했습니다.", e);
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
    private DietRecommendationResponseDto parseGptResponseWithRetry(String content, InbodyDataRequestDto inbody, Long userId) {
        try {
            // 첫 번째 시도: 직접 파싱
            DietRecommendationResponseDto result = parseGptResponse(content);
            log.info("✅ JSON 파싱 성공 (첫 번째 시도)");
            return result;
        } catch (BusinessException e) {
            // BusinessException도 재시도 대상으로 처리
            log.warn("❌ [DietAI] JSON 파싱 실패 (BusinessException) → JSON 추출 및 재시도. err={}", e.getMessage());
            return handleParseFailure(content, inbody, userId, e);
        } catch (Exception e) {
            log.warn("❌ [DietAI] JSON 파싱 실패 → JSON 추출 및 재시도. err={}", e.getMessage());
            return handleParseFailure(content, inbody, userId, e);
        }
    }

    /**
     * 파싱 실패 처리 헬퍼 (성능 최적화: API 재호출 제거)
     */
    private DietRecommendationResponseDto handleParseFailure(String content, InbodyDataRequestDto inbody, Long userId, Exception originalException) {
        // JSON 재시도 로직 (빠른 정제 시도만 수행)
        String cleanedContent = tryExtractJsonFromText(content);
        if (cleanedContent != null && !cleanedContent.trim().isEmpty() && !cleanedContent.equals(content)) {
            log.info("🔄 JSON 재시도: 정제된 내용으로 파싱 시도");
            try {
                DietRecommendationResponseDto result = parseGptResponse(cleanedContent);
                log.info("✅ JSON 재시도 성공");
                return result;
            } catch (Exception retryException) {
                log.error("❌ JSON 재시도도 실패: {}", retryException.getMessage());
                log.error("❌ 원본 예외: {}", originalException.getMessage());
            }
        }
        // 성능 최적화: API 재호출 제거, 에러만 반환
        log.warn("⚠️ JSON 파싱 완전 실패 (성능 최적화: API 재호출 없이 에러 반환)");
        
        // 최종적으로 더 자세한 에러 메시지 제공
        String errorMessage = "AI가 올바른 JSON 형식으로 응답하지 않았습니다.";
        if (originalException.getMessage() != null) {
            if (originalException.getMessage().contains("잘린") || originalException.getMessage().contains("truncated")) {
                errorMessage = "AI 응답이 길어서 잘렸습니다. 잠시 후 다시 시도해주세요.";
            } else if (originalException.getMessage().contains("JSON") || originalException.getMessage().contains("파싱")) {
                errorMessage = "AI 응답 형식 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
            }
        }
        
        throw new BusinessException(ErrorCode.INTERNAL_ERROR, errorMessage, originalException);
    }

    /**
     * GPT 응답을 DietRecommendationResponseDto로 파싱
     * - 1차: 코드펜스 제거 및 JSON 정리 후 DTO로 직접 파싱
     * - 2차: Map으로 파싱 후 수동으로 DTO 구성 (타입이 조금 달라도 최대한 수용)
     */
    private DietRecommendationResponseDto parseGptResponse(String content) {
        try {
            log.info("🔍 GPT 응답 파싱 시작:");
            log.info("  - 응답 길이: {} 문자", content.length());
            log.info("  - 응답 전체 내용: {}", content);

            // 0) 코드펜스 제거
            String originalContent = content;
            content = stripCodeFences(content);
            log.info("🔧 코드펜스 제거 후: {}", content.length() > 200 ? content.substring(0, 200) + "..." : content);

            // 1) 앞/뒤 설명이 섞여 있어도 JSON 블록만 추출 - 여러 방법 시도
            String extractedJson = extractFirstJsonObject(content);
            
            // extractFirstJsonObject가 실패하면 다른 방법 시도
            if (extractedJson == null || extractedJson.trim().isEmpty() || !extractedJson.trim().startsWith("{")) {
                log.warn("⚠️ extractFirstJsonObject 실패, 다른 방법 시도");
                // 방법 2: 첫 번째 { 부터 마지막 } 까지 추출
                int jsonStart = content.indexOf('{');
                int jsonEnd = content.lastIndexOf('}');
                if (jsonStart >= 0 && jsonEnd > jsonStart) {
                    extractedJson = content.substring(jsonStart, jsonEnd + 1);
                    log.info("🔧 직접 JSON 추출 (방법 2): {} 문자", extractedJson.length());
                }
            }
            
            // 방법 3: 정규식으로 시도
            if (extractedJson == null || extractedJson.trim().isEmpty() || !extractedJson.trim().startsWith("{")) {
                log.warn("⚠️ 방법 2도 실패, 정규식 시도");
                java.util.regex.Pattern jsonPattern = java.util.regex.Pattern.compile("\\{[\\s\\S]*\\}", java.util.regex.Pattern.DOTALL);
                java.util.regex.Matcher matcher = jsonPattern.matcher(content);
                if (matcher.find()) {
                    extractedJson = matcher.group();
                    log.info("🔧 정규식 JSON 추출 (방법 3): {} 문자", extractedJson.length());
                }
            }
            
            content = extractedJson;
            log.info("🔧 JSON 블록 추출 후: {}", content != null ? (content.length() > 200 ? content.substring(0, 200) + "..." : content) : "null");
            
            if (content == null || content.trim().isEmpty() || !content.trim().startsWith("{")) {
                log.error("❌ 원본 응답 (처음 1000자): {}", originalContent.length() > 1000 ? originalContent.substring(0, 1000) + "..." : originalContent);
                log.error("❌ 원본 응답 (마지막 500자): {}", originalContent.length() > 500 ? originalContent.substring(Math.max(0, originalContent.length() - 500)) : originalContent);
                log.error("❌ 코드펜스 제거 후 (처음 500자): {}", stripCodeFences(originalContent).length() > 500 ? stripCodeFences(originalContent).substring(0, 500) + "..." : stripCodeFences(originalContent));
                log.error("❌ JSON 추출 결과: {}", content);
                throw new IllegalArgumentException("응답에 유효한 JSON 객체가 없습니다. GPT 응답이 JSON 형식이 아니거나 잘렸을 수 있습니다.");
            }

            // JSON 형태로 응답이 오는 경우 파싱 시도
            if (content.trim().startsWith("{")) {
                log.info("✅ JSON 형태 응답 감지, 파싱 시도");
                try {
                    // 먼저 Map으로 파싱해서 구조 확인
                    @SuppressWarnings("unchecked")
                    Map<String, Object> jsonMap = objectMapper.readValue(content, Map.class);
                    log.info("🔍 파싱된 JSON 구조: {}", jsonMap.keySet());

                    // DietRecommendationResponseDto로 직접 파싱 시도
                    DietRecommendationResponseDto result = objectMapper.readValue(content, DietRecommendationResponseDto.class);
                    log.info("✅ JSON 파싱 성공: mealStyle={}, diets={}", result.mealStyle(), result.diets() != null ? "있음" : "없음");

                    // diets 필드가 null인 경우 Map에서 직접 추출 시도
                    if (result.diets() == null && jsonMap.containsKey("diets")) {
                        log.info("🔍 diets 필드가 null이므로 Map에서 직접 추출 시도");
                        Object dietsObj = jsonMap.get("diets");
                        if (dietsObj instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> dietsMap = (Map<String, Object>) dietsObj;
                            log.info("✅ diets Map 추출 성공: {}", dietsMap.keySet());
                            // 새로운 DTO 생성 (diets 포함)
                            return new DietRecommendationResponseDto(
                                    result.mealStyle(),
                                    result.dailyCalories(),
                                    result.macroSplit(),
                                    result.sampleMenu(),
                                    result.shoppingList(),
                                    result.precautions(),
                                    result.mealTiming(),
                                    result.hydration(),
                                    result.supplements(),
                                    dietsMap
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

                        String mealStyle = toStringOrNull(root.get("mealStyle"));
                        Integer dailyCalories = toIntegerOrNull(root.get("dailyCalories"));
                        @SuppressWarnings("unchecked")
                        Map<String, Integer> macroSplit = root.get("macroSplit") instanceof Map
                                ? (Map<String, Integer>) root.get("macroSplit")
                                : Map.of();
                        String sampleMenu = toStringOrNull(root.get("sampleMenu"));
                        @SuppressWarnings("unchecked")
                        List<String> shoppingList = root.get("shoppingList") instanceof List
                                ? (List<String>) root.get("shoppingList")
                                : List.of();
                        String precautions = toStringOrNull(root.get("precautions"));
                        String mealTiming = toStringOrNull(root.get("mealTiming"));
                        String hydration = toStringOrNull(root.get("hydration"));
                        String supplements = toStringOrNull(root.get("supplements"));
                        @SuppressWarnings("unchecked")
                        Map<String, Object> diets = root.get("diets") instanceof Map
                                ? (Map<String, Object>) root.get("diets")
                                : Map.of();

                        return new DietRecommendationResponseDto(
                                mealStyle,
                                dailyCalories,
                                macroSplit,
                                sampleMenu,
                                shoppingList,
                                precautions,
                                mealTiming,
                                hydration,
                                supplements,
                                diets
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
            // BusinessException이 아닌 일반 예외로 변환하여 상위에서 재시도 가능하도록
            throw new RuntimeException("GPT 응답 파싱 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    // 성능 최적화: API 재호출 제거로 인해 더 이상 사용되지 않음
    // JSON 파싱 실패 시 빠른 JSON 정제만 시도하고, 실패하면 에러 반환

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
        String extracted = extractFirstJsonObject(cleaned);
        if (extracted != null && isValidJson(extracted)) {
            return extracted;
        }
        // 3. 첫 번째 { 부터 마지막 } 까지 추출
        int startIndex = content.indexOf('{');
        int endIndex = content.lastIndexOf('}');
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            String candidate = content.substring(startIndex, endIndex + 1);
            if (isValidJson(candidate)) {
                return candidate;
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
     * 문자열 내 JSON 객체 추출 시 따옴표 내부의 중괄호는 무시
     */
    private String extractFirstJsonObject(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        
        int start = -1;
        int depth = 0;
        boolean inString = false;
        char stringChar = 0;
        boolean escapeNext = false;
        
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            
            if (escapeNext) {
                escapeNext = false;
                continue;
            }
            
            if (c == '\\') {
                escapeNext = true;
                continue;
            }
            
            if (!inString && (c == '"' || c == '\'')) {
                inString = true;
                stringChar = c;
            } else if (inString && c == stringChar) {
                inString = false;
            } else if (!inString) {
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
        }
        
        // 중괄호가 닫히지 않은 경우에도 유효한 JSON인지 확인
        if (start >= 0 && depth > 0) {
            log.warn("⚠️ JSON이 완전히 닫히지 않음. depth={}, 시작 위치={}", depth, start);
            // 마지막 }까지 포함하여 시도
            int lastBrace = s.lastIndexOf('}');
            if (lastBrace > start) {
                String candidate = s.substring(start, lastBrace + 1);
                if (isValidJson(candidate)) {
                    return candidate;
                }
            }
        }
        
        return null;
    }

    private String toStringOrNull(Object value) {
        return value == null ? null : value.toString();
    }

    private Integer toIntegerOrNull(Object value) {
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * 시스템 프롬프트 구성
     */
    private String buildSystemPrompt() {
        return """
            당신은 한국의 전문 영양사이자 식단 전문가입니다.
            [출력 규칙 - 반드시 준수]
            - 오직 하나의 유효한 JSON 객체로만 응답 (설명/마크다운/코드펜스 금지).
            - 요일은 월~금(Monday, Tuesday, Wednesday, Thursday, Friday) **모두** 포함.
            - 각 요일에 대해, 끼니는 사용자가 선택한 목록(mealsToGenerate)에 **포함된 끼니만** 생성.
            - 선택되지 않은 끼니는 절대 포함하지 말 것.

            [체형별 식단 우선순위]
            - 운동선수/고근육형: 1순위 근육증가식단, 2순위 균형식단
            - 근육형/적정체중: 1순위 균형식단, 2순위 근육증가식단
            - 날씬형/마른형/근육형날씬형: 1순위 근육증가식단, 2순위 균형식단
            - 과체중/비만/고도비만: 1순위 지방감소식단, 2순위 균형식단
            - 마른비만/복부비만/근육형비만(체지방 불균형형): 1순위 지방감소식단, 2순위 근육증가식단

            [식단 종류 세분화]
            - 균형식단: 전반적인 건강 유지와 생활 패턴을 고려해 탄수화물·단백질·지방·비타민·무기질을 고르게 구성한 기본 식단
            - 근육증가식단(벌크): 근육량 증가를 목표로, 일일 총칼로리를 유지 칼로리 수준 또는 약간 상회하도록 설정한 고단백·적정 탄수화물·적절한 지방 중심 식단
            - 지방감소식단(다이어트): 체지방 감소를 목표로, 유지 칼로리보다 다소 낮춘 저칼로리·고단백·저당·저지방 식단
            - 운동전·운동후 보조식단: 운동 1~2시간 전에는 소화가 잘 되는 탄수화물 중심, 운동 직후에는 단백질+탄수화물 중심으로 회복을 돕는 보조용 소량 식단 (기존 아침/점심/저녁/간식 식단에 추가되거나 일부를 대체하는 형태)

            [추천 이유(reason) 작성 규칙 - 매우 중요]
            - **절대 금지: "건강에 좋다", "영양가 높다", "추천합니다" 같은 단순하고 모호한 표현은 사용하지 마세요.**
            - **최소 150자 이상, 4-5문장 이상**으로 구체적이고 상세하게 작성해야 합니다.
            - **반드시 3-4개의 핵심 포인트**를 포함해야 하며, 각 포인트는 다음과 같은 구조로 작성:
              1) 영양학적 근거 (구체적인 영양소 함량과 수치 포함)
              2) 사용자 체형/목표와의 연관성 (왜 이 사람에게 특별히 좋은지)
              3) 건강 효과 (구체적인 생리적/대사적 효과)
              4) 실용적 이점 (조리 편의성, 포만감, 식사 타이밍 등)
            - 각 포인트는 **콜론(:)으로 구분**하고, 각각 **2-3문장으로 상세히 설명**해야 합니다.
            - 구체적인 수치를 반드시 포함: "단백질 23g", "식이섬유 5g", "칼로리 400kcal" 등
            - 사용자의 설문 내용, 체형, 목표를 반드시 언급하고 연결해야 합니다.
            - 형식 예시:
            "고단백 저지방 구성: 닭가슴살 120g에는 약 23g의 단백질이 함유되어 있어 근육 합성과 회복에 필수적이며, 지방은 3g 미만으로 체지방 증가 없이 단백질을 공급할 수 있습니다. 체지방 감량 최적화: 이 식단의 총 칼로리는 약 400kcal로 하루 권장 칼로리의 적절한 비율을 차지하며, 고단백 식사는 식후 열생산(TEF)을 증가시켜 실제 소모 칼로리를 높여줍니다. 포만감 지속: 브로콜리의 식이섬유 5g과 단백질의 조합은 혈당 상승을 완만하게 하여 포만감을 오래 유지시켜 간식 섭취 욕구를 줄여줍니다. 실용적 편의성: 조리 시간이 15분 이내로 짧고 특별한 조리 기술이 필요 없어 바쁜 직장인도 쉽게 실천할 수 있는 식단입니다."

            [조리방법(instructions) 작성 규칙 - 매우 중요]
            - **절대 금지: "재료를 섞는다", "볶는다", "굽는다" 같은 단순하고 모호한 표현은 사용하지 마세요.**
            - **반드시 '1. ... 2. ... 3. ...' 형식의 번호가 붙은 여러 단계 문자열**로 작성하세요.
            - **최소 6~8단계 이상**의 상세한 조리법을 작성해야 합니다. 3~4단계로 끝나는 짧은 조리법은 절대 사용하지 마세요.
            - 각 단계는 **초보자도 그대로 따라 할 수 있을 만큼 구체적**이어야 하며, 다음을 모두 포함해야 합니다:
              * 재료 준비 단계 (세척, 손질, 자르기 등)
              * 밑간/양념 단계 (구체적인 양과 시간)
              * 조리 단계 (온도, 시간, 방법)
              * 마무리 단계 (접시 담기, 장식 등)
            - **반드시 포함해야 할 구체적 정보:**
              * 재료의 정확한 양 (예: 닭가슴살 120g, 올리브오일 1큰술)
              * 조리 온도 (예: 중불, 약불, 강불)
              * 조리 시간 (예: 3~4분, 10분간 재우기)
              * 조리 방법 (예: 앞뒤로 구우기, 저어가며 볶기)
              * 주의사항 (예: 타지 않도록 주의, 물기를 완전히 제거)
            - 각 단계는 **최소 1문장 이상**으로 작성하고, 왜 그렇게 해야 하는지 간단한 이유도 포함하면 더 좋습니다.
            - 예시 (6단계 이상):
            "1. 닭가슴살 120g을 흐르는 찬물에 깨끗이 씻은 후 키친타월로 물기를 완전히 제거합니다. (물기가 남아있으면 구울 때 수분이 나와 바삭함이 떨어집니다) 2. 닭가슴살을 1cm 두께로 일정하게 슬라이스합니다. 3. 슬라이스한 닭가슴살에 소금 1/4작은술, 후추 약간을 앞뒤로 골고루 뿌려 밑간한 후 냉장고에서 10분간 재웁니다. (밑간 시간을 두면 맛이 더 깊어집니다) 4. 브로콜리 100g을 한 입 크기로 자르고, 끓는 물 1L에 소금 1작은술을 넣어 2~3분간 데친 후 체에 받쳐 물기를 제거합니다. 5. 팬에 올리브오일 1큰술을 두르고 중불에서 30초간 예열한 후, 밑간한 닭가슴살을 한 면당 3~4분씩 노릇하게 구워줍니다. (타지 않도록 중간중간 뒤집어가며 구워주세요) 6. 구운 닭가슴살을 접시에 담고, 데친 브로콜리를 곁들입니다. 7. 현미밥 150g을 공기에 담아 닭가슴살 옆에 올립니다. 8. 레몬즙 1작은술을 뿌려 풍미를 더하고 완성합니다."

            [스키마 - DTO와 1:1]
            {
              "mealStyle": "식단 스타일 (문자열)",
              "dailyCalories": "일일 칼로리 (문자열, 예: 2000kcal)",
              "macroSplit": "영양소 비율 (문자열, 예: 탄수화물 45% : 단백질 30% : 지방 25%)",
              "sampleMenu": "예시 식단 (문자열)",
              "shoppingList": "장보기 리스트 (문자열)",
              "precautions": "유의사항 (문자열)",
              "mealTiming": "식사 타이밍 (문자열)",
              "hydration": "수분 섭취 (문자열)",
              "supplements": "보충제 (문자열)",
              "diets": {
                "Monday": {
                  "끼니명": Meal객체, ...
                },
                "Tuesday": {
                  "끼니명": Meal객체, ...
                },
                "Wednesday": {
                  "끼니명": Meal객체, ...
                },
                "Thursday": {
                  "끼니명": Meal객체, ...
                },
                "Friday": {
                  "끼니명": Meal객체, ...
                }
              }
            }

            [Meal 객체 구조 및 설명 규칙]
            {
              "name": "음식명 (문자열)",
              "description": "설명 (문자열)",
              "calories": 칼로리숫자,
              "nutrients": {
                "carbs": 탄수화물숫자,
                "protein": 단백질숫자,
                "fat": 지방숫자
              },
              "reason": "추천 이유 (문자열, 최소 150자 이상, 4-5문장 이상. 3-4개의 핵심 포인트를 콜론으로 구분하여 각각 2-3문장으로 상세히 설명. 구체적인 영양소 수치와 사용자 체형/목표와의 연관성을 반드시 포함)",
              "ingredients": ["재료1", "재료2", ...],
              "instructions": "조리법 (문자열, 반드시 '1. ... 2. ... 3. ...' 형식으로 최소 6-8단계 이상 작성. 각 단계마다 재료 양, 온도, 시간, 방법을 구체적으로 명시)",
              "tips": "팁 (문자열, 대체 재료나 외식 시 비슷하게 선택하는 방법 등)"
            }

            [주의사항]
            - diets[요일]는 **객체**여야 하며, 키는 사용자가 선택한 끼니(예: breakfast, lunch, dinner, snack 등)만 존재.
            - 'totalCalories' 같은 대체 키 금지. 칼로리는 Meal.calories(숫자)만 사용.
            - 요일은 반드시 Monday, Tuesday, Wednesday, Thursday, Friday 모두 포함.
            - 각 요일마다 mealsToGenerate에 지정된 끼니만 생성.
            - unsplashQuery는 음식 사진 검색에 최적화된 영문 검색어를 생성하세요 (예: "grilled salmon with quinoa salad healthy meal plate").

            **응답 스키마:**
            {
              "mealStyle": "식단 스타일 (문자열)",
              "dailyCalories": 숫자,
              "macroSplit": {
                "carbs": 숫자,
                "protein": 숫자,
                "fat": 숫자
              },
              "sampleMenu": "예시 식단 (문자열)",
              "shoppingList": ["재료1", "재료2"],
              "precautions": "유의사항",
              "mealTiming": "식사 타이밍",
              "hydration": "수분 섭취",
              "supplements": "보충제",
              "diets": {
                "Monday": {
                  "끼니명": Meal객체, ...
                },
                "Tuesday": {
                  "끼니명": Meal객체, ...
                },
                "Wednesday": {
                  "끼니명": Meal객체, ...
                },
                "Thursday": {
                  "끼니명": Meal객체, ...
                },
                "Friday": {
                  "끼니명": Meal객체, ...
                }
              }
            }

            **Meal 객체:**
            {
              "name": "음식명",
              "description": "설명",
              "calories": 숫자,
              "nutrients": {
                "carbs": 숫자,
                "protein": 숫자,
                "fat": 숫자
              },
              "reason": "구체적이고 상세한 추천 이유 (최소 150자 이상, 4-5문장 이상. 3-4개의 핵심 포인트를 콜론으로 구분하여 각각 2-3문장으로 상세히 설명. 구체적인 영양소 수치와 사용자 체형/목표와의 연관성을 반드시 포함)",
              "ingredients": ["재료1", "재료2"],
              "instructions": "단계별 상세 조리법 (반드시 '1. ... 2. ... 3. ...' 형식, 최소 6-8단계 이상. 각 단계마다 재료 양, 온도, 시간, 방법을 구체적으로 명시하고, 왜 그렇게 해야 하는지 간단한 이유도 포함)",
              "tips": "팁",
              "unsplashQuery": "영문으로 구체적인 음식 이미지 검색어 (예: grilled chicken breast with broccoli healthy meal plate)"
            }

            **매우 중요 - 반드시 준수:**
            - reason은 절대 단순하고 모호한 표현("건강에 좋다", "영양가 높다", "추천합니다")을 사용하지 마세요.
            - reason은 최소 150자 이상, 4-5문장 이상으로 작성하고, 반드시 3-4개의 핵심 포인트를 콜론(:)으로 구분하여 각각 2-3문장으로 상세히 설명해야 합니다.
            - reason에는 구체적인 영양소 수치(예: 단백질 23g, 식이섬유 5g)와 사용자의 체형/목표와의 연관성을 반드시 포함해야 합니다.
            - instructions는 절대 단순하고 모호한 표현("재료를 섞는다", "볶는다", "굽는다")을 사용하지 마세요.
            - instructions는 최소 6-8단계 이상으로 작성하고, 각 단계마다 재료 양, 온도, 시간, 방법을 구체적으로 명시해야 합니다.
            - instructions의 각 단계는 최소 1문장 이상으로 작성하고, 왜 그렇게 해야 하는지 간단한 이유도 포함하면 더 좋습니다.
            - 예: reason (좋은 예) - "고단백 저지방 구성: 닭가슴살 120g에는 약 23g의 단백질이 함유되어 있어 근육 합성과 회복에 필수적이며, 지방은 3g 미만으로 체지방 증가 없이 단백질을 공급할 수 있습니다. 체지방 감량 최적화: 이 식단의 총 칼로리는 약 400kcal로 하루 권장 칼로리의 적절한 비율을 차지하며, 고단백 식사는 식후 열생산(TEF)을 증가시켜 실제 소모 칼로리를 높여줍니다. 포만감 지속: 브로콜리의 식이섬유 5g과 단백질의 조합은 혈당 상승을 완만하게 하여 포만감을 오래 유지시켜 간식 섭취 욕구를 줄여줍니다."
            - 예: instructions (좋은 예) - "1. 닭가슴살 120g을 흐르는 찬물에 깨끗이 씻은 후 키친타월로 물기를 완전히 제거합니다. (물기가 남아있으면 구울 때 수분이 나와 바삭함이 떨어집니다) 2. 닭가슴살을 1cm 두께로 일정하게 슬라이스합니다. 3. 슬라이스한 닭가슴살에 소금 1/4작은술, 후추 약간을 앞뒤로 골고루 뿌려 밑간한 후 냉장고에서 10분간 재웁니다. (밑간 시간을 두면 맛이 더 깊어집니다) 4. 브로콜리 100g을 한 입 크기로 자르고, 끓는 물 1L에 소금 1작은술을 넣어 2~3분간 데친 후 체에 받쳐 물기를 제거합니다. 5. 팬에 올리브오일 1큰술을 두르고 중불에서 30초간 예열한 후, 밑간한 닭가슴살을 한 면당 3~4분씩 노릇하게 구워줍니다. (타지 않도록 중간중간 뒤집어가며 구워주세요) 6. 구운 닭가슴살을 접시에 담고, 데친 브로콜리를 곁들입니다. 7. 현미밥 150g을 공기에 담아 닭가슴살 옆에 올립니다. 8. 레몬즙 1작은술을 뿌려 풍미를 더하고 완성합니다."

            **중요: 사용자가 선택한 끼니(mealsToGenerate)에 따라 각 요일의 diets 객체에는 해당 끼니만 포함되어야 합니다.**
            **예를 들어, mealsToGenerate가 ["breakfast", "dinner"]이면 각 요일에는 breakfast와 dinner만 포함하고, lunch나 snack은 절대 포함하지 마세요.**
            **응답 예시 (mealsToGenerate: ["breakfast", "dinner"] 인 경우 - 이는 예시일 뿐이며, 실제로는 사용자가 선택한 끼니에 맞춰 생성해야 합니다):**
            {
              "mealStyle": "73세 남성, 내장지방 감소와 체중 감량을 목표로 한 고단백·저당 한식 위주 식단입니다. 가공식품과 설탕이 많이 들어간 음식은 최대한 줄이고, 현미·잡곡·채소·양질의 단백질을 중심으로 구성합니다. 씹기 편한 식감과 위에 부담이 적은 조리법을 우선 사용합니다.",
              "dailyCalories": 1900,
              "macroSplit": {
                "carbs": 50,
                "protein": 25,
                "fat": 25
              },
              "sampleMenu": "아침에는 요거트·오트밀·주먹밥 등으로 가볍게 시작하고, 점심·저녁에는 현미밥과 살코기·생선·두부·채소를 조합한 한식 위주의 식단으로 구성합니다. 간식은 견과류, 그릭요거트, 단백질 위주의 간단한 메뉴로 소량 섭취하여 혈당 급상승을 막습니다.",
              "shoppingList": ["현미", "닭가슴살", "연어", "두부", "계란", "브로콜리", "김치", "올리브오일", "그릭요거트", "견과류"],
              "precautions": "내장지방과 혈압·혈당 악화를 막기 위해 과식·야식·폭식을 피하고, 라면·패스트푸드·튀김류·달콤한 음료를 가급적 제한합니다. 가공육(햄·소시지)은 주 1회 이하로 줄이고, 짠 찌개·국물은 가능한 건더기 위주로 섭취합니다.",
              "mealTiming": "아침 07:00 ~ 08:30 / 저녁 18:00 ~ 19:30 사이 규칙적인 시간에 섭취하는 것을 권장합니다. 늦은 밤(21시 이후) 탄수화물 섭취는 피하고, 저녁 식사 후 2~3시간은 소화 시간을 확보한 뒤 취침합니다.",
              "hydration": "하루 2L 이상 수분 섍취를 목표로 하되, 카페인 음료와 당이 들어간 음료는 하루 1~2잔 이내로 제한합니다. 물·보리차·무가당 차 위주로 자주 조금씩 마셔 탈수를 예방합니다.",
              "supplements": "필요 시 오메가3, 종합비타민, 비타민 D를 활용할 수 있지만, 기존 복용 약물이 있다면 반드시 의사와 상의 후 섭취합니다. 보충제는 음식 섭취를 대체하기보다는 식단을 보완하는 수준에서 사용합니다.",
              "diets": {
                "Monday": {
                  "breakfast": {
                    "name": "그릭요거트 볼(요거트+베리+견과)",
                    "description": "단백질·프로바이오틱스와 식이섬유",
                    "calories": 480,
                    "nutrients": {
                      "carbs": 55,
                      "protein": 28,
                      "fat": 16
                    },
                    "reason": "장 건강·포만감: 그릭요거트는 일반 요거트보다 단백질이 2배 이상 풍부해 아침 포만감을 오래 유지시키고, 프로바이오틱스가 장내 환경을 개선해 배변 리듬을 안정시켜 줍니다. 항산화·심혈관 보호: 블루베리의 안토시아닌과 아몬드의 불포화지방산이 혈관 건강과 항산화에 도움을 주어 심혈관 질환 위험을 낮추는 데 기여합니다. 체지방 감량 지원: 전체 당류와 칼로리를 과하지 않게 조절하면서도 단백질·지방·탄수화물의 균형을 맞춰, 내장지방 감소와 체중 관리 목표에 잘 맞는 아침 메뉴입니다.",
                    "ingredients": ["그릭요거트 200g", "블루베리 80g", "아몬드 15g", "꿀 소량"],
                    "instructions": "1. 그릭요거트 200g을 깨끗한 볼에 담습니다. 2. 블루베리 80g을 흐르는 물에 깨끗이 씻어 물기를 제거합니다. 3. 아몬드 15g을 거칠게 다지거나 슬라이스합니다. 4. 요거트 위에 블루베리를 골고루 올립니다. 5. 다진 아몬드를 뿌리고 꿀을 소량(1티스푼) 뿌려 완성합니다. 6. 바로 먹거나 10분 정도 냉장 보관 후 드시면 더욱 시원하게 즐길 수 있습니다.",
                    "tips": "무가당 요거트 선택",
                    "unsplashQuery": "greek yogurt bowl with berries and nuts healthy breakfast"
                  },
                  "dinner": {
                    "name": "현미밥+닭가슴살 구이+브로콜리",
                    "description": "고단백 저지방 저녁",
                    "calories": 600,
                    "nutrients": {
                      "carbs": 60,
                      "protein": 40,
                      "fat": 14
                    },
                    "reason": "근육 유지·체지방 감량: 닭가슴살은 100g당 23g의 단백질을 함유하면서 지방 함량이 1~2g에 불과해 근육 손실 없이 체중 감량을 돕는 대표적인 고단백 저지방 식재료입니다. 혈당·포만감 관리: 현미는 백미보다 식이섬유가 3배 이상 많아 혈당을 천천히 올리고 포만감을 오래 유지시켜 야식 및 간식 충동을 줄여줍니다. 회복·면역 강화: 브로콜리는 비타민 C·K·엽산과 항산화 성분이 풍부해 운동 후 회복과 면역력 향상에 도움을 주며, 전체적으로 저녁 칼로리를 과하지 않게 유지하면서도 영양 균형을 맞춘 메뉴입니다.",
                    "ingredients": ["현미밥 1공기", "닭가슴살 120g", "브로콜리 120g", "올리브오일", "후추·소금"],
                    "instructions": "1. 닭가슴살 120g을 깨끗이 씻어 키친타월로 물기를 제거합니다. 2. 닭가슴살을 1cm 두께로 슬라이스하여 소금, 후추로 앞뒤 골고루 밑간합니다(10분 재우기). 3. 브로콜리는 한 입 크기로 자르고 끓는 물에 소금을 약간 넣어 2-3분간 데쳐 건집니다. 4. 팬에 올리브오일 1큰술을 두르고 중불로 예열합니다. 5. 밑간한 닭가슴살을 팬에 올려 한쪽 면을 3-4분씩 노릇하게 굽습니다(속까지 익도록). 6. 현미밥 1공기를 그릇에 담고 구운 닭가슴살과 데친 브로콜리를 곁들입니다. 7. 브로콜리 위에 올리브오일을 약간 뿌려 완성합니다.",
                    "tips": "소금 과다 사용 주의",
                    "unsplashQuery": "grilled chicken breast with broccoli and brown rice healthy meal plate"
                  }
                },
                "Tuesday": {
                  "breakfast": {
                    "name": "오트밀 바나나볼",
                    "description": "저GI 탄수 + 식이섬유",
                    "calories": 500,
                    "nutrients": {
                      "carbs": 70,
                      "protein": 18,
                      "fat": 12
                    },
                    "reason": "아침 에너지 공급에 최적화된 저혈당지수(GI) 식단입니다. 오트밀은 수용성 식이섬유인 베타글루칸이 풍부하여 혈당을 천천히 올리고 콜레스테롤 수치를 개선하는 데 도움이 됩니다. 바나나의 칼륨은 전해질 균형을 유지하고 운동 후 근육 회복에 기여하며, 호두의 오메가-3 지방산은 뇌 건강과 염증 감소에 효과적입니다. 아침 식사로 충분한 에너지를 제공하면서도 점심까지 포만감을 유지시켜 과식을 예방합니다.",
                    "ingredients": ["오트밀 60g", "우유 200ml", "바나나 1개", "호두 10g"],
                    "instructions": "1. 냄비에 우유 200ml를 붓고 약불에서 데웁니다. 2. 우유가 따뜻해지면 오트밀 60g을 넣고 저으면서 3-4분간 끓입니다. 3. 오트밀이 부드럽게 익으면 불을 끄고 그릇에 담습니다. 4. 바나나를 슬라이스하여 오트밀 위에 예쁘게 올립니다. 5. 호두를 잘게 다져 뿌립니다. 6. 기호에 따라 꿀이나 시나몬 가루를 약간 추가할 수 있습니다.",
                    "tips": "우유 대신 두유 가능",
                    "unsplashQuery": "oatmeal bowl with banana and nuts healthy breakfast"
                  },
                  "dinner": {
                    "name": "연어스테이크 + 퀴노아샐러드",
                    "description": "오메가3와 완전단백질",
                    "calories": 650,
                    "nutrients": {
                      "carbs": 50,
                      "protein": 42,
                      "fat": 24
                    },
                    "reason": "심혈관 건강과 근육 회복에 최적화된 고급 영양 식단입니다. 연어는 EPA와 DHA가 풍부한 오메가-3 지방산의 최고 공급원으로 염증 감소, 혈액순환 개선, 뇌 건강 증진에 탁월합니다. 퀴노아는 9가지 필수 아미노산을 모두 포함한 완전 단백질 식품이며, 철분과 마그네슘이 풍부해 에너지 생성과 근육 기능을 돕습니다. 다양한 색상의 채소는 비타민, 미네랄, 항산화 성분을 제공하여 체내 염증을 줄이고 면역력을 강화합니다. 건강한 지방과 양질의 단백질로 운동 후 회복을 촉진하면서도 체지방 증가를 최소화합니다.",
                    "ingredients": ["연어 150g", "퀴노아 120g", "올리브오일", "레몬", "채소 믹스"],
                    "instructions": "1. 퀴노아 120g을 체에 받쳐 찬물로 2-3번 헹굽니다. 2. 냄비에 퀴노아와 물 240ml를 넣고 끓인 후 약불로 줄여 15분간 익힙니다. 3. 연어는 키친타월로 물기를 제거하고 소금, 후추로 밑간합니다. 4. 팬에 올리브오일을 두르고 중불에서 연어 껍질 면을 먼저 4분, 뒤집어 3분 더 굽습니다. 5. 채소(양상추, 토마토, 오이, 파프리카 등)를 씻어 한입 크기로 자릅니다. 6. 익은 퀴노아, 채소, 올리브오일, 레몬즙을 볼에 담아 버무립니다. 7. 접시에 퀴노아샐러드를 깔고 구운 연어를 올려 완성합니다.",
                    "tips": "레몬즙으로 나트륨 절감",
                    "unsplashQuery": "grilled salmon steak with quinoa salad healthy dinner plate"
                  }
                },
                "Wednesday": {
                  "breakfast": {
                    "name": "두부 스크램블 & 토스트",
                    "description": "식물성 단백질 강화",
                    "calories": 460,
                    "nutrients": {
                      "carbs": 45,
                      "protein": 26,
                      "fat": 15
                    },
                    "reason": "식물성과 동물성 단백질을 균형있게 섭취하는 아침 메뉴입니다. 두부는 저칼로리 고단백 식품으로 100g당 8g의 단백질을 제공하며, 이소플라본 성분이 호르몬 균형과 뼈 건강에 도움이 됩니다. 달걀은 완전 단백질 식품으로 필수 아미노산을 모두 포함하며, 콜린 성분이 뇌 기능 개선에 기여합니다. 통밀식빵의 복합 탄수화물은 지속적인 에너지를 공급하고, 시금치의 철분과 엽산은 빈혈 예방과 신진대사 촉진에 효과적입니다. 채식 선호자에게도 적합한 영양 균형 식단입니다.",
                    "ingredients": ["두부 150g", "달걀 1개", "통밀식빵 1장", "시금치"],
                    "instructions": "1. 두부 150g을 키친타월로 물기를 제거하고 으깹니다. 2. 달걀 1개를 볼에 풀어 소금, 후추로 간합니다. 3. 시금치 한줌을 깨끗이 씻어 물기를 뺍니다. 4. 팬에 올리브오일 1작은술을 두르고 약불에서 시금치를 30초간 볶아 꺼냅니다. 5. 같은 팬에 으깬 두부를 넣고 중불에서 2분간 볶습니다. 6. 풀어놓은 달걀을 부어 나무주걱으로 저으며 스크램블을 만듭니다(2-3분). 7. 통밀식빵을 토스터에 구워 접시에 담고 두부 스크램블과 시금치를 곁들여 완성합니다.",
                    "tips": "기름 최소화",
                    "unsplashQuery": "tofu scrambled eggs with whole wheat toast healthy breakfast plate"
                  },
                  "dinner": {
                    "name": "잡곡밥 + 소고기 불고기 + 샐러드",
                    "description": "단백질·철분 보충",
                    "calories": 680,
                    "nutrients": {
                      "carbs": 65,
                      "protein": 38,
                      "fat": 22
                    },
                    "reason": "근육 회복과 빈혈 예방에 최적화된 균형 식단입니다. 소고기는 흡수율이 높은 헴철분의 최고 공급원으로 빈혈 예방과 산소 운반 능력 향상에 필수적이며, 크레아틴과 카르노신 성분이 근력 강화와 운동 능력 향상에 도움이 됩니다. 잡곡밥(현미, 보리, 수수 등)은 백미보다 비타민 B군과 미네랄이 풍부하여 에너지 대사를 촉진하고 피로 회복을 돕습니다. 양파의 퀘르세틴은 항염 효과가 뛰어나며, 샐러드의 다양한 채소는 비타민 A, C, K와 식이섬유를 제공하여 소화를 돕고 면역력을 강화합니다. 운동하는 분들에게 특히 추천하는 고영양 저녁 식단입니다.",
                    "ingredients": ["잡곡밥 1공기", "소고기 120g", "양파", "샐러드 채소"],
                    "instructions": "1. 소고기 120g을 얇게 슬라이스합니다. 2. 볼에 간장 2큰술, 설탕 1작은술, 다진 마늘 1작은술, 참기름 1작은술, 후추 약간을 섞어 양념장을 만듭니다. 3. 소고기와 양념장을 버무려 10분간 재웁니다. 4. 양파는 채썰고, 샐러드 채소(양상추, 토마토, 오이, 당근 등)는 한입 크기로 준비합니다. 5. 팬에 기름을 두르지 않고 중불에서 양념한 소고기와 양파를 3-4분간 볶습니다. 6. 잡곡밥을 그릇에 담고 불고기를 올립니다. 7. 별도 접시에 샐러드를 담아 함께 제공합니다.",
                    "tips": "양념 당분 줄이기",
                    "unsplashQuery": "korean beef bulgogi with multigrain rice and salad healthy meal"
                  }
                },
                "Thursday": {
                  "breakfast": {
                    "name": "김치두부 달걀부침",
                    "description": "단백질·프로바이오틱스",
                    "calories": 440,
                    "nutrients": {
                      "carbs": 30,
                      "protein": 28,
                      "fat": 18
                    },
                    "reason": "한국 전통 발효식품과 고단백 식재료를 결합한 건강식입니다. 김치의 유산균은 장내 미생물 균형을 개선하고 소화를 돕습니다. 두부는 식물성 단백질의 우수한 공급원이며 칼슘과 철분이 풍부하여 뼈 건강과 혈액 생성을 돕습니다. 달걀 2개로 약 12g의 단백질을 추가 섭취하여 아침 포만감을 높이고, 비타민 B12와 D가 에너지 대사와 면역 기능을 지원합니다. 저탄수화물 고단백 구성으로 체지방 감량에 효과적이며, 한식을 선호하는 분들에게 익숙하고 맛있는 아침 식단입니다.",
                    "ingredients": ["두부 120g", "달걀 2개", "김치 50g", "쪽파"],
                    "instructions": "1. 두부 120g을 으깨서 물기를 최대한 짜냅니다. 2. 김치 50g을 잘게 다집니다(너무 신 김치는 물에 헹구기). 3. 쪽파를 송송 썹니다. 4. 볼에 으깬 두부, 달걀 2개, 다진 김치, 쪽파를 넣고 골고루 섞습니다. 5. 소금과 후추로 간을 맞춥니다. 6. 팬에 식용유를 약간 두르고 중약불로 예열합니다. 7. 반죽을 팬에 부어 동그랗게 펴고 한쪽 면을 3-4분씩 노릇하게 구워 완성합니다.",
                    "tips": "기름 과다 사용 금지",
                    "unsplashQuery": "korean kimchi tofu egg pancake healthy breakfast"
                  },
                  "dinner": {
                    "name": "메밀면 샐러드(닭가슴살)",
                    "description": "가벼운 저녁의 단백질·탄수 균형",
                    "calories": 590,
                    "nutrients": {
                      "carbs": 65,
                      "protein": 35,
                      "fat": 14
                    },
                    "reason": "저녁 늦게 먹어도 소화 부담이 적은 가벼운 식단입니다. 메밀은 글루텐이 없고 루틴 성분이 혈관 건강을 돕는 저GI 식품으로, 일반 면보다 혈당 상승이 완만하여 밤사이 체지방 축적을 줄여줍니다. 닭가슴살의 풍부한 단백질은 수면 중 근육 회복을 촉진하며, 트립토판 성분이 세로토닌 생성을 도와 숙면에 도움이 됩니다. 다양한 채소의 비타민과 미네랄은 피로 회복과 항산화 작용을 하며, 참깨 드레싱의 세사민 성분은 간 기능 개선과 항염 효과가 있습니다. 저녁 운동 후 가볍게 먹기 좋은 메뉴입니다.",
                    "ingredients": ["메밀면 80g", "닭가슴살 100g", "채소", "참깨 드레싱"],
                    "instructions": "1. 메밀면 80g을 끓는 물에 넣고 5-6분간 삶습니다. 2. 삶은 면을 체에 받쳐 찬물로 여러 번 헹궈 전분기를 제거합니다. 3. 닭가슴살 100g을 소금, 후추로 밑간하여 팬에 구워 식힌 후 결대로 찢습니다. 4. 채소(양상추, 오이, 당근, 파프리카)를 채썰어 준비합니다. 5. 큰 볼에 찬물로 헹군 메밀면, 찢은 닭가슴살, 채소를 담습니다. 6. 참깨 드레싱(또는 간장 1큰술, 식초 1큰술, 참기름 1작은술 섞기)을 넣고 골고루 버무립니다. 7. 접시에 담고 통깨를 뿌려 완성합니다.",
                    "tips": "드레싱 양 조절",
                    "unsplashQuery": "soba noodle salad with grilled chicken breast healthy dinner bowl"
                  }
                },
                "Friday": {
                  "breakfast": {
                    "name": "현미주먹밥(참치/김) + 미소된장국",
                    "description": "탄·단·지 균형 아침",
                    "calories": 510,
                    "nutrients": {
                      "carbs": 70,
                      "protein": 22,
                      "fat": 14
                    },
                    "reason": "바쁜 아침에도 간편하게 준비할 수 있는 영양 균형 식단입니다. 참치는 단백질 함량이 높고(100g당 25g) 필수 아미노산이 풍부하여 근육 합성을 돕습니다. 김은 요오드, 칼슘, 철분이 풍부하여 갑상선 기능과 뼈 건강을 지원합니다. 된장국의 발효 대두는 장 건강을 개선하고 단백질을 추가로 제공하며, 된장의 이소플라본은 항암 효과와 혈압 조절에 도움이 됩니다. 현미의 복합 탄수화물은 아침부터 오후까지 지속적인 에너지를 공급하여 업무나 활동 집중력을 유지시킵니다.",
                    "ingredients": ["현미밥 1공기", "참치 60g", "김", "된장", "두부"],
                    "instructions": "1. 참치 통조림 60g의 기름기를 키친타월로 제거합니다. 2. 현미밥 1공기를 볼에 담고 참치를 섞습니다. 3. 손에 물을 묻혀 현미밥을 주먹밥 크기로 동그랗게 빚습니다. 4. 김으로 주먹밥을 감싸줍니다. 5. 냄비에 물 400ml를 끓이고 된장 1큰술을 풀어줍니다. 6. 두부를 깍둑썰어 넣고 2-3분 더 끓입니다. 7. 쪽파를 송송 썰어 국에 넣고 불을 끕니다. 8. 주먹밥과 된장국을 함께 제공합니다.",
                    "tips": "참치 기름 제거",
                    "unsplashQuery": "korean tuna rice ball with miso soup healthy breakfast"
                  },
                  "dinner": {
                    "name": "현미밥 + 고등어구이 + 찐야채",
                    "description": "오메가3·미네랄 보강",
                    "calories": 670,
                    "nutrients": {
                      "carbs": 55,
                      "protein": 40,
                      "fat": 26
                    },
                    "reason": "주말 시작을 앞두고 영양을 충전하는 건강 저녁입니다. 고등어는 DHA와 EPA가 풍부한 등푸른 생선으로, 뇌 기능 향상, 심혈관 질환 예방, 항염 효과가 뛰어납니다. 비타민 D도 풍부하여 뼈 건강과 면역력 강화에 도움이 되며, 단백질 함량이 높아 근육 회복과 성장을 촉진합니다. 찐 야채(브로콜리, 당근)는 비타민 A, C, 식이섬유가 풍부하여 소화를 돕고 항산화 효과를 제공합니다. 현미의 비타민 B군은 에너지 대사를 활성화하여 한 주의 피로를 풀어주며, 건강한 지방과 단백질로 주말 활동에 필요한 영양을 충분히 공급합니다.",
                    "ingredients": ["현미밥 1공기", "고등어 150g", "브로콜리·당근"],
                    "instructions": "1. 고등어 150g을 흐르는 물에 씻어 키친타월로 물기를 제거합니다. 2. 고등어 양면에 소금을 약간 뿌려 10분간 재웁니다. 3. 에어프라이어를 180도로 예열합니다. 4. 고등어를 에어프라이어에 넣고 180도에서 12-15분간 구웁니다(중간에 한번 뒤집기). 5. 브로콜리와 당근을 한입 크기로 자릅니다. 6. 찜기에 채소를 넣고 5-7분간 찝니다(또는 전자레인지에 물을 약간 뿌려 3분). 7. 현미밥을 그릇에 담고 구운 고등어와 찐 야채를 곁들여 완성합니다.",
                    "tips": "짠지/젓갈은 소량",
                    "unsplashQuery": "grilled mackerel fish with steamed vegetables and brown rice healthy dinner"
                  }
                }
              }
            }
            """;
    }

    /**
     * 사용자 프롬프트 구성
     */
    private String buildUserPrompt(InbodyDataRequestDto inbody) {
        String dietaryPreference = inbody.survey() != null && inbody.survey().text() != null ? inbody.survey().text() : "고단백 선호, 한식 위주, 특별한 제약 없음";
        StringBuilder prompt = new StringBuilder();
        prompt.append("다음 정보를 바탕으로 맞춤형 식단을 추천해주세요:\n\n");
        // 기본 정보
        prompt.append("**개인 정보**\n");
        prompt.append(String.format("- 성별: %s\n", inbody.getGenderKorean()));
        prompt.append(String.format("- 연령: %d세\n", inbody.getCurrentAge()));
        prompt.append(String.format("- 체중: %.1f kg\n", inbody.weight()));
        prompt.append(String.format("- BMI: %.1f\n", inbody.bmi()));
        prompt.append(String.format("- 식습관 선호도: %s\n", dietaryPreference));
        // 체성분 정보
        prompt.append("\n**체성분 분석**\n");
        if (inbody.bodyFatPercentage() != null) {
            prompt.append(String.format("- 체지방률: %.1f%%\n", inbody.bodyFatPercentage()));
        }
        if (inbody.muscleMass() != null) {
            prompt.append(String.format("- 근육량: %.1f kg\n", inbody.muscleMass()));
        }
        if (inbody.basalMetabolism() != null) {
            prompt.append(String.format("- 기초대사량: %d kcal\n", inbody.basalMetabolism()));
        }
        if (inbody.visceralFatLevel() != null) {
            prompt.append(String.format("- 내장지방레벨: %.1f\n", inbody.visceralFatLevel()));
        }
        // 조절 지표
        prompt.append("\n**체중 조절 지표**\n");
        if (inbody.weightControl() != null) {
            String controlType = inbody.weightControl() > 0 ? "체중 증가" : inbody.weightControl() < 0 ? "체중 감소" : "체중 유지";
            prompt.append(String.format("- 체중조절: %.1f kg (%s)\n", Math.abs(inbody.weightControl()), controlType));
        }
        if (inbody.fatControl() != null) {
            String fatControlType = inbody.fatControl() > 0 ? "지방 증가" : inbody.fatControl() < 0 ? "지방 감소" : "지방 유지";
            prompt.append(String.format("- 지방조절: %.1f kg (%s)\n", Math.abs(inbody.fatControl()), fatControlType));
        }
        if (inbody.muscleControl() != null) {
            String muscleControlType = inbody.muscleControl() > 0 ? "근육 증가" : inbody.muscleControl() < 0 ? "근육 감소" : "근육 유지";
            prompt.append(String.format("- 근육조절: %.1f kg (%s)\n", Math.abs(inbody.muscleControl()), muscleControlType));
        }
        // 설문조사 데이터가 없으면 예외 처리
        if (inbody.survey() == null) {
            log.error("❌ 설문조사 데이터가 없습니다. 식단 추천을 위해서는 설문조사가 필수입니다.");
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "식단 추천을 위해서는 설문조사가 필요합니다. 먼저 설문조사를 작성해주세요.");
        }
        // mealsToGenerate 우선, 없으면 기본값
        List<String> mealTypes = (inbody.survey().mealsToGenerate() != null && !inbody.survey().mealsToGenerate().isEmpty()) ? inbody.survey().mealsToGenerate() : List.of("breakfast", "lunch", "dinner");
        int mealCount = mealTypes.size();
        log.info("🍽️ 선택된 끼니: {} (총 {}끼)", mealTypes, mealCount);
        prompt.append("\n위 데이터를 종합하여 건강하고 지속 가능한 식단을 추천해주세요.");
        // 핵심 요구사항
        prompt.append("\n\n[요구사항]");
        prompt.append("\n- 요일은 Monday~Friday 모두 생성하세요.");
        prompt.append(String.format("\n- **중요**: 각 요일에는 오직 다음 끼니만 생성하세요: %s", String.join(", ", mealTypes)));
        prompt.append(String.format("\n- **절대 금지**: 선택되지 않은 끼니(breakfast, lunch, dinner, snack 중 %s가 아닌 것)는 절대 포함하지 마세요.", String.join(", ", mealTypes)));
        
        // 선택된 끼니를 명확히 나열
        prompt.append(String.format("\n- **생성해야 할 끼니**: %s", String.join(", ", mealTypes)));
        
        // 선택되지 않은 끼니 명시
        List<String> allMeals = List.of("breakfast", "lunch", "dinner", "snack");
        List<String> excludedMeals = allMeals.stream()
            .filter(meal -> !mealTypes.contains(meal))
            .toList();
        if (!excludedMeals.isEmpty()) {
            prompt.append(String.format("\n- **포함하지 말아야 할 끼니**: %s (이 끼니들은 절대 생성하지 마세요)", String.join(", ", excludedMeals)));
        }
        
        prompt.append("\n- 각 Meal에는 calories(숫자)와 nutrients(숫자 3종: carbs/protein/fat)를 포함하세요.");
        prompt.append("\n- 한국인의 기호에 맞는 현실적인 메뉴로 구성하세요.");
        prompt.append("\n- 출력은 시스템에서 지정한 JSON 스키마만 사용하세요.");
        
        // 예시 JSON 구조 명시
        prompt.append(String.format("\n\n**JSON 구조 예시 (선택된 끼니: %s):**", String.join(", ", mealTypes)));
        prompt.append("\n{");
        prompt.append("\n  \"diets\": {");
        prompt.append("\n    \"Monday\": {");
        for (String mealType : mealTypes) {
            prompt.append(String.format("\n      \"%s\": { Meal객체 },", mealType));
        }
        prompt.append("\n    },");
        prompt.append("\n    \"Tuesday\": {");
        for (String mealType : mealTypes) {
            prompt.append(String.format("\n      \"%s\": { Meal객체 },", mealType));
        }
        prompt.append("\n    },");
        prompt.append("\n    ... (Wednesday, Thursday, Friday도 동일한 구조)");
        prompt.append("\n  }");
        prompt.append("\n}");
        // 끼니별 칼로리 가이드(간결)
        if (mealCount == 1) {
            prompt.append(String.format("\n- 칼로리 가이드: %s 400-550 kcal", mealTypes.get(0)));
        } else if (mealCount == 2) {
            prompt.append("\n- 칼로리 가이드: 각 끼니 600-800 kcal");
        } else if (mealCount == 3 && mealTypes.containsAll(List.of("breakfast","lunch","dinner"))) {
            prompt.append("\n- 칼로리 가이드: 아침 450-600, 점심 600-800, 저녁 500-700 kcal");
        } else if (mealCount >= 4) {
            prompt.append("\n- 칼로리 가이드: 주요끼니 400-600, 간식 200-300 kcal");
        }
        
        // reason과 instructions 강조 추가
        prompt.append("\n\n**매우 중요 - reason(추천 이유) 작성 규칙:**");
        prompt.append("\n- 절대 '건강에 좋다', '영양가 높다' 같은 단순한 표현 금지");
        prompt.append("\n- 최소 150자 이상, 4-5문장 이상으로 작성");
        prompt.append("\n- 3-4개의 핵심 포인트를 콜론(:)으로 구분하여 각각 2-3문장으로 상세히 설명");
        prompt.append("\n- 구체적인 영양소 수치 포함 (예: 단백질 23g, 식이섬유 5g)");
        prompt.append("\n- 사용자의 체형, 목표, 설문 내용과의 연관성을 반드시 명시");
        prompt.append("\n\n**매우 중요 - instructions(조리방법) 작성 규칙:**");
        prompt.append("\n- 절대 '재료를 섞는다', '볶는다' 같은 단순한 표현 금지");
        prompt.append("\n- 최소 6-8단계 이상으로 작성");
        prompt.append("\n- 각 단계마다 재료 양, 온도, 시간, 방법을 구체적으로 명시");
        prompt.append("\n- 각 단계는 최소 1문장 이상으로 작성하고, 왜 그렇게 해야 하는지 간단한 이유도 포함");
        prompt.append("\n- 초보자도 따라할 수 있을 만큼 상세하게 작성");
        
        return prompt.toString();
    }

}