package com.example.smart_healthcare.repository;

import com.example.smart_healthcare.entity.FoodImageCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FoodImageCacheRepository extends JpaRepository<FoodImageCache, Long> {
    
    /**
     * 음식명으로 이미지 캐시 조회
     */
    Optional<FoodImageCache> findByFoodName(String foodName);
    
    /**
     * 이미지 URL로 캐시 조회
     */
    List<FoodImageCache> findByImageUrl(String imageUrl);
    
    /**
     * 음식명으로 검색 (부분 일치)
     */
    List<FoodImageCache> findByFoodNameContainingIgnoreCase(String foodName);
    
    /**
     * 캐시 통계 조회
     */
    @Query(value = "SELECT COUNT(*) FROM food_image_cache f", nativeQuery = true)
    long countAllCachedImages();
    
    /**
     * 특정 음식명으로 시작하는 캐시 조회
     */
    List<FoodImageCache> findByFoodNameStartingWithIgnoreCase(String foodName);
}