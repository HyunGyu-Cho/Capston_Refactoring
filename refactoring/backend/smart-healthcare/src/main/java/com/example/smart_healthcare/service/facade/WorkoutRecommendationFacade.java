package com.example.smart_healthcare.service.facade;

import com.example.smart_healthcare.dto.request.InbodyDataRequestDto;
import com.example.smart_healthcare.dto.response.WorkoutRecommendationResponseDto;
import com.example.smart_healthcare.service.ai.WorkoutRecommendAIService;
import com.example.smart_healthcare.service.WorkoutRecommendationService;
import com.example.smart_healthcare.service.YoutubeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import java.util.List;
import java.util.Map;

/**
 * 운동 추천 Facade
 * - 외부 API 호출과 DB 저장을 분리하여 트랜잭션 문제 해결
 * - 외부 API 호출은 트랜잭션 밖에서, DB 저장은 짧은 트랜잭션으로 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkoutRecommendationFacade {

    private final WorkoutRecommendAIService aiService;        // 외부 AI 호출 전용 (트랜잭션 없음)
    private final WorkoutRecommendationService dbService;     // DB 저장 전용 (짧은 트랜잭션)
    
    @Autowired(required = false)
    private YoutubeService youtubeService;  // YouTube API 서비스 (선택사항)

    /**
     * 운동 추천 및 결과 저장
     * 1. 외부 AI API 호출 (트랜잭션 없음)
     * 2. DB 저장 (짧은 트랜잭션)
     */
    public WorkoutRecommendationResponseDto recommendAndSave(InbodyDataRequestDto request) {
        log.info("==========================================");
        log.info("🔄 Facade: 운동 추천 시작");
        log.info("  - userId: {}", request.userId());
        log.info("==========================================");
        
        try {
            log.info("🔄 1단계: AI 서비스 호출 시작...");
            // 1) 외부 AI API 호출: 트랜잭션 없음
            WorkoutRecommendationResponseDto result = aiService.recommend(request, request.userId());
            log.info("✅ 1단계 완료: AI 서비스 응답 수신");
            
            log.info("🔄 2단계: YouTube 영상 링크 업그레이드...");
            // 2) YouTube 영상 링크 업그레이드 (선택사항)
            result = enhanceWithYoutubeVideos(result);
            log.info("✅ 2단계 완료: YouTube 링크 처리 완료");
            
            log.info("🔄 3단계: DB 저장 시작...");
            // 3) DB 저장: 짧은 트랜잭션으로 처리
            String goal = request.survey() != null && request.survey().text() != null ? 
                         request.survey().text() : "체지방 감량 및 근력 향상";
            
            dbService.saveWorkoutRecommendation(result, request.userId(), goal);
            log.info("✅ 3단계 완료: DB 저장 완료");
            log.info("✅ 운동 추천 및 저장 완료: userId={}", request.userId());
            
            return result;
            
        } catch (Exception e) {
            log.error("==========================================");
            log.error("❌ Facade: 운동 추천 실패");
            log.error("  - userId: {}", request.userId());
            log.error("  - 예외 타입: {}", e.getClass().getSimpleName());
            log.error("  - 예외 메시지: {}", e.getMessage());
            log.error("  - 스택 트레이스:", e);
            log.error("==========================================");
            // 원본 예외를 그대로 던지기 (메시지 중복 방지)
            throw e;
        }
    }

    /**
     * 사용자별 최신 운동 추천 결과 조회
     */
    public WorkoutRecommendationResponseDto getLatestRecommendation(Long userId) {
        log.info("최신 운동 추천 조회: userId={}", userId);
        
        try {
            return dbService.getLatestWorkoutRecommendation(userId)
                    .orElse(null);
        } catch (Exception e) {
            log.error("운동 추천 조회 실패: userId={}", userId, e);
            throw new RuntimeException("운동 추천 조회 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 사용자별 운동 추천 히스토리 조회
     */
    public Page<WorkoutRecommendationResponseDto> getRecommendationHistory(Long userId, int page, int size) {
        log.info("운동 추천 히스토리 조회: userId={}, page={}, size={}", userId, page, size);
        
        try {
            return dbService.getWorkoutRecommendationHistory(userId, page, size);
        } catch (Exception e) {
            log.error("운동 추천 히스토리 조회 실패: userId={}", userId, e);
            throw new RuntimeException("운동 추천 히스토리 조회 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
    
    /**
     * YouTube 영상 링크 강화 (AI가 생성한 youtubeQuery 활용)
     * - youtubeService가 있고 API 키가 설정되어 있으면 실제 영상 링크로 교체
     * - 없으면 AI가 생성한 검색 링크 그대로 사용
     */
    @SuppressWarnings("unchecked")
    private WorkoutRecommendationResponseDto enhanceWithYoutubeVideos(WorkoutRecommendationResponseDto dto) {
        // YouTubeService가 없으면 원본 그대로 반환
        if (youtubeService == null) {
            log.debug("YoutubeService가 없음, 원본 videoUrl 유지");
            return dto;
        }
        
        if (dto.workouts() == null || dto.workouts().isEmpty()) {
            return dto;
        }
        
        try {
            Map<String, Object> workouts = dto.workouts();
            
            // 각 요일별 운동 처리
            for (Map.Entry<String, Object> entry : workouts.entrySet()) {
                Object workoutList = entry.getValue();
                
                if (workoutList instanceof List) {
                    List<Map<String, Object>> exercises = (List<Map<String, Object>>) workoutList;
                    
                    for (Map<String, Object> exercise : exercises) {
                        String exerciseName = (String) exercise.get("name");
                        String youtubeQuery = (String) exercise.get("youtubeQuery");
                        
                        // 검색어 우선순위: 1) AI가 생성한 youtubeQuery (최우선) 2) 영어 변환 3) 한글 그대로
                        String searchQuery = null;
                        
                        // 1순위: AI가 생성한 youtubeQuery 사용 (최적화된 검색어)
                        if (youtubeQuery != null && !youtubeQuery.trim().isEmpty()) {
                            // 검색 링크 형태가 아닌 실제 검색어인지 확인
                            if (!youtubeQuery.startsWith("http") && !youtubeQuery.contains("search_query")) {
                                searchQuery = youtubeQuery.trim();
                                log.debug("✅ AI 생성 youtubeQuery 사용: {}", searchQuery);
                            }
                        }
                        
                        // 2순위: 영어로 변환된 운동명 사용 (매핑 테이블 활용)
                        if (searchQuery == null && exerciseName != null && !exerciseName.trim().isEmpty()) {
                            String englishExerciseName = convertToEnglishExerciseName(exerciseName);
                            if (englishExerciseName != null && !englishExerciseName.equals(exerciseName)) {
                                searchQuery = englishExerciseName + " tutorial proper form";
                                log.debug("✅ 영어 변환 사용: {} → {}", exerciseName, searchQuery);
                            }
                        }
                        
                        // 3순위: 한글 운동명 그대로 사용 (YouTube 한글 검색 지원)
                        if (searchQuery == null && exerciseName != null && !exerciseName.trim().isEmpty()) {
                            searchQuery = exerciseName + " 운동 자세 tutorial";
                            log.debug("✅ 한글 운동명 사용: {}", searchQuery);
                        }
                        
                        // 검색어가 없으면 스킵
                        if (searchQuery == null || searchQuery.trim().isEmpty()) {
                            log.warn("⚠️ 검색어를 생성할 수 없음, 스킵: exerciseName={}, youtubeQuery={}", exerciseName, youtubeQuery);
                            continue;
                        }
                        
                        log.info("🔍 YouTube 검색: 운동명={}, 검색어={}", exerciseName, searchQuery);
                        
                        // YouTube API로 실제 영상 검색 (운동 이름 전달)
                        String actualVideoUrl = youtubeService.findVideoUrl(searchQuery, "exercise", exerciseName);
                        
                        // 검색 링크가 아닌 실제 영상 링크인 경우만 교체
                        if (actualVideoUrl != null && actualVideoUrl.contains("watch?v=")) {
                            exercise.put("videoUrl", actualVideoUrl);
                            log.info("✨ YouTube 영상 교체: {} → {}", exerciseName, actualVideoUrl);
                        } else {
                            log.warn("⚠️ 관련 영상 없음, 원본 videoUrl 유지: {}", exerciseName);
                        }
                    }
                }
            }
            
            // 수정된 workouts로 새 DTO 생성
            return new WorkoutRecommendationResponseDto(
                dto.programName(),
                dto.weeklySchedule(),
                dto.caution(),
                dto.warmup(),
                dto.mainSets(),
                dto.cooldown(),
                dto.equipment(),
                dto.targetMuscles(),
                dto.expectedResults(),
                workouts
            );
            
        } catch (Exception e) {
            log.warn("⚠️ YouTube 영상 링크 강화 실패, 원본 유지: {}", e.getMessage());
            return dto;
        }
    }
    
    /**
     * 한글 운동명을 영어로 변환 (보조 수단 - AI의 youtubeQuery가 없을 때만 사용)
     * 
     * 주의: 이 메서드는 제한적인 매핑만 제공합니다.
     * 새로운 운동이 추가될 때마다 매핑을 추가해야 하므로 확장성이 떨어집니다.
     * 따라서 AI가 생성한 youtubeQuery를 최우선으로 사용하고,
     * 이 메서드는 fallback으로만 사용됩니다.
     */
    private String convertToEnglishExerciseName(String koreanName) {
        if (koreanName == null || koreanName.trim().isEmpty()) {
            return null;
        }
        
        String lower = koreanName.toLowerCase().trim();
        
        // 주요 운동명 매핑 (제한적 - 확장 시 매핑 추가 필요)
        if (lower.contains("푸시업") || lower.contains("푸쉬업")) {
            return "push up";
        }
        if (lower.contains("풀업") || lower.contains("턱걸이")) {
            return "pull up";
        }
        if (lower.contains("랫풀다운") || lower.contains("랫풀")) {
            return "lat pulldown";
        }
        if (lower.contains("스쿼트")) {
            return "squat";
        }
        if (lower.contains("런지")) {
            return "lunge";
        }
        if (lower.contains("플랭크")) {
            return "plank";
        }
        if (lower.contains("크런치")) {
            return "crunch";
        }
        if (lower.contains("데드리프트")) {
            return "deadlift";
        }
        if (lower.contains("로우") && (lower.contains("덤벨") || lower.contains("바벨"))) {
            if (lower.contains("덤벨")) {
                return "dumbbell row";
            } else {
                return "barbell row";
            }
        }
        if (lower.contains("프레스")) {
            if (lower.contains("벤치")) {
                return "bench press";
            } else if (lower.contains("숄더") || lower.contains("어깨")) {
                return "shoulder press";
            } else if (lower.contains("덤벨")) {
                return "dumbbell press";
            }
        }
        if (lower.contains("레터럴") && lower.contains("레이즈")) {
            return "lateral raise";
        }
        if (lower.contains("컬")) {
            if (lower.contains("이두") || lower.contains("바이셉")) {
                return "bicep curl";
            } else if (lower.contains("해머")) {
                return "hammer curl";
            }
        }
        if (lower.contains("마운틴") && lower.contains("클라이머")) {
            return "mountain climber";
        }
        if (lower.contains("버피")) {
            return "burpee";
        }
        if (lower.contains("레그") && lower.contains("레이즈")) {
            return "leg raise";
        }
        if (lower.contains("러닝") || lower.contains("달리기")) {
            return "running";
        }
        if (lower.contains("걷기")) {
            return "walking";
        }
        
        // 매핑되지 않으면 null 반환 (원본 사용)
        return null;
    }
}
