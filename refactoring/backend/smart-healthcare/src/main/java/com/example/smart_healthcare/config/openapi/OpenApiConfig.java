package com.example.smart_healthcare.config.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI smartHealthcareOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Smart Healthcare API")
                        .description("스마트 헬스케어 플랫폼 API 문서")
                        )
                .tags(List.of(
                        new Tag().name("User").description("사용자 관리 API"),
                        new Tag().name("Health").description("건강 데이터 관리 API"),
                        new Tag().name("Community").description("커뮤니티 관리 API"),
                        new Tag().name("Recommendation").description("추천 시스템 API"),
                        new Tag().name("Analytics").description("데이터 분석 API"),
                        new Tag().name("Admin").description("관리자 API")));
    }
}
