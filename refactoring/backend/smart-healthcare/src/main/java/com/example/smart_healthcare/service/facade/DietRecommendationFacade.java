package com.example.smart_healthcare.service.facade;

import com.example.smart_healthcare.dto.request.InbodyDataRequestDto;
import com.example.smart_healthcare.dto.response.DietRecommendationResponseDto;
import com.example.smart_healthcare.service.ai.DietRecommendAIService;
import com.example.smart_healthcare.service.DietRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 식단 추천 Facade
 * - 외부 API 호출과 DB 저장을 분리하여 트랜잭션 문제 해결
 * - 외부 API 호출은 트랜잭션 밖에서, DB 저장은 짧은 트랜잭션으로 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DietRecommendationFacade {

    private final DietRecommendAIService aiService;        // 외부 AI 호출 전용 (트랜잭션 없음)
    private final DietRecommendationService dbService; // DB 저장 전용 (짧은 트랜잭션)

    /**
     * 식단 추천 및 결과 저장
     * 1. 외부 AI API 호출 (트랜잭션 없음)
     * 2. DB 저장 (짧은 트랜잭션)
     */
    public DietRecommendationResponseDto recommendAndSave(InbodyDataRequestDto request) {
        log.info("식단 추천 시작: userId={}", request.userId());
        
        try {
            // 1) 외부 AI API 호출: 트랜잭션 없음
            DietRecommendationResponseDto result = aiService.recommend(request, request.userId());
            
            // 2) DB 저장: 짧은 트랜잭션으로 처리
            String preference = request.survey() != null && request.survey().text() != null ? 
                               request.survey().text() : "균형잡힌 건강 식단";
            
            dbService.saveDietRecommendation(result, request.userId(), preference);
            log.info("✅ 식단 추천 및 저장 완료: userId={}", request.userId());
            
            return result;
            
        } catch (Exception e) {
            log.error("식단 추천 실패: userId={}", request.userId(), e);
            // 원본 예외를 그대로 던지기 (메시지 중복 방지)
            throw e;
        }
    }

    /**
     * 사용자별 식단 추천 히스토리 조회
     */
    public List<DietRecommendationResponseDto> getRecommendationHistory(Long userId, int page, int size) {
        log.info("식단 추천 히스토리 조회: userId={}, page={}, size={}", userId, page, size);
        
        try {
            return dbService.getDietRecommendationHistory(userId, page, size);
        } catch (Exception e) {
            log.error("식단 추천 히스토리 조회 실패: userId={}", userId, e);
            throw new RuntimeException("식단 추천 히스토리 조회 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
}
