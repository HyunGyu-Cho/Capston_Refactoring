package com.example.smart_healthcare.service.facade;

import com.example.smart_healthcare.dto.request.InbodyDataRequestDto;
import com.example.smart_healthcare.dto.response.BodyAnalysisResponseDto;
import com.example.smart_healthcare.service.ai.ChatGPTBodyAnalysisService;
import com.example.smart_healthcare.service.BodyAnalysisResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


/**
 * 체형 분석 Facade
 * - 외부 API 호출과 DB 저장을 분리하여 트랜잭션 문제 해결
 * - 외부 API 호출은 트랜잭션 밖에서, DB 저장은 짧은 트랜잭션으로 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BodyAnalysisFacade {

    private final ChatGPTBodyAnalysisService chatgptService; // 외부 호출 전용 (트랜잭션 없음)
    private final BodyAnalysisResultService resultService;   // DB 저장 전용 (짧은 트랜잭션)

    /**
     * 체형 분석 및 결과 저장
     * 1. 외부 API 호출 (트랜잭션 없음)
     * 2. DB 저장 (짧은 트랜잭션)
     */
    public BodyAnalysisResponseDto analyzeAndSave(InbodyDataRequestDto request) {
        log.info("체형 분석 시작: userId={}", request.userId());
        
        try {
            // 1) 외부 API 호출: 트랜잭션 없음
            // 여기서 예외 나면 그대로 밖으로 던짐 (catch하지 않음)
            String aiAnalysisResult = chatgptService.analyzeBodyType(request);
            
            // 2) DB 저장: 짧은 트랜잭션으로 처리
            return resultService.saveAnalysisResult(request, aiAnalysisResult);
            
        } catch (Exception e) {
            log.error("체형 분석 실패: userId={}", request.userId(), e);
            // 예외를 그대로 던져서 컨트롤러 advice에서 처리
            throw new RuntimeException("체형 분석 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

}