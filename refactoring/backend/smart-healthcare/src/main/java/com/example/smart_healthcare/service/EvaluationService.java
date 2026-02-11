package com.example.smart_healthcare.service;

import com.example.smart_healthcare.dto.request.EvaluationRequestDto;
import com.example.smart_healthcare.dto.response.EvaluationResponseDto;
import com.example.smart_healthcare.entity.Evaluation;
import com.example.smart_healthcare.entity.User;
import com.example.smart_healthcare.exception.ResourceNotFoundException;
import com.example.smart_healthcare.repository.EvaluationRepository;
import com.example.smart_healthcare.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EvaluationService {
    
    private final EvaluationRepository evaluationRepository;
    private final UserRepository userRepository;
    
    /**
     * 모든 평가 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<EvaluationResponseDto> getAllEvaluations(int page, int size) {
        log.info("모든 평가 조회: page={}, size={}", page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Evaluation> evaluations = evaluationRepository.findAllByOrderByCreatedAtDesc(pageable);
        
        return evaluations.map(EvaluationResponseDto::toDto);
    }
    
    /**
     * 평가 생성
     */
    public EvaluationResponseDto createEvaluation(EvaluationRequestDto request) {
        log.info("평가 생성: userId={}, rating={}", request.getUserId(), request.getRating());
        
        // userId가 null인 경우 기본 사용자(1번) 사용 (개발용)
        Long userId = request.getUserId() != null ? request.getUserId() : 1L;
        
        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + userId, "User", userId.toString()));
        
        // 기존 평가 확인 (한 사용자는 하나의 평가만 가능)
        evaluationRepository.findByUserId(userId)
                .ifPresent(evaluation -> {
                    throw new IllegalArgumentException("이미 평가를 제출한 사용자입니다.");
                });
        
        // 새 평가 생성
        Evaluation evaluation = request.toEntity();
        evaluation.setUser(user);
        
        Evaluation savedEvaluation = evaluationRepository.save(evaluation);
        log.info("평가 생성 완료: evaluationId={}", savedEvaluation.getId());
        
        return EvaluationResponseDto.toDto(savedEvaluation);
    }
    
    /**
     * 사용자별 평가 조회
     */
    @Transactional(readOnly = true)
    public EvaluationResponseDto getEvaluationByUserId(Long userId) {
        log.info("사용자별 평가 조회: userId={}", userId);
        
        Evaluation evaluation = evaluationRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("평가를 찾을 수 없습니다: userId=" + userId, "Evaluation", userId.toString()));
        
        return EvaluationResponseDto.toDto(evaluation);
    }
    
    /**
     * 특정 평점의 평가 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<EvaluationResponseDto> getEvaluationsByRating(Integer rating, int page, int size) {
        log.info("특정 평점 평가 조회: rating={}, page={}, size={}", rating, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Evaluation> evaluations = evaluationRepository.findByRatingOrderByCreatedAtDesc(rating, pageable);
        
        return evaluations.map(EvaluationResponseDto::toDto);
    }
    
    /**
     * 전체 평가 개수 조회
     */
    @Transactional(readOnly = true)
    public Long getTotalCount() {
        log.info("전체 평가 개수 조회");
        
        return evaluationRepository.getTotalCount();
    }
    
    /**
     * 평점 평균 조회
     */
    @Transactional(readOnly = true)
    public Double getAverageRating() {
        log.info("평점 평균 조회");
        
        return evaluationRepository.getAverageRating();
    }
    
    /**
     * 평가 통계 조회
     */
    @Transactional(readOnly = true)
    public EvaluationStatsDto getEvaluationStats() {
        log.info("평가 통계 조회");
        
        List<Evaluation> evaluations = evaluationRepository.findAll();
        
        if (evaluations.isEmpty()) {
            return new EvaluationStatsDto(0, 0.0, 0, 0, 0, 0, 0);
        }
        
        double averageRating = evaluations.stream()
                .mapToInt(Evaluation::getRating)
                .average()
                .orElse(0.0);
        
        long totalCount = evaluations.size();
        long rating1Count = evaluations.stream().mapToLong(e -> e.getRating() == 1 ? 1 : 0).sum();
        long rating2Count = evaluations.stream().mapToLong(e -> e.getRating() == 2 ? 1 : 0).sum();
        long rating3Count = evaluations.stream().mapToLong(e -> e.getRating() == 3 ? 1 : 0).sum();
        long rating4Count = evaluations.stream().mapToLong(e -> e.getRating() == 4 ? 1 : 0).sum();
        long rating5Count = evaluations.stream().mapToLong(e -> e.getRating() == 5 ? 1 : 0).sum();
        
        return new EvaluationStatsDto(
                totalCount,
                averageRating,
                rating1Count,
                rating2Count,
                rating3Count,
                rating4Count,
                rating5Count
        );
    }
    
    /**
     * 평가 통계 DTO
     */
    @Getter
    @AllArgsConstructor
    public static class EvaluationStatsDto {
        private long totalCount;
        private double averageRating;
        private long rating1Count;
        private long rating2Count;
        private long rating3Count;
        private long rating4Count;
        private long rating5Count;
    }
}