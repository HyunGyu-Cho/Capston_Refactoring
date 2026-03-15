package com.example.smart_healthcare.auth.security;

import com.example.smart_healthcare.common.api.ApiError;
import com.example.smart_healthcare.common.api.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
// 인증 실패(401)를 표준 JSON 에러 응답으로 변환한다.
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    @Override
    // AuthFailureException이 전달된 경우 코드/메시지를 그대로 사용한다.
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        String code = "AUTH-401-002";
        String message = "Access token is invalid";
        if (authException instanceof AuthFailureException ex) {
            code = ex.getCode();
            message = ex.getMessage();
        }
        ApiError error = new ApiError(code, message, traceId(request), null);
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.fail(error)));
    }

    private String traceId(HttpServletRequest request) {
        Object value = request.getAttribute("traceId");
        return value == null ? "" : value.toString();
    }

}


