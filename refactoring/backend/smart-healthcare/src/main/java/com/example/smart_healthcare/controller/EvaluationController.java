package com.example.smart_healthcare.controller;

import com.example.smart_healthcare.common.dto.ApiResponseDto;
import com.example.smart_healthcare.dto.request.EvaluationRequestDto;
import com.example.smart_healthcare.dto.response.EvaluationResponseDto;
import com.example.smart_healthcare.service.EvaluationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/evaluation")
@RequiredArgsConstructor
public class EvaluationController {
    
    private final EvaluationService evaluationService;
    
    
    /**
     * 평가 제출
     */
    @PostMapping
    public ResponseEntity<ApiResponseDto<EvaluationResponseDto>> createEvaluation(
            @Valid @RequestBody EvaluationRequestDto request) {
        log.info("평가 제출 API 호출: userId={}, rating={}", request.getUserId(), request.getRating());
        
        EvaluationResponseDto response = evaluationService.createEvaluation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("평가가 제출되었습니다.", response));
    }
    
    /**
     * 사용자별 평가 조회
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponseDto<EvaluationResponseDto>> getEvaluationByUserId(
            @PathVariable Long userId) {
        log.info("사용자별 평가 조회 API 호출: userId={}", userId);
        
        EvaluationResponseDto response = evaluationService.getEvaluationByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDto.success("평가 조회가 완료되었습니다.", response));
    }
    
    /**
     * 모든 평가 조회 (페이징)
     */
    @GetMapping
    public ResponseEntity<ApiResponseDto<Page<EvaluationResponseDto>>> getAllEvaluations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("모든 평가 조회 API 호출: page={}, size={}", page, size);
        
        Page<EvaluationResponseDto> evaluations = evaluationService.getAllEvaluations(page, size);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDto.success("평가 목록 조회가 완료되었습니다.", evaluations));
    }
    
    /**
     * 특정 평점의 평가 조회 (페이징)
     */
    @GetMapping("/rating/{rating}")
    public ResponseEntity<ApiResponseDto<Page<EvaluationResponseDto>>> getEvaluationsByRating(
            @PathVariable Integer rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("특정 평점 평가 조회 API 호출: rating={}, page={}, size={}", rating, page, size);
        
        // 평점 유효성 검증
        if (rating < 1 || rating > 5) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error("평점은 1-5 사이의 값이어야 합니다."));
        }
        
        Page<EvaluationResponseDto> evaluations = evaluationService.getEvaluationsByRating(rating, page, size);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDto.success("평점별 평가 조회가 완료되었습니다.", evaluations));
    }
    
    /**
     * 전체 평가 개수 조회
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponseDto<Long>> getTotalCount() {
        log.info("전체 평가 개수 조회 API 호출");
        
        Long count = evaluationService.getTotalCount();
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDto.success("전체 평가 개수 조회가 완료되었습니다.", count));
    }
    
    /**
     * 평점 평균 조회
     */
    @GetMapping("/average")
    public ResponseEntity<ApiResponseDto<Double>> getAverageRating() {
        log.info("평점 평균 조회 API 호출");
        
        Double average = evaluationService.getAverageRating();
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDto.success("평점 평균 조회가 완료되었습니다.", average));
    }
    
    /**
     * 평가 통계 조회 (관리자용 - 전체 통계)
     */
    @GetMapping("/admin/stats")
    public ResponseEntity<ApiResponseDto<EvaluationService.EvaluationStatsDto>> getEvaluationStats() {
        log.info("평가 통계 조회 API 호출 (관리자용)");
        
        EvaluationService.EvaluationStatsDto stats = evaluationService.getEvaluationStats();
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDto.success("평가 통계 조회가 완료되었습니다.", stats));
    }
}
