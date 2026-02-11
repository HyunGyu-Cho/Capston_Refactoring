package com.example.smart_healthcare.controller;

import com.example.smart_healthcare.common.dto.ApiResponseDto;
import com.example.smart_healthcare.dto.response.BodyAnalysisResponseDto;
import com.example.smart_healthcare.dto.request.InbodyDataRequestDto;
import com.example.smart_healthcare.service.facade.BodyAnalysisFacade;
import com.example.smart_healthcare.service.BodyAnalysisResultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 체형 분석 컨트롤러
 * 체형 분석 관련 API만 담당
 */
@RestController
@RequestMapping("/api/body-analysis")
@RequiredArgsConstructor
@Slf4j
public class BodyAnalysisController {

    private final BodyAnalysisFacade bodyAnalysisFacade;
    private final BodyAnalysisResultService bodyAnalysisResultService;

    /**
     * 체형 분석 API
     * 인바디 데이터를 기반으로 AI가 체형을 분석하고 건강 조언을 제공
     */
    @PostMapping
    public ResponseEntity<ApiResponseDto<BodyAnalysisResponseDto>> analyzeBodyType(
            @Valid @RequestBody InbodyDataRequestDto request) {
        
        log.info("🔍 AI 체형 분석 요청 시작: userId={}, gender={}, age={}", 
                request.userId(), request.gender(), 
                request.getCurrentAge());
        log.info("🔍 요청 데이터: {}", request);
        
        try {
            // Facade를 통한 체형 분석 및 저장
            BodyAnalysisResponseDto result = bodyAnalysisFacade.analyzeAndSave(request);
            
            log.info("✅ AI 체형 분석 완료: userId={}, label={}", 
                    request.userId(), result.label());
            
            return ResponseEntity.ok(
                    ApiResponseDto.success("체형 분석이 완료되었습니다.", result)
            );
            
        } catch (Exception e) {
            log.error("❌ AI 체형 분석 실패: userId={}, error={}", 
                    request.userId(), e.getMessage(), e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("체형 분석 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 사용자별 최신 체형 분석 결과 조회
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponseDto<BodyAnalysisResponseDto>> getLatestBodyAnalysis(
            @PathVariable Long userId) {
        
        log.info("🔍 최신 체형 분석 조회 요청: userId={}", userId);
        
        try {
            // Service를 통한 최신 분석 결과 조회
            BodyAnalysisResponseDto result = bodyAnalysisResultService.getLatestAnalysisByUserId(userId);
            
            if (result != null) {
                log.info("✅ 최신 체형 분석 조회 완료: userId={}, label={}, summary={}", 
                    userId, result.label(), result.summary());
                return ResponseEntity.ok(
                        ApiResponseDto.success("최신 체형 분석 결과를 조회했습니다.", result)
                );
            } else {
                log.info("📝 체형 분석 결과가 없음: userId={} (API 응답: success=true, data=null)", userId);
                return ResponseEntity.ok(
                        ApiResponseDto.success("체형 분석 결과가 없습니다.", null)
                );
            }
            
        } catch (Exception e) {
            log.error("❌ 최신 체형 분석 조회 실패: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("체형 분석 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 사용자별 체형 분석 히스토리 조회 (페이징)
     */
    @GetMapping("/{userId}/history")
    public ResponseEntity<ApiResponseDto<Page<BodyAnalysisResponseDto>>> getAnalysisHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("🔍 체형 분석 히스토리 조회 요청: userId={}, page={}, size={}", userId, page, size);
        
        try {
            // Service를 통한 분석 히스토리 조회
            Page<BodyAnalysisResponseDto> result = bodyAnalysisResultService.getAnalysisHistoryByUserId(userId, page, size);
            
            log.info("✅ 체형 분석 히스토리 조회 완료: userId={}, totalElements={}", 
                    userId, result.getTotalElements());
            
            return ResponseEntity.ok(
                    ApiResponseDto.success("체형 분석 히스토리를 조회했습니다.", result)
            );
            
        } catch (Exception e) {
            log.error("❌ 체형 분석 히스토리 조회 실패: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("체형 분석 히스토리 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}
