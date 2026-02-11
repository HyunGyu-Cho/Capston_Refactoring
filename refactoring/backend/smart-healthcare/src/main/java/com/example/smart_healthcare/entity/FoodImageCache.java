package com.example.smart_healthcare.entity;

import com.example.smart_healthcare.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "food_image_cache")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FoodImageCache extends BaseEntity {
    
    @Column(name = "food_name", length = 100, nullable = false, unique = true)
    private String foodName;

    @Column(name = "image_url", length = 255, nullable = false)
    private String imageUrl;
}
