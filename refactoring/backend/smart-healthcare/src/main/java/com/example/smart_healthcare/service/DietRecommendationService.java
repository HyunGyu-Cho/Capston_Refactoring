package com.example.smart_healthcare.service;

import com.example.smart_healthcare.dto.response.DietRecommendationResponseDto;
import com.example.smart_healthcare.entity.AIDietRecommendation;
import com.example.smart_healthcare.entity.Member;
import com.example.smart_healthcare.repository.AIDietRecommendationRepository;
import com.example.smart_healthcare.repository.MemberRepository;
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

/**
 * 식단 추천 비즈니스 로직 서비스
 * 모든 식단 추천 관련 비즈니스 로직을 담당
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DietRecommendationService {

    private final AIDietRecommendationRepository aiDietRecommendationRepository;
    private final MemberRepository userRepository;


    /**
     * 사용자별 식단 추천 히스토리 조회 (페이징 지원)
     */
    public List<DietRecommendationResponseDto> getDietRecommendationHistory(Long userId, int page, int size) {
        log.info("🔍 식단 추천 히스토리 조회: userId={}, page={}, size={}", userId, page, size);
        
        try {
            // 사용자 존재 확인
            Member user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
            
            // DB 레벨에서 페이징 처리
            Pageable pageable = PageRequest.of(page, size);
            Page<AIDietRecommendation> pagedRecommendations = 
                    aiDietRecommendationRepository.findHistoryByUser(user, pageable);
            
            List<DietRecommendationResponseDto> result = pagedRecommendations.getContent().stream()
                    .map(AIDietRecommendation::toDto)
                    .collect(java.util.stream.Collectors.toList());
            
            log.info("✅ 식단 추천 히스토리 조회 성공: userId={}, count={}, total={}", 
                    userId, result.size(), pagedRecommendations.getTotalElements());
            return result;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ 식단 추천 히스토리 조회 실패: userId={}, error={}", userId, e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * 식단 추천 저장 (AI 생성 후 호출) - 중복 방지 로직 포함
     */
    @Transactional
    public AIDietRecommendation saveDietRecommendation(DietRecommendationResponseDto dto, Long userId, String preference) {
        log.info("💾 식단 추천 저장: userId={}, style={}", userId, dto.mealStyle());
        
        try {
            // 사용자 조회
            Member user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
            
            // 중복 저장 방지: 최근 10분 내에 동일한 식단 스타일이 있는지 확인
            java.time.LocalDateTime tenMinutesAgo = java.time.LocalDateTime.now().minusMinutes(10);
            Pageable pageable = PageRequest.of(0, 10); // 최근 10개만 조회
            Page<AIDietRecommendation> recentPage = aiDietRecommendationRepository.findHistoryByUser(user, pageable);
            
            var recentRecommendations = recentPage.getContent().stream()
                    .filter(rec -> rec.getCreatedAt().isAfter(tenMinutesAgo))
                    .filter(rec -> rec.getMealStyle() != null && rec.getMealStyle().equals(dto.mealStyle()))
                    .findFirst();
            
            if (recentRecommendations.isPresent()) {
                log.info("🔄 최근 10분 내 동일한 식단 추천이 있어 기존 데이터 반환: id={}", 
                        recentRecommendations.get().getId());
                return recentRecommendations.get();
            }
            
            // DTO를 엔티티로 변환
            AIDietRecommendation dietEntity = AIDietRecommendation.toEntity(dto, user, null, preference);
            
            // 데이터베이스에 저장
            AIDietRecommendation savedEntity = aiDietRecommendationRepository.save(dietEntity);
            
            log.info("✅ 식단 추천 저장 완료: userId={}, id={}", userId, savedEntity.getId());
            return savedEntity;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ 식단 추천 저장 실패: userId={}, error={}", userId, e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * 사용자의 식단 추천 개수 조회
     */
    public long getDietRecommendationCount(Long userId) {
        log.info("🔢 식단 추천 개수 조회: userId={}", userId);
        
        try {
            Member user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
            
            // 페이징을 사용하여 총 개수 조회
            Pageable pageable = PageRequest.of(0, 1);
            Page<AIDietRecommendation> page = aiDietRecommendationRepository.findHistoryByUser(user, pageable);
            
            long count = page.getTotalElements();
            log.info("✅ 식단 추천 개수 조회 성공: userId={}, count={}", userId, count);
            return count;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ 식단 추천 개수 조회 실패: userId={}, error={}", userId, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 특정 식단 추천 삭제 (소프트 삭제) - Repository의 deleteByIdAndUserId 메서드 사용
     */
    @Transactional
    public boolean deleteDietRecommendation(Long recommendationId, Long userId) {
        log.info("🗑️ 식단 추천 삭제: recommendationId={}, userId={}", recommendationId, userId);
        
        try {
            // Repository의 deleteByIdAndUserId 메서드 사용 (권한 검증 포함)
            int deletedCount = aiDietRecommendationRepository.deleteByIdAndUserId(recommendationId, userId);
            
            if (deletedCount > 0) {
                log.info("✅ 식단 추천 삭제 완료: recommendationId={}", recommendationId);
                return true;
            } else {
                log.warn("⚠️ 식단 추천을 찾을 수 없거나 권한이 없음: recommendationId={}, userId={}", recommendationId, userId);
                return false;
            }
            
        } catch (Exception e) {
            log.error("❌ 식단 추천 삭제 실패: recommendationId={}, userId={}, error={}", recommendationId, userId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 식단 추천 논리 삭제 (ID + 사용자 ID) - 본인 기록만 삭제 가능
     */
    @Transactional
    public void deleteDietRecommendationByIdAndUserId(Long id, Long userId) {
        log.info("🗑️ 식단 추천 삭제 요청: id={}, userId={}", id, userId);
        
        try {
            // 논리 삭제 실행 (존재 여부는 삭제 쿼리에서 자동 확인)
            int deletedCount = aiDietRecommendationRepository.deleteByIdAndUserId(id, userId);
            
            if (deletedCount == 0) {
                log.warn("⚠️ 삭제할 식단 추천을 찾을 수 없거나 권한이 없음: id={}, userId={}", id, userId);
                throw new BusinessException(ErrorCode.NOT_FOUND);
            }
            
            log.info("✅ 식단 추천 논리 삭제 완료: id={}, userId={}", id, userId);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ 식단 추천 삭제 실패: id={}, userId={}, error={}", id, userId, e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
    }
}
