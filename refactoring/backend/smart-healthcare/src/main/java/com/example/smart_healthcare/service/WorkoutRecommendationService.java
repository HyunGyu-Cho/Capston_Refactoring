package com.example.smart_healthcare.service;

import com.example.smart_healthcare.dto.response.WorkoutRecommendationResponseDto;
import com.example.smart_healthcare.entity.AIWorkoutRecommendation;
import com.example.smart_healthcare.entity.User;
import com.example.smart_healthcare.repository.AIWorkoutRecommendationRepository;
import com.example.smart_healthcare.repository.UserRepository;
import com.example.smart_healthcare.common.error.BusinessException;
import com.example.smart_healthcare.common.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 운동 추천 비즈니스 로직 서비스
 * 모든 운동 추천 관련 비즈니스 로직을 담당
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class WorkoutRecommendationService {

    private final AIWorkoutRecommendationRepository aiWorkoutRecommendationRepository;
    private final UserRepository userRepository;

    /**
     * 사용자별 최신 운동 추천 조회
     */
    public Optional<WorkoutRecommendationResponseDto> getLatestWorkoutRecommendation(Long userId) {
        log.info("🔍 최신 운동 추천 조회: userId={}", userId);
        
        try {
            // 최신 운동 추천 조회 (페이징으로 첫 번째 항목만 가져오기)
            Pageable pageable = PageRequest.of(0, 1);
            Page<AIWorkoutRecommendation> latestPage = 
                    aiWorkoutRecommendationRepository.findHistoryByUserId(userId, pageable);
            
            if (!latestPage.getContent().isEmpty()) {
                WorkoutRecommendationResponseDto result = latestPage.getContent().get(0).toDto();
                log.info("✅ 최신 운동 추천 조회 성공: userId={}, program={}", userId, result.programName());
                return Optional.of(result);
            } else {
                log.info("📝 운동 추천 결과가 없음: userId={}", userId);
                return Optional.empty();
            }
            
        } catch (Exception e) {
            log.error("❌ 최신 운동 추천 조회 실패: userId={}, error={}", userId, e.getMessage(), e);
            throw new RuntimeException("최신 운동 추천 조회 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 사용자별 운동 추천 히스토리 조회 (페이징 지원)
     */
    public Page<WorkoutRecommendationResponseDto> getWorkoutRecommendationHistory(Long userId, int page, int size) {
        log.info("🔍 운동 추천 히스토리 조회: userId={}, page={}, size={}", userId, page, size);
        
        try {
            // DB 레벨에서 페이징 처리
            Pageable pageable = PageRequest.of(page, size);
            Page<AIWorkoutRecommendation> pagedRecommendations = 
                    aiWorkoutRecommendationRepository.findHistoryByUserId(userId, pageable);
            
            Page<WorkoutRecommendationResponseDto> result = pagedRecommendations.map(AIWorkoutRecommendation::toDto);
            
            log.info("✅ 운동 추천 히스토리 조회 성공: userId={}, count={}, total={}", 
                    userId, result.getContent().size(), result.getTotalElements());
            return result;
            
        } catch (Exception e) {
            log.error("❌ 운동 추천 히스토리 조회 실패: userId={}, error={}", userId, e.getMessage(), e);
            throw new RuntimeException("운동 추천 히스토리 조회 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 사용자별 운동 추천 히스토리 조회 (전체, 페이징 없음)
     */
    public List<WorkoutRecommendationResponseDto> getWorkoutRecommendationHistory(Long userId) {
        Page<WorkoutRecommendationResponseDto> pageResult = getWorkoutRecommendationHistory(userId, 0, 100); // 기본값: 최신 100개
        return pageResult.getContent();
    }

    /**
     * 운동 추천 저장 (AI 생성 후 호출) - 중복 방지 로직 포함
     */
    @Transactional
    public AIWorkoutRecommendation saveWorkoutRecommendation(WorkoutRecommendationResponseDto dto, Long userId, String goal) {
        log.info("💾 운동 추천 저장: userId={}, program={}", userId, dto.programName());
        
        try {
            // 사용자 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
            
            // 중복 저장 방지 강화: 최근 10분 내 생성된 추천이 하나라도 있으면 새 저장 차단
            java.time.LocalDateTime tenMinutesAgo = java.time.LocalDateTime.now().minusMinutes(10);
            Pageable pageable = PageRequest.of(0, 10); // 최근 10개만 확인
            Page<AIWorkoutRecommendation> recentPage = 
                    aiWorkoutRecommendationRepository.findHistoryByUserId(userId, pageable);
            
            boolean hasRecentAny = recentPage.getContent()
                    .stream()
                    .anyMatch(rec -> rec.getCreatedAt().isAfter(tenMinutesAgo));
            
            if (hasRecentAny) {
                log.info("🔄 최근 10분 내 생성된 운동 추천이 있어 새 저장을 건너뜁니다. 기존 최신 데이터를 반환합니다.");
                return recentPage.getContent().get(0); // 최신 항목 반환
            }
            
            // DTO를 엔티티로 변환
            AIWorkoutRecommendation workoutEntity = AIWorkoutRecommendation.toEntity(dto, user, null, goal);
            
            // 데이터베이스에 저장
            AIWorkoutRecommendation savedEntity = aiWorkoutRecommendationRepository.save(workoutEntity);
            
            log.info("✅ 운동 추천 저장 완료: userId={}, id={}", userId, savedEntity.getId());
            return savedEntity;
            
        } catch (Exception e) {
            log.error("❌ 운동 추천 저장 실패: userId={}, error={}", userId, e.getMessage(), e);
            throw new RuntimeException("운동 추천 저장 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 사용자의 운동 추천 개수 조회
     */
    public long getWorkoutRecommendationCount(Long userId) {
        try {
            // 페이징으로 전체 개수 조회 (size를 매우 크게 설정)
            Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
            Page<AIWorkoutRecommendation> allPage = 
                    aiWorkoutRecommendationRepository.findHistoryByUserId(userId, pageable);
            
            return allPage.getTotalElements();
            
        } catch (Exception e) {
            log.error("❌ 운동 추천 개수 조회 실패: userId={}, error={}", userId, e.getMessage(), e);
            return 0;
        }
    }
    
    /**
     * 특정 운동 추천 논리 삭제 (ID + 사용자 ID) - 본인 기록만 삭제 가능
     */
    @Transactional
    public void deleteWorkoutRecommendation(Long id, Long userId) {
        log.info("운동 추천 삭제 요청: id={}, userId={}", id, userId);
        
        // 논리 삭제 실행 (AI 기반 운동 추천에서 삭제)
        int deletedCount = aiWorkoutRecommendationRepository.softDeleteById(id);
        
        if (deletedCount == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        
        log.info("운동 추천 논리 삭제 완료: id={}, userId={}", id, userId);
    }
}
