package com.example.smart_healthcare.auth.security;

import com.example.smart_healthcare.auth.error.AuthErrorCode;
import com.example.smart_healthcare.common.error.AppException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
// [Filter 역할]
// JWT 인증을 위해 요청당 한 번 실행되는 커스텀 필터
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // [인증 지원 객체]
    // JWT 생성/검증/파싱을 담당하는 객체
    private final JwtProvider jwtProvider;

    // [Filter 역할]
    // 특정 URL은 이 필터를 적용하지 않음
    // 공개 API는 JWT 없이도 접근 가능해야 하므로 제외
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.equals("/api/v1/auth/signup")
                || uri.equals("/api/v1/auth/login")
                || uri.equals("/api/v1/auth/refresh")
                || uri.equals("/api/v1/health")
                || uri.equals("/api/v1/ready");
    }

    // [Filter 역할의 시작점]
    // 요청을 가로채서 JWT 인증을 시도하는 핵심 메서드
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // [Filter 역할]
            // HTTP 요청 헤더에서 Authorization 값 읽기
            String header = request.getHeader("Authorization");

            // [Filter 역할]
            // Authorization 헤더가 없거나 Bearer 형식이 아니면
            // 이 필터에서는 인증하지 않고 다음 필터로 그냥 넘김
            if (header == null || !header.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            // [Filter 역할]
            // "Bearer "를 제외한 실제 JWT 토큰 문자열 추출
            String token = header.substring(7);

            // [Provider 비슷한 역할 - 실제 인증 수행]
            // JWT Access Token 검증 및 claims 추출
            // 여기서 서명, 만료, 형식 등을 검사한다고 보면 됨
            Claims claims = jwtProvider.parseAccessClaims(token);

            // [Provider 비슷한 역할 - 사용자 정보 추출]
            // subject에서 회원 ID 추출
            Long memberId = Long.valueOf(claims.getSubject());

            // [Provider 비슷한 역할 - 사용자 정보 추출]
            // email claim 추출
            String email = claims.get("email", String.class);

            // [Provider 비슷한 역할 - 권한 정보 추출]
            @SuppressWarnings("unchecked")
            List<String> roles = claims.get("roles", List.class);

            // [Provider 비슷한 역할 - 유효성 검증]
            // roles가 없으면 정상 토큰이 아니라고 판단
            if (roles == null || roles.isEmpty()) {
                throw new AppException(AuthErrorCode.AUTH_401_002);
            }

            // [Provider 비슷한 역할 - 권한 객체 생성]
            // Spring Security가 이해하는 GrantedAuthority 목록으로 변환
            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .toList();

            // [Provider 비슷한 역할 - principal 생성]
            // 로그인 사용자 정보를 담는 principal 객체 생성
            MemberUserDetails principal = new MemberUserDetails(memberId, email, "", authorities);

            // [Provider 비슷한 역할 - Authentication 객체 생성]
            // 인증 완료된 사용자를 나타내는 Authentication 객체 생성
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);

            // [SecurityContext 저장 역할]
            // 현재 요청의 인증 정보를 SecurityContextHolder에 저장
            // 이 시점부터 Spring Security는 로그인된 사용자로 인식
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // [Filter 역할]
            // 인증 처리가 끝났으므로 다음 필터로 요청 전달
            filterChain.doFilter(request, response);

        } catch (AppException ex) {
            // [SecurityContext 정리 역할]
            // 인증 실패 시 기존 인증 정보 제거
            SecurityContextHolder.clearContext();

            // [Filter 역할 + 예외 전달]
            // 인증 실패 예외를 상위 보안 예외 처리 흐름으로 전달
            throw new AuthFailureException(ex.getErrorCode().code(), ex.getErrorCode().message(), ex);
        }
    }
}