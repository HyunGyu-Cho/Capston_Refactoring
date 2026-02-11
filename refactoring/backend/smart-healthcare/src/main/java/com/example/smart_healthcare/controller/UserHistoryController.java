package com.example.smart_healthcare.controller;

import com.example.smart_healthcare.entity.UserHistory;
import com.example.smart_healthcare.service.UserHistoryService;
import com.example.smart_healthcare.service.BodyAnalysisResultService;
import com.example.smart_healthcare.dto.response.BodyAnalysisResponseDto;
import com.example.smart_healthcare.common.dto.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users/history")
@RequiredArgsConstructor
@Tag(name = "UserHistory", description = "사용자 히스토리 관리 API")
public class UserHistoryController {
    
    private final UserHistoryService userHistoryService;
    private final BodyAnalysisResultService bodyAnalysisResultService;

    /**
     * 히스토리 저장
     */
    @Operation(summary = "히스토리 저장", description = "운동/식단 완료 상태를 저장합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "저장 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping
    public ResponseEntity<Map<String, Object>> save(@RequestBody Map<String, Object> request) {
        log.info("히스토리 저장 요청: {}", request);
        
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            String date = request.get("date").toString();
            String type = request.get("type").toString(); // "workout" or "diet"
            String workoutName = (String) request.get("workoutName"); // 운동 이름 (운동인 경우)
            Boolean completed = (Boolean) request.get("completed");
            
            log.info("파싱된 데이터: userId={}, date={}, type={}, workoutName={}, completed={}", 
                    userId, date, type, workoutName, completed);
            
            UserHistory history = new UserHistory();
            history.setUserId(userId);
            history.setDate(LocalDate.parse(date));
            history.setType(type);
            history.setCompleted(completed);
            
            // payload에 운동 정보 저장 (운동인 경우)
            if ("workout".equals(type) && workoutName != null) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("name", workoutName);
                
                // GPT API 추천 결과의 상세 정보가 있는 경우 추가
                if (request.containsKey("workoutDetails")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> workoutDetails = (Map<String, Object>) request.get("workoutDetails");
                    payload.put("workoutDetails", workoutDetails);
                    log.info("GPT API 추천 결과 상세 정보 포함: {}", workoutDetails);
                }
                
                String payloadJson = userHistoryService.convertToJson(payload);
                history.setPayload(payloadJson);
                log.info("운동 payload 저장 (GPT 추천 결과 포함): {}", payloadJson);
            }
            
            UserHistory savedHistory = userHistoryService.save(history);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "히스토리가 성공적으로 저장되었습니다.");
            response.put("history", savedHistory);
            
            log.info("히스토리 저장 완료: id={}, userId={}, date={}, type={}, completed={}", 
                    savedHistory.getId(), savedHistory.getUserId(), savedHistory.getDate(), 
                    savedHistory.getType(), savedHistory.getCompleted());
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            log.error("히스토리 저장 실패: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "히스토리 저장 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 히스토리 조회
     */
    @Operation(summary = "히스토리 조회", description = "기간별 히스토리를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping
    public ResponseEntity<Map<String, Object>> getHistory(
            @Parameter(description = "사용자 ID", required = true) @RequestParam Long userId,
            @Parameter(description = "시작 날짜", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "종료 날짜", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        log.info("히스토리 조회 요청: userId={}, from={}, to={}", userId, from, to);
        
        try {
            List<UserHistory> histories = userHistoryService.findByUserIdAndDateBetween(userId, from, to);
            log.info("조회된 히스토리 개수: {}", histories.size());
            
            // 날짜별로 그룹화
            Map<String, Map<String, Boolean>> groupedHistories = new HashMap<>();
            for (UserHistory history : histories) {
                String dateKey = history.getDate().toString();
                if (!groupedHistories.containsKey(dateKey)) {
                    groupedHistories.put(dateKey, new HashMap<>());
                }
                
                log.info("히스토리 처리: id={}, date={}, type={}, completed={}, payload={}", 
                        history.getId(), history.getDate(), history.getType(), 
                        history.getCompleted(), history.getPayload());
                
                if ("workout".equals(history.getType())) {
                    // 운동의 경우 payload에서 운동 이름 추출
                    String workoutName = userHistoryService.extractWorkoutName(history.getPayload());
                    log.info("추출된 운동 이름: {}", workoutName);
                    if (workoutName != null && !"Unknown Workout".equals(workoutName)) {
                        groupedHistories.get(dateKey).put(workoutName, history.getCompleted());
                    } else {
                        groupedHistories.get(dateKey).put("workout", history.getCompleted());
                    }
                } else if ("diet".equals(history.getType())) {
                    groupedHistories.get(dateKey).put("diet", history.getCompleted());
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("histories", groupedHistories);
            
            log.info("히스토리 조회 완료: userId={}, count={}, groupedHistories={}", 
                    userId, histories.size(), groupedHistories);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            log.error("히스토리 조회 실패: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "히스토리 조회 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 체형분석 히스토리 조회
     */
    @Operation(summary = "체형분석 히스토리 조회", description = "사용자의 체형분석 히스토리를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/body-analysis/{userId}")
    public ResponseEntity<ApiResponseDto<Page<BodyAnalysisResponseDto>>> getBodyAnalysisHistory(
            @Parameter(description = "사용자 ID", required = true) @PathVariable Long userId,
            @Parameter(description = "페이지 번호", required = false) @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", required = false) @RequestParam(defaultValue = "10") int size) {
        
        log.info("체형분석 히스토리 조회 요청: userId={}, page={}, size={}", userId, page, size);
        
        try {
            Page<BodyAnalysisResponseDto> results = bodyAnalysisResultService.getAnalysisHistoryByUserId(userId, page, size);
            
            log.info("체형분석 히스토리 조회 완료: userId={}, totalElements={}", userId, results.getTotalElements());
            
            return ResponseEntity.ok(
                    ApiResponseDto.success("체형분석 히스토리를 조회했습니다.", results)
            );
            
        } catch (Exception e) {
            log.error("체형분석 히스토리 조회 실패: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("체형분석 히스토리 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

}
