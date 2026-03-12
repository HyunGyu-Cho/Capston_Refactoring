package com.example.smart_healthcare.auth.security;

import com.example.smart_healthcare.auth.error.AuthErrorCode;
import com.example.smart_healthcare.common.error.AppException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    public static final String AUTH_ERROR_CODE_ATTR = "authErrorCode";
    public static final String AUTH_ERROR_MESSAGE_ATTR = "authErrorMessage";

    private final JwtProvider jwtProvider;

    public JwtAuthenticationFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.equals("/api/v1/auth/signup")
                || uri.equals("/api/v1/auth/login")
                || uri.equals("/api/v1/auth/refresh")
                || uri.equals("/api/v1/health")
                || uri.equals("/api/v1/ready");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String header = request.getHeader("Authorization");
            if (header == null || !header.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = header.substring(7);
            Claims claims = jwtProvider.parseAccessClaims(token);
            Long memberId = Long.valueOf(claims.getSubject());
            String email = claims.get("email", String.class);
            @SuppressWarnings("unchecked")
            List<String> roles = claims.get("roles", List.class);
            if (roles == null || roles.isEmpty()) {
                throw new AppException(AuthErrorCode.AUTH_401_002);
            }
            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .toList();
            MemberUserDetails principal = new MemberUserDetails(memberId, email, "", authorities);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (AppException ex) {
            SecurityContextHolder.clearContext();
            request.setAttribute(AUTH_ERROR_CODE_ATTR, ex.getErrorCode().code());
            request.setAttribute(AUTH_ERROR_MESSAGE_ATTR, ex.getErrorCode().message());
            throw new AuthenticationException(ex.getMessage(), ex) {
            };
        }
    }
}
