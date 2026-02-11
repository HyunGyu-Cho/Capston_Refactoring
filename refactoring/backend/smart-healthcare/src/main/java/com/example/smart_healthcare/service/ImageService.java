package com.example.smart_healthcare.service;

import com.example.smart_healthcare.common.error.BusinessException;
import com.example.smart_healthcare.common.error.ErrorCode;
import com.example.smart_healthcare.entity.FoodImageCache;
import com.example.smart_healthcare.repository.FoodImageCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@Service 
@RequiredArgsConstructor
public class ImageService {
    
    private final FoodImageCacheRepository foodImageCacheRepository;
    private final RestTemplate restTemplate;
    
    @Value("${unsplash.access.key:}")
    private String unsplashAccessKey;

    /**
     * 음식 이미지 URL 반환 (캐싱)
     */
    @Transactional(readOnly = true)
    public String getImageUrl(String foodName) {
        log.info("음식 이미지 URL 요청: foodName={}", foodName);
        
        try {
            // 캐시에서 먼저 확인
            var cached = foodImageCacheRepository.findByFoodName(foodName);
            if (cached.isPresent()) {
                log.info("캐시에서 이미지 URL 반환: foodName={}", foodName);
                return cached.get().getImageUrl();
            }
            
            // API에서 가져오기
            String url = fetchFromApi(foodName, "food");
            saveToCache(foodName, url);
            
            log.info("API에서 이미지 URL 가져오기 완료: foodName={}", foodName);
            return url;
        } catch (Exception e) {
            log.error("이미지 URL 가져오기 실패: foodName={}, error={}", foodName, e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "이미지 URL 가져오기 실패: " + e.getMessage());
        }
    }

    /**
     * 운동 이미지 URL 반환
     */
    @Transactional(readOnly = true)
    public String getWorkoutImageUrl(String workoutName) {
        log.info("운동 이미지 URL 요청: workoutName={}", workoutName);
        
        try {
            // 운동 이미지는 캐시하지 않고 직접 API 호출
            String url = fetchFromApi(workoutName, "exercise");
            log.info("API에서 운동 이미지 URL 가져오기 완료: workoutName={}", workoutName);
            return url;
        } catch (Exception e) {
            log.error("운동 이미지 URL 가져오기 실패: workoutName={}, error={}", workoutName, e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "운동 이미지 URL 가져오기 실패: " + e.getMessage());
        }
    }
    
    /**
     * 캐시에 이미지 URL 저장
     */
    @Transactional
    private void saveToCache(String foodName, String imageUrl) {
        try {
            FoodImageCache cache = new FoodImageCache();
            cache.setFoodName(foodName);
            cache.setImageUrl(imageUrl);
            
            foodImageCacheRepository.save(cache);
            log.info("이미지 URL 캐시 저장 완료: foodName={}", foodName);
        } catch (Exception e) {
            log.error("이미지 URL 캐시 저장 실패: foodName={}, error={}", foodName, e.getMessage());
            // 캐시 저장 실패시 전체 작업을 중단하지 않음
        }
    }
    
    /**
     * Unsplash API에서 이미지 URL 가져오기
     * - 검색어를 헬스/식단 특화로 최적화
     * - 응답 결과 중 실제 운동/음식 관련도가 높은 이미지를 우선 선택
     */
    private String fetchFromApi(String name, String type) {
        try {
            // 검색어 결정: 한글이 포함되어 있으면 의미 기반으로 최적화,
            // 그렇지 않으면 이미 최적화된 검색어(예: unsplashQuery)로 간주하되
            // 헬스/식단 관련 키워드를 보강
            String rawQuery = (name != null) ? name.trim() : "";
            if (rawQuery.isEmpty()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "검색어가 비어 있습니다");
            }

            String query;
            if (containsKorean(rawQuery)) {
                query = optimizeQuery(rawQuery, type);
            } else {
                // 영문 위주의 문구는 그대로 사용하되, 타입에 따라 헬스/식단 키워드를 보강
                query = optimizeEnglishQuery(rawQuery, type);
            }

            // URL 인코딩
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = String.format(
                    "https://api.unsplash.com/search/photos?query=%s&per_page=10&orientation=squarish&content_filter=high&client_id=%s",
                    encodedQuery, unsplashAccessKey);

            log.info("📷 Unsplash 이미지 검색 요청: query='{}', type={}, url={}", query, type, url);
            
            @SuppressWarnings("unchecked")
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
            
            if (responseBody == null) {
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "API 응답이 비어있습니다");
            }
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> results = (List<Map<String, Object>>) responseBody.get("results");
            if (results == null || results.isEmpty()) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "이미지를 찾을 수 없습니다");
            }
            
            // 운동/식단 타입별로 관련도가 높은 이미지를 우선 선택
            Map<String, String> urls = selectBestImageByType(results, type);
            if (urls == null || urls.get("regular") == null) {
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "이미지 URL을 찾을 수 없습니다");
            }
            
            return urls.get("regular");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unsplash API 호출 실패: name={}, type={}, error={}", name, type, e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Unsplash API 호출 실패: " + e.getMessage());
        }
    }

    /**
     * 문자열에 한글이 포함되어 있는지 검사
     */
    private boolean containsKorean(String text) {
        if (text == null) return false;
        for (char ch : text.toCharArray()) {
            Character.UnicodeBlock block = Character.UnicodeBlock.of(ch);
            if (block == Character.UnicodeBlock.HANGUL_SYLLABLES
                    || block == Character.UnicodeBlock.HANGUL_JAMO
                    || block == Character.UnicodeBlock.HANGUL_JAMO_EXTENDED_A
                    || block == Character.UnicodeBlock.HANGUL_JAMO_EXTENDED_B
                    || block == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO) {
                return true;
            }
        }
        return false;
    }

    /**
     * 검색어 최적화 (한글 기준)
     */
    private String optimizeQuery(String name, String type) {
        if ("exercise".equals(type)) {
            // 운동 관련 검색어 최적화 (헬스/짐 이미지를 유도)
            if (name.contains("푸시업") || name.contains("pushup")) {
                return "push up exercise in gym, fitness training, person doing push up, photo";
            } else if (name.contains("스쿼트") || name.contains("squat")) {
                return "squat exercise in gym, leg workout, fitness training, photo";
            } else if (name.contains("플랭크") || name.contains("plank")) {
                return "plank core exercise on mat, gym workout, fitness training, photo";
            } else if (name.contains("덤벨") || name.contains("dumbbell")) {
                return "dumbbell strength training in gym, weightlifting workout, photo";
            } else if (name.contains("런지") || name.contains("lunge")) {
                return "lunge leg exercise in gym, fitness training, photo";
            } else {
                return name + " exercise in gym, fitness workout, training photo";
            }
        } else if ("food".equals(type)) {
            // 음식 관련 검색어 최적화 (건강식/접시 위 음식 이미지를 유도)
            if (name.contains("닭가슴살") || name.contains("chicken")) {
                return "grilled chicken breast healthy meal on plate, high protein food, photo";
            } else if (name.contains("연어") || name.contains("salmon")) {
                return "grilled salmon with vegetables healthy meal on plate, photo";
            } else if (name.contains("샐러드") || name.contains("salad")) {
                return "fresh vegetable salad healthy food bowl on table, photo";
            } else if (name.contains("현미밥") || name.contains("brown rice")) {
                return "brown rice bowl with healthy side dishes, balanced meal, photo";
            } else {
                return name + " healthy meal on plate, clean eating food, photo";
            }
        } else {
            return name;
        }
    }

    /**
     * 영문 검색어 보강 (AI가 생성한 unsplashQuery 등)
     */
    private String optimizeEnglishQuery(String rawQuery, String type) {
        String lower = rawQuery.toLowerCase();
        
        if ("exercise".equals(type)) {
            // 이미 exercise/gym 등이 포함되어 있으면 그대로 사용
            if (lower.contains("exercise") || lower.contains("workout") || lower.contains("gym")) {
                return rawQuery;
            }
            return rawQuery + ", exercise in gym, fitness workout training photo";
        } else if ("food".equals(type) || "diet".equals(type)) {
            if (lower.contains("meal") || lower.contains("food") || lower.contains("dish") || lower.contains("salad")) {
                return rawQuery;
            }
            return rawQuery + ", healthy meal on plate, food photography, top view";
        } else {
            return rawQuery;
        }
    }

    /**
     * Unsplash 결과 중에서 타입별로 가장 관련도 높은 이미지 선택
     */
    @SuppressWarnings("unchecked")
    private Map<String, String> selectBestImageByType(List<Map<String, Object>> results, String type) {
        if (results == null || results.isEmpty()) {
            return null;
        }

        // 타입별 키워드 설정
        String[] exerciseKeywords = new String[] {
                "exercise", "workout", "gym", "training", "fitness", "bodybuilding", "sport"
        };
        String[] foodKeywords = new String[] {
                "food", "meal", "dish", "plate", "lunch", "dinner", "breakfast", "salad", "healthy"
        };

        String[] keywords;
        if ("exercise".equals(type)) {
            keywords = exerciseKeywords;
        } else if ("food".equals(type) || "diet".equals(type)) {
            keywords = foodKeywords;
        } else {
            // 타입 모르면 첫 번째만 사용
            return (Map<String, String>) results.get(0).get("urls");
        }

        // 각 결과의 설명/태그를 모아서 키워드가 포함된 첫 번째 이미지를 선택
        for (Map<String, Object> result : results) {
            StringBuilder sb = new StringBuilder();
            Object altDesc = result.get("alt_description");
            Object desc = result.get("description");

            if (altDesc instanceof String) {
                sb.append(((String) altDesc).toLowerCase()).append(" ");
            }
            if (desc instanceof String) {
                sb.append(((String) desc).toLowerCase()).append(" ");
            }

            Object tagsObj = result.get("tags");
            if (tagsObj instanceof List) {
                for (Object tagObj : (List<?>) tagsObj) {
                    if (tagObj instanceof Map) {
                        Object title = ((Map<?, ?>) tagObj).get("title");
                        if (title instanceof String) {
                            sb.append(((String) title).toLowerCase()).append(" ");
                        }
                    }
                }
            }

            String text = sb.toString();
            for (String kw : keywords) {
                if (text.contains(kw.toLowerCase())) {
                    Map<String, String> urls = (Map<String, String>) result.get("urls");
                    if (urls != null && urls.get("regular") != null) {
                        log.info("✅ Unsplash 결과 중 '{}' 관련 이미지 선택: alt_description={}", type, altDesc);
                        return urls;
                    }
                }
            }
        }

        // 관련 키워드를 찾지 못한 경우, 첫 번째 결과를 그대로 사용
        log.info("⚠️ 관련 키워드를 포함한 이미지를 찾지 못해 첫 번째 결과를 사용합니다.");
        return (Map<String, String>) results.get(0).get("urls");
    }
    
    /**
     * 캐시에서 이미지 URL 삭제
     */
    @Transactional
    public void deleteCachedImage(String foodName) {
        log.info("캐시에서 이미지 URL 삭제 요청: foodName={}", foodName);
        
        try {
            var cached = foodImageCacheRepository.findByFoodName(foodName);
            if (cached.isPresent()) {
                foodImageCacheRepository.delete(cached.get());
                log.info("캐시에서 이미지 URL 삭제 완료: foodName={}", foodName);
            } else {
                log.info("삭제할 캐시가 없습니다: foodName={}", foodName);
            }
        } catch (Exception e) {
            log.error("캐시에서 이미지 URL 삭제 실패: foodName={}, error={}", foodName, e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "캐시에서 이미지 URL 삭제 실패: " + e.getMessage());
        }
    }
    
    /**
     * 캐시 통계 조회
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCacheStats() {
        log.info("캐시 통계 조회 요청");
        
        try {
            long totalCount = foodImageCacheRepository.countAllCachedImages();
            
            Map<String, Object> stats = Map.of(
                "totalCachedImages", totalCount,
                "cacheStatus", "active"
            );
            
            log.info("캐시 통계 조회 완료: totalCount={}", totalCount);
            return stats;
        } catch (Exception e) {
            log.error("캐시 통계 조회 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "캐시 통계 조회 실패: " + e.getMessage());
        }
    }
}