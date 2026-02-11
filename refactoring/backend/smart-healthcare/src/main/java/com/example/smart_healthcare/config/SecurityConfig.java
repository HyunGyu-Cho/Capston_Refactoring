package com.example.smart_healthcare.config;

import com.example.smart_healthcare.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.and()) // CORS 활성화
            .headers(headers -> headers.frameOptions().disable()) // H2 콘솔을 위한 프레임 옵션 비활성화
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // H2 콘솔 허용 (개발 환경에서만)
                .requestMatchers(
                    new AntPathRequestMatcher("/h2-console/**")
                ).permitAll()
                // Swagger & OpenAPI docs 허용
                .requestMatchers(
                    new AntPathRequestMatcher("/api-docs/**"),
                    new AntPathRequestMatcher("/swagger-ui/**"),
                    new AntPathRequestMatcher("/swagger-ui.html"),
                    new AntPathRequestMatcher("/swagger-resources/**"),
                    new AntPathRequestMatcher("/webjars/**")
                ).permitAll()
                // 정적 리소스 및 헬스체크 허용
                .requestMatchers(
                    new AntPathRequestMatcher("/actuator/health"),
                    new AntPathRequestMatcher("/actuator/info"),
                    new AntPathRequestMatcher("/favicon.ico"),
                    new AntPathRequestMatcher("/error")
                ).permitAll()
                // API 테스트 엔드포인트 허용
                .requestMatchers(
                    new AntPathRequestMatcher("/api/test-openai"),
                    new AntPathRequestMatcher("/api/analysis-status")
                ).permitAll()
                // 공개 이미지 조회 API 허용 (운동/식단 썸네일용)
                .requestMatchers(
                    new AntPathRequestMatcher("/api/images/**")
                ).permitAll()
                // 인증 관련 API 허용
                .requestMatchers(
                    new AntPathRequestMatcher("/api/auth/**")
                ).permitAll()
                // CORS preflight 요청 허용
                .requestMatchers(
                    new AntPathRequestMatcher("/api/**", "OPTIONS")
                ).permitAll()
                // 관리자 API는 ADMIN 역할 필요
                .requestMatchers(
                    new AntPathRequestMatcher("/api/admin/**")
                ).hasRole("ADMIN")
                // 사용자 관련 API는 인증 필요
                .requestMatchers(
                    new AntPathRequestMatcher("/api/users/**"),
                    new AntPathRequestMatcher("/api/community/**"),
                    new AntPathRequestMatcher("/api/evaluation/**"),
                    new AntPathRequestMatcher("/api/survey/**"),
                    new AntPathRequestMatcher("/api/inbody/**"),
                    new AntPathRequestMatcher("/api/health/**"),
                    new AntPathRequestMatcher("/api/ai/**"),
                    new AntPathRequestMatcher("/api/body-analysis/**"),
                    new AntPathRequestMatcher("/api/workout-recommendation/**"),
                    new AntPathRequestMatcher("/api/diet-recommendation/**")
                ).authenticated()
                // 그 외 모든 API는 인증 필요
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
} 