package com.example.smart_healthcare.controller;

import com.example.smart_healthcare.common.dto.ApiResponseDto;
import com.example.smart_healthcare.dto.response.WorkoutRecommendationResponseDto;
import com.example.smart_healthcare.dto.request.InbodyDataRequestDto;
import com.example.smart_healthcare.service.facade.WorkoutRecommendationFacade;
import com.example.smart_healthcare.service.WorkoutRecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 운동 추천 컨트롤러
 * 운동 추천 관련 API만 담당
 */
@RestController
@RequestMapping("/api/workout-recommendation")
@RequiredArgsConstructor
@Slf4j
public class WorkoutRecommendationController {

    private final WorkoutRecommendationFacade workoutRecommendationFacade;
    private final WorkoutRecommendationService workoutRecommendationService;

    /**
     * 운동 추천 API
     * 인바디 데이터와 설문 데이터를 기반으로 AI가 맞춤형 운동 프로그램을 추천
     */
    @PostMapping
    public ResponseEntity<ApiResponseDto<WorkoutRecommendationResponseDto>> recommendWorkout(
            @Valid @RequestBody InbodyDataRequestDto request) {
        
        log.info("==========================================");
        log.info("🏃 AI 운동 추천 요청 수신");
        log.info("  - userId: {}", request.userId());
        log.info("  - gender: {}", request.gender());
        log.info("  - age: {}", request.getCurrentAge());
        log.info("  - survey: {}", request.survey() != null ? "있음" : "없음");
        log.info("==========================================");
        
        try {
            log.info("🔄 Facade 호출 시작...");
            // Facade를 통한 운동 추천 및 저장
            WorkoutRecommendationResponseDto result = workoutRecommendationFacade.recommendAndSave(request);
            
            log.info("✅ AI 운동 추천 완료: userId={}, program={}", 
                    request.userId(), result.programName());
            
            return ResponseEntity.ok(
                    ApiResponseDto.success("맞춤형 운동 프로그램이 추천되었습니다.", result)
            );
            
        } catch (Exception e) {
            log.error("==========================================");
            log.error("❌ AI 운동 추천 실패");
            log.error("  - userId: {}", request.userId());
            log.error("  - 예외 타입: {}", e.getClass().getSimpleName());
            log.error("  - 예외 메시지: {}", e.getMessage());
            log.error("  - 스택 트레이스:", e);
            log.error("==========================================");
            
            // 예외 메시지가 이미 포함되어 있으면 그대로 사용, 아니면 기본 메시지 사용
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("운동 추천")) {
                // 이미 "운동 추천" 메시지가 포함되어 있으면 그대로 사용
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponseDto.error(errorMessage));
            } else {
                // 기본 메시지 사용
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponseDto.error("운동 추천 중 오류가 발생했습니다: " + errorMessage));
            }
        }
    }

    /**
     * 사용자별 운동 추천 히스토리 조회 (페이징)
     * 첫 번째 항목이 자동으로 최신 추천
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponseDto<Page<WorkoutRecommendationResponseDto>>> getWorkoutRecommendationHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("🔍 운동 추천 히스토리 조회 요청: userId={}, page={}, size={}", userId, page, size);
        
        try {
            // Service를 통한 페이징 히스토리 조회
            Page<WorkoutRecommendationResponseDto> result = 
                    workoutRecommendationService.getWorkoutRecommendationHistory(userId, page, size);
            
            log.info("✅ 운동 추천 히스토리 조회 완료: userId={}, totalElements={}", userId, result.getTotalElements());
            
            return ResponseEntity.ok(
                    ApiResponseDto.success("운동 추천 히스토리를 조회했습니다.", result)
            );
            
        } catch (Exception e) {
            log.error("❌ 운동 추천 히스토리 조회 실패: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("운동 추천 히스토리 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @PutMapping("/{userId}/{id}")
    public ResponseEntity<ApiResponseDto<?>> deleteWorkoutRecommendation(
            @PathVariable Long userId, 
            @PathVariable Long id) {
        
        log.info("운동 추천 논리삭제 API 호출: userId={}, id={}", userId, id);
        
        try {
            workoutRecommendationService.deleteWorkoutRecommendation(id, userId);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponseDto.success("운동 추천 이력이 삭제되었습니다.", null));
        } catch (Exception e) {
            log.error("❌ 운동 추천 논리삭제 실패: userId={}, id={}, error={}", userId, id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.error("운동 추천 이력을 찾을 수 없습니다."));
        }
    }
}
