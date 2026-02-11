package com.example.smart_healthcare.controller;

import com.example.smart_healthcare.common.dto.ApiResponseDto;
import com.example.smart_healthcare.dto.request.SurveyRequestDto;
import com.example.smart_healthcare.dto.response.SurveyResponseDto;
import com.example.smart_healthcare.service.SurveyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/survey")
@RequiredArgsConstructor
@Tag(name = "Survey", description = "설문조사 관리 API")
public class SurveyController {

    private final SurveyService surveyService;

    /**
     * 설문조사 생성
     */
    @Operation(summary = "설문조사 생성", description = "새로운 설문조사를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "설문조사 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping
    public ResponseEntity<ApiResponseDto<SurveyResponseDto>> createSurvey(@Valid @RequestBody SurveyRequestDto request) {
        log.info("설문조사 생성 API 호출: userId={}", request.getUserId());
        
        try {
            SurveyResponseDto response = surveyService.createSurvey(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseDto.success("설문조사가 생성되었습니다.", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error(e.getMessage()));
        }
    }

    /**
     * 설문조사 상세 조회
     */
    @Operation(summary = "설문조사 상세 조회", description = "특정 설문조사의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "설문조사를 찾을 수 없음")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<SurveyResponseDto>> getSurvey(@PathVariable Long id) {
        log.info("설문조사 상세 조회 API 호출: id={}", id);
        
        try {
            SurveyResponseDto response = surveyService.findById(id);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponseDto.success("설문조사 조회가 완료되었습니다.", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.error(e.getMessage()));
        }
    }

    /**
     * 사용자별 설문조사 이력 조회 (페이징)
     */
    @Operation(summary = "사용자별 설문조사 이력 조회", description = "특정 사용자의 설문조사 이력을 페이징으로 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @GetMapping("/user/{userId}/history")
    public ResponseEntity<ApiResponseDto<Page<SurveyResponseDto>>> getSurveyHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("사용자별 설문조사 이력 조회 API 호출: userId={}, page={}, size={}", userId, page, size);
        
        try {
            Page<SurveyResponseDto> response = surveyService.findHistoryByUserId(userId, page, size);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponseDto.success("설문조사 이력 조회가 완료되었습니다.", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error(e.getMessage()));
        }
    }

    /**
     * 사용자별 최신 설문조사 조회
     */
    @Operation(summary = "사용자별 최신 설문조사 조회", description = "특정 사용자의 가장 최근 설문조사를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "설문조사를 찾을 수 없음")
    })
    @GetMapping("/user/{userId}/latest")
    public ResponseEntity<ApiResponseDto<SurveyResponseDto>> getLatestSurvey(@PathVariable Long userId) {
        log.info("사용자별 최신 설문조사 조회 API 호출: userId={}", userId);
        
        try {
            SurveyResponseDto response = surveyService.findLatestByUserId(userId);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponseDto.success("최신 설문조사 조회가 완료되었습니다.", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.error(e.getMessage()));
        }
    }

    /**
     * 설문조사 삭제
     */
    @Operation(summary = "설문조사 삭제", description = "특정 설문조사를 논리삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "설문조사를 찾을 수 없음")
    })
    @DeleteMapping("/user/{userId}/history/{id}")
    public ResponseEntity<ApiResponseDto<String>> deleteSurvey(@PathVariable Long id, @PathVariable Long userId) {
        log.info("설문조사 삭제 API 호출: id={}, userId={}", id, userId);
        
        try {
            int deletedCount = surveyService.deleteSurveyByIdAndUserId(id, userId);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponseDto.success("설문조사가 삭제되었습니다.", "삭제된 항목 수: " + deletedCount));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.error(e.getMessage()));
        }
    }
}