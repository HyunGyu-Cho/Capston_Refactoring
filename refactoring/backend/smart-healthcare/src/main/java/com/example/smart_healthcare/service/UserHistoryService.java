package com.example.smart_healthcare.service;

import com.example.smart_healthcare.entity.UserHistory;
import com.example.smart_healthcare.repository.UserHistoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserHistoryService {

    private final UserHistoryRepository userHistoryRepository;
    private final ObjectMapper objectMapper;

    /**
     * 사용자 히스토리 저장
     */
    public UserHistory save(UserHistory userHistory) {
        return userHistoryRepository.save(userHistory);
    }

    /**
     * 사용자별 히스토리 조회
     */
    public List<UserHistory> findByUserId(Long userId) {
        return userHistoryRepository.findByUserId(userId);
    }

    /**
     * 사용자별 특정 날짜 히스토리 조회
     */
    public List<UserHistory> findByUserIdAndDate(Long userId, LocalDate date) {
        return userHistoryRepository.findByUserIdAndDate(userId, date);
    }

    /**
     * 사용자별 특정 유형 히스토리 조회
     */
    public List<UserHistory> findByUserIdAndType(Long userId, String type) {
        return userHistoryRepository.findByUserIdAndType(userId, type);
    }

    /**
     * JSON 문자열을 Map으로 변환
     */
    public String convertToJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 변환 실패", e);
        }
    }

    /**
     * JSON 문자열을 Map으로 변환
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> convertFromJson(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 파싱 실패", e);
        }
    }

    /**
     * 히스토리 삭제
     */
    public void delete(Long id) {
        userHistoryRepository.deleteById(id);
    }

    /**
     * 히스토리 ID로 조회
     */
    public Optional<UserHistory> findById(Long id) {
        return userHistoryRepository.findById(id);
    }

    /**
     * 사용자별 특정 기간 히스토리 조회
     */
    public List<UserHistory> findByUserIdAndDateBetween(Long userId, LocalDate from, LocalDate to) {
        return userHistoryRepository.findByUserIdAndDateBetween(userId, from, to);
    }

    /**
     * 운동 이름 추출
     */
    public String extractWorkoutName(String workoutData) {
        try {
            Map<String, Object> data = convertFromJson(workoutData);
            String workoutName = (String) data.get("name");
            return workoutName != null ? workoutName : "Unknown Workout";
        } catch (Exception e) {
            return "Unknown Workout";
        }
    }

}