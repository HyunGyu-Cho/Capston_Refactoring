package com.example.smart_healthcare.service;

import com.example.smart_healthcare.common.error.BusinessException;
import com.example.smart_healthcare.common.error.ErrorCode;
import com.example.smart_healthcare.dto.request.SurveyRequestDto;
import com.example.smart_healthcare.dto.response.SurveyResponseDto;
import com.example.smart_healthcare.entity.Survey;
import com.example.smart_healthcare.entity.User;
import com.example.smart_healthcare.repository.SurveyRepository;
import com.example.smart_healthcare.repository.UserRepository;
import com.example.smart_healthcare.repository.InbodyRecordRepository;
import com.example.smart_healthcare.entity.InbodyRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final UserRepository userRepository;
    private final InbodyRecordRepository inbodyRecordRepository;

    /**
     * 설문조사 생성
     */
    @Transactional
    public SurveyResponseDto createSurvey(SurveyRequestDto request) {
        log.info("설문조사 생성 요청: userId={}, inbodyRecordId={}", request.getUserId(), request.getInbodyRecordId());
        
        // 1. DTO를 Entity로 변환
        Survey survey = request.toEntity();
        
        // 2. User 연결 확인 및 설정
        if (request.getUserId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "사용자 정보가 필요합니다.");
        }
        
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        survey.setUser(user);
        
        // 3. InbodyRecord 연결 (선택적)
        if (request.getInbodyRecordId() != null) {
            InbodyRecord inbodyRecord = inbodyRecordRepository.findById(request.getInbodyRecordId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "인바디 기록을 찾을 수 없습니다."));
            survey.setInbody(inbodyRecord);
        }
        
        
        // 5. Entity 저장
        Survey savedSurvey = surveyRepository.save(survey);
        log.info("설문조사 생성 완료: id={}, userId={}", savedSurvey.getId(), savedSurvey.getUser().getId());
        
        // 6. Entity를 DTO로 변환하여 반환
        return SurveyResponseDto.toDto(savedSurvey);
    }



    /**
     * 설문조사 상세 조회
     */
    public SurveyResponseDto findById(Long id) {
        log.info("설문조사 상세 조회 요청: id={}", id);
        
        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "설문조사를 찾을 수 없습니다."));
        
        if (survey.getIsDeleted()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "삭제된 설문조사입니다.");
        }
        
        log.info("설문조사 상세 조회 완료: id={}, userId={}", id, survey.getUser().getId());
        return SurveyResponseDto.toDto(survey);
    }

    /**
     * 사용자별 설문조사 이력 조회 (페이징)
     */
    public Page<SurveyResponseDto> findHistoryByUserId(Long userId, int page, int size) {
        log.info("사용자별 설문조사 이력 조회 요청: userId={}, page={}, size={}", userId, page, size);
        
        //직접 PageRequest 사용하는 방법: PageReqeust.of() 메서드 사용
        Pageable pageable = PageRequest.of(page, size);
        Page<Survey> pageResults = surveyRepository.findHistoryByUserId(userId, pageable);
        
        // Page<Survey>를 Page<SurveyResponseDto>로 변환
        return pageResults.map(SurveyResponseDto::toDto);
    }


    /**
     * 사용자별 특정 설문조사 논리삭제
     */
    @Transactional
    public int deleteSurveyByIdAndUserId(Long id, Long userId) {
        log.info("설문조사 논리삭제 요청: id={}, userId={}", id, userId);
        
        // 사용자 권한 확인
        surveyRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "설문조사를 찾을 수 없습니다."));
        
        // 논리삭제 실행
        int deletedCount = surveyRepository.softDeleteById(id);
        log.info("설문조사 논리삭제 완료: id={}, userId={}, deletedCount={}", id, userId, deletedCount);
        
        return deletedCount;
    }
    
    /**
     * 사용자별 최신 설문조사 조회
     */
    public SurveyResponseDto findLatestByUserId(Long userId) {
        log.info("사용자별 최신 설문조사 조회 요청: userId={}", userId);
        
        // 사용자 존재 확인
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        
        // 최신 설문조사 조회 (첫 번째 페이지만 가져오기)
        Pageable pageable = PageRequest.of(0, 1);
        Page<Survey> pageResults = surveyRepository.findHistoryByUserId(userId, pageable);
        
        if (pageResults.getContent().isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "설문조사가 없습니다.");
        }
        
        Survey latestSurvey = pageResults.getContent().get(0);
        log.info("사용자별 최신 설문조사 조회 완료: userId={}, surveyId={}", userId, latestSurvey.getId());
        
        return SurveyResponseDto.toDto(latestSurvey);
    }

    /**
     * 사용자별 모든 설문조사 논리삭제 (사용자 탈퇴 시 사용)
     */
    @Transactional
    public int deleteAllSurveysByUserId(Long userId) {
        log.info("사용자별 모든 설문조사 논리삭제 요청: userId={}", userId);
        
        int deletedCount = surveyRepository.deleteAllByUserId(userId);
        log.info("사용자별 모든 설문조사 논리삭제 완료: userId={}, deletedCount={}", userId, deletedCount);
        
        return deletedCount;
    }
}