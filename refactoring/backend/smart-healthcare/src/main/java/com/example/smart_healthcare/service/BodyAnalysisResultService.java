package com.example.smart_healthcare.service;

import com.example.smart_healthcare.dto.request.InbodyDataRequestDto;
import com.example.smart_healthcare.dto.response.BodyAnalysisResponseDto;
import com.example.smart_healthcare.entity.AIBodyAnalysisResult;
import com.example.smart_healthcare.entity.User;
import com.example.smart_healthcare.repository.AIBodyAnalysisResultRepository;
import com.example.smart_healthcare.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 체형 분석 결과 저장 서비스
 * - 짧은 트랜잭션으로 DB 저장만 처리
 * - 외부 API 호출은 포함하지 않음
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BodyAnalysisResultService {

    private final AIBodyAnalysisResultRepository aiBodyAnalysisResultRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    /**
     * 체형 분석 결과를 DB에 저장
     * - 짧은 트랜잭션으로 처리
     * - 외부 API 호출은 포함하지 않음
     * - 인바디 기록은 생성하지 않고 AI 분석 결과만 저장
     */
    @Transactional
    public BodyAnalysisResponseDto saveAnalysisResult(InbodyDataRequestDto request, String aiAnalysisResult) {
        log.info("체형 분석 결과 저장 시작: userId={}", request.userId());
        
        try {
            // 1) 사용자 조회
            User user = userRepository.findById(request.userId())
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + request.userId()));

            // 2) AI 분석 결과만 저장 (인바디 기록 생성 없음)
            AIBodyAnalysisResult analysisResult = createAIBodyAnalysisResult(request, aiAnalysisResult, user);
            analysisResult = aiBodyAnalysisResultRepository.save(analysisResult);
            log.info("AI 분석 결과 저장 완료: id={}", analysisResult.getId());

            // 3) 응답 DTO 생성
            return analysisResult.toDto();
            
        } catch (Exception e) {
            log.error("체형 분석 결과 저장 실패: userId={}", request.userId(), e);
            throw new RuntimeException("체형 분석 결과 저장에 실패했습니다: " + e.getMessage(), e);
        }
    }


    /**
     * AI 분석 결과 엔티티 생성 (인바디 기록 없이)
     */
    private AIBodyAnalysisResult createAIBodyAnalysisResult(InbodyDataRequestDto request, String aiAnalysisResult, User user) {
        try {
            // JSON 문자열을 BodyAnalysisResponseDto로 파싱
            BodyAnalysisResponseDto dto = objectMapper.readValue(aiAnalysisResult, BodyAnalysisResponseDto.class);
            // 인바디 기록 없이 AI 분석 결과만 저장
            return AIBodyAnalysisResult.toEntity(dto, user, null);
        } catch (Exception e) {
            log.error("AI 분석 결과 파싱 실패", e);
            throw new RuntimeException("AI 분석 결과 파싱에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 사용자별 최신 체형 분석 결과 조회
     */
    @Transactional(readOnly = true)
    public BodyAnalysisResponseDto getLatestAnalysisByUserId(Long userId) {
        log.info("🔍 최신 체형 분석 조회 시작: userId={}", userId);
        
        try {
            // 먼저 해당 사용자의 모든 체형 분석 결과 개수 확인
            long totalCount = aiBodyAnalysisResultRepository.countByUserId(userId);
            log.info("🔍 사용자 {}의 전체 체형 분석 결과 개수: {}", userId, totalCount);
            
            var latestResult = aiBodyAnalysisResultRepository.findLatestByUserId(userId);
            
            if (latestResult.isEmpty()) {
                log.info("📝 체형 분석 결과가 없음: userId={} (전체 개수: {})", userId, totalCount);
                return null;
            }
            
            AIBodyAnalysisResult result = latestResult.get();
            log.info("✅ 최신 체형 분석 결과 조회 완료: id={}, label={}, createdAt={}", 
                result.getId(), result.getLabel(), result.getCreatedAt());
            log.info("🔍 분석 결과 상세: summary={}, healthRisk={}", 
                result.getSummary(), result.getHealthRisk());
            
            BodyAnalysisResponseDto dto = result.toDto();
            log.info("🔍 DTO 변환 완료: label={}, summary={}", dto.label(), dto.summary());
            
            return dto;
            
        } catch (Exception e) {
            log.error("❌ 체형 분석 조회 실패: userId={}", userId, e);
            throw new RuntimeException("체형 분석 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 사용자별 체형 분석 히스토리 조회
     */
    @Transactional(readOnly = true)
    public Page<BodyAnalysisResponseDto> getAnalysisHistoryByUserId(Long userId, int page, int size) {
        log.info("체형 분석 히스토리 조회: userId={}, page={}, size={}", userId, page, size);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            var pageResults = aiBodyAnalysisResultRepository.findHistoryByUserId(userId, pageable);
            
            log.info("체형 분석 히스토리 조회 완료: userId={}, count={}", userId, pageResults.getContent().size());
            
            return pageResults.map(AIBodyAnalysisResult::toDto);
            
        } catch (Exception e) {
            log.error("체형 분석 히스토리 조회 실패: userId={}", userId, e);
            throw new RuntimeException("체형 분석 히스토리 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }
    
    /**
     * 사용자별 모든 체형 분석 결과 논리삭제 (사용자 탈퇴 시 사용)
     */
    @Transactional
    public int deleteAllAnalysisByUserId(Long userId) {
        log.info("사용자별 체형 분석 결과 논리삭제: userId={}", userId);
        
        try {
            int deletedCount = aiBodyAnalysisResultRepository.deleteAllByUserId(userId);
            log.info("체형 분석 결과 논리삭제 완료: userId={}, deletedCount={}", userId, deletedCount);
            return deletedCount;
        } catch (Exception e) {
            log.error("체형 분석 결과 논리삭제 실패: userId={}", userId, e);
            throw new RuntimeException("체형 분석 결과 논리삭제에 실패했습니다: " + e.getMessage(), e);
        }
    }
    
    /**
     * 특정 체형 분석 결과 논리삭제
     */
    @Transactional
    public int deleteAnalysisById(Long id) {
        log.info("체형 분석 결과 논리삭제: id={}", id);
        
        try {
            int deletedCount = aiBodyAnalysisResultRepository.softDeleteById(id);
            log.info("체형 분석 결과 논리삭제 완료: id={}, deletedCount={}", id, deletedCount);
            return deletedCount;
        } catch (Exception e) {
            log.error("체형 분석 결과 논리삭제 실패: id={}", id, e);
            throw new RuntimeException("체형 분석 결과 논리삭제에 실패했습니다: " + e.getMessage(), e);
        }
    }
}
