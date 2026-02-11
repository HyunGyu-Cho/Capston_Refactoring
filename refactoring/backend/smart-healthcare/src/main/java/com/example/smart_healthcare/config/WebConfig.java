package com.example.smart_healthcare.config;

import com.example.smart_healthcare.interceptor.LoggingInterceptor;
import com.example.smart_healthcare.interceptor.AdminAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    
    private final LoggingInterceptor loggingInterceptor;
    private final AdminAuthInterceptor adminAuthInterceptor;
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);

        // Swagger/OpenAPI 문서 접근 허용
        registry.addMapping("/v3/api-docs/**")
                .allowedOrigins("*")
                .allowedMethods("GET");
        registry.addMapping("/swagger-ui/**")
                .allowedOrigins("*")
                .allowedMethods("GET");
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 로깅 인터셉터 (모든 요청)
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/error", "/favicon.ico", "/css/**", "/js/**", "/images/**");
        
        // 관리자 권한 인터셉터 비활성화 (Spring Security로 대체)
        // registry.addInterceptor(adminAuthInterceptor)
        //         .addPathPatterns("/api/admin/**")
        //         .excludePathPatterns("/api/admin/health");
    }
    
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
} 