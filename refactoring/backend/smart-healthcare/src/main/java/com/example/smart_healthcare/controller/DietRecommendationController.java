package com.example.smart_healthcare.controller;

import com.example.smart_healthcare.common.dto.ApiResponseDto;
import com.example.smart_healthcare.dto.response.DietRecommendationResponseDto;
import com.example.smart_healthcare.dto.request.InbodyDataRequestDto;
import com.example.smart_healthcare.service.facade.DietRecommendationFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 식단 추천 컨트롤러
 * 식단 추천 관련 API만 담당
 */
@RestController
@RequestMapping("/api/diet-recommendation")
@RequiredArgsConstructor
@Slf4j
public class DietRecommendationController {

    private final DietRecommendationFacade dietRecommendationFacade;

    /**
     * 식단 추천 API
     * 인바디 데이터와 설문 데이터를 기반으로 AI가 맞춤형 식단을 추천
     */
    @PostMapping
    public ResponseEntity<ApiResponseDto<DietRecommendationResponseDto>> recommendDiet(
            @Valid @RequestBody InbodyDataRequestDto request) {
        
        log.info("🍽️ AI 식단 추천 요청: userId={}, gender={}, age={}", 
                request.userId(), request.gender(), 
                request.getCurrentAge());
        
        try {
            // Facade를 통한 식단 추천 및 저장
            DietRecommendationResponseDto result = dietRecommendationFacade.recommendAndSave(request);
            
            log.info("✅ AI 식단 추천 완료: userId={}, mealStyle={}", 
                    request.userId(), result.mealStyle());
            
            return ResponseEntity.ok(
                    ApiResponseDto.success("맞춤형 식단이 추천되었습니다.", result)
            );
            
        } catch (Exception e) {
            log.error("❌ AI 식단 추천 실패: userId={}, error={}", 
                    request.userId(), e.getMessage(), e);
            log.error("❌ 예외 타입: {}, 스택 트레이스:", e.getClass().getSimpleName(), e);
            
            // 예외 메시지 추출 (null 체크)
            String errorMessage = e.getMessage();
            if (errorMessage == null || errorMessage.trim().isEmpty()) {
                errorMessage = e.getClass().getSimpleName() + " 발생";
            }
            
            // 예외 메시지가 이미 포함되어 있으면 그대로 사용, 아니면 기본 메시지 사용
            if (errorMessage.contains("식단 추천") || errorMessage.contains("OpenAI") || errorMessage.contains("API")) {
                // 이미 구체적인 메시지가 포함되어 있으면 그대로 사용
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponseDto.error(errorMessage));
            } else {
                // 기본 메시지와 함께 구체적인 에러 정보 포함
                String detailedMessage = String.format("식단 추천 중 오류가 발생했습니다: %s", errorMessage);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponseDto.error(detailedMessage));
            }
        }
    }

    /**
     * 사용자별 식단 추천 히스토리 조회
     */
    @GetMapping("/{userId}/history")
    public ResponseEntity<ApiResponseDto<List<DietRecommendationResponseDto>>> getDietRecommendationHistory(
            @PathVariable Long userId) {
        
        log.info("🔍 식단 추천 히스토리 조회 요청: userId={}", userId);
        
        try {
            // Facade를 통한 비즈니스 로직 처리
            List<DietRecommendationResponseDto> result = 
                    dietRecommendationFacade.getRecommendationHistory(userId, 0, 50);
            
            log.info("✅ 식단 추천 히스토리 조회 완료: userId={}, count={}", userId, result.size());
            
            return ResponseEntity.ok(
                    ApiResponseDto.success("식단 추천 히스토리를 조회했습니다.", result)
            );
            
        } catch (Exception e) {
            log.error("❌ 식단 추천 히스토리 조회 실패: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("식단 추천 히스토리 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}
