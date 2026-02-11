package com.example.smart_healthcare.service;

import com.example.smart_healthcare.common.error.BusinessException;
import com.example.smart_healthcare.common.error.ErrorCode;
import com.example.smart_healthcare.dto.request.InbodyDataRequestDto;
import com.example.smart_healthcare.dto.response.InbodyRecordResponseDto;
import com.example.smart_healthcare.entity.InbodyRecord;
import com.example.smart_healthcare.repository.InbodyRecordRepository;
import com.example.smart_healthcare.entity.User;
import com.example.smart_healthcare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InbodyRecordService {
    
    private final InbodyRecordRepository inbodyRecordRepository;
    private final UserRepository userRepository;
    
    /**
     * 인바디 기록 생성
     */
    @Transactional
    public InbodyRecordResponseDto createInbodyRecord(InbodyDataRequestDto request) {
        log.info("인바디 기록 생성 요청: userId={}", request.userId());
        
        // 사용자 존재 확인
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        
        // Builder 패턴으로 인바디 기록 엔티티 생성
        InbodyRecord record = InbodyRecord.toEntity(request, user);
        
        InbodyRecord savedRecord = inbodyRecordRepository.save(record);
        log.info("인바디 기록 생성 완료: id={}, userId={}", savedRecord.getId(), savedRecord.getUser().getId());
        
        return InbodyRecordResponseDto.toDto(savedRecord);
    }
    
    
    
    /**
     * 사용자별 인바디 기록 조회 (Pageable 방식)
     * - 기본 조회 및 기간별 조회를 통합
     */
    public Page<InbodyRecordResponseDto> getInbodyRecordsByUserId(Long userId, Pageable pageable, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("사용자별 인바디 기록 조회 요청: userId={}, pageable={}, startDate={}, endDate={}", 
                userId, pageable, startDate, endDate);
        
        Page<InbodyRecord> records;
        
        if (startDate != null && endDate != null) {
            // 특정 기간 조회
            records = inbodyRecordRepository.findByUserIdAndPeriod(userId, startDate, endDate, pageable);
        } else {
            // 전체 조회 (최신순)
            records = inbodyRecordRepository.findByUserId(userId, pageable);
        }
        
        return records.map(InbodyRecordResponseDto::toDto);
    }
    
    /**
     * 인바디 기록 삭제 (ID + 사용자 ID) - 본인 기록만 삭제 가능
     */
    @Transactional
    public void deleteInbodyRecordByIdAndUserId(Long id, Long userId) {
        log.info("인바디 기록 삭제 요청: id={}, userId={}", id, userId);
        
        // 논리 삭제 실행 (존재 여부는 삭제 쿼리에서 자동 확인)
        int deletedCount = inbodyRecordRepository.deleteByIdAndUserId(id, userId);
        
        if (deletedCount == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        
        log.info("인바디 기록 논리 삭제 완료: id={}, userId={}", id, userId);
    }
    
    
}
