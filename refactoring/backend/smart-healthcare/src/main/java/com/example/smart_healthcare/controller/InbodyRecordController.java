package com.example.smart_healthcare.controller;

import com.example.smart_healthcare.common.dto.ApiResponseDto;
import com.example.smart_healthcare.dto.request.InbodyDataRequestDto;
import com.example.smart_healthcare.dto.response.InbodyRecordResponseDto;
import com.example.smart_healthcare.service.InbodyRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/inbody")
@RequiredArgsConstructor
public class InbodyRecordController {
    
    private final InbodyRecordService inbodyRecordService;
    
    /**
     * 인바디 기록 생성
     */
    @PostMapping
    public ResponseEntity<ApiResponseDto<InbodyRecordResponseDto>> createInbodyRecord(
            @Valid @RequestBody InbodyDataRequestDto request) {
        log.info("인바디 기록 생성 API 호출: userId={}", request.userId());
        
        InbodyRecordResponseDto response = inbodyRecordService.createInbodyRecord(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("인바디 기록이 성공적으로 생성되었습니다.", response));
    }
    
    
    /**
     * 사용자별 인바디 기록 조회 (Pageable 방식)
     * - size=1: 최신 기록만 조회
     * - period: 특정 기간 조회
     * - 본인 기록만 조회 가능
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponseDto<Page<InbodyRecordResponseDto>>> getInbodyRecordsByUserId(
            @PathVariable Long userId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("사용자별 인바디 기록 조회 API 호출: userId={}, pageable={}, startDate={}, endDate={}", 
                userId, pageable, startDate, endDate);
        
        Page<InbodyRecordResponseDto> response = inbodyRecordService.getInbodyRecordsByUserId(userId, pageable, startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDto.success("사용자별 인바디 기록 조회가 완료되었습니다.", response));
    }
    
    /**
     * 인바디 기록 삭제 - 본인 기록만 삭제 가능
     * 사용자별 목록 조회 후 삭제할 기록의 ID를 확인하여 사용
     */
    @DeleteMapping("/user/{userId}/records/{id}")
    public ResponseEntity<ApiResponseDto<Void>> deleteInbodyRecord(
            @PathVariable Long userId,
            @PathVariable Long id) {
        log.info("인바디 기록 삭제 API 호출: userId={}, id={}", userId, id);
        
        inbodyRecordService.deleteInbodyRecordByIdAndUserId(id, userId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDto.success("인바디 기록이 성공적으로 삭제되었습니다.", null));
    }
}
