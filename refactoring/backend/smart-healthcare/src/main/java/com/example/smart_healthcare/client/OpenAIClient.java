package com.example.smart_healthcare.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * OpenAI API 호출을 담당하는 단일 클라이언트
 * 모든 AI 서비스에서 공통으로 사용
 */
@Slf4j
@Component
public class OpenAIClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String baseUrl;

    public OpenAIClient(WebClient.Builder webClientBuilder, 
                       ObjectMapper objectMapper,
                       @Value("${openai.api.base-url:https://api.openai.com/v1}") String baseUrl,
                       @Value("${openai.api.key:}") String apiKey) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        
        // API 키 유효성 검증
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.error("❌ OpenAI API 키가 설정되지 않음. AI 기능이 비활성화됩니다.");
            log.error("❌ 환경변수 OPENAI_API_KEY를 설정하거나 application.properties에서 openai.api.key를 설정하세요.");
        } else {
            log.info("✅ OpenAI API 키 설정됨 (길이: {})", apiKey.length());
        }
        
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .codecs(configurer -> {
                    // 요청/응답 크기 제한 증가
                    configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024); // 10MB
                })
                .build();
    }

    /**
     * API 키 유효성 검증
     */
    public boolean isApiKeyValid() {
        return apiKey != null && !apiKey.trim().isEmpty() && !apiKey.equals("your-api-key-here");
    }

    /**
     * Chat Completions API 호출 (JSON 응답)
     */
    public <T> T postJson(String path, Object requestBody, Class<T> responseType) {
        log.info("🔑 API 키 유효성 검사: isValid={}", isApiKeyValid());
        if (!isApiKeyValid()) {
            log.warn("❌ OpenAI API 키가 유효하지 않음. 호출을 건너뜁니다.");
            return null;
        }

        try {
            log.info("🚀 OpenAI API 호출 시작: {}", baseUrl + path);
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);
            log.info("📤 요청 본문 크기: {} bytes", requestBodyJson.length());
            log.info("📤 요청 본문 미리보기: {}", requestBodyJson.length() > 500 ? requestBodyJson.substring(0, 500) + "..." : requestBodyJson);

            T response = webClient.post()
                    .uri(path)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(responseType)
                    .timeout(Duration.ofSeconds(180)) // 120초 → 180초로 증가 (긴 응답 처리)
                    .retryWhen(Retry.backoff(2, Duration.ofSeconds(3)) // 재시도 횟수를 2회로 줄이고 간격 증가
                            .filter(throwable -> {
                                // 타임아웃이나 네트워크 오류만 재시도
                                boolean shouldRetry = throwable instanceof java.util.concurrent.TimeoutException ||
                                       throwable instanceof org.springframework.web.reactive.function.client.WebClientRequestException ||
                                       (throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException &&
                                        ((org.springframework.web.reactive.function.client.WebClientResponseException) throwable).getStatusCode().is5xxServerError());
                                if (shouldRetry) {
                                    log.warn("⚠️ OpenAI API 재시도 대상 예외: {}", throwable.getClass().getSimpleName());
                                }
                                return shouldRetry;
                            })
                            .doBeforeRetry(retrySignal -> {
                                log.warn("🔄 OpenAI API 재시도 {}/2회...", retrySignal.totalRetries() + 1);
                            }))
                    .doOnError(throwable -> {
                        if (throwable instanceof java.util.concurrent.TimeoutException) {
                            log.error("⏰ OpenAI API 타임아웃 (180초 초과): {}", throwable.getMessage());
                        } else if (throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException) {
                            org.springframework.web.reactive.function.client.WebClientResponseException webClientEx = 
                                (org.springframework.web.reactive.function.client.WebClientResponseException) throwable;
                            log.error("🌐 OpenAI API HTTP 오류: {}", webClientEx.getStatusCode());
                            log.error("🌐 응답 본문: {}", webClientEx.getResponseBodyAsString());
                        } else {
                            log.error("🌐 OpenAI API 네트워크 오류: {} - {}", throwable.getClass().getSimpleName(), throwable.getMessage());
                        }
                    })
                    .block();

            log.info("✅ OpenAI API 호출 성공: responseType={}", responseType.getSimpleName());
            if (response != null) {
                log.info("✅ 응답 객체 생성 성공: {}", response.getClass().getSimpleName());
            }
            return response;

        } catch (Exception e) {
            log.error("❌ OpenAI API 호출 실패: {}", e.getMessage(), e);
            log.error("❌ 예외 타입: {}", e.getClass().getSimpleName());
            log.error("❌ 예외 스택 트레이스:", e);
            
            // WebClientResponseException인 경우 추가 정보 출력
            if (e instanceof org.springframework.web.reactive.function.client.WebClientResponseException) {
                org.springframework.web.reactive.function.client.WebClientResponseException webClientEx = 
                    (org.springframework.web.reactive.function.client.WebClientResponseException) e;
                log.error("❌ HTTP 상태 코드: {}", webClientEx.getStatusCode());
                log.error("❌ 응답 본문: {}", webClientEx.getResponseBodyAsString());
                log.error("❌ 응답 헤더: {}", webClientEx.getHeaders());
            }
            
            return null;
        }
    }

    /**
     * 표준 Chat Completions 호출 (Map 응답)
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> chatCompletions(Map<String, Object> request) {
        log.info("🔍 OpenAI API 호출 디버깅 정보:");
        log.info("  - Base URL: {}", baseUrl);
        log.info("  - API 키 길이: {}", apiKey != null ? apiKey.length() : 0);
        log.info("  - API 키 유효성: {}", isApiKeyValid());
        log.info("  - 요청 경로: /chat/completions");
        log.info("  - 요청 본문 키: {}", request.keySet());
        
        Map<String, Object> response = postJson("/chat/completions", request, Map.class);
        
        if (response != null) {
            log.info("✅ OpenAI API 응답 수신 성공:");
            log.info("  - 응답 키: {}", response.keySet());
            if (response.containsKey("choices")) {
                log.info("  - choices 수: {}", ((List<?>) response.get("choices")).size());
            }
            if (response.containsKey("usage")) {
                log.info("  - usage 정보: {}", response.get("usage"));
            }
        } else {
            log.error("❌ OpenAI API 응답이 null입니다.");
        }
        
        return response;
    }

    /**
     * 기본 모델 설정
     */
    public String getDefaultModel() {
        // gpt-4o 모델 사용 (높은 품질의 추천을 위해)
        return "gpt-4o";
    }

    /**
     * 기본 온도 설정
     */
    public double getDefaultTemperature() {
        // 응답의 다양성과 일관성 사이에서 적당한 균형을 위해 0.5 사용
        return 0.5;
    }

    /**
     * 기본 최대 토큰 설정
     */
    public int getDefaultMaxTokens() {
        // gpt-4o에서 긴 JSON 응답을 위해 8192 사용
        // (운동/식단 추천은 상세한 내용으로 인해 긴 응답이 필요)
        return 8192;
    }
}
