package com.example.smart_healthcare.auth.security;

import com.example.smart_healthcare.common.api.ApiError;
import com.example.smart_healthcare.common.api.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
// 인가 실패(403)를 표준 JSON 에러 응답으로 변환한다.
public class JsonAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;

    @Override
    // AccessDeniedException 발생 시 API 에러 응답 바디를 작성한다.
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        ApiError error = new ApiError("AUTH-403-001", "Access denied", traceId(request), null);
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.fail(error)));
    }

    private String traceId(HttpServletRequest request) {
        Object value = request.getAttribute("traceId");
        return value == null ? "" : value.toString();
    }
}


