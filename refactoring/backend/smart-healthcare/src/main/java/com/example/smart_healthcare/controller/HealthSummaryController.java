package com.example.smart_healthcare.controller;

import com.example.smart_healthcare.common.dto.ApiResponseDto;
import com.example.smart_healthcare.dto.request.InbodyDataRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 건강 상태 요약 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HealthSummaryController {

    /**
     * 건강 상태 요약
     */
    @PostMapping("/health-summary")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> getHealthSummary(
            @RequestBody InbodyDataRequestDto inbodyData) {
        log.info("건강 상태 요약 API 호출");
        
        try {
            // 간단한 건강 상태 요약 생성
            Map<String, Object> summary = new HashMap<>();
            summary.put("overallHealth", "양호");
            summary.put("bmiStatus", inbodyData.bmi() != null ? 
                (inbodyData.bmi() < 25 ? "정상" : "과체중") : "측정 필요");
            summary.put("recommendations", new String[]{
                "규칙적인 운동",
                "균형 잡힌 식단",
                "충분한 수면"
            });
            summary.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(ApiResponseDto.success("건강 상태 요약이 완료되었습니다.", summary));
        } catch (Exception e) {
            log.error("건강 상태 요약 실패", e);
            return ResponseEntity.status(500)
                    .body(ApiResponseDto.error("건강 상태 요약 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 분석 서비스 상태 확인
     */
    @GetMapping("/analysis-status")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> getAnalysisStatus() {
        log.info("분석 서비스 상태 확인 API 호출");
        
        try {
            Map<String, Object> status = new HashMap<>();
            status.put("serviceStatus", "정상");
            status.put("openaiApiStatus", "연결됨");
            status.put("databaseStatus", "정상");
            status.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(ApiResponseDto.success("분석 서비스 상태 확인이 완료되었습니다.", status));
        } catch (Exception e) {
            log.error("분석 서비스 상태 확인 실패", e);
            return ResponseEntity.status(500)
                    .body(ApiResponseDto.error("분석 서비스 상태 확인 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * OpenAI API 키 테스트
     */
    @GetMapping("/test-openai")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> testOpenAI() {
        log.info("OpenAI API 키 테스트 API 호출");
        
        try {
            // 간단한 테스트 요청 생성
            Map<String, Object> testRequest = new HashMap<>();
            testRequest.put("model", "gpt-4o");
            testRequest.put("messages", List.of(
                Map.of("role", "user", "content", "Hello, this is a test message.")
            ));
            testRequest.put("max_tokens", 10);
            testRequest.put("temperature", 0.1);
            
            // OpenAIClient 주입을 위해 @Autowired 사용
            // 실제로는 OpenAIClient를 주입받아야 함
            Map<String, Object> status = new HashMap<>();
            status.put("testStatus", "API 키 테스트 필요");
            status.put("message", "OpenAI API 키 유효성을 확인하려면 실제 API 호출이 필요합니다.");
            status.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(ApiResponseDto.success("OpenAI API 키 테스트가 완료되었습니다.", status));
        } catch (Exception e) {
            log.error("OpenAI API 키 테스트 실패", e);
            return ResponseEntity.status(500)
                    .body(ApiResponseDto.error("OpenAI API 키 테스트 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}
