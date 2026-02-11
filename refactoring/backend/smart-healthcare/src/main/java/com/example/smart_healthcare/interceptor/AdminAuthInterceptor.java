package com.example.smart_healthcare.interceptor;

import com.example.smart_healthcare.entity.User;
import com.example.smart_healthcare.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Map;

/**
 * 관리자 권한 체크 인터셉터
 * /api/admin/** 경로에 대한 접근을 관리자만 허용
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminAuthInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    
    @Value("${app.dev-mode:true}")
    private boolean devMode;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        log.info("🔐 관리자 권한 체크 - URI: {}", requestURI);

        // OPTIONS 요청은 통과
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 🚀 개발 모드: 간단한 인증으로 우회
        if (devMode) {
            log.info("🔧 개발 모드 활성화 - 간단한 관리자 인증 사용");
            
            // 기본 관리자 계정 자동 생성 및 사용
            User adminUser = getOrCreateDefaultAdmin();
            
            // 요청에 관리자 정보 추가
            request.setAttribute("currentUser", adminUser);
            request.setAttribute("userId", adminUser.getId());
            request.setAttribute("userEmail", adminUser.getEmail());
            
            log.info("✅ 개발 모드 관리자 인증 성공: {} ({})", adminUser.getEmail(), requestURI);
            return true;
        }

        // 🔒 운영 모드: JWT 토큰 검증 (현재는 사용 안 함)
        log.warn("❌ 운영 모드 JWT 인증은 아직 미구현 - 개발 모드를 사용하세요");
        sendUnauthorizedResponse(response, "개발 모드를 사용하거나 JWT 토큰을 제공하세요.");
        return false;
    }

    /**
     * 개발용 기본 관리자 계정 생성 또는 조회
     */
    private User getOrCreateDefaultAdmin() {
        String adminEmail = "admin@healthcare.com";
        
        return userRepository.findByEmail(adminEmail)
                .orElseGet(() -> {
                    log.info("🔧 개발용 기본 관리자 계정 생성: {}", adminEmail);
                    
                    User admin = new User();
                    admin.setEmail(adminEmail);
                    admin.setRole(User.Role.ADMIN);
                    admin.setProvider(User.AuthProvider.LOCAL);
                    admin.setIsDeleted(false);
                    admin.setPassword("dev_admin_password"); // 개발용 임시 비밀번호
                    
                    return userRepository.save(admin);
                });
    }

    /**
     * 401 Unauthorized 응답 전송
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorResponse = Map.of(
            "success", false,
            "error", message,
            "timestamp", System.currentTimeMillis(),
            "code", "UNAUTHORIZED"
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    /**
     * 403 Forbidden 응답 전송
     */
    private void sendForbiddenResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorResponse = Map.of(
            "success", false,
            "error", message,
            "timestamp", System.currentTimeMillis(),
            "code", "FORBIDDEN"
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
