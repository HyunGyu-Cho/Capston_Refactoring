package com.example.smart_healthcare.controller;

import com.example.smart_healthcare.common.dto.ApiResponseDto;
import com.example.smart_healthcare.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Tag(name = "Image", description = "이미지 관리 API")
public class ImageController {

    private final ImageService imageService;

    /**
     * 식단 이미지 URL 조회
     */
    @Operation(summary = "식단 이미지 조회", description = "식단명으로 Unsplash에서 이미지 URL을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이미지 URL 조회 성공"),
            @ApiResponse(responseCode = "404", description = "이미지를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/diet/{foodName}")
    public ResponseEntity<ApiResponseDto<Map<String, String>>> getDietImage(
            @Parameter(description = "식단명", required = true)
            @PathVariable String foodName) {
        
        log.info("식단 이미지 조회 요청: foodName={}", foodName);
        
        try {
            String imageUrl = imageService.getImageUrl(foodName);
            Map<String, String> response = Map.of("imageUrl", imageUrl);
            
            return ResponseEntity.ok(
                    ApiResponseDto.success("식단 이미지 조회 성공", response)
            );
        } catch (Exception e) {
            log.error("식단 이미지 조회 실패: foodName={}, error={}", foodName, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("식단 이미지 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 운동 이미지 URL 조회
     */
    @Operation(summary = "운동 이미지 조회", description = "운동명으로 Unsplash에서 이미지 URL을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이미지 URL 조회 성공"),
            @ApiResponse(responseCode = "404", description = "이미지를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/workout/{workoutName}")
    public ResponseEntity<ApiResponseDto<Map<String, String>>> getWorkoutImage(
            @Parameter(description = "운동명", required = true)
            @PathVariable String workoutName) {
        
        log.info("운동 이미지 조회 요청: workoutName={}", workoutName);
        
        try {
            String imageUrl = imageService.getWorkoutImageUrl(workoutName);
            Map<String, String> response = Map.of("imageUrl", imageUrl);
            
            return ResponseEntity.ok(
                    ApiResponseDto.success("운동 이미지 조회 성공", response)
            );
        } catch (Exception e) {
            log.error("운동 이미지 조회 실패: workoutName={}, error={}", workoutName, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("운동 이미지 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 이미지 캐시 통계 조회
     */
    @Operation(summary = "이미지 캐시 통계", description = "이미지 캐시 통계를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "캐시 통계 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/cache/stats")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> getCacheStats() {
        log.info("이미지 캐시 통계 조회 요청");
        
        try {
            Map<String, Object> stats = imageService.getCacheStats();
            return ResponseEntity.ok(
                    ApiResponseDto.success("캐시 통계 조회 성공", stats)
            );
        } catch (Exception e) {
            log.error("캐시 통계 조회 실패: error={}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("캐시 통계 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 캐시된 이미지 삭제
     */
    @Operation(summary = "이미지 캐시 삭제", description = "특정 식단의 캐시된 이미지를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "캐시 삭제 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @DeleteMapping("/cache/{foodName}")
    public ResponseEntity<ApiResponseDto<Void>> deleteCachedImage(
            @Parameter(description = "식단명", required = true)
            @PathVariable String foodName) {
        
        log.info("이미지 캐시 삭제 요청: foodName={}", foodName);
        
        try {
            imageService.deleteCachedImage(foodName);
            return ResponseEntity.ok(
                    ApiResponseDto.success("캐시 삭제 성공", null)
            );
        } catch (Exception e) {
            log.error("캐시 삭제 실패: foodName={}, error={}", foodName, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("캐시 삭제 실패: " + e.getMessage()));
        }
    }
}
