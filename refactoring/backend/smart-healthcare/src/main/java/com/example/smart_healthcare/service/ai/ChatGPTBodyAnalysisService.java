package com.example.smart_healthcare.service.ai;

import com.example.smart_healthcare.client.OpenAIClient;
import com.example.smart_healthcare.dto.response.BodyAnalysisResponseDto;
import com.example.smart_healthcare.dto.request.InbodyDataRequestDto;
import com.example.smart_healthcare.entity.AIBodyAnalysisResult;
import com.example.smart_healthcare.entity.InbodyRecord;
import com.example.smart_healthcare.entity.User;
import com.example.smart_healthcare.repository.AIBodyAnalysisResultRepository;
import com.example.smart_healthcare.repository.InbodyRecordRepository;
import com.example.smart_healthcare.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 기반 체형 분석 서비스
 * OpenAI ChatGPT를 활용하여 체형 분석 결과를 제공
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatGPTBodyAnalysisService {
    
    private final OpenAIClient openAIClient;
    private final ObjectMapper objectMapper;
    private final AIBodyAnalysisResultRepository analysisResultRepository;
    private final UserRepository userRepository;
    private final InbodyRecordRepository inbodyRecordRepository;
    
    /**
     * 인바디 데이터를 기반으로 체형 분석 수행 (외부 API 호출만)
     * - 트랜잭션 없음
     * - DB 저장은 별도 서비스에서 처리
     */
    public String analyzeBodyType(InbodyDataRequestDto inbody) {
        try {
            log.info("🔍 체형 분석 시작: 성별={}, 나이={}, BMI={}", 
                    inbody.getGenderKorean(), inbody.getCurrentAge(), inbody.bmi());
            
            // 1. 프롬프트 구성
            String systemPrompt = buildSystemPrompt();
            String userPrompt = buildUserPrompt(inbody);
            
            // 2. ChatGPT API 요청 구성
            Map<String, Object> request = new HashMap<>();
            request.put("model", openAIClient.getDefaultModel());
            request.put("messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userPrompt)
            ));
            // 체형 분석은 보수적으로: temperature를 0.3으로 고정
            request.put("temperature", 0.3);
            request.put("max_tokens", openAIClient.getDefaultMaxTokens());
            
            // 3. API 호출
            Map<String, Object> response = openAIClient.chatCompletions(request);
            
            if (response == null) {
                throw new RuntimeException("OpenAI API 응답이 null입니다.");
            }
            
            // 4. 응답 파싱
            String content = extractContentFromResponse(response);
            log.info("📝 GPT 응답 내용: {}", content);
            
            BodyAnalysisResponseDto result = parseGptResponse(content);
            log.info("✅ 체형 분석 완료: {}", result.label());
            
            // 5. JSON 문자열로 반환 (DB 저장은 별도 서비스에서 처리)
            return objectMapper.writeValueAsString(result);
            
        } catch (Exception e) {
            log.error("❌ 체형 분석 실패: {}", e.getMessage(), e);
            // 예외를 그대로 던져서 컨트롤러에서 처리
            throw new RuntimeException("체형 분석 중 오류가 발생했습니다: " + e.getMessage(), e);
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
        throw new RuntimeException("ChatGPT 응답에서 content를 추출할 수 없습니다.");
    }
    
    /**
     * GPT 응답을 BodyAnalysisResponseDto로 파싱
     */
    private BodyAnalysisResponseDto parseGptResponse(String content) {
        try {
            log.info("🔍 GPT 응답 파싱 시작: length={}", content != null ? content.length() : 0);
            if (content == null || content.isBlank()) {
                throw new RuntimeException("GPT 응답이 비어 있습니다.");
            }

            String originalContent = content;

            // 1) 코드펜스 제거
            String cleaned = stripCodeFences(content);
            // 2) 첫 번째 JSON 객체만 추출
            String json = extractFirstJsonObject(cleaned);

            if (json != null && json.trim().startsWith("{")) {
                log.debug("🔍 JSON 파싱 시도: {}", json.length() > 300 ? json.substring(0, 300) + "..." : json);
                BodyAnalysisResponseDto result = objectMapper.readValue(json, BodyAnalysisResponseDto.class);
                log.info("✅ JSON 파싱 성공: label={}", result.label());
                return result;
            }

            // 3) 여전히 JSON을 찾지 못한 경우, 텍스트 응답으로 간주하고 기본 분석 결과로 fallback
            log.warn("⚠️ 유효한 JSON을 찾지 못해 텍스트 기반 기본 분석으로 fallback: {}",
                    originalContent.substring(0, Math.min(150, originalContent.length())));
            return buildDefaultAnalysisFallback(originalContent);

        } catch (Exception e) {
            log.error("❌ GPT 응답 파싱 실패: {}, 응답: {}", e.getMessage(), content);
            // 완전 실패 시에는 '분석불가'로 반환
            return buildErrorFallback();
        }
    }

    /**
     * ```json ... ``` 또는 ``` ... ``` 제거
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

    /**
     * JSON 파싱 실패 시 사용할 기본 분석 결과 (텍스트 기반)
     */
    private BodyAnalysisResponseDto buildDefaultAnalysisFallback(String content) {
        return new BodyAnalysisResponseDto(
                null, // id는 저장 시 생성됨
                "균형형",
                content != null && content.length() > 100 ? content.substring(0, 100) + "..." : (content != null ? content : "AI 분석 결과"),
                "AI 분석 결과",
                "규칙적인 운동과 균형잡힌 식단을 유지하세요.",
                "낮음",
                "전반적으로 균형잡힌 근육 분포를 보입니다.",
                "대사 건강 상태가 양호합니다.",
                "체성분 구성이 적절합니다.",
                null, // bmiCategory
                null, // bodyFatCategory
                null, // visceralFatCategory
                null, // inbodyScore
                "AI",
                null // analyzedAt은 저장 시 생성됨
        );
    }

    /**
     * 완전한 파싱 실패 시 사용할 '분석불가' 결과
     */
    private BodyAnalysisResponseDto buildErrorFallback() {
        return new BodyAnalysisResponseDto(
                null, // id는 저장 시 생성됨
                "분석불가", "응답 파싱 실패", "기술적 오류", "잠시 후 다시 시도해주세요.",
                "알 수 없음", "분석 불가", "분석 불가", "분석 불가",
                null, // bmiCategory
                null, // bodyFatCategory
                null, // visceralFatCategory
                null, // inbodyScore
                "AI_FAILED_FALLBACK",
                null // analyzedAt은 저장 시 생성됨
        );
    }
    
    /**
     * 시스템 프롬프트 구성 - 전문적이고 세부적인 분석 지시
     * 인바디의 거의 모든 항목(30여 개)을 최대한 활용하도록 요구한다.
     */
    private String buildSystemPrompt() {
        return """
            당신은 한국의 전문 체성분 분석가이자 운동생리학 전문가입니다.
            
            사용자가 제공한 인바디 측정 데이터(체중, BMI, 체지방률, 근육량, 분절 근육/지방, 내장지방, 복부지방률, 허리둘레, 체중/지방/근육 조절량, 인바디 점수 등)를
            가능한 한 **모두 반영**하여 체형을 분석해야 합니다.
            
            [세분화된 체형 분류 기준]
            - BMI: 16-18.5(마름), 18.5-22(날씬), 20-23(근육형날씬), 18.5-23(적정), 23-26(근육형), 23-27(운동선수급),
                    17-19(약간마름), 23-25(과체중), 25-28(경도비만), 28-32(비만), 23-27(마른비만), 28+(근육형비만),
                    32+(고도비만), 25+(복부비만형)
            - 체지방률(남성): 6-10%(마름), 8-12%(운동선수급/근육형날씬), 10-15%(날씬), 12-18%(적정/근육형),
                           20-25%(과체중), 25-30%(경도비만/근육형비만), 25%+(마른비만), 30%+(비만), 35%+(고도비만), 30%+(복부비만형)
            - 체지방률(여성): 12-16%(마름), 14-18%(운동선수급), 14-20%(근육형날씬), 16-22%(날씬),
                           18-25%(적정/근육형), 28-32%(과체중), 32-37%(경도비만/근육형비만),
                           35%+(마른비만), 37%+(비만), 40%+(고도비만), 37%+(복부비만형)
            - 내장지방레벨: 1-9(정상), 10-14(주의), 15+(위험/복부비만형)
            - 인바디점수: 80+(우수), 70-79(양호), 60-69(표준), 60 미만(관리필요)
            - 분절 근육량/체지방량: 좌우/상하체 불균형이 10% 이상이면 “불균형”으로 간주
            
            [체형 라벨 후보 (14개 세분화된 분류)]
            운동선수급, 근육형, 적정, 날씬, 근육형날씬, 약간마름, 마름,
            과체중, 경도비만, 비만, 마른비만, 고도비만, 복부비만형, 근육형비만
            
            [분석 시 반드시 다루어야 할 요소들]
            1. 전체적인 체성분 균형
               - 체중, BMI, 체지방률, 근육량, 제지방량, 총체수분을 함께 고려하여
                 “체성분이 얼마나 건강한지”를 한 문단 이상으로 상세히 설명합니다.
            2. 분절 근육/지방 분포와 좌우·상하체 균형
               - 팔/다리/몸통의 근육량과 체지방량을 비교하여
                 어느 부위가 약한지, 어느 부위가 강한지, 좌우 차이가 있는지를 구체적인 수치와 함께 설명합니다.
            3. 대사 건강 지표
               - 기초대사량, 내장지방레벨, 복부지방률, 허리둘레, 비만도 등을 활용해
                 대사질환(당뇨, 고혈압, 심혈관 질환 등)의 위험도를 판단하고 이유를 제시합니다.
            4. 체중/지방/근육 조절량
               - “체중조절, 지방조절, 근육조절” 항목을 이용해
                 얼마만큼 감량·증량이 필요한지, 그 방향이 “감량 위주 / 근육 증가 위주 / 체중 유지” 중 어디에 해당하는지 설명합니다.
            5. 연령·성별 표준 대비 평가
               - 같은 성별/연령대 평균과 비교했을 때 어떤 점이 강점/약점인지, 구체적인 표현으로 설명합니다.
            6. 종합 건강 위험도 및 관리 우선순위
               - 건강Risk를 “낮음/보통/높음” 중 하나로 분류하되,
                 왜 그렇게 판단했는지(인바디 점수, 내장지방, 복부지방률, 혈관·대사 위험 등)를 2~3문장으로 구체적으로 설명하고
                 특히 관리가 필요한 항목(복부 지방, 근육 부족, 저체중 등)을 2~3가지로 요약합니다.
            
            [특별 주의 체형 판별 규칙]
            - 복부비만형: 내장지방 레벨이 15 이상이거나, 복부지방률/허리둘레가 높은데 체중/전신 BMI는 상대적으로 낮은 경우.
            - 고도비만: BMI 32 이상이거나, 체지방률이 매우 높고 비만도가 크게 증가한 경우.
            - 근육형비만: 근육량은 높은데 체지방률과 BMI도 함께 높은 경우.
            - 마른비만: 체중과 BMI는 정상 또는 낮지만, 체지방률과 복부지방률이 높은 경우.
            
            [출력 형식 (JSON 스키마)]
            아래 JSON 필드를 모두 채워서 하나의 JSON 객체로만 응답하세요.
            각 텍스트 필드는 최소 2~3문장 이상의 한국어 문단으로 작성합니다
            (healthRisk, bmiCategory, bodyFatCategory, visceralFatCategory는 짧은 문장/키워드도 허용).
            {
              "label": "위의 14개 분류 중 하나를 선택",
              "summary": "현재 체형을 한 문장으로 요약 (예: '상체 근육이 발달한 근육형 체형으로, 체지방은 약간 높은 편입니다.')",
              "reasoning": "BMI, 체지방률, 근육량, 내장지방, 체중/지방/근육 조절량 등 핵심 수치를 직접 언급하면서 체형을 이렇게 분류한 이유를 상세히 설명",
              "tips": "체형에 따라 우선적으로 실천하면 좋은 운동/식단/생활 습관을 3~5개 항목 정도로 구체적으로 제안",
              "healthRisk": "건강 위험도 요약과 이유 (예: '낮음 - 인바디 점수와 내장지방 수치가 모두 양호하여 대사질환 위험이 낮은 편입니다. 다만 복부지방이 약간 높아 향후 체중 증가에만 주의하면 됩니다.' 처럼 수준 + 근거를 최소 2문장으로 작성)",
              "muscleBalance": "팔/다리/몸통의 근육량과 좌우 균형을 분석하여, 어느 부위를 강화/보완해야 하는지 설명",
              "metabolicHealth": "기초대사량, 내장지방레벨, 복부지방률, 허리둘레 등을 근거로 대사 건강 상태와 질환 위험도를 평가",
              "bodyComposition": "총체수분, 단백질, 무기질, 체지방량, 제지방량, 비만도를 종합하여 체성분의 장점과 보완점을 정리",
              "bmiCategory": "BMI를 기반으로 한 간단 분류 (예: '정상', '과체중', '비만', '저체중' 등)",
              "bodyFatCategory": "체지방률을 기반으로 한 분류 (예: '정상 범위', '높은 편', '매우 높은 편' 등)",
              "visceralFatCategory": "내장지방레벨/복부지방률/허리둘레를 바탕으로 한 복부 비만 위험도 (예: '정상', '주의', '위험' 등)",
              "inbodyScore": "인바디점수가 제공된 경우 그대로 숫자로 기입"
            }
            
            반드시 위 JSON 형식만 출력하고, 설명 문장이나 마크다운, 코드블록(```json 등)은 포함하지 마세요.
            """;
    }
    
    /**
     * 사용자 프롬프트 구성 - 모든 인바디 데이터 포함
     */
    private String buildUserPrompt(InbodyDataRequestDto inbody) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("다음 인바디 데이터를 종합적으로 분석해주세요:\n\n");
        
        // 기본 정보
        prompt.append("**기본 정보**\n");
        prompt.append(String.format("- 성별: %s\n", inbody.getGenderKorean()));
        prompt.append(String.format("- 연령: %d세\n", inbody.getCurrentAge()));
        prompt.append(String.format("- 체중: %.1f kg\n", inbody.weight()));
        prompt.append(String.format("- BMI: %.1f\n", inbody.bmi()));
        
        // 핵심 체성분
        prompt.append("\n**핵심 체성분**\n");
        if (inbody.bodyFatPercentage() != null) {
            prompt.append(String.format("- 체지방률: %.1f%%\n", inbody.bodyFatPercentage()));
        }
        if (inbody.muscleMass() != null) {
            prompt.append(String.format("- 근육량: %.1f kg\n", inbody.muscleMass()));
        }
        if (inbody.skeletalMuscleMass() != null) {
            prompt.append(String.format("- 골격근량: %.1f kg\n", inbody.skeletalMuscleMass()));
        }
        if (inbody.fatFreeMass() != null) {
            prompt.append(String.format("- 제지방량: %.1f kg\n", inbody.fatFreeMass()));
        }
        if (inbody.bodyFatMass() != null) {
            prompt.append(String.format("- 체지방량: %.1f kg\n", inbody.bodyFatMass()));
        }
        
        // 체수분 및 영양소
        prompt.append("\n**체수분 및 영양소**\n");
        appendIfNotNull(prompt, "총체수분", inbody.totalBodyWater(), "L");
        appendIfNotNull(prompt, "단백질", inbody.protein(), "kg");
        appendIfNotNull(prompt, "무기질", inbody.mineral(), "kg");
        
        // 분절 근육량 (균형 분석)
        prompt.append("\n**분절 근육량 (균형 분석)**\n");
        appendIfNotNull(prompt, "오른팔 근육량", inbody.rightArmMuscleMass(), "kg");
        appendIfNotNull(prompt, "왼팔 근육량", inbody.leftArmMuscleMass(), "kg");
        appendIfNotNull(prompt, "몸통 근육량", inbody.trunkMuscleMass(), "kg");
        appendIfNotNull(prompt, "오른다리 근육량", inbody.rightLegMuscleMass(), "kg");
        appendIfNotNull(prompt, "왼다리 근육량", inbody.leftLegMuscleMass(), "kg");
        
        // 분절 체지방량
        prompt.append("\n**분절 체지방량**\n");
        appendIfNotNull(prompt, "오른팔 체지방량", inbody.rightArmFatMass(), "kg");
        appendIfNotNull(prompt, "왼팔 체지방량", inbody.leftArmFatMass(), "kg");
        appendIfNotNull(prompt, "몸통 체지방량", inbody.trunkFatMass(), "kg");
        appendIfNotNull(prompt, "오른다리 체지방량", inbody.rightLegFatMass(), "kg");
        appendIfNotNull(prompt, "왼다리 체지방량", inbody.leftLegFatMass(), "kg");
        
        // 건강 지표
        prompt.append("\n**건강 지표**\n");
        if (inbody.inbodyScore() != null) {
            String scoreLevel = inbody.inbodyScore() >= 80 ? "우수" :
                               inbody.inbodyScore() >= 70 ? "양호" :
                               inbody.inbodyScore() >= 60 ? "표준" : "관리필요";
            prompt.append(String.format("- 인바디점수: %d점 (%s)\n", inbody.inbodyScore(), scoreLevel));
        }
        appendIfNotNull(prompt, "내장지방레벨", inbody.visceralFatLevel(), "");
        appendIfNotNull(prompt, "복부지방률", inbody.abdominalFatPercentage(), "%");
        appendIfNotNull(prompt, "허리둘레", inbody.waistCircumference(), "cm");
        if (inbody.basalMetabolism() != null) {
            prompt.append(String.format("- 기초대사량: %d kcal\n", inbody.basalMetabolism()));
        }
        
        // 체중 조절 지표
        prompt.append("\n**체중 조절 지표**\n");
        appendIfNotNull(prompt, "적정체중", inbody.idealWeight(), "kg");
        appendIfNotNull(prompt, "체중조절", inbody.weightControl(), "kg");
        appendIfNotNull(prompt, "지방조절", inbody.fatControl(), "kg");
        appendIfNotNull(prompt, "근육조절", inbody.muscleControl(), "kg");
        appendIfNotNull(prompt, "비만도", inbody.obesityDegree(), "%");
        appendIfNotNull(prompt, "골무기질량", inbody.boneMineralContent(), "kg");
        
        prompt.append("\n위 모든 데이터를 종합하여 체형을 정확히 분석하고, 실용적인 건강 관리 조언을 제공해주세요.");
        
        return prompt.toString();
    }
    
    /**
     * null이 아닌 값만 프롬프트에 추가하는 헬퍼 메서드
     */
    private void appendIfNotNull(StringBuilder sb, String name, Object value, String unit) {
        if (value != null) {
            if (value instanceof Float) {
                sb.append(String.format("- %s: %.1f%s\n", name, (Float) value, unit));
            } else {
                sb.append(String.format("- %s: %s%s\n", name, value, unit));
            }
        }
    }
    
    /**
     * 분석 결과를 별도 트랜잭션에서 저장
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveAnalysisResultInSeparateTransaction(BodyAnalysisResponseDto result, Long userId) {
        saveAnalysisResult(result, userId);
    }
    
    /**
     * 분석 결과를 데이터베이스에 저장
     */
    private void saveAnalysisResult(BodyAnalysisResponseDto result, Long userId) {
        try {
            log.info("🔍 저장 시작: userId={}", userId);
            
            // 1. 사용자 조회
            log.info("🔍 사용자 조회 중: userId={}", userId);
            
            // 모든 사용자 조회해서 디버깅
            long totalUsers = userRepository.count();
            log.info("🔍 전체 사용자 수: {}", totalUsers);
            
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId + " (전체 사용자 수: " + totalUsers + ")"));
            log.info("✅ 사용자 조회 성공: user={}", user.getEmail());
            
            // 2. 최신 인바디 기록 조회 (선택사항)
            log.info("🔍 인바디 기록 조회 중...");
            Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<InbodyRecord> inbodyPage = inbodyRecordRepository.findByUserId(user.getId(), pageable);
            InbodyRecord inbodyRecord = inbodyPage.getContent().isEmpty() ? null : inbodyPage.getContent().get(0);
            log.info("✅ 인바디 기록 조회 완료: {}", inbodyRecord != null ? "기록 있음" : "기록 없음");
            
            // 3. 엔티티로 변환
            log.info("🔍 엔티티 변환 중...");
            AIBodyAnalysisResult entity = AIBodyAnalysisResult.toEntity(result, user, inbodyRecord);
            log.info("✅ 엔티티 변환 완료: label={}", entity.getLabel());
            
            // 4. 데이터베이스 저장
            log.info("🔍 데이터베이스 저장 중...");
            AIBodyAnalysisResult savedEntity = analysisResultRepository.save(entity);
            log.info("✅ 데이터베이스 저장 완료: id={}", savedEntity.getId());
            
            log.info("📊 분석 결과 저장 완료: userId={}, label={}", userId, result.label());
            
        } catch (Exception e) {
            log.error("❌ 분석 결과 저장 실패: {}", e.getMessage(), e);
            log.error("❌ 예외 스택 트레이스:", e);
            // 저장 실패해도 분석 결과는 반환
        }
    }
}