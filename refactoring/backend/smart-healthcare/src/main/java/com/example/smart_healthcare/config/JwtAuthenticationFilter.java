package com.example.smart_healthcare.config;

import com.example.smart_healthcare.service.CustomUserDetailsService;
import com.example.smart_healthcare.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        final String authorizationHeader = request.getHeader("Authorization");
        
        log.info("🔐 JWT 필터 - 요청 URL: {}", request.getRequestURI());
        log.info("🔐 JWT 필터 - Authorization 헤더: {}", authorizationHeader != null ? "존재함" : "없음");
        
        String email = null;
        String jwt = null;

        // Authorization 헤더에서 Bearer 토큰 추출
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            log.info("🔐 JWT 토큰 추출됨: {}", jwt.substring(0, Math.min(20, jwt.length())) + "...");
            try {
                email = jwtUtil.extractEmail(jwt);
                log.info("🔐 JWT에서 추출된 이메일: {}", email);
            } catch (Exception e) {
                log.warn("JWT 토큰에서 이메일 추출 실패: {}", e.getMessage());
            }
        } else {
            log.warn("🔐 Authorization 헤더가 없거나 Bearer 형식이 아님: {}", authorizationHeader);
        }

        // 이메일이 추출되었고, 현재 인증 컨텍스트가 비어있는 경우
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);
            
            // 토큰 유효성 검증
            if (jwtUtil.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                
                log.info("✅ JWT 인증 성공: email={}", email);
            } else {
                log.warn("❌ JWT 토큰 검증 실패: email={}", email);
            }
        } else if (email == null) {
            log.warn("🔐 JWT 토큰에서 이메일을 추출할 수 없음");
        } else {
            log.info("🔐 이미 인증된 사용자: email={}", email);
        }

        filterChain.doFilter(request, response);
    }
}
