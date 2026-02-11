package com.example.smart_healthcare.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * YouTube Data API v3 서비스
 * 운동/식단 관련 영상 검색
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class YoutubeService {

    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${youtube.api.key:}")
    private String apiKey;
    
    private static final String API_URL = "https://www.googleapis.com/youtube/v3/search";
    
    /**
     * YouTube 영상 검색 (캐싱 적용)
     * 
     * @param query 검색어 (GPT가 최적화한 쿼리 권장)
     * @param type 'exercise' 또는 'diet'
     * @param exerciseName 운동 이름 (관련성 검증용, 선택사항)
     * @return YouTube 영상 URL 또는 검색 페이지 URL (fallback)
     */
    @Cacheable(value = "youtubeVideos", key = "#query + '_' + (#exerciseName != null ? #exerciseName : '')")
    public String findVideoUrl(String query, String type, String exerciseName) {
        // API 키가 없으면 검색 링크로 fallback
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("YOUR_YOUTUBE_API_KEY")) {
            log.warn("⚠️ YouTube API 키가 설정되지 않음, 검색 링크로 fallback");
            return buildSearchUrl(query);
        }
        
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            
            // YouTube Data API v3 호출
            String url = API_URL 
                + "?part=snippet"
                + "&type=video"
                + "&maxResults=5"
                + "&videoEmbeddable=true"
                + "&videoSyndicated=true"
                + "&videoDuration=short"
                + "&safeSearch=strict"
                + "&order=relevance"
                + "&relevanceLanguage=" + (isKoreanQuery(query) ? "ko" : "en")
                + "&q=" + encoded
                + "&key=" + apiKey;
            
            log.info("🔍 YouTube API 호출: query={}", query);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response == null) {
                log.warn("❌ YouTube API 응답이 null");
                return buildSearchUrl(query);
            }
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
            
            if (items == null || items.isEmpty()) {
                log.warn("❌ YouTube 검색 결과 없음: query={}", query);
                return buildSearchUrl(query);
            }
            
            // 관련도 높은 영상 필터링
            for (Map<String, Object> item : items) {
                @SuppressWarnings("unchecked")
                Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");
                @SuppressWarnings("unchecked")
                Map<String, Object> id = (Map<String, Object>) item.get("id");
                
                if (snippet != null && id != null) {
                    String title = (String) snippet.get("title");
                    String description = (String) snippet.get("description");
                    String videoId = (String) id.get("videoId");
                    
                    log.debug("🔍 영상 검토: title={}, exerciseName={}", title, exerciseName);
                    
                    // 관련성 검증 (운동 이름 포함)
                    if (isRelevantVideo(title, description, type, exerciseName)) {
                        String videoUrl = "https://www.youtube.com/watch?v=" + videoId;
                        log.info("✅ YouTube 영상 발견: title={}, url={}", title, videoUrl);
                        return videoUrl;
                    } else {
                        log.debug("❌ 관련성 부족: title={}", title);
                    }
                }
            }
            
            // 필터링 후에도 관련 영상이 없으면 검색 링크로 fallback
            log.warn("⚠️ 관련 영상 없음, 검색 링크로 fallback: query={}, exerciseName={}", query, exerciseName);
            return buildSearchUrl(query);
            
        } catch (Exception e) {
            log.error("❌ YouTube API 호출 실패: query={}, error={}", query, e.getMessage(), e);
            return buildSearchUrl(query);
        }
    }
    
    /**
     * YouTube 검색 페이지 URL 생성 (fallback)
     */
    private String buildSearchUrl(String query) {
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            return "https://www.youtube.com/results?search_query=" + encoded;
        } catch (Exception e) {
            return "https://www.youtube.com/results?search_query=exercise+tutorial";
        }
    }
    
    /**
     * 한글 쿼리 여부 확인
     */
    private boolean isKoreanQuery(String query) {
        return query.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*");
    }
    
    /**
     * 영상 관련성 검증 (더 엄격한 검증)
     */
    private boolean isRelevantVideo(String title, String description, String type, String exerciseName) {
        if (title == null && description == null) {
            return false;
        }
        
        String lowerTitle = (title != null ? title.toLowerCase() : "");
        String lowerDesc = (description != null ? description.toLowerCase() : "");
        String combined = lowerTitle + " " + lowerDesc;
        
        // 운동 영상 키워드 (필수)
        String[] exerciseKeywords = {
            "tutorial", "proper form", "how to", "exercise", "workout", 
            "training", "form check", "technique", "beginner", "guide"
        };
        
        // 식단 영상 키워드 (필수)
        String[] dietKeywords = {
            "recipe", "how to make", "cooking", "healthy", "meal prep",
            "nutrition", "step by step", "easy recipe"
        };
        
        String[] keywords = type.equals("exercise") ? exerciseKeywords : dietKeywords;
        
        // 1단계: 필수 키워드 검증 (하나 이상 포함되어야 함)
        boolean hasKeyword = false;
        for (String keyword : keywords) {
            if (combined.contains(keyword)) {
                hasKeyword = true;
                break;
            }
        }
        
        if (!hasKeyword) {
            log.debug("❌ 필수 키워드 없음: title={}", title);
            return false;
        }
        
        // 2단계: 운동 이름 검증 (운동 추천인 경우 필수)
        if (exerciseName != null && !exerciseName.trim().isEmpty() && type.equals("exercise")) {
            String lowerExerciseName = exerciseName.toLowerCase().trim();
            String[] exerciseWords = lowerExerciseName.split("\\s+");
            boolean hasExerciseName = false;
            
            // 1) 전체 운동 이름이 포함되어 있는지 확인
            if (combined.contains(lowerExerciseName)) {
                hasExerciseName = true;
                log.debug("✅ 전체 운동 이름 매칭: {}", exerciseName);
            } else {
                // 2) 주요 단어가 포함되어 있는지 확인 (2글자 이상 단어만)
                int matchedWords = 0;
                for (String word : exerciseWords) {
                    if (word.length() >= 2) {
                        // 직접 포함 또는 유사 단어 매칭
                        if (combined.contains(word) || containsSimilarWord(combined, word)) {
                            matchedWords++;
                        }
                    }
                }
                // 운동 이름의 주요 단어 중 절반 이상이 매칭되어야 함
                if (matchedWords >= Math.max(1, exerciseWords.length / 2)) {
                    hasExerciseName = true;
                    log.debug("✅ 주요 단어 매칭: {} ({}개 중 {}개)", exerciseName, exerciseWords.length, matchedWords);
                }
            }
            
            // 영어 운동명인 경우 직접 매칭 시도
            if (!hasExerciseName && !lowerExerciseName.matches(".*[가-힣].*")) {
                // 영어 운동명은 전체 또는 주요 부분이 포함되어야 함
                if (combined.contains(lowerExerciseName)) {
                    hasExerciseName = true;
                } else {
                    // 주요 단어 하나라도 포함되어야 함
                    for (String word : exerciseWords) {
                        if (word.length() >= 3 && combined.contains(word)) {
                            hasExerciseName = true;
                            break;
                        }
                    }
                }
            }
            
            // 운동 이름이 없으면 관련 영상으로 간주하지 않음
            if (!hasExerciseName) {
                log.debug("❌ 운동 이름 매칭 실패: exerciseName={}, title={}", exerciseName, title);
                return false;
            }
        }
        
        // 3단계: 부적절한 키워드 필터링 (음악, 게임, 뉴스 등)
        String[] excludeKeywords = {
            "music", "song", "game", "news", "movie", "trailer", "comedy",
            "funny", "prank", "challenge", "dance", "k-pop", "mv"
        };
        
        for (String exclude : excludeKeywords) {
            if (combined.contains(exclude)) {
                log.debug("❌ 부적절한 키워드 포함: {}", exclude);
                return false;
            }
        }
        
        log.debug("✅ 관련 영상으로 판단: title={}", title);
        return true;
    }
    
    /**
     * 유사 단어 포함 여부 확인 (한글/영어 매칭)
     */
    private boolean containsSimilarWord(String text, String word) {
        // 한글-영어 매핑 (운동명)
        if (word.contains("로우") || word.contains("row")) {
            return text.contains("row");
        }
        if (word.contains("스쿼트") || word.contains("squat")) {
            return text.contains("squat");
        }
        if (word.contains("프레스") || word.contains("press")) {
            return text.contains("press");
        }
        if (word.contains("풀업") || word.contains("pull")) {
            return text.contains("pull");
        }
        if (word.contains("랫풀다운") || word.contains("랫풀") || word.contains("pulldown")) {
            return text.contains("pulldown") || text.contains("pull down") || text.contains("lat pulldown");
        }
        if (word.contains("크런치") || word.contains("crunch")) {
            return text.contains("crunch");
        }
        if (word.contains("플랭크") || word.contains("plank")) {
            return text.contains("plank");
        }
        if (word.contains("런지") || word.contains("lunge")) {
            return text.contains("lunge");
        }
        if (word.contains("데드리프트") || word.contains("deadlift")) {
            return text.contains("deadlift");
        }
        if (word.contains("푸시업") || word.contains("푸쉬업") || word.contains("push")) {
            return text.contains("push");
        }
        if (word.contains("덤벨") || word.contains("dumbbell")) {
            return text.contains("dumbbell") || text.contains("dumb bell");
        }
        if (word.contains("바벨") || word.contains("barbell")) {
            return text.contains("barbell") || text.contains("bar bell");
        }
        if (word.contains("숄더") || word.contains("shoulder")) {
            return text.contains("shoulder");
        }
        if (word.contains("레터럴") || word.contains("lateral")) {
            return text.contains("lateral");
        }
        if (word.contains("레즈") || word.contains("raise")) {
            return text.contains("raise");
        }
        if (word.contains("컬") || word.contains("curl")) {
            return text.contains("curl");
        }
        if (word.contains("익스텐션") || word.contains("extension")) {
            return text.contains("extension");
        }
        if (word.contains("마운틴") || word.contains("mountain")) {
            return text.contains("mountain");
        }
        if (word.contains("클라이머") || word.contains("climber")) {
            return text.contains("climber");
        }
        if (word.contains("버피") || word.contains("burpee")) {
            return text.contains("burpee");
        }
        if (word.contains("점프") || word.contains("jump")) {
            return text.contains("jump");
        }
        if (word.contains("러닝") || word.contains("running")) {
            return text.contains("running") || text.contains("run");
        }
        if (word.contains("걷기") || word.contains("walking")) {
            return text.contains("walking") || text.contains("walk");
        }
        return false;
    }
}

